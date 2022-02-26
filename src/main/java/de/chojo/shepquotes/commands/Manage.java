package de.chojo.shepquotes.commands;

import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import javax.sql.DataSource;

public class Manage extends SimpleCommand {
    protected Manage(DataSource dataSource) {
        super("manage", null, "Manage bot settings", subCommandBuilder()
                .add("info", "Informations about the current settings.")
                .add("managerrole", "Set the manager role of the bot.",
                        argsBuilder().add(OptionType.ROLE, "role", "The new manager role", true)
                                .build())
                .add("quotechannel", "Set a channel where new quotes should be posted. Leave empty to remove.", argsBuilder()
                        .add(OptionType.CHANNEL, "channel", "the quote channel")
                        .build())
                .build(), Permission.UNKNOWN);
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        
    }
}
