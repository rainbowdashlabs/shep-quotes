package de.chojo.shepquotes.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import javax.sql.DataSource;

public class Source extends SimpleCommand {
    protected Source(DataSource dataSource) {
        super(CommandMeta.builder("source", "Manage Quote Sources")
                .addSubCommand("merge", "Merge a source with another",
                        argsBuilder()
                                .add(SimpleArgument.string("merge", "The source which should be merged").asRequired())
                                .add(SimpleArgument.string("into", "The new source").asRequired()))
                .addSubCommand("rename", "Rename a source", argsBuilder()
                        .add(SimpleArgument.string("source", "The old source name").asRequired())
                        .add(SimpleArgument.string("into", "The new source name").asRequired()))
                .addSubCommand("delete", "Delete a source", argsBuilder()
                        .add(SimpleArgument.string("source", "The old source name"))
                        .add(SimpleArgument.string("into", "The new source name")))
                .build());
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
    }
}
