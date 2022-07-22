package de.chojo.shepquotes.commands.quote.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.shepquotes.data.QuoteData;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class Random extends BasePagedHandler implements SlashHandler {
    private static final Logger log = getLogger(Random.class);
    private final QuoteData quoteData;

    public Random(QuoteData quoteData) {
        this.quoteData = quoteData;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var quotes = quoteData.quotes(event.getGuild());
        var quote = quotes.random();

        registerPage(quotes, event, context, quote);
    }
}
