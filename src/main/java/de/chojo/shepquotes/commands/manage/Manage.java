package de.chojo.shepquotes.commands.manage;

import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashCommand;
import de.chojo.shepquotes.commands.manage.handler.Info;
import de.chojo.shepquotes.commands.manage.handler.QuoteChannel;
import de.chojo.shepquotes.commands.manage.handler.Refresh;
import de.chojo.shepquotes.data.QuoteData;
import net.dv8tion.jda.api.Permission;

public class Manage extends SlashCommand {
    public Manage(QuoteData quoteData) {
        super(Slash.of("manage", "command.manage.description")
                .guildOnly()
                .withPermission(Permission.MESSAGE_MANAGE)
                .subCommand(SubCommand.of("info", "command.manage.info.description")
                        .handler(new Info(quoteData)))
                .subCommand(SubCommand.of("refresh", "command.manage.refresh.description")
                        .handler(new Refresh(quoteData)))
                .subCommand(SubCommand.of("quotechannel", "command.manage.quotechannel.description")
                        .handler(new QuoteChannel(quoteData))
                        .argument(Argument.channel("channel", "command.manage.quotechannel.channel.description")))
        );
    }
}
