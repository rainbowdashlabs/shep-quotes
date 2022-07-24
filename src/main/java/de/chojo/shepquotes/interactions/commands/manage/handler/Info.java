package de.chojo.shepquotes.interactions.commands.manage.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.shepquotes.data.QuoteData;
import de.chojo.shepquotes.data.dao.Quotes;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Info implements SlashHandler {
    private final QuoteData quoteData;

    public Info(QuoteData quoteData) {
        this.quoteData = quoteData;
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext context) {

    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var quotes = quoteData.quotes(event.getGuild());
        var quoteChannel = quotes.quoteChannel();
        var embed = new LocalizedEmbedBuilder(context.guildLocalizer())
                .setTitle("command.manage.info.embed.title")
                .addField("command.manage.info.embed.quoteChannel", quoteChannel.channel().map(Channel::getAsMention).orElse("phrase.notSet"), true)
                .build();
        event.replyEmbeds(embed).setEphemeral(true).queue();
    }
}
