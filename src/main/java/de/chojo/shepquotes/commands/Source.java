package de.chojo.shepquotes.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import javax.sql.DataSource;

public class Source extends SimpleCommand {
    protected Source(DataSource dataSource) {
        super("source", null, "Manage Quote Sources", subCommandBuilder()
                .add("merge", "Merge a source with another", argsBuilder()
                        .add(OptionType.STRING, "merge", "The source which should be merged")
                        .add(OptionType.STRING, "into", "The new source")
                        .build())
                .add("rename", "Rename a source", argsBuilder()
                        .add(OptionType.STRING, "source", "The old source name")
                        .add(OptionType.STRING, "into", "The new source name")
                        .build())
                .add("delete", "Delete a source", argsBuilder()
                        .add(OptionType.STRING, "source", "The old source name")
                        .add(OptionType.STRING, "into", "The new source name")
                        .build())
                .build(), Permission.UNKNOWN);
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
    }
}
