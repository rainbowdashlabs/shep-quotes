package de.chojo.shepquotes.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.util.Choice;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.shepquotes.data.QuoteData;
import de.chojo.shepquotes.data.dao.Post;
import de.chojo.shepquotes.data.dao.Quote;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

public class Source extends SimpleCommand {
    private final QuoteData quoteData;
    private static final Logger log = getLogger(Source.class);

    public Source(QuoteData quoteData) {
        super(CommandMeta.builder("source", "Manage Quote Sources")
                .addSubCommand("merge", "Merge a source with another",
                        argsBuilder()
                                .add(SimpleArgument.string("merge", "The source which should be merged").asRequired().withAutoComplete())
                                .add(SimpleArgument.string("into", "The new source").asRequired().withAutoComplete()))
                .addSubCommand("rename", "Rename a source", argsBuilder()
                        .add(SimpleArgument.string("source", "The old source name").asRequired().withAutoComplete())
                        .add(SimpleArgument.string("into", "The new source name").asRequired()))
                .addSubCommand("delete", "Delete a source", argsBuilder()
                        .add(SimpleArgument.string("name", "source name").asRequired().withAutoComplete()))
                .build());
        this.quoteData = quoteData;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var cmd = event.getSubcommandName();
        var quotes = quoteData.quotes(event.getGuild());
        var sources = quotes.sources();
        if ("merge".equalsIgnoreCase(cmd)) {
            sources.get(event.getOption("merge", OptionMapping::getAsString))
                    .ifPresentOrElse(source -> sources.get(event.getOption("into", OptionMapping::getAsString))
                            .ifPresentOrElse(into -> {
                                Set<Quote> update = new HashSet<>(quotes.search().source(source));
                                update.addAll(quotes.search().source(into));
                                into.merge(source);
                                event.reply("Merged source " + source.name() + " into " + into.name() + ". Updating " + update.size() + " quotes.").queue();
                                update.forEach(quote -> quotes.quoteChannel().getPost(quote).ifPresent(Post::update));
                            }, () -> event.reply("Unkown target").queue()), () -> event.reply("Unkown Source").queue());
            return;
        }

        if ("rename".equalsIgnoreCase(cmd)) {
            sources.get(event.getOption("source", OptionMapping::getAsString))
                    .ifPresentOrElse(source -> {
                        var oldName = source.name();
                        source.rename(event.getOption("into", OptionMapping::getAsString));
                        var updates = quotes.search().source(source);
                        event.reply("Source " + oldName + " renamed to " + source.name() + ". Updating " + updates.size() + " quotes.").queue();
                        updates.forEach(quote -> quotes.quoteChannel().getPost(quote).ifPresent(Post::update));
                    }, () -> event.reply("Unknown source").queue());
            return;
        }

        if ("delete".equalsIgnoreCase(cmd)) {
            sources.get(event.getOption("name", OptionMapping::getAsString))
                    .ifPresentOrElse(source -> {
                        var update = quotes.search().source(source);
                        source.delete();
                        event.reply("Source `" + source.name() + "` deleted. Updating " + update.size() + " quotes.").queue();
                        update.forEach(quote -> quotes.quoteChannel().getPost(quote).ifPresent(Post::update));
                    }, () -> event.reply("Unknown source").queue());
        }
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, SlashCommandContext slashCommandContext) {
        suggest(event);
    }

    private void suggest(CommandAutoCompleteInteractionEvent event) {
        var option = event.getFocusedOption();
        var sources = quoteData.quotes(event.getGuild()).sources();
        var suggest = sources.suggest(option.getValue());
        event.replyChoices(Choice.toStringChoice(suggest)).queue();
    }
}
