package de.chojo.shepquotes.interactions.commands.info;

import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.shepquotes.config.Configuration;

public class Info extends SlashCommand {
    public Info(Configuration configuration) {
        super(Slash.of("info", "Get information about me!")
                .guildOnly()
                .command(Handler.create(configuration)));
    }
}
