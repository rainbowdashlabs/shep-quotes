package de.chojo.shepquotes.commands;

import de.chojo.jdautil.command.CommandMeta;
import de.chojo.jdautil.command.SimpleCommand;
import de.chojo.jdautil.modals.handler.ModalHandler;
import de.chojo.jdautil.modals.handler.TextInputHandler;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

import javax.sql.DataSource;

public class Add extends SimpleCommand {
    protected Add(DataSource dataSource) {
        super(CommandMeta.builder("add","Add a new quote"));
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, SlashCommandContext context) {
        context.registerModal(ModalHandler.builder("Add Quote")
                .addInput(TextInputHandler.builder("author", "Author", TextInputStyle.PARAGRAPH)
                        .withPlaceholder("Add authors which were involved in this quote. Separate them with \",\""))
                .addInput(TextInputHandler.builder("text", "Text", TextInputStyle.PARAGRAPH)
                        .withPlaceholder("The quote text"))
                .withHandler(modal -> {
                    modal.reply("Created").queue();
                    var author = modal.getValue("author").getAsString();
                    var quote = modal.getValue("quote").getAsString();
                })
                .build());
        // TODO: Implement modal when implemented
        // https://github.com/DV8FromTheWorld/JDA/pull/2024
    }
}
