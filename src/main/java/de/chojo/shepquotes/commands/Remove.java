package de.chojo.shepquotes.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.util.Futures;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.shepquotes.data.QuoteData;
import de.chojo.shepquotes.data.dao.Quote;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class Remove extends SimpleCommand {
    private final QuoteData quoteData;

    public Remove(QuoteData quoteData) {
        super(CommandMeta.builder("remove", "Remove a quote")
                .addArgument(SimpleArgument.integer("id", "The quote id").asRequired()));
        this.quoteData = quoteData;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        quoteData.quotes(event.getGuild())
                .byLocalId(event.getOption("id", OptionMapping::getAsInt), event.getUser())
                .whenComplete(Futures.whenComplete(
                        optQuote -> {
                            if (optQuote.isEmpty()) {
                                event.reply(context.localize("Unknown quote")).queue();
                                return;
                            }

                            var quote = optQuote.get();
                            quote.delete();
                            event.reply("Deleted quote " + quote.localId()).queue();
                        }, err -> {

                        }));
    }
}
