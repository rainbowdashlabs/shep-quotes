package de.chojo.shepquotes.commands.add;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.modals.handler.ModalHandler;
import de.chojo.jdautil.modals.handler.TextInputHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.shepquotes.data.QuoteData;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

public class Handler implements SlashHandler {
    private final QuoteData quoteData;

    public Handler(QuoteData quoteData) {
        this.quoteData = quoteData;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        context.registerModal(ModalHandler.builder("command.add.modal.label")
                .addInput(TextInputHandler.builder("content", "words.quote", TextInputStyle.PARAGRAPH)
                        .withPlaceholder("command.add.modal.content.placeholder"))
                .addInput(TextInputHandler.builder("source", "words.sources", TextInputStyle.PARAGRAPH)
                        .withPlaceholder("command.add.modal.source.placeholder"))
                .withHandler(modal -> {
                    var authors = modal.getValue("source").getAsString().split("\n");
                    var content = modal.getValue("content").getAsString();

                    var quotes = quoteData.quotes(event.getGuild());

                    var quote = quotes.create(event.getUser()).get();
                    quote.content(content).join();
                    for (var author : authors) {
                        quotes.sources().getOrCreate(author).link(quote);
                    }
                    modal.replyEmbeds(quote.snapshot().embed()).queue();
                    quotes.quoteChannel().createPost(quote);
                })
                .build());
    }
}
