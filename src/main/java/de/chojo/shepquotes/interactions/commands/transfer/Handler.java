package de.chojo.shepquotes.interactions.commands.transfer;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.shepquotes.data.QuoteData;
import de.chojo.shepquotes.data.dao.Post;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Collections;

public class Handler implements SlashHandler {
    private final QuoteData quoteData;

    public Handler(QuoteData quoteData) {
        this.quoteData = quoteData;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var quotes = quoteData.quotes(event.getGuild());
        var quoteById = quotes.byLocalId(event.getOption("id").getAsInt());
        if (quoteById.isEmpty()) {
            event.reply(context.localize("error.unkownQuote")).setEphemeral(true).queue();
            return;
        }
        var quote = quoteById.get();
        if (!quote.canAccess(event.getMember())) {
            event.reply(context.localize("error.notOwner")).setEphemeral(true).queue();
            return;
        }

        var user = event.getOption("user").getAsUser();

        event.reply(context.localize("command.transfer.transfered",
                        Replacement.create("ID", quote.id()), Replacement.createMention(user)))
                .allowedMentions(Collections.emptyList())
                .queue();

        quote.owner(user);
        quotes.quoteChannel().getPost(quote).ifPresent(Post::update);
    }
}
