package de.chojo.shepquotes.data;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.chojo.shepquotes.data.elements.Author;
import de.chojo.shepquotes.data.elements.Quote;
import de.chojo.sqlutil.base.QueryFactoryHolder;
import de.chojo.sqlutil.conversion.ArrayConverter;
import de.chojo.sqlutil.exceptions.ExceptionTransformer;
import de.chojo.sqlutil.wrapper.QueryBuilderConfig;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

public class QuoteData extends QueryFactoryHolder {
    private static final Logger log = getLogger(QuoteData.class);
    private final Cache<Integer, Author> authorCache = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.SECONDS).build();

    /**
     * Create a new QueryFactoryholder
     *
     * @param dataSource datasource
     */
    public QuoteData(DataSource dataSource) {
        super(dataSource, QueryBuilderConfig.builder().withExceptionHandler(err -> log.error(ExceptionTransformer.prettyException(err))).build());
    }

    public void addQuote(Quote quote) {
        CompletableFuture.supplyAsync(() ->
                        quote.authors().stream()
                                .map(this::getOrCreateAuthor)
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .collect(Collectors.toList())
                ).exceptionally(err -> {
                    log.error("Could not retrieve authors", err);
                    return Collections.emptyList();
                })
                .thenAccept(authors -> builder(Integer.class)
                        .query("INSERT INTO quote(owner, guild_id) VALUES(?,?) RETURNING id")
                        .paramsBuilder(stmt -> stmt.setLong(quote.ownerId()).setLong(quote.guildId()))
                        .readRow(r -> r.getInt(1))
                        .first()
                        .thenAccept(id -> {
                            var builder = builder().query("INSERT INTO content(quote_id, content) VALUES(?,?)")
                                    .paramsBuilder(stmt -> stmt.setInt(id.get()));

                            for (var author : authors) {
                                builder.append()
                                        .query("INSERT INTO author_links(quote_id, author_id) VALUES(?,?)")
                                        .paramsBuilder(stmt -> stmt.setInt(id.get()).setInt(author.id()));
                            }
                            builder.insert().executeSync();
                        })).exceptionally(err -> {
                    log.error("Could not link authors", err);
                    return null;
                });
    }

    private Optional<Author> getOrCreateAuthor(Author author) {
        return builder(Author.class)
                .query("SELECT id FROM author WHERE name ILIKE ? AND guild_id = ?")
                .paramsBuilder(stmt -> stmt.setString(author.name()).setLong(author.guildId()))
                .readRow(r -> Author.of(r.getInt(1), r.getString(2)))
                .firstSync()
                .flatMap(this::createAuthor);
    }

    private Optional<Author> createAuthor(Author author) {
        return builder(Author.class)
                .query("INSERT INTO author(name, guild_id) VALUES(?, ?) RETURNING id")
                .paramsBuilder(stmt -> stmt.setString(author.name()).setLong(author.guildId()))
                .readRow(r -> author.toRegisteredAuthor(r.getInt(1)))
                .firstSync();
    }

    private Author getAuthorById(int id) {
        try {
            return authorCache.get(id, () -> builder(Author.class).query("SELECT id, name, guild_id FROM author WHERE id = ?")
                    .paramsBuilder(stmt -> stmt.setInt(id))
                    .readRow(r -> Author.of(r.getInt("id"), r.getLong("guild_id"), r.getString("name")))
                    .firstSync().get());
        } catch (ExecutionException e) {
            return Author.of(0, -1, "Unknown");
        }
    }

    public CompletableFuture<List<Quote>> getQuotesByAuthor(Guild guild, String name) {
        return builder(Integer.class)
                .query("""
                        WITH authors AS(
                             SELECT DISTINCT a.id
                             FROM author a
                             WHERE a.guild_id = ? AND a.name ILIKE ?
                             )
                        SELECT DISTINCT l.quote_id
                        FROM author_links l
                        WHERE l.quote_id IN (SELECT a.id FROM authors a)
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild.getIdLong()).setString(name))
                .readRow(r -> r.getInt("id"))
                .all()
                .thenApply(this::getQuotesByIds);
    }

    private List<Quote> getQuotesByIds(List<Integer> ids) {
        return ids.stream().map(this::getQuoteById).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
    }

    private Optional<Quote> getQuoteById(int id) {
        return builder(Quote.class)
                .query("""
                        SELECT q.id, q.guild_id, c.content, q.owner, q.created, q.modified, a.ids
                        FROM quote q
                        LEFT JOIN content c ON q.id = c.quote_id
                        LEFT JOIN author_ids a ON q.id = a.quote_id
                        WHERE q.id = ?
                        """)
                .paramsBuilder(stmt -> stmt.setInt(id))
                .readRow(r -> {
                    List<Integer> authorIds = ArrayConverter.toList(r, "ids");
                    return new Quote(r.getInt("id"),
                            r.getLong("guild_id"),
                            r.getLong("owner"),
                            r.getString("content"),
                            authorIds.stream().map(this::getAuthorById).collect(Collectors.toList()),
                            r.getTimestamp("created").toLocalDateTime(),
                            r.getTimestamp("modified").toLocalDateTime());
                })
                .firstSync();
    }
}
