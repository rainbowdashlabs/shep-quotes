package de.chojo.shepquotes.data.dao;

import de.chojo.sadu.base.QueryFactory;
import de.chojo.sadu.wrapper.QueryBuilderConfig;
import de.chojo.sadu.wrapper.util.UpdateResult;
import de.chojo.shepquotes.data.elements.QuoteSnapshot;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.slf4j.LoggerFactory.getLogger;

public class Quote extends QueryFactory {
    private static final Logger log = getLogger(Quote.class);
    private Quotes quotes;
    private Sources sources;
    private Links links;
    private final int id;
    private final int localId;
    private long owner;

    public Quote(Quotes quotes, Sources sources, Links links, int id, int localId, long owner) {
        super(links.source(), QueryBuilderConfig.builder()
                .withExceptionHandler(err -> log.error("Unhandled exception", err))
                .build());
        this.quotes = quotes;
        this.sources = sources;
        this.links = links;
        this.id = id;
        this.localId = localId;
        this.owner = owner;
    }

    public CompletableFuture<UpdateResult> content(String content) {
        return builder().query("""
                        INSERT INTO content(quote_id, content) VALUES(?,?)
                        ON CONFLICT (quote_id)
                            DO UPDATE
                                SET content = excluded.content
                        """)
                .parameter(stmt -> stmt.setInt(id).setString(content))
                .append()
                .query("""
                        UPDATE quote SET modified = (NOW() AT TIME ZONE 'utc') WHERE id = ?
                        """)
                .parameter(stmt -> stmt.setInt(id))
                .insert()
                .send();
    }

    public void link(Source source) {
        links.link(this, source);
    }

    public void delete() {
        builder().query("""
                        DELETE FROM quote WHERE id = ?
                        """)
                .parameter(stmt -> stmt.setInt(id))
                .delete()
                .sendSync();
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
                .parameter(stmt -> stmt.setInt(id))
                .readRow(r -> {
                    return new QuoteSnapshot(r.getInt("id"),
                            r.getInt("local_id"),
                            r.getLong("guild_id"),
                            r.getLong("owner"),
                            r.getString("content"),
                            r.getList("ids"),
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

    public boolean canAccess(Member member) {
        if (member.hasPermission(Permission.MESSAGE_MANAGE)) {
            return true;
        }
        return owner == member.getIdLong();
    }

    @Override
    public int hashCode() {
        return id;
    }

    public void owner(User owner) {
        var success = builder().query("UPDATE quote SET owner = ? WHERE id = ?")
                .parameter(stmt -> stmt.setLong(owner.getIdLong()))
                .update()
                .sendSync()
                .changed();
        if (success) {
            this.owner = owner.getIdLong();
        }
    }
}
