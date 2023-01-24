package de.chojo.shepquotes.commands.manage.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.shepquotes.data.QuoteData;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Info implements SlashHandler {
    private final QuoteData quoteData;

    public Info(QuoteData quoteData) {

        this.quoteData = quoteData;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var quotes = quoteData.quotes(event.getGuild());
        var quoteChannel = quotes.quoteChannel();
        var embed = new LocalizedEmbedBuilder(context.localizer())
                .setTitle("command.manage.info.embed.title")
                .addField("command.manage.info.embed.quotec_channel", quoteChannel.channel().map(Channel::getAsMention)
                                                                                  .orElse("phrase.notSet"), true)
                .build();
        event.replyEmbeds(embed).setEphemeral(true).queue();

    }
}
