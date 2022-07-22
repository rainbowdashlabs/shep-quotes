package de.chojo.shepquotes.commands.source;

import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.shepquotes.commands.source.handler.Delete;
import de.chojo.shepquotes.commands.source.handler.Merge;
import de.chojo.shepquotes.commands.source.handler.Rename;
import de.chojo.shepquotes.data.QuoteData;
import net.dv8tion.jda.api.Permission;

public class Source extends SlashCommand {
    public Source(QuoteData quoteData) {
        super(Slash.of("source", "command.source.description")
                .withPermission(Permission.MESSAGE_MANAGE)
                .subCommand(SubCommand.of("merge", "command.source.merge.description")
                        .handler(new Merge(quoteData))
                        .argument(Argument.string("merge", "command.source.merge.merge.description").asRequired().withAutoComplete())
                        .argument(Argument.string("into", "command.source.merge.into.description").asRequired().withAutoComplete()))
                .subCommand(SubCommand.of("rename", "command.source.rename.description")
                        .handler(new Rename(quoteData))
                        .argument(Argument.string("source", "command.source.rename.source.description").asRequired().withAutoComplete())
                        .argument(Argument.string("into", "command.source.rename.into.description").asRequired()))
                .subCommand(SubCommand.of("delete", "command.source.delete.description")
                        .handler(new Delete(quoteData))
                        .argument(Argument.string("name", "command.source.delete.name.description").asRequired().withAutoComplete()))
                .build());
    }
}
