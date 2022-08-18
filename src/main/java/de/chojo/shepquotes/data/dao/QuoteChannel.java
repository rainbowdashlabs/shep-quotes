package de.chojo.shepquotes.data.dao;

import de.chojo.sadu.base.QueryFactory;
import de.chojo.sadu.wrapper.QueryBuilderConfig;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

public class QuoteChannel extends QueryFactory {
    private static final Logger log = getLogger(QuoteChannel.class);
    private final Quotes quotes;

    private long channelId = -1L;

    public QuoteChannel(Quotes quotes) {
        super(quotes.source(), QueryBuilderConfig.builder()
                .withExceptionHandler(err -> log.error("Unhandled exception", err))
                .build());
        this.quotes = quotes;
    }

    public Optional<TextChannel> channel() {
        if (channelId == -1L) {
            channelId = builder(Long.class).query("""
                                SELECT quote_channel FROM settings WHERE guild_id = ?;
                            """)
                    .parameter(stmt -> stmt.setLong(quotes.guild().getIdLong()))
                    .readRow(r -> r.getLong("quote_channel"))
                    .firstSync()
                    .orElse(-1L);
        }
        return Optional.ofNullable(quotes.guild().getTextChannelById(channelId));
    }

    public void remove() {
        clearPosts();
        builder().query("""
                        UPDATE settings
                        SET quote_channel = NULL
                        WHERE guild_id = ?;
                        """)
                .parameter(stmt -> stmt.setLong(quotes.guild().getIdLong()))
                .update()
                .send();
    }

    public void set(TextChannel channel) {
        remove();
        channelId = channel.getIdLong();
        builder().query("""
                            INSERT INTO settings(guild_id, quote_channel) VALUES(?, ?)
                            ON CONFLICT(guild_id)
                                DO UPDATE SET quote_channel = excluded.quote_channel
                        """)
                .parameter(stmt -> stmt.setLong(channel.getGuild().getIdLong()).setLong(channelId))
                .insert()
                .send();
        buildPosts();
    }

    public List<Post> posts() {
        return builder(Post.class).query("""
                        SELECT quote_id,
                            message_id
                        FROM quote_posts p LEFT JOIN quote q ON p.quote_id = q.id
                        WHERE guild_id = ?
                        """)
                .parameter(stmt -> stmt.setLong(quotes.guild().getIdLong()))
                .readRow(r -> new Post(this, quotes.getById(r.getInt("quote_id")).get(), r.getLong("message_id")))
                .allSync();
    }

    public Optional<Post> getPost(Quote quote) {
        return builder(Post.class)
                .query("""
                        SELECT quote_id, message_id FROM quote_posts WHERE quote_id = ?;
                        """)
                .parameter(stmt -> stmt.setInt(quote.id()))
                .readRow(r -> new Post(this, quotes.getById(r.getInt("quote_id")).get(), r.getLong("message_id")))
                .firstSync();
    }

    public void clear() {
        clearPosts();
        remove();
    }

    public void clearPosts() {
        for (var post : posts()) {
            post.delete();
        }
    }

    public void buildPosts() {
        var count = quotes.quoteCount();
        for (var i = 1; i < count; i++) {
            quotes.byLocalId(i).ifPresent(this::createPost);
        }
    }

    public void createPost(Quote quote) {
        new Post(this, quote).post();
    }
}
