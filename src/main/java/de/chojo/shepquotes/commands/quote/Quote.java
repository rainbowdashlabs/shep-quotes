package de.chojo.shepquotes.commands.quote;

import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.shepquotes.commands.quote.handler.Default;
import de.chojo.shepquotes.data.QuoteData;

public class Quote extends SlashCommand {

    public Quote(QuoteData quoteData) {
        super(Slash.of("quote", "command.quote.description")
                .command(new Default(quoteData))
                .argument(Argument.integer("id", "command.quote.options.id.description"))
                .argument(Argument.text("source", "command.quote.options.source.description").withAutoComplete())
                .argument(Argument.text("content", "command.quote.options.content.description"))
                .build());
    }
}
