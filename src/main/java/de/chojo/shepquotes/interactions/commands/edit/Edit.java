package de.chojo.shepquotes.interactions.commands.edit;

import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.shepquotes.data.QuoteData;

public class Edit extends SlashCommand {

    public Edit(QuoteData quoteData) {
        super(Slash.of("edit", "Add a new quote")
                .guildOnly()
                .command(new Handler(quoteData))
                .argument(Argument.integer("id", "Id of the quote.").asRequired())
        );
    }
}
