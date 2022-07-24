package de.chojo.shepquotes.interactions.commands.transfer;

import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.shepquotes.data.QuoteData;

public class Transfer extends SlashCommand {
    public Transfer(QuoteData quoteData) {
        super(Slash.of("transfer", "Transfer a quote to another user.")
                .guildOnly()
                .command(new Handler(quoteData))
                .argument(Argument.integer("id", "Id of the quote.").asRequired())
                .argument(Argument.user("user", "The new owner of this quote.").asRequired()));
    }
}
