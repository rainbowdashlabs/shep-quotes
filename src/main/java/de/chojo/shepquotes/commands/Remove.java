package de.chojo.shepquotes.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.shepquotes.data.QuoteData;
import de.chojo.shepquotes.data.dao.Post;
import de.chojo.shepquotes.data.dao.Quote;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Optional;

public class Remove extends SimpleCommand {
    private final QuoteData quoteData;

    public Remove(QuoteData quoteData) {
        super(CommandMeta.builder("remove", "Remove a quote")
                .addArgument(SimpleArgument.integer("id", "The quote id").asRequired()));
        this.quoteData = quoteData;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var quotes = quoteData.quotes(event.getGuild());
        Optional<Quote> quoteById;
        if (event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
            quoteById = quotes.byLocalId(event.getOption("id").getAsInt());
        } else {
            quoteById = quotes.byLocalId(event.getOption("id").getAsInt(), event.getUser());
        }
        quoteById.ifPresentOrElse(quote -> {
            quote.delete();
            event.reply("Deleted quote " + quote.localId()).queue();
            var count = quotes.quoteCount();
            for (var i = quote.localId(); i < count; i++) {
                quotes.byLocalId(i)
                        .flatMap(currQuote -> quotes.quoteChannel().getPost(currQuote))
                        .ifPresent(Post::update);
            }
        }, () -> event.reply(context.localize("Unknown quote")).queue());
    }
}
