package de.chojo.shepquotes.commands.source.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.localization.util.Format;
import de.chojo.jdautil.localization.util.Replacement;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.shepquotes.data.QuoteData;
import de.chojo.shepquotes.data.dao.Post;
import de.chojo.shepquotes.data.dao.Quote;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.HashSet;
import java.util.Set;

public class Merge extends BaseSourceHandler {
    private final QuoteData quoteData;

    public Merge(QuoteData quoteData) {
        super(quoteData);
        this.quoteData = quoteData;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var quotes = quoteData.quotes(event.getGuild());
        var sources = quotes.sources();
        var mergeName = event.getOption("merge", OptionMapping::getAsString);
        var optSource = sources.get(mergeName);
        if (optSource.isEmpty()) {
            event.reply(context.localize("error.noSource", Replacement.create("NAME", mergeName).addFormatting(Format.CODE)))
                    .setEphemeral(true).queue();
            return;
        }
        var source = optSource.get();

        var intoName = event.getOption("into", OptionMapping::getAsString);
        var optTarget = sources.get(intoName);
        if (optTarget.isEmpty()) {
            event.reply(context.localize("error.noSource", Replacement.create("NAME", intoName).addFormatting(Format.CODE)))
                    .setEphemeral(true).queue();
            return;
        }
        var target = optTarget.get();

        Set<Quote> update = new HashSet<>(quotes.search().source(source));
        update.addAll(quotes.search().source(target));
        target.merge(source);
        event.reply(context.localize("command.source.merge.merged",
                        Replacement.create("SOURCE", source).addFormatting(Format.CODE),
                        Replacement.create("TARGET", target).addFormatting(Format.CODE),
                        Replacement.create("SIZE", update.size())))
                .setEphemeral(true).queue();
        update.forEach(quote -> quotes.quoteChannel().getPost(quote).ifPresent(Post::update));
    }
}
