package de.chojo.shepquotes.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.localization.util.Format;
import de.chojo.jdautil.localization.util.Replacement;
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
        super(CommandMeta.builder("source", "command.source.descr")
                .addSubCommand("merge", "command.source.merge.descr",
                        argsBuilder()
                                .add(SimpleArgument.string("merge", "command.source.merge.descr.arg.merge").asRequired().withAutoComplete())
                                .add(SimpleArgument.string("into", "command.source.merge.descr.arg.into").asRequired().withAutoComplete()))
                .addSubCommand("rename", "command.source.rename.descr", argsBuilder()
                        .add(SimpleArgument.string("source", "command.source.rename.arg.source").asRequired().withAutoComplete())
                        .add(SimpleArgument.string("into", "command.source.rename.arg.into").asRequired()))
                .addSubCommand("delete", "command.source.delete.descr", argsBuilder()
                        .add(SimpleArgument.string("name", "command.source.delete.arg.name").asRequired().withAutoComplete()))
                .build());
        this.quoteData = quoteData;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        var cmd = event.getSubcommandName();
        var quotes = quoteData.quotes(event.getGuild());
        var sources = quotes.sources();
        if ("merge".equalsIgnoreCase(cmd)) {
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
            return;
        }

        if ("rename".equalsIgnoreCase(cmd)) {
            var sourceName = event.getOption("source", OptionMapping::getAsString);
            var optSource = sources.get(sourceName);
            if (optSource.isEmpty()) {
                event.reply(context.localize("error.noSource", Replacement.create("NAME", sourceName).addFormatting(Format.CODE)))
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
            return;
        }

        if ("delete".equalsIgnoreCase(cmd)) {
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
