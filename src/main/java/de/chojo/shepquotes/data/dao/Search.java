package de.chojo.shepquotes.data.dao;

import de.chojo.sqlutil.base.QueryFactoryHolder;
import de.chojo.sqlutil.wrapper.QueryBuilderConfig;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.slf4j.LoggerFactory.getLogger;

public class Search extends QueryFactoryHolder {

    private static final Logger log = getLogger(Search.class);
    private final long guild;
    private final Quotes quotes;

    public Search(Guild guild, Quotes quotes) {
        super(quotes.source(), QueryBuilderConfig.builder()
                .withExceptionHandler(err -> log.error("Unhandled exception", err))
                .build());
        this.guild = guild.getIdLong();
        this.quotes = quotes;
    }

    public CompletableFuture<List<Quote>> content(String content) {
        return builder(Integer.class)
                .query("""
                        SELECT quote_id
                        FROM content c
                        LEFT JOIN quote q ON c.quote_id = q.id
                        WHERE q.guild_id = ? AND c.content ILIKE ?
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild).setString(String.format("%%%s%%", content)))
                .readRow(r -> r.getInt("quote_id"))
                .all()
                .thenApply(quotes::getById);
    }

    public CompletableFuture<List<Quote>> source(String name) {
        return builder(Integer.class)
                .query("""
                        WITH sources AS(
                             SELECT DISTINCT a.id
                             FROM source a
                             WHERE a.guild_id = ? AND a.name ILIKE ?
                             )
                        SELECT DISTINCT l.quote_id as id
                        FROM source_links l
                        WHERE l.source_id = ANY (SELECT a.id FROM sources a)
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild).setString(String.format("%%%s%%", name)))
                .readRow(r -> r.getInt("id"))
                .all()
                .thenApply(quotes::getById);
    }

    public CompletableFuture<Optional<Quote>> id(int id) {
        return quotes.byLocalId(id);
    }
}
