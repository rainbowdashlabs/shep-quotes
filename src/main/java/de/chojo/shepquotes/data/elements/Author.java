package de.chojo.shepquotes.data.elements;

public class Author {
    private final int id;
    private final long guildId;
    private final String name;

    public Author(int id, long guildId, String name) {
        this.id = id;
        this.guildId = guildId;
        this.name = name;
    }

    public static Author of(long guildId, String name) {
        return new Author(-1, guildId, name);
    }

    public static Author of(int id, long guildId, String name) {
        return new Author(id, guildId, name);
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

    public Author toRegisteredAuthor(int id){
        return new Author(id, guildId, name);
    }
}
