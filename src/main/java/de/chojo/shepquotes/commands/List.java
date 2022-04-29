package de.chojo.shepquotes.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import javax.sql.DataSource;

public class List extends SimpleCommand {
    protected List(DataSource dataSource) {
        super(CommandMeta.builder("list", "List all existing quotes."));
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        // TODO: Implement modal when implemented
        // https://github.com/DV8FromTheWorld/JDA/pull/2024
    }
}
