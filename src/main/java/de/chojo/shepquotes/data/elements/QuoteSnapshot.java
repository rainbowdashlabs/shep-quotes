package de.chojo.shepquotes.data.elements;

import de.chojo.shepquotes.data.dao.Source;

import java.time.LocalDateTime;
import java.util.List;

public record QuoteSnapshot(int id, int localId, long guildId, long ownerId, String content, List<Source> sources,
                            LocalDateTime created, LocalDateTime modified) {
}
