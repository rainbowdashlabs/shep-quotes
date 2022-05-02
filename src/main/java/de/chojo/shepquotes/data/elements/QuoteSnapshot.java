package de.chojo.shepquotes.data.elements;

import de.chojo.shepquotes.data.dao.Source;

import java.time.LocalDateTime;
import java.util.List;

public class QuoteSnapshot {
    private int id;
    private final int guildQuoteId;
    private final long guildId;
    private final long ownerId;
    private final String content;
    private final List<Source> sources;
    private final LocalDateTime created;
    private final LocalDateTime modified;

    public QuoteSnapshot(int id, int guildQuoteId, long guildId, long ownerId, String content, List<Source> sources, LocalDateTime created, LocalDateTime modified) {
        this.id = id;
        this.guildQuoteId = guildQuoteId;
        this.guildId = guildId;
        this.ownerId = ownerId;
        this.content = content;
        this.sources = sources;
        this.created = created;
        this.modified = modified;
    }

    public String content() {
        return content;
    }

    public List<Source> sources() {
        return sources;
    }

    public long guildId() {
        return guildId;
    }

    public long ownerId() {
        return ownerId;
    }

    public int id() {
        return id;
    }

    public void id(int id) {
        if (id != -1) throw new IllegalStateException("Id is already set");
        this.id = id;
    }

    public int guildQuoteId() {
        return guildQuoteId;
    }

    public LocalDateTime created() {
        return created;
    }

    public LocalDateTime modified() {
        return modified;
    }
}
