package de.chojo.shepquotes.data.elements;

public class SourceSnapshot {
    private final int id;
    private final long guildId;
    private final String name;

    public SourceSnapshot(int id, long guildId, String name) {
        this.id = id;
        this.guildId = guildId;
        this.name = name;
    }

    public static SourceSnapshot of(long guildId, String name) {
        return new SourceSnapshot(-1, guildId, name);
    }

    public static SourceSnapshot of(int id, long guildId, String name) {
        return new SourceSnapshot(id, guildId, name);
    }

    public static SourceSnapshot unkown() {
        return of(-1, -1, "Unkown");
    }

    public int id() {
        return id;
    }

    public String name() {
        return name;
    }

    public long guildId() {
        return guildId;
    }

    public SourceSnapshot toRegisteredAuthor(int id) {
        return new SourceSnapshot(id, guildId, name);
    }
}
