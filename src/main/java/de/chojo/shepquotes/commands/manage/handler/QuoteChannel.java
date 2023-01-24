package de.chojo.shepquotes.commands.manage.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.util.PermissionErrorHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.shepquotes.data.QuoteData;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Collections;

public class QuoteChannel implements SlashHandler {
    private final QuoteData quoteData;

    public QuoteChannel(QuoteData quoteData) {

        this.quoteData = quoteData;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var quotes = quoteData.quotes(event.getGuild());
        var channel = event.getOption("channel");
        if (channel == null) {
            event.reply("Quote channel Removed").setEphemeral(true).queue();
            quotes.quoteChannel().remove();
            return;
        }
        if (channel.getChannelType() != ChannelType.TEXT) {
            event.reply(context.localize("error.noTextChannel")).setEphemeral(true).queue();
            return;
        }

        if (PermissionErrorHandler.assertAndHandle(channel.getAsChannel().asTextChannel(), context.guildLocalizer().localizer(), Collections.emptyList(),
                Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND)) {
            return;
        }

        event.reply(context.localize("command.manage.quotechannel.set")).setEphemeral(true).queue();
        quotes.quoteChannel().set(channel.getAsChannel().asTextChannel());

    }
}
