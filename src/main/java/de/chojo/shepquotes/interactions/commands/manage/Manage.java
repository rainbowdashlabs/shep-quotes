package de.chojo.shepquotes.interactions.commands.manage;

import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.shepquotes.interactions.commands.manage.handler.Info;
import de.chojo.shepquotes.interactions.commands.manage.handler.QuoteChannel;
import de.chojo.shepquotes.interactions.commands.manage.handler.Refresh;
import de.chojo.shepquotes.data.QuoteData;
import net.dv8tion.jda.api.Permission;

public class Manage extends SlashCommand {
    public Manage(QuoteData quoteData) {
        super(Slash.of("manage", "Manage bot settings")
                .guildOnly()
                .withPermission(Permission.MESSAGE_MANAGE)
                .subCommand(SubCommand.of("info", "Manage bot settings")
                        .handler(new Info(quoteData)))
                .subCommand(SubCommand.of("refresh", "Refresh all quote messages in the quote channel.")
                        .handler(new Refresh(quoteData)))
                .subCommand(SubCommand.of("quotechannel", "Set a channel where quotes should be posted.")
                        .handler(new QuoteChannel(quoteData))
                        .argument(Argument.channel("channel", "The quote channel. Leave empty to remove.")))
        );
    }
}
