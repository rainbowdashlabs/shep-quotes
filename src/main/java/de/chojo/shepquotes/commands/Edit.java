package de.chojo.shepquotes.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.modals.handler.ModalHandler;
import de.chojo.jdautil.modals.handler.TextInputHandler;
import de.chojo.jdautil.util.Futures;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.shepquotes.data.QuoteData;
import de.chojo.shepquotes.data.dao.Quote;
import de.chojo.shepquotes.data.dao.Source;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

import java.util.stream.Collectors;

public class Edit extends SimpleCommand {
    private QuoteData quoteData;

    public Edit(QuoteData quoteData) {
        super(CommandMeta.builder("edit", "Edit a existing quote")
                .addArgument(SimpleArgument.integer("id", "Quote id").asRequired()));
        this.quoteData = quoteData;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        quoteData.quotes(event.getGuild())
                .byLocalId(event.getOption("id").getAsInt(), event.getUser())
                .whenComplete(Futures.whenComplete(
                        optQuote -> {
                            if (optQuote.isEmpty()) {
                                event.reply(context.localize("Unknown quote")).queue();
                                return;
                            }
                            var quote = optQuote.get();
                            var snapshot = quote.snapshot().join();
                            var sources = snapshot.get().sources().stream().map(Source::name)
                                    .collect(Collectors.joining("\n"));
                            context.registerModal(ModalHandler.builder("Edit Quote")
                                    .addInput(TextInputHandler.builder("content", "Quote", TextInputStyle.PARAGRAPH)
                                            .withValue(snapshot.get().content()))
                                    .addInput(TextInputHandler.builder("source", "Source", TextInputStyle.PARAGRAPH)
                                            .withValue(sources))
                                    .withHandler(modal -> {
                                        var authors = modal.getValue("source").getAsString().split("\n");
                                        var content = modal.getValue("content").getAsString();

                                        var quotes = quoteData.quotes(event.getGuild());

                                        quote.content(content).join();
                                        quote.clearSources().join();
                                        for (var author : authors) {
                                            quotes.sources().getOrCreate(author).join().link(quote);
                                        }
                                        modal.replyEmbeds(quote.embed(context.localizer())).queue();
                                    }).build());
                        },
                        err -> {

                        }));
    }
}
