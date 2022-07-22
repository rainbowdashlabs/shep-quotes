package de.chojo.shepquotes.commands.remove;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.shepquotes.data.QuoteData;
import de.chojo.shepquotes.data.dao.Post;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

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
        quote.delete();
        event.reply(context.localize("command.remove.deleted", Replacement.create("ID", quote.localId()))).queue();
        var count = quotes.quoteCount();
        for (var i = quote.localId(); i < count; i++) {
            quotes.byLocalId(i)
                    .flatMap(currQuote -> quotes.quoteChannel().getPost(currQuote))
                    .ifPresent(Post::update);
        }
    }
}
