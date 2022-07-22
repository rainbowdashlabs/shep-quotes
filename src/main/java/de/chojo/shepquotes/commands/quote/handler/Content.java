package de.chojo.shepquotes.commands.quote.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.shepquotes.data.QuoteData;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Content extends BasePagedHandler implements SlashHandler {
    private final QuoteData quoteData;

    public Content(QuoteData quoteData) {
        this.quoteData = quoteData;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var quotes = quoteData.quotes(event.getGuild());
        var search = quotes.search();
        var localizer = context.localizer();
        var term = event.getOption("content").getAsString();
        var source = search.content(term);

        if (source.isEmpty()) {
            event.reply(localizer.localize("error.notFound.", Replacement.create("ID", term))).queue();
            return;
        }

        registerPage(source, event, context);
    }
}
