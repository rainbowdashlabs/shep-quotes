package de.chojo.shepquotes.interactions.commands.source;

import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.shepquotes.interactions.commands.source.handler.Delete;
import de.chojo.shepquotes.interactions.commands.source.handler.Merge;
import de.chojo.shepquotes.interactions.commands.source.handler.Rename;
import de.chojo.shepquotes.data.QuoteData;
import net.dv8tion.jda.api.Permission;

public class Source extends SlashCommand {
    public Source(QuoteData quoteData) {
        super(Slash.of("source", "Manage Quote Sources")
                .guildOnly()
                .withPermission(Permission.MESSAGE_MANAGE)
                .subCommand(SubCommand.of("merge", "Merge a source with another")
                        .handler(new Merge(quoteData))
                        .argument(Argument.text("merge", "The source which should be merged").asRequired().withAutoComplete())
                        .argument(Argument.text("into", "The new source").asRequired().withAutoComplete()))
                .subCommand(SubCommand.of("rename", "Rename a source")
                        .handler(new Rename(quoteData))
                        .argument(Argument.text("source", "The current source name").asRequired().withAutoComplete())
                        .argument(Argument.text("into", "The new source name").asRequired()))
                .subCommand(SubCommand.of("delete", "Delete a source")
                        .handler(new Delete(quoteData))
                        .argument(Argument.text("name", "Name of the source to delete").asRequired().withAutoComplete()))
                .build());
    }
}
