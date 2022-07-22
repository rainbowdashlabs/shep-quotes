package de.chojo.shepquotes.commands;

import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;

import javax.sql.DataSource;

public class Import extends SlashCommand {
    protected Import(DataSource dataSource) {
        super(Slash.of("import", "Import quotes from another bot"));
    }
}
