package de.chojo.shepquotes.data.elements;

import java.time.LocalDateTime;
import java.util.List;

public class Quote {
    private final int id;
    private final long guildId;
    private final long ownerId;
    private final String content;
    private final List<Author> authors;
    private final LocalDateTime created;
    private final LocalDateTime modified;

    public Quote(int id, long guildId, long ownerId, String content, List<Author> authors, LocalDateTime created, LocalDateTime modified) {
        this.id = id;
        this.guildId = guildId;
        this.ownerId = ownerId;
        this.content = content;
        this.authors = authors;
        this.created = created;
        this.modified = modified;
    }

    public String content() {
        return content;
    }

    public List<Author> authors() {
        return authors;
    }

    public long guildId() {
        return guildId;
    }

    public long ownerId() {
        return ownerId;
    }
}
