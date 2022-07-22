package de.chojo.shepquotes.listener;

import de.chojo.shepquotes.data.QuoteData;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class SaveQuote extends ListenerAdapter {
    private final QuoteData quoteData;

    public SaveQuote(QuoteData quoteData) {
        this.quoteData = quoteData;
    }

    @Override
    public void onMessageContextInteraction(@NotNull MessageContextInteractionEvent event) {
        event.getHook().retrieveOriginal().queue(message -> {
            var quotes = quoteData.quotes(event.getGuild());
            quotes.create(event.getUser()).ifPresent(quote -> {
                quote.content(message.getContentDisplay());
                var author = quotes.sources().getOrCreate(message.getAuthor().getName());
                quotes.links().link(quote, author);
                quotes.quoteChannel().createPost(quote);
            });
        });
    }
}
