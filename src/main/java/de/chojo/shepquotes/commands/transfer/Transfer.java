package de.chojo.shepquotes.commands.transfer;

import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.shepquotes.commands.transfer.handler.Default;
import de.chojo.shepquotes.data.QuoteData;

public class Transfer extends SlashCommand {

    public Transfer(QuoteData quoteData) {
        super(Slash.of("transfer", "command.transfer.description")
                .command(new Default(quoteData))
                .argument(Argument.integer("id", "command.transfer.options.id.description").asRequired())
                .argument(Argument.user("user", "command.transfer.options.user.description").asRequired()));
    }
}
