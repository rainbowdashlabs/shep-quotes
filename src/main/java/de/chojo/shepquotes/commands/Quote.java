package de.chojo.shepquotes.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.pagination.bag.PrivateListPageBag;
import de.chojo.jdautil.pagination.bag.PrivatePageBag;
import de.chojo.jdautil.util.Choice;
import de.chojo.jdautil.util.Consumers;
import de.chojo.jdautil.util.Futures;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.shepquotes.data.QuoteData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.slf4j.LoggerFactory.getLogger;

public class Quote extends SimpleCommand {
    private final QuoteData quoteData;
    private static final Logger log = getLogger(Quote.class);

    public Quote(QuoteData quoteData) {
        super(CommandMeta.builder("quote", "Search for quotes")
                .addArgument(SimpleArgument.integer("id", "Quote id"))
                .addArgument(SimpleArgument.string("source", "Search by source"))
                .addArgument(SimpleArgument.string("content", "Search by quote content"))
                .build());
        this.quoteData = quoteData;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var quotes = quoteData.quotes(event.getGuild());
        var search = quotes.search();
        var localizer = context.localizer();

        CompletableFuture<List<de.chojo.shepquotes.data.dao.Quote>> source = null;
        if (event.getOption("source") != null) {
            source = search.source(event.getOption("source").getAsString());
        }

        if (event.getOption("content") != null) {
            source = search.content(event.getOption("content").getAsString());
        }

        if (source != null) {
            source.whenComplete(Futures.whenComplete(matches -> {
                context.registerPage(new PrivateListPageBag<>(matches, event.getUser().getIdLong()) {
                    @Override
                    public CompletableFuture<MessageEmbed> buildPage() {
                        return CompletableFuture.supplyAsync(() -> currentElement().embed(localizer));
                    }

                    @Override
                    public CompletableFuture<MessageEmbed> buildEmptyPage() {
                        return CompletableFuture.completedFuture(new EmbedBuilder().setTitle("Empty").build());
                    }
                });
            }, err -> {

            }));
            return;
        }


        CompletableFuture<Optional<de.chojo.shepquotes.data.dao.Quote>> start = null;
        if (event.getOption("id") != null) {
            start = search.id(event.getOption("id").getAsInt());
        }

        if (start == null) {
            start = quotes.random();
        }

        start.whenComplete(Futures.whenComplete(quote -> {
                    if (quote.isEmpty()) {
                        event.reply("No quote found.").queue();
                        return;
                    }
                    context.registerPage(new QuoteScroll(quotes.quoteCount().join(), event.getUser().getIdLong(), quote.get().localId()) {

                        @Override
                        public CompletableFuture<MessageEmbed> buildPage() {
                            return CompletableFuture.supplyAsync(() -> quotes.byLocalId(current() + 1).join().get().embed(localizer)).exceptionally(err -> {
                                log.error("Error", err);
                                return null;
                            });
                        }

                        @Override
                        public CompletableFuture<MessageEmbed> buildEmptyPage() {
                            return CompletableFuture.completedFuture(new EmbedBuilder().setTitle("Empty").build());
                        }
                    });
                }, err -> {
                    log.error("Could not send quote page", err);
                }))
                .whenComplete(Futures.whenComplete(Consumers.empty(), err -> log.error("Opps", err)));
    }

    private abstract static class QuoteScroll extends PrivatePageBag {
        private QuoteScroll(int pages, long ownerId, int start) {
            super(pages, ownerId);
            current(start - 1);
        }
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, SlashCommandContext slashCommandContext) {
        if ("source".equals(event.getFocusedOption().getName())) {
            suggest(event);
        }
    }

    private void suggest(CommandAutoCompleteInteractionEvent event) {
        var option = event.getFocusedOption();
        var sources = quoteData.sources(event.getGuild());
        sources.suggest(option.getValue())
                .whenComplete(Futures.whenComplete(results -> {
                    event.replyChoices(Choice.toStringChoice(results)).queue();
                }, err -> {
                    log.error("Could not compute choices", err);
                    event.replyChoices().queue();
                }));

    }

}
