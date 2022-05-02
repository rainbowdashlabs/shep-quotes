package de.chojo.shepquotes.data.dao;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.chojo.sqlutil.base.QueryFactoryHolder;
import de.chojo.sqlutil.wrapper.QueryBuilderConfig;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

public class Sources extends QueryFactoryHolder {

    private final Cache<Integer, Source> authorCache = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build();
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

    public CompletableFuture<de.chojo.shepquotes.data.dao.Source> getOrCreate(String name) {
        return builder(de.chojo.shepquotes.data.dao.Source.class)
                .query("""
                        SELECT id FROM source WHERE name ILIKE ? AND guild_id = ?
                        """)
                .paramsBuilder(stmt -> stmt.setString(name).setLong(guild))
                .readRow(r -> new de.chojo.shepquotes.data.dao.Source(links, r.getInt(1)))
                .first()
                .thenApply(source -> source.orElseGet(() -> create(name).get()));
    }

    public Optional<de.chojo.shepquotes.data.dao.Source> create(String name) {
        return builder(de.chojo.shepquotes.data.dao.Source.class)
                .query("""
                        INSERT INTO source(name, guild_id) VALUES(?, ?) RETURNING id
                        """)
                .paramsBuilder(stmt -> stmt.setString(name).setLong(guild))
                .readRow(r -> new de.chojo.shepquotes.data.dao.Source(links, r.getInt(1)))
                .firstSync();
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent") // Used only internally
    public Source getAuthorById(int id) {
        try {
            return authorCache.get(id, () -> builder(Source.class)
                    .query("""
                            SELECT id FROM source WHERE id = ?
                            """)
                    .paramsBuilder(stmt -> stmt.setInt(id))
                    .readRow(r -> new Source(links, r.getInt("id")))
                    .firstSync().get());
        } catch (ExecutionException e) {
            return null;
        }
    }
}
