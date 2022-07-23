package de.chojo.shepquotes.commands.quote;

import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.shepquotes.commands.quote.handler.Content;
import de.chojo.shepquotes.commands.quote.handler.Id;
import de.chojo.shepquotes.commands.quote.handler.Random;
import de.chojo.shepquotes.commands.quote.handler.Source;
import de.chojo.shepquotes.data.QuoteData;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class Quote extends SlashCommand {
    public Quote(QuoteData quoteData) {
        super(Slash.of("quote", "command.quote.description")
                .guildOnly()
                .subCommand(SubCommand.of("id", "command.quote.id.description")
                        .handler(new Id(quoteData))
                        .argument(Argument.integer("id", "command.quote.id.id.description").asRequired()))
                .subCommand(SubCommand.of("source", "command.quote.source.description")
                        .handler(new Source(quoteData))
                        .argument(Argument.string("source", "command.quote.source.source.description").asRequired().withAutoComplete()))
                .subCommand(SubCommand.of("content", "command.quote.content.description")
                        .handler(new Content(quoteData))
                        .argument(Argument.string("content", "command.quote.content.content.description").asRequired()))
                .subCommand(SubCommand.of("random", "command.quote.random.description")
                        .handler(new Random(quoteData))));
    }
}
