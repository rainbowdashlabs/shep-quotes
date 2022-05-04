package de.chojo.shepquotes.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.shepquotes.data.QuoteData;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Manage extends SimpleCommand {
    private QuoteData quoteData;

    public Manage(QuoteData quoteData) {
        super(CommandMeta.builder("manage", "Manage bot settings")
                .addSubCommand("info", "Informations about the current settings.")
                .addSubCommand("quote_channel", "Set a channel where quotes should be posted.", argsBuilder()
                        .add(SimpleArgument.channel("channel", "the quote channel. Leave empty to remove")))
                .build()
        );
        this.quoteData = quoteData;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        if ("quote_channel".equals(event.getSubcommandName())) {
            var quotes = quoteData.quotes(event.getGuild());
            var channel = event.getOption("channel");
            if (channel == null) {
                event.reply("Quote channel Removed").queue();
                quotes.quoteChannel().remove();
                return;
            }
            event.reply("Quote channel set. Posting quotes.").queue();
            //TODO handle invalid channel type
            quotes.quoteChannel().set(channel.getAsTextChannel());
        }
    }
}
