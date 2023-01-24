package de.chojo.shepquotes.commands.manage.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.shepquotes.data.QuoteData;
import de.chojo.shepquotes.data.dao.Post;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Refresh implements SlashHandler {
    private final QuoteData quoteData;

    public Refresh(QuoteData quoteData) {

        this.quoteData = quoteData;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var quotes = quoteData.quotes(event.getGuild());
        event.reply("command.manage.refresh.refresh").setEphemeral(true).queue();
        var count = quotes.quoteCount();
        for (var i = 0; i < count; i++) {
            quotes.byLocalId(i)
                  .flatMap(currQuote -> quotes.quoteChannel().getPost(currQuote))
                  .ifPresent(Post::update);
        }

    }
}
