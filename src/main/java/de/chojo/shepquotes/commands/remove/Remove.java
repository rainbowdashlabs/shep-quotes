package de.chojo.shepquotes.commands.remove;

import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.shepquotes.data.QuoteData;

public class Remove extends SlashCommand {
    public Remove(QuoteData quoteData) {
        super(Slash.of("remove", "command.remove.description")
                .command(new Handler(quoteData))
                .argument(Argument.integer("id", "command.remove.id.description").asRequired()));
    }
}
