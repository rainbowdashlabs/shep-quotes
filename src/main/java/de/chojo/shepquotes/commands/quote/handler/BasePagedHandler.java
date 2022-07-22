package de.chojo.shepquotes.commands.quote.handler;

import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.pagination.bag.PrivateListPageBag;
import de.chojo.jdautil.pagination.bag.PrivatePageBag;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.shepquotes.data.QuoteData;
import de.chojo.shepquotes.data.dao.Quote;
import de.chojo.shepquotes.data.dao.Quotes;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.slf4j.LoggerFactory.getLogger;

public class BasePagedHandler {
    private static final Logger log = getLogger(BasePagedHandler.class);

    public void registerPage(Quotes quotes, SlashCommandInteractionEvent event, EventContext context, Optional<Quote> quote) {
        context.registerPage(new QuoteScroll(quotes.quoteCount(), event.getUser().getIdLong(), quote.get().localId()) {
            @Override
            public CompletableFuture<MessageEmbed> buildPage() {
                return CompletableFuture.supplyAsync(() -> quotes.byLocalId(current() + 1).get().snapshot().embed()).exceptionally(err -> {
                    log.error("Error", err);
                    return null;
                });
            }

            @Override
            public CompletableFuture<MessageEmbed> buildEmptyPage() {
                return CompletableFuture.completedFuture(new LocalizedEmbedBuilder(context.localizer()).setTitle("error.noQuote").build());
            }
        });
    }

    public void registerPage(List<Quote> source, SlashCommandInteractionEvent event, EventContext context) {
        context.registerPage(new PrivateListPageBag<>(source, event.getUser().getIdLong()) {
            @Override
            public CompletableFuture<MessageEmbed> buildPage() {
                return CompletableFuture.supplyAsync(() -> currentElement().snapshot().embed());
            }

            @Override
            public CompletableFuture<MessageEmbed> buildEmptyPage() {
                return CompletableFuture.completedFuture(new LocalizedEmbedBuilder(context.localizer()).setTitle("error.noQuote").build());
            }
        });
    }

    private abstract class QuoteScroll extends PrivatePageBag {
        public QuoteScroll(int pages, long ownerId, int start) {
            super(pages, ownerId);
            current(start - 1);
        }
    }

}
