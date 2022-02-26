package de.chojo.shepquotes.listener;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.chojo.shepquotes.data.QuoteData;
import de.chojo.shepquotes.data.elements.Author;
import de.chojo.shepquotes.data.elements.Quote;
import de.chojo.shepquotes.listener.page.Page;
import de.chojo.shepquotes.listener.page.QuotePage;
import de.chojo.shepquotes.listener.page.ScrollPage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class QuotePageService extends ListenerAdapter {
    private final QuoteData quoteData;
    private final Cache<Long, Page> quotePages = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();

    public QuotePageService(QuoteData quoteData) {
        this.quoteData = quoteData;
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        event.getHook().retrieveOriginal().queue(message -> {
            var page = quotePages.getIfPresent(message.getIdLong());
            if (page == null) {
                //event.reply("This page is no longer active").setEphemeral(true).queue();
                return;
            }

            var label = event.getButton().getLabel();
            if ("next".equalsIgnoreCase(label)) page.next();
            if ("previous".equalsIgnoreCase(label)) page.previous();

            quoteData.retrieveQuoteById(event.getGuild(), page.pageValue())
                    .thenAccept(quote -> {
                        if (quote.isEmpty()) {
                            event.reply("Unkown Quote").queue();
                            return;
                        }

                        event.replyEmbeds(buildQuote(quote.get()))
                                .addActionRows(getPageButtons(page))
                                .queue();
                    });
        });
    }

    public void registerPage(SlashCommandInteractionEvent event, List<Quote> quotes) {
        if (quotes.isEmpty()) {
            event.reply("No quotes found").setEphemeral(true).queue();
            return;
        }

        var page = new QuotePage(quotes.stream().map(Quote::guildQuoteId).collect(Collectors.toList()), event.getUser().getIdLong());

        quoteData.retrieveQuoteById(event.getGuild(), page.pageValue())
                .thenAccept(quote -> {
                    if (quote.isEmpty()) {
                        event.reply("Unkown Quote").queue();
                        return;
                    }

                    event.replyEmbeds(buildQuote(quote.get())).addActionRows(getPageButtons(page))
                            .flatMap(InteractionHook::retrieveOriginal)
                            .queue(message -> quotePages.put(message.getIdLong(), page));
                });
    }

    public ActionRow getPageButtons(Page page) {
        return ActionRow.of(
                Button.of(ButtonStyle.SUCCESS, "previous", "Previous", Emoji.fromUnicode("⬅")),
                Button.of(ButtonStyle.SUCCESS, "page", page.current() + 1 + "/" + page.pages()).asDisabled(),
                Button.of(ButtonStyle.SUCCESS, "next", "Next", Emoji.fromUnicode("➡️"))
        );
    }

    private MessageEmbed buildQuote(Quote quote) {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle(String.format("#%s", quote.guildQuoteId()))
                .setDescription(quote.content());

        if (!quote.authors().isEmpty()) {
            builder.setFooter(quote.authors().stream().map(Author::name).collect(Collectors.joining(", ")));
        }
        builder.setTimestamp(quote.created());
        return builder.build();
    }

    public void scrollQuotes(SlashCommandInteractionEvent event, int start) {
        quoteData.retrieveQuoteCount(event.getGuild()).thenAccept(count -> {

        var page = new ScrollPage(start, count, event.getUser().getIdLong());

        quoteData.retrieveQuoteById(event.getGuild(), page.current())
                .thenAccept(quote -> {
                    if (quote.isEmpty()) {
                        event.reply("Unkown Quote").queue();
                        return;
                    }

                    event.replyEmbeds(buildQuote(quote.get())).addActionRows(getPageButtons(page))
                            .flatMap(InteractionHook::retrieveOriginal)
                            .queue(message -> quotePages.put(message.getIdLong(), page));
                });
        });
    }

}
