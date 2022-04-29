package de.chojo.shepquotes.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import javax.sql.DataSource;

public class Manage extends SimpleCommand {
    protected Manage(DataSource dataSource) {
        super(CommandMeta.builder("manage", "Manage bot settings")
                .addSubCommand("info", "Informations about the current settings.")
                .addSubCommand("quotechannel", "Set a channel where new quotes should be posted.", argsBuilder()
                        .add(SimpleArgument.channel("channel", "the quote channel. Leave empty to remove")))
                .build()
        );
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {

    }
}
