package de.chojo.shepquotes.data.dao;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.chojo.shepquotes.data.elements.QuoteSnapshot;
import de.chojo.sqlutil.base.QueryFactoryHolder;
import de.chojo.sqlutil.wrapper.QueryBuilderConfig;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
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

    public Quotes(Guild guild, Sources sources, Links links) {
        super(sources.source(), QueryBuilderConfig.builder()
                .withExceptionHandler(err -> log.error("Unhandled exception", err))
                .build());
        this.guild = guild.getIdLong();
        this.search = new Search(guild, this);
        this.links = links;
        this.sources = sources;
    }

    public CompletableFuture<Optional<Quote>> create(User user) {
        return builder(Integer.class)
                .query("INSERT INTO quote(owner, guild_id) VALUES(?,?) RETURNING id")
                .paramsBuilder(stmt -> stmt.setLong(user.getIdLong()).setLong(guild))
                .readRow(r -> r.getInt(1))
                .first()
                .thenApply(id -> getByLocalId(id.get()));
    }

    public CompletableFuture<Optional<Quote>> random() {
        return builder(Quote.class).
                query("""
                        SELECT id, local_id
                        FROM quote
                        LEFT JOIN local_ids li ON quote.id = li.quote_id
                        WHERE guild_id = ?
                        ORDER BY RANDOM()
                        LIMIT 1
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild))
                .readRow(this::buildQuote)
                .first();
    }

    public CompletableFuture<Optional<Quote>> byLocalId(int id) {
        return builder(Quote.class).
                query("""
                        SELECT id, local_id
                        FROM quote q
                        LEFT JOIN local_ids gqi ON q.id = gqi.quote_id
                        WHERE guild_id = ? AND local_id = ?
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild).setInt(id))
                .readRow(this::buildQuote)
                .first();
    }

    public CompletableFuture<Optional<Quote>> byLocalId(int id, User user) {
        return builder(Quote.class).
                query("""
                        SELECT id, local_id
                        FROM quote q
                        LEFT JOIN local_ids gqi ON q.id = gqi.quote_id
                        WHERE guild_id = ?
                            AND local_id = ?
                            AND owner = ?
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild).setInt(id).setLong(user.getIdLong()))
                .readRow(this::buildQuote)
                .first();
    }

    public CompletableFuture<Optional<Quote>> byId(int id) {
        return builder(Quote.class).
                query("""
                        SELECT id, local_id
                        FROM quote q
                        LEFT JOIN local_ids gqi ON q.id = gqi.quote_id
                        WHERE guild_id = ? AND id = ?
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild).setInt(id))
                .readRow(this::buildQuote)
                .first();
    }


    List<Quote> getByLocalId(List<Integer> ids) {
        return ids.stream().map(this::getByLocalId).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
    }
    List<Quote> getById(List<Integer> ids) {
        return ids.stream().map(this::getById).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
    }

    private Optional<Quote> getByLocalId(int id) {
        return builder(Quote.class).
                query("""
                        SELECT id, local_id
                        FROM quote q
                        LEFT JOIN local_ids gqi ON q.id = gqi.quote_id
                        WHERE guild_id = ? AND local_id = ?
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild).setInt(id))
                .readRow(this::buildQuote)
                .firstSync();
    }
    private Optional<Quote> getById(int id) {
        return builder(Quote.class).
                query("""
                        SELECT id, local_id
                        FROM quote q
                        LEFT JOIN local_ids gqi ON q.id = gqi.quote_id
                        WHERE guild_id = ? AND id = ?
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild).setInt(id))
                .readRow(this::buildQuote)
                .firstSync();
    }

    private Quote buildQuote(ResultSet r) throws SQLException {
        return ofId(r.getInt("id"), r.getInt("local_id"));
    }

    private Quote ofId(int id, int localId) {
        return new Quote(this, sources, links, id, localId);
    }

    public CompletableFuture<Integer> quoteCount() {
        return builder(Integer.class)
                .query("SELECT COUNT(1) FROM quote WHERE guild_id = ?")
                .paramsBuilder(stmt -> stmt.setLong(guild))
                .readRow(r -> r.getInt(1))
                .first()
                .thenApply(Optional::get);
    }

    void invalidate(Quote quote) {
        quoteCache.invalidate(quote.id());
    }
}
