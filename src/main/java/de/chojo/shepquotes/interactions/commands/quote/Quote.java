package de.chojo.shepquotes.interactions.commands.quote;

import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.shepquotes.interactions.commands.quote.handler.Content;
import de.chojo.shepquotes.interactions.commands.quote.handler.Id;
import de.chojo.shepquotes.interactions.commands.quote.handler.Random;
import de.chojo.shepquotes.interactions.commands.quote.handler.Source;
import de.chojo.shepquotes.data.QuoteData;

import static org.slf4j.LoggerFactory.getLogger;

public class Quote extends SlashCommand {
    public Quote(QuoteData quoteData) {
        super(Slash.of("quote", "See the quotes or search for them")
                .guildOnly()
                .subCommand(SubCommand.of("id", "Search quotes by id.")
                        .handler(new Id(quoteData))
                        .argument(Argument.integer("id", "Display a quote with a specific id").asRequired()))
                .subCommand(SubCommand.of("source", "Search quotes by source.")
                        .handler(new Source(quoteData))
                        .argument(Argument.text("source", "The source to search a quote.").asRequired().withAutoComplete()))
                .subCommand(SubCommand.of("content", "Search quotes by content.")
                        .handler(new Content(quoteData))
                        .argument(Argument.text("content", "Display quotes which contain a term").asRequired()))
                .subCommand(SubCommand.of("random", "Random quote")
                        .handler(new Random(quoteData))));
    }
}
