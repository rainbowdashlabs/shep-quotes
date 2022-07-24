package de.chojo.shepquotes.interactions.commands.quote.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.shepquotes.data.QuoteData;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class Id extends BasePagedHandler implements SlashHandler {
    private static final Logger log = getLogger(Id.class);
    private final QuoteData quoteData;

    public Id(QuoteData quoteData) {
        this.quoteData = quoteData;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var quotes = quoteData.quotes(event.getGuild());
        var search = quotes.search();

        var term = event.getOption("id").getAsString();
        var quote = search.id(event.getOption("id").getAsInt());

        if (quote.isEmpty()) {
            event.reply(context.localize("error.notFound.", Replacement.create("ID", term))).queue();
            return;
        }

        registerPage(quotes, event, context, quote);
    }
}
