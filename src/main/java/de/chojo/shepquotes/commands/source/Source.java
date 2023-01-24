package de.chojo.shepquotes.commands.source;

import de.chojo.jdautil.interactions.slash.Argument;
import de.chojo.jdautil.interactions.slash.Slash;
import de.chojo.jdautil.interactions.slash.SubCommand;
import de.chojo.jdautil.interactions.slash.provider.SlashProvider;
import de.chojo.jdautil.util.Choice;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.shepquotes.commands.source.handler.Delete;
import de.chojo.shepquotes.commands.source.handler.Merge;
import de.chojo.shepquotes.commands.source.handler.Rename;
import de.chojo.shepquotes.data.QuoteData;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;

public class Source implements SlashProvider<Slash> {
    private final QuoteData quoteData;

    public Source(QuoteData quoteData) {
        this.quoteData = quoteData;
    }

    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext slashCommandContext) {
        suggest(event);
    }

    private void suggest(CommandAutoCompleteInteractionEvent event) {
        var option = event.getFocusedOption();
        var sources = quoteData.quotes(event.getGuild()).sources();
        var suggest = sources.suggest(option.getValue());
        event.replyChoices(Choice.toStringChoice(suggest)).queue();
    }

    @Override
    public Slash slash() {
        return Slash.of("source", "command.source.description")
                .adminCommand()
                .subCommand(SubCommand.of("merge", "command.source.merge.description")
                        .handler(new Merge(quoteData, this))
                        .argument(Argument.text("merge", "command.source.merge.options.merge.description").asRequired()
                                          .withAutoComplete())
                        .argument(Argument.text("into", "command.source.merge.options.into.description").asRequired()
                                          .withAutoComplete()))
                .subCommand(SubCommand.of("rename", "command.source.rename.description")
                        .handler(new Rename(quoteData, this))
                        .argument(Argument.text("source", "command.source.rename.options.source.description").asRequired()
                                          .withAutoComplete())
                        .argument(Argument.text("into", "command.source.rename.options.into.description").asRequired()))
                .subCommand(SubCommand.of("delete", "command.source.delete.description")
                        .handler(new Delete(quoteData, this))
                        .argument(Argument.text("name", "command.source.delete.options.name.description").asRequired()
                                          .withAutoComplete()))
                .build();
    }
}
