package de.chojo.shepquotes.commands.source.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.util.Choice;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.shepquotes.data.QuoteData;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;

public abstract class BaseSourceHandler implements SlashHandler {
    private final QuoteData quoteData;

    protected BaseSourceHandler(QuoteData quoteData) {
        this.quoteData = quoteData;
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext context) {
        suggest(event);
    }

    private void suggest(CommandAutoCompleteInteractionEvent event) {
        var option = event.getFocusedOption();
        var sources = quoteData.quotes(event.getGuild()).sources();
        var suggest = sources.suggest(option.getValue());
        event.replyChoices(Choice.toStringChoice(suggest)).queue();
    }

}
