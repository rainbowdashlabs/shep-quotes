package de.chojo.shepquotes.commands.edit;

import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.shepquotes.data.QuoteData;

public class Edit extends SlashCommand {

    public Edit(QuoteData quoteData) {
        super(Slash.of("edit", "command.edit.description")
                .publicCommand()
                .command(new Handler(quoteData))
                .argument(Argument.integer("id", "command.edit.id.description").asRequired())
        );
    }
}
