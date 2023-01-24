package de.chojo.shepquotes.commands.remove;

import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.shepquotes.commands.remove.handler.Default;
import de.chojo.shepquotes.data.QuoteData;

public class Remove extends SlashCommand {

    public Remove(QuoteData quoteData) {
        super(Slash.of("remove", "command.remove.description")
                .command(new Default(quoteData))
                .argument(Argument.integer("id", "command.remove.options.id.description").asRequired()));
    }
}
