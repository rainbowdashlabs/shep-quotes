package de.chojo.shepquotes.commands.edit;

import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.shepquotes.commands.edit.handler.Default;
import de.chojo.shepquotes.data.QuoteData;

public class Edit extends SlashCommand {

    public Edit(QuoteData quoteData) {
        super(Slash.of("edit", "command.edit.description")
                .command(new Default(quoteData))
                .argument(Argument.integer("id", "command.edit.options.id.description").asRequired()));
    }
}
