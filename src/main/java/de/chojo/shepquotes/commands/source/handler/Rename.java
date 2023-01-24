package de.chojo.shepquotes.commands.source.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Format;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.shepquotes.commands.source.Source;
import de.chojo.shepquotes.data.QuoteData;
import de.chojo.shepquotes.data.dao.Post;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class Rename implements SlashHandler {
    private final QuoteData quoteData;
    private final Source source;

    public Rename(QuoteData quoteData, Source source) {
        this.quoteData = quoteData;
        this.source = source;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var quotes = quoteData.quotes(event.getGuild());
        var sources = quotes.sources();
        var sourceName = event.getOption("source", OptionMapping::getAsString);
        var optSource = sources.get(sourceName);
        if (optSource.isEmpty()) {
            event.reply(context.localize("error.noSource", Replacement.create("NAME", sourceName)
                         .addFormatting(Format.CODE)))
                 .setEphemeral(true).queue();
            return;
        }
        var source = optSource.get();
        var oldName = source.name();
        source.rename(event.getOption("into", OptionMapping::getAsString));
        var updates = quotes.search().source(source);
        event.reply(context.localize("command.source.rename.renamed",
                     Replacement.create("OLD", oldName).addFormatting(Format.CODE),
                     Replacement.create("NEW", sourceName).addFormatting(Format.CODE),
                     Replacement.create("SIZE", updates.size())))
             .setEphemeral(true)
             .queue();
        updates.forEach(quote -> quotes.quoteChannel().getPost(quote).ifPresent(Post::update));
    }


    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext slashCommandContext) {
        source.onAutoComplete(event, slashCommandContext);
    }
}
