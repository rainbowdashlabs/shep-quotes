package de.chojo.shepquotes.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.shepquotes.data.QuoteData;
import de.chojo.shepquotes.listener.QuotePageService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class Quote extends SimpleCommand {
    private final QuoteData quoteData;
    private final QuotePageService quotePages;

    protected Quote(QuoteData quoteData, QuotePageService quotePages) {
        super("quote", null, "Search for quotes",
                argsBuilder()
                        .add(OptionType.INTEGER, "id", "Quote id", false)
                        .add(OptionType.STRING, "source", "Search by source or author", false)
                        .add(OptionType.STRING, "content", "Search by quote content", false)
                        .build(),
                Permission.UNKNOWN);
        this.quoteData = quoteData;
        this.quotePages = quotePages;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        if (event.getOption("id") != null) {
            quoteData.retrieveQuoteById(event.getGuild(), event.getOption("id").getAsInt())
                    .thenAccept(quote -> {
                        if (quote.isEmpty()) {
                            event.reply("No quote found.").queue();
                            return;
                        }
                        quotePages.scrollQuotes(event, quote.get().guildQuoteId());
                    });
            return;
        }

        if (event.getOption("source") != null) {
            quoteData.retrieveQuotesBySource(event.getGuild(), event.getOption("source").getAsString())
                    .thenAccept(quotes -> quotePages.registerPage(event, quotes));
            return;
        }

        if (event.getOption("content") != null) {
            quoteData.retrieveQuotesByContent(event.getGuild(), event.getOption("content").getAsString())
                    .thenAccept(quotes -> quotePages.registerPage(event, quotes));
            return;
        }

        quoteData.retrieveRandomQuote(event.getGuild())
                .thenAccept(quote -> {
                    if (quote.isEmpty()) {
                        event.reply("No quote found.").queue();
                        return;
                    }
                    quotePages.scrollQuotes(event, quote.get().guildQuoteId());
                });
    }
}
