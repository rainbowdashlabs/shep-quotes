package de.chojo.shepquotes.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleArgument;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.util.Choice;
import de.chojo.jdautil.util.Futures;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import de.chojo.shepquotes.data.QuoteData;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.slf4j.Logger;

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
        var sources = quoteData.sources(event.getGuild());
        if ("merge".equalsIgnoreCase(cmd)) {
            sources.get(event.getOption("merge", OptionMapping::getAsString))
                    .whenComplete(Futures.whenComplete(source -> {
                        if (source.isEmpty()) {
                            event.reply("Unkown Source").queue();
                            return;
                        }
                        var into = sources.get(event.getOption("into", OptionMapping::getAsString)).join();
                        if (into.isEmpty()) {
                            event.reply("Unkown target").queue();
                            return;
                        }

                        into.get().merge(source.get());
                        event.reply("Merged source " + source.get().name() + " into " + into.get().name()).queue();
                    }, err -> log.error("Could not retrieve source", err)));
            return;
        }

        if ("rename".equalsIgnoreCase(cmd)) {
            sources.get(event.getOption("source", OptionMapping::getAsString))
                    .whenComplete(Futures.whenComplete(
                            optSource -> {
                                if (optSource.isEmpty()) {
                                    event.reply("Unknown source").queue();
                                    return;
                                }
                                var source = optSource.get();
                                var oldName = source.name();
                                source.rename(event.getOption("into", OptionMapping::getAsString));
                                event.reply("Source " + oldName + " renamed to " + source.name()).queue();
                            }, err -> {

                            }
                    ));
            return;
        }

        if ("delete".equalsIgnoreCase(cmd)) {
            sources.get(event.getOption("name", OptionMapping::getAsString))
                    .whenComplete(Futures.whenComplete(
                            source -> {
                                if (source.isEmpty()) {
                                    event.reply("Unknown source").queue();
                                    return;
                                }
                                source.get().delete();
                                event.reply("Source `" + source.get().name() + "` deleted").queue();
                            }, err -> {

                            }
                    ));
        }
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, SlashCommandContext slashCommandContext) {
        suggest(event);
    }

    private void suggest(CommandAutoCompleteInteractionEvent event) {
        var option = event.getFocusedOption();
        var sources = quoteData.sources(event.getGuild());
        sources.suggest(option.getValue())
                .whenComplete(Futures.whenComplete(results -> {
                    event.replyChoices(Choice.toStringChoice(results)).queue();
                }, err -> {
                    log.error("Could not compute choices", err);
                    event.replyChoices().queue();
                }));

    }
}
