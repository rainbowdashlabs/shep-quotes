package de.chojo.shepquotes.data.elements;

import de.chojo.jdautil.util.MentionUtil;
import de.chojo.shepquotes.data.dao.Source;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

public record QuoteSnapshot(int id, int localId, long guildId, long ownerId, String content, List<Source> sources,
                            LocalDateTime created, LocalDateTime modified) {
    public MessageEmbed embed() {
        var quote = String.format("%s%n%n%s%n%s | %s",
                content,
                sources().stream().map(Source::name).collect(Collectors.joining(", ")),
                MentionUtil.user(ownerId()),
                TimeFormat.DATE_TIME_LONG.format(modified.toEpochSecond(ZoneOffset.UTC) * 1000));

        return new EmbedBuilder()
                .setTitle(String.format("#%s", localId()))
                .setDescription(quote)
                .build();
    }
}
