package de.chojo.shepquotes.commands.quote.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.pagination.bag.PrivateListPageBag;
import de.chojo.jdautil.pagination.bag.PrivatePageBag;
import de.chojo.jdautil.util.Choice;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.shepquotes.data.QuoteData;
import de.chojo.shepquotes.data.dao.Quote;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.slf4j.LoggerFactory.getLogger;

//TODO: Split into subcommands
public class Default implements SlashHandler {
    private static final Logger log = getLogger(Default.class);
    private final QuoteData quoteData;

    public Default(QuoteData quoteData) {
        this.quoteData = quoteData;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var quotes = quoteData.quotes(event.getGuild());
        var search = quotes.search();
        var localizer = context.guildLocalizer();
        var term = "";
        List<Quote> source = null;
        if (event.getOption("source") != null) {
            term = event.getOption("source").getAsString();
            source = search.source(term);
        }

        if (event.getOption("content") != null) {
            term = event.getOption("content").getAsString();
            source = search.content(term);
        }

        if (source != null) {
            context.registerPage(new PrivateListPageBag<>(source, event.getUser().getIdLong()) {
                @Override
                public CompletableFuture<MessageEmbed> buildPage() {
                    return CompletableFuture.supplyAsync(() -> currentElement().snapshot().embed());
                }

                @Override
                public CompletableFuture<MessageEmbed> buildEmptyPage() {
                    return CompletableFuture.completedFuture(new LocalizedEmbedBuilder(context.guildLocalizer()).setTitle("error.noQuote")
                                                                                                           .build());
                }
            });
            return;
        }


        Optional<Quote> quote = null;
        if (event.getOption("id") != null) {
            term = event.getOption("id").getAsString();
            quote = search.id(event.getOption("id").getAsInt());
        }

        if (quote == null) {
            quote = quotes.random();
        }

        if (quote.isEmpty()) {
            event.reply(localizer.localize("error.unkownQuote", Replacement.create("ID", term))).queue();
            return;
        }

        context.registerPage(new QuoteScroll(quotes.quoteCount(), event.getUser()
                                                                       .getIdLong(), quote.get()
                                                                                          .localId()) {
            @Override
            public CompletableFuture<MessageEmbed> buildPage() {
                return CompletableFuture.supplyAsync(() -> quotes.byLocalId(current() + 1).get().snapshot().embed())
                                        .exceptionally(err -> {
                                            log.error("Error", err);
                                            return null;
                                        });
            }

            @Override
            public CompletableFuture<MessageEmbed> buildEmptyPage() {
                return CompletableFuture.completedFuture(new LocalizedEmbedBuilder(context.guildLocalizer()).setTitle("error.noQuote")
                                                                                                       .build());
            }
        });
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext context) {
        if ("source".equals(event.getFocusedOption().getName())) {
            suggest(event);
        }
    }

    private void suggest(CommandAutoCompleteInteractionEvent event) {
        var option = event.getFocusedOption();
        var sources = quoteData.quotes(event.getGuild()).sources();
        var suggest = sources.suggest(option.getValue());
        event.replyChoices(Choice.toStringChoice(suggest)).queue();
    }

    private abstract static class QuoteScroll extends PrivatePageBag {
        private QuoteScroll(int pages, long ownerId, int start) {
            super(pages, ownerId);
            current(start - 1);
        }
    }
}
