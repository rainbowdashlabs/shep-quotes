package de.chojo.shepquotes.interactions.commands.remove;

import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.shepquotes.data.QuoteData;

public class Remove extends SlashCommand {
    public Remove(QuoteData quoteData) {
        super(Slash.of("remove", "Remove a quote")
                .guildOnly()
                .command(new Handler(quoteData))
                .argument(Argument.integer("id", "The id of the quote which should be deleted.").asRequired()));
    }
}
