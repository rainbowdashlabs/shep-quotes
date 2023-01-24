package de.chojo.shepquotes.commands.add;

import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.shepquotes.commands.add.handler.Default;
import de.chojo.shepquotes.data.QuoteData;

public class Add extends SlashCommand {

    public Add(QuoteData quoteData) {
        super(Slash.of("add", "command.add.description")
                .command(new Default(quoteData)));
    }
}
