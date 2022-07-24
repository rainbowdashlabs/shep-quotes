package de.chojo.shepquotes.interactions.commands.add;

import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.shepquotes.data.QuoteData;

public class Add extends SlashCommand {
    public Add(QuoteData quoteData) {
        super(Slash.of("add", "Add a new quote")
                .guildOnly()
                .command(new Handler(quoteData)));
    }
}
