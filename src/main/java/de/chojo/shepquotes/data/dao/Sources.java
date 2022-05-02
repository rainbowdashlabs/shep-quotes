package de.chojo.shepquotes.data.dao;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.chojo.sqlutil.base.QueryFactoryHolder;
import de.chojo.sqlutil.wrapper.QueryBuilderConfig;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

public class Sources extends QueryFactoryHolder {

    private final Cache<Integer, Source> sourceCache = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build();
    private static final Logger log = getLogger(Sources.class);
    private final long guild;
    private final Links links;

    public Sources(Guild guild, Links links) {
        super(links.source(), QueryBuilderConfig.builder()
                .withExceptionHandler(err -> log.error("Unhandled exception", err))
                .build());
        this.guild = guild.getIdLong();
        this.links = links;
    }

    public CompletableFuture<List<Source>> ofQuote(Quote quote) {
        return builder(Source.class).query("""
                        SELECT s.id, s.name FROM source_links l
                        LEFT JOIN source s ON s.id = l.source_id
                        WHERE l.quote_id = ?
                        """)
                .paramsBuilder(stmt -> stmt.setInt(quote.id()))
                .readRow(r -> new Source(this, links, r.getInt(1), r.getString("name")))
                .all();
    }

    public CompletableFuture<Source> getOrCreate(String name) {
        return get(name).thenApply(source -> source.orElseGet(() -> create(name).get()));
    }

    public CompletableFuture<Optional<Source>> get(String name) {
        return builder(Source.class)
                .query("""
                        SELECT id, name FROM source WHERE name ILIKE ? AND guild_id = ?
                        """)
                .paramsBuilder(stmt -> stmt.setString(name).setLong(guild))
                .readRow(this::buildSource)
                .first();
    }

    public Optional<Source> create(String name) {
        return builder(Source.class)
                .query("""
                        INSERT INTO source(name, guild_id) VALUES(?, ?) RETURNING id, name
                        """)
                .paramsBuilder(stmt -> stmt.setString(name).setLong(guild))
                .readRow(this::buildSource)
                .firstSync();
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent") // Used only internally
    public Source getAuthorById(int id) {
        try {
            return sourceCache.get(id, () -> builder(Source.class)
                    .query("""
                            SELECT id, name FROM source WHERE id = ?
                            """)
                    .paramsBuilder(stmt -> stmt.setInt(id))
                    .readRow(this::buildSource)
                    .firstSync().get());
        } catch (ExecutionException e) {
            return null;
        }
    }

    private Source buildSource(ResultSet r) throws SQLException {
        return ofIdAndName(r.getInt("id"), r.getString("name"));
    }

    private Source ofIdAndName(int id, String name) {
        return new Source(this, links, id, name);
    }

    void invalidate(Source source) {
        sourceCache.invalidate(source.id());
    }

    public CompletableFuture<List<String>> suggest(String value) {
        if (value.isBlank()) {
            return builder(String.class).query("""
                            SELECT name FROM source WHERE guild_id = ? LIMIT 15
                            """).paramsBuilder(stmt -> stmt.setLong(guild))
                    .readRow(r -> r.getString("name"))
                    .all();
        }
        return builder(String.class).query("""
                    SELECT name FROM source WHERE guild_id = ? AND name ILIKE ? LIMIT 15;
                """).paramsBuilder(stmt -> stmt.setLong(guild).setString(String.format("%%%s%%", value)))
                .readRow(r -> r.getString("name"))
                .all();
    }
}
