package de.chojo.shepquotes.data.dao;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.chojo.shepquotes.data.elements.QuoteSnapshot;
import de.chojo.sqlutil.base.QueryFactoryHolder;
import de.chojo.sqlutil.conversion.ArrayConverter;
import de.chojo.sqlutil.wrapper.QueryBuilderConfig;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;

import javax.sql.DataSource;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

public class Quotes extends QueryFactoryHolder {
    private static final Logger log = getLogger(Quotes.class);

    private final Cache<Integer, Optional<QuoteSnapshot>> quoteCache = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build();

    private final Sources sources;
    private final long guild;
    private final Search search;
    private final Links links;

    public Search search() {
        return search;
    }

    public Sources sources() {
        return sources;
    }

    public Quotes(Guild guild, DataSource dataSource) {
        super(dataSource, QueryBuilderConfig.builder()
                .withExceptionHandler(err -> log.error("Unhandled exception", err))
                .build());
        this.guild = guild.getIdLong();
        this.search = new Search(guild, this, dataSource);
        this.links = new Links(dataSource);
        sources = new Sources(guild, links);
    }

    public CompletableFuture<Optional<de.chojo.shepquotes.data.dao.Quote>> create(User user) {
        return builder(de.chojo.shepquotes.data.dao.Quote.class)
                .query("INSERT INTO quote(owner, guild_id) VALUES(?,?) RETURNING id")
                .paramsBuilder(stmt -> stmt.setLong(user.getIdLong()).setLong(guild))
                .readRow(r -> new de.chojo.shepquotes.data.dao.Quote(sources, links, r.getInt(1)))
                .first();
    }

    public CompletableFuture<Optional<Quote>> random() {
        return builder(Integer.class).
                query("SELECT id FROM quote WHERE guild_id = ? ORDER BY RANDOM() LIMIT 1")
                .paramsBuilder(stmt -> stmt.setLong(guild))
                .readRow(r -> r.getInt("quote_id"))
                .first()
                .thenApply(quoteId -> quoteId.flatMap(this::getQuoteById));
    }

    public CompletableFuture<Optional<Quote>> byId(int id) {
        return builder(Integer.class).
                query("""
                        SELECT id
                        FROM quote q
                        LEFT JOIN guild_quote_ids gqi ON q.id = gqi.quote_id
                        WHERE guild_id = ? AND guild_quote_id = ?
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild).setInt(id))
                .readRow(r -> r.getInt("quote_id"))
                .first()
                .thenApply(quoteId -> quoteId.flatMap(this::getQuoteById));
    }


    List<Quote> getQuotesByIds(List<Integer> ids) {
        return ids.stream().map(this::getQuoteById).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
    }

    private Optional<Quote> getQuoteById(int id) {
        return builder(Quote.class).
                query("""
                        SELECT id
                        FROM quote q
                        LEFT JOIN guild_quote_ids gqi ON q.id = gqi.quote_id
                        WHERE guild_id = ? AND guild_quote_id = ?
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild).setInt(id))
                .readRow(r -> new Quote(sources, links, r.getInt("quote_id")))
                .firstSync();
    }

    public CompletableFuture<Integer> quoteCount() {
        return builder(Integer.class)
                .query("SELECT COUNT(1) FROM quote WHERE guild_id = ?")
                .paramsBuilder(stmt -> stmt.setLong(guild))
                .readRow(r -> r.getInt(1))
                .first()
                .thenApply(Optional::get);
    }
}
