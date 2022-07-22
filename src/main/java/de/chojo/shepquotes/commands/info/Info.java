package de.chojo.shepquotes.commands.info;

import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.shepquotes.config.Configuration;

public class Info extends SlashCommand {
    public Info(Configuration configuration) {
        super(Slash.of("info", "command.info.description")
                .command(Handler.create(configuration)));
    }
}
