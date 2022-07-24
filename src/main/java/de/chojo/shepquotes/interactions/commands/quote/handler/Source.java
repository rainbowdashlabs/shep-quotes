package de.chojo.shepquotes.interactions.commands.quote.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.util.Choice;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.shepquotes.data.QuoteData;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Source extends BasePagedHandler implements SlashHandler {
    private final QuoteData quoteData;

    public Source(QuoteData quoteData) {
        this.quoteData = quoteData;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var quotes = quoteData.quotes(event.getGuild());
        var search = quotes.search();

        var term = event.getOption("source").getAsString();
        var source = search.source(term);

        if (source.isEmpty()) {
            event.reply(context.localize("error.notFound.", Replacement.create("ID", term))).queue();
            return;
        }

        registerPage(source, event, context);
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext slashCommandContext) {
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
}
