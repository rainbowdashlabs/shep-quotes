package de.chojo.shepquotes.data.dao;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.chojo.sadu.base.QueryFactory;
import de.chojo.sadu.wrapper.QueryBuilderConfig;
import de.chojo.sadu.wrapper.util.Row;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

public class Sources extends QueryFactory {

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

    public List<Source> ofQuote(Quote quote) {
        return builder(Source.class).query("""
                        SELECT s.id, s.name FROM source_links l
                        LEFT JOIN source s ON s.id = l.source_id
                        WHERE l.quote_id = ?
                        """)
                .parameter(stmt -> stmt.setInt(quote.id()))
                .readRow(r -> new Source(this, links, r.getInt(1), r.getString("name")))
                .allSync();
    }

    public Source getOrCreate(String name) {
        return get(name).orElseGet(() -> create(name));
    }

    public Optional<Source> get(String name) {
        return builder(Source.class)
                .query("""
                        SELECT id, name FROM source WHERE name ILIKE ? AND guild_id = ?
                        """)
                .parameter(stmt -> stmt.setString(name).setLong(guild))
                .readRow(this::buildSource)
                .firstSync();
    }

    public Source create(String name) {
        return builder(Source.class)
                .query("""
                        INSERT INTO source(name, guild_id) VALUES(?, ?) RETURNING id, name
                        """)
                .parameter(stmt -> stmt.setString(name).setLong(guild))
                .readRow(this::buildSource)
                .firstSync()
                .get();
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent") // Used only internally
    public Source getAuthorById(int id) {
        try {
            return sourceCache.get(id, () -> builder(Source.class)
                    .query("""
                            SELECT id, name FROM source WHERE id = ?
                            """)
                    .parameter(stmt -> stmt.setInt(id))
                    .readRow(r -> new Source(this, links, r.getInt("id"), r.getString("name")))
                    .firstSync().get());
        } catch (ExecutionException e) {
            return null;
        }
    }

    private Source buildSource(Row r) throws SQLException {
        return ofIdAndName(r.getInt("id"), r.getString("name"));
    }

    private Source ofIdAndName(int id, String name) {
        return syncCache(id, name);
    }

    private Source syncCache(int id, String name) {
        try {
            return sourceCache.get(id, () -> new Source(this, links, id, name)).update(name);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    void invalidate(Source source) {
        sourceCache.invalidate(source.id());
    }

    public List<String> suggest(String value) {
        if (value.isBlank()) {
            return builder(String.class).query("""
                            SELECT name FROM source WHERE guild_id = ? LIMIT 15
                            """).parameter(stmt -> stmt.setLong(guild))
                    .readRow(r -> r.getString("name"))
                    .allSync();
        }
        return builder(String.class).query("""
                            SELECT name FROM source WHERE guild_id = ? AND name ILIKE ? LIMIT 15;
                        """).parameter(stmt -> stmt.setLong(guild).setString(String.format("%%%s%%", value)))
                .readRow(r -> r.getString("name"))
                .allSync();
    }
}
