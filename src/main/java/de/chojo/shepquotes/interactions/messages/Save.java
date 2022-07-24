package de.chojo.shepquotes.interactions.messages;

import de.chojo.jdautil.interactions.message.Message;
import de.chojo.jdautil.interactions.message.MessageHandler;
import de.chojo.jdautil.interactions.message.provider.MessageProvider;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;

public class Save implements MessageProvider<Message> {

    @Override
    public Message message() {
        return Message.of("Save as quote")
                .handler(handler())
                .setPermission(DefaultMemberPermissions.ENABLED)
                .build();
    }

    private MessageHandler handler() {
        return (event, context) -> {
            //tbd
        };
    }
}
