package de.chojo.shepquotes.data.dao;

import de.chojo.sqlutil.base.QueryFactoryHolder;
import de.chojo.sqlutil.wrapper.QueryBuilderConfig;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.slf4j.LoggerFactory.getLogger;

public class Search extends QueryFactoryHolder {

    private static final Logger log = getLogger(Search.class);
    private final long guild;
    private final Quotes quotes;

    public Search(Guild guild, Quotes quotes, DataSource dataSource) {
        super(dataSource, QueryBuilderConfig.builder()
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
                .thenApply(quotes::getQuotesByIds);
    }

    public CompletableFuture<List<Quote>> source(String name) {
        return builder(Integer.class)
                .query("""
                        WITH sourcesnapshots AS(
                             SELECT DISTINCT a.id
                             FROM source a
                             WHERE a.guild_id = ? AND a.name ILIKE ?
                             )
                        SELECT DISTINCT l.quote_id
                        FROM source_links l
                        WHERE l.quote_id IN (SELECT a.id FROM sourcesnapshots a)
                        """)
                .paramsBuilder(stmt -> stmt.setLong(guild).setString(name))
                .readRow(r -> r.getInt("id"))
                .all()
                .thenApply(quotes::getQuotesByIds);
    }

    public CompletableFuture<Optional<Quote>> id(int id) {
        return quotes.byId(id);
    }
}
