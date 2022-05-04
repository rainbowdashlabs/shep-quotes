package de.chojo.shepquotes.data.elements;

import de.chojo.shepquotes.data.dao.Source;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record QuoteSnapshot(int id, int localId, long guildId, long ownerId, String content, List<Source> sources,
                            LocalDateTime created, LocalDateTime modified) {
    public MessageEmbed embed() {
        var sources = sources().stream().map(Source::name).collect(Collectors.joining(", "));

        return new EmbedBuilder()
                .setTitle(String.format("#%s", localId()))
                .setDescription(content())
                .setTimestamp(created())
                .addField("", sources, false)
                .setFooter(String.format("%s - %s", "unkown", ownerId()))
                .build();
    }

    public Message message(){
        return new MessageBuilder().setEmbeds(embed()).build();
    }
}
