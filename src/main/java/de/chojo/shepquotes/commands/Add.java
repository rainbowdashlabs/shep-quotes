package de.chojo.shepquotes.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.modals.handler.ModalHandler;
import de.chojo.jdautil.modals.handler.TextInputHandler;
import de.chojo.jdautil.util.Futures;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.shepquotes.data.QuoteData;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class Add extends SimpleCommand {
    private static final Logger log = getLogger(Add.class);
    QuoteData quoteData;

    public Add(QuoteData quoteData) {
        super(CommandMeta.builder("add", "Add a new quote"));
        this.quoteData = quoteData;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        context.registerModal(ModalHandler.builder("Add Quote")
                .addInput(TextInputHandler.builder("content", "Text", TextInputStyle.PARAGRAPH)
                        .withPlaceholder("The quote text"))
                .addInput(TextInputHandler.builder("author", "Author", TextInputStyle.PARAGRAPH)
                        .withPlaceholder("Add authors which were involved in this quote. One author per line"))
                .withHandler(modal -> {
                    var authors = modal.getValue("author").getAsString().split("\n");
                    var content = modal.getValue("content").getAsString();

                    var quotes = quoteData.quotes(event.getGuild());

                    var quote = quotes.create(event.getUser()).join().get();
                    quote.content(content).join();
                    for (var author : authors) {
                        quotes.sources().getOrCreate(author).join().link(quote);
                    }
                    modal.replyEmbeds(quote.embed(context.localizer())).queue();
                })
                .build());
    }
}
