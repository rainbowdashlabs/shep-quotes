package de.chojo.shepquotes.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.shepquotes.data.QuoteData;
import de.chojo.shepquotes.data.dao.Post;
import de.chojo.shepquotes.data.dao.Quotes;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Manage extends SimpleCommand {
    private final QuoteData quoteData;

    public Manage(QuoteData quoteData) {
        super(CommandMeta.builder("manage", "command.manage.descr")
                .addSubCommand("info", "command.manage.info.descr")
                .addSubCommand("refresh", "command.manage.refresh.descr")
                .addSubCommand("quote_channel", "command.manage.quoteChannel.descr", argsBuilder()
                        .add(SimpleArgument.channel("channel", "command.manage.quoteChannel.arg.channel")))
                .withPermission()
                .build()
        );
        this.quoteData = quoteData;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var quotes = quoteData.quotes(event.getGuild());
        if ("quote_channel".equals(event.getSubcommandName())) {
            var channel = event.getOption("channel");
            if (channel == null) {
                event.reply("Quote channel Removed").setEphemeral(true).queue();
                quotes.quoteChannel().remove();
                return;
            }
            if (channel.getChannelType() != ChannelType.TEXT) {
                event.reply(context.localize("error.noTextChannel")).setEphemeral(true).queue();
                return;
            }
            event.reply(context.localize("command.manage.quoteChannel.set")).setEphemeral(true).queue();
            quotes.quoteChannel().set(channel.getAsTextChannel());
        }

        if ("info".equals(event.getSubcommandName())) {
            var quoteChannel = quotes.quoteChannel();
            var embed = new LocalizedEmbedBuilder(context.localizer())
                    .setTitle("command.manage.info.embed.title")
                    .addField("command.manage.info.embed.quoteChannel", quoteChannel.channel().map(Channel::getAsMention).orElse("phrase.notSet"), true)
                    .build();
            event.replyEmbeds(embed).setEphemeral(true).queue();
        }

        if ("refresh".equals(event.getSubcommandName())) {
            var quoteChannel = quotes.quoteChannel();
            var embed = new LocalizedEmbedBuilder(context.localizer())
                    .setTitle("command.manage.info.embed.title")
                    .addField("command.manage.info.embed.quoteChannel", quoteChannel.channel().map(Channel::getAsMention).orElse("phrase.notSet"), true)
                    .build();
            var count = quotes.quoteCount();
            for (var i = 0; i < count; i++) {
                quotes.byLocalId(i)
                        .flatMap(currQuote -> quotes.quoteChannel().getPost(currQuote))
                        .ifPresent(Post::update);
            }
            event.replyEmbeds(embed).setEphemeral(true).queue();
        }
    }
}
