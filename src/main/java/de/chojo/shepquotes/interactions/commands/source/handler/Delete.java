package de.chojo.shepquotes.interactions.commands.source.handler;

import de.chojo.jdautil.localization.util.Format;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.shepquotes.data.QuoteData;
import de.chojo.shepquotes.data.dao.Post;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class Delete extends BaseSourceHandler {
    private final QuoteData quoteData;

    public Delete(QuoteData quoteData) {
        super(quoteData);
        this.quoteData = quoteData;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var quotes = quoteData.quotes(event.getGuild());
        var sources = quotes.sources();
        var name = event.getOption("name", OptionMapping::getAsString);
        var optSource = sources.get(name);
        if (optSource.isEmpty()) {
            event.reply(context.localize("error.noSource", Replacement.create("NAME", name).addFormatting(Format.CODE)))
                    .setEphemeral(true).queue();
            return;
        }
        var source = optSource.get();
        var update = quotes.search().source(source);
        source.delete();
        event.reply(context.localize("command.source.delete.deleted",
                        Replacement.create("NAME", name).addFormatting(Format.CODE),
                        Replacement.create("SIZE", update.size())))
                .queue();
        update.forEach(quote -> quotes.quoteChannel().getPost(quote).ifPresent(Post::update));
    }
}
