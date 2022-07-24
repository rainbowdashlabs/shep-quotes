package de.chojo.shepquotes.interactions.commands.edit;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.modals.handler.ModalHandler;
import de.chojo.jdautil.modals.handler.TextInputHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.shepquotes.data.QuoteData;
import de.chojo.shepquotes.data.dao.Post;
import de.chojo.shepquotes.data.dao.Source;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

import java.util.stream.Collectors;

public class Handler implements SlashHandler {

    private final QuoteData quoteData;

    public Handler(QuoteData quoteData) {
        this.quoteData = quoteData;
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext context) {

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

        var snapshot = quote.snapshot();
        var sources = snapshot.sources().stream().map(Source::name)
                .collect(Collectors.joining("\n"));
        context.registerModal(ModalHandler.builder("command.edit.modal.label")
                .addInput(TextInputHandler.builder("content", "words.quote", TextInputStyle.PARAGRAPH)
                        .withValue(snapshot.content()))
                .addInput(TextInputHandler.builder("source", "words.sources", TextInputStyle.PARAGRAPH)
                        .withValue(sources))
                .withHandler(modal -> {
                    var authors = modal.getValue("source").getAsString().split("\n");
                    var content = modal.getValue("content").getAsString();

                    quote.content(content).join();
                    quote.clearSources();
                    for (var author : authors) {
                        quotes.sources().getOrCreate(author).link(quote);
                    }
                    modal.replyEmbeds(quote.snapshot().embed()).queue();
                    quotes.quoteChannel().getPost(quote).ifPresent(Post::update);
                }).build());
    }
}
