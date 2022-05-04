package de.chojo.shepquotes.data.dao;

import de.chojo.shepquotes.data.elements.QuoteSnapshot;
import de.chojo.sqlutil.base.QueryFactoryHolder;
import de.chojo.sqlutil.conversion.ArrayConverter;
import de.chojo.sqlutil.wrapper.QueryBuilderConfig;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

public class Quote extends QueryFactoryHolder {
    private static final Logger log = getLogger(Quote.class);
    private Quotes quotes;
    private Sources sources;
    private Links links;
    private final int id;
    private final int localId;

    public Quote(Quotes quotes, Sources sources, Links links, int id, int localId) {
        super(links.source(), QueryBuilderConfig.builder()
                .withExceptionHandler(err -> log.error("Unhandled exception", err))
                .build());
        this.quotes = quotes;
        this.sources = sources;
        this.links = links;
        this.id = id;
        this.localId = localId;
    }

    public CompletableFuture<Integer> content(String content) {
        return builder().query("""
                        INSERT INTO content(quote_id, content) VALUES(?,?)
                        ON CONFLICT (quote_id)
                            DO UPDATE
                                SET content = excluded.content
                        """)
                .paramsBuilder(stmt -> stmt.setInt(id).setString(content))
                .append()
                .query("""
                        UPDATE quote SET modified = (NOW() AT TIME ZONE 'utc') WHERE id = ?
                        """)
                .paramsBuilder(stmt -> stmt.setInt(id))
                .insert()
                .execute();
    }

    public void link(Source source) {
        links.link(this, source);
    }

    public void delete() {
        builder().query("""
                DELETE FROM quote WHERE id = ?
                """)
                .paramsBuilder(stmt -> stmt.setInt(id))
                .delete()
                .executeSync();
        quotes.invalidate(this);
    }

    public List<Source> sources() {
        return sources.ofQuote(this);
    }

    public int id() {
        return id;
    }

    public QuoteSnapshot snapshot() {
        //TODO implement caching
        return builder(QuoteSnapshot.class)
                .query("""
                        SELECT q.id, gqi.local_id, q.guild_id, c.content, q.owner, q.created, q.modified, a.ids
                        FROM quote q
                        LEFT JOIN content c ON q.id = c.quote_id
                        LEFT JOIN source_ids a ON q.id = a.quote_id
                        LEFT JOIN local_ids gqi ON q.id = gqi.quote_id
                        WHERE q.id = ?
                        """)
                .paramsBuilder(stmt -> stmt.setInt(id))
                .readRow(r -> {
                    List<Integer> authorIds = ArrayConverter.toList(r, "ids");
                    return new QuoteSnapshot(r.getInt("id"),
                            r.getInt("local_id"),
                            r.getLong("guild_id"),
                            r.getLong("owner"),
                            r.getString("content"),
                            authorIds.stream().map(this.sources::getAuthorById).collect(Collectors.toList()),
                            r.getTimestamp("created").toLocalDateTime(),
                            r.getTimestamp("modified").toLocalDateTime());
                })
                .firstSync()
                .get();
    }

    public void clearSources() {
        links.clear(this);
    }

    public int localId() {
        return localId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Quote)) return false;

        Quote quote = (Quote) o;

        return id == quote.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
