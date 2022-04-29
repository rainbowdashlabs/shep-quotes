package de.chojo.shepquotes.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import javax.sql.DataSource;

public class Remove extends SimpleCommand {
    protected Remove(DataSource dataSource) {
        super(CommandMeta.builder("remove", "Remove a quote")
                .addArgument(SimpleArgument.integer("id", "The quote id")));
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        // TODO: Implement modal when implemented
        // https://github.com/DV8FromTheWorld/JDA/pull/2024
    }
}
