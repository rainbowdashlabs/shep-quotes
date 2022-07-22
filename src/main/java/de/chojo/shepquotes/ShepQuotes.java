package de.chojo.shepquotes;

import com.zaxxer.hikari.HikariDataSource;
import de.chojo.jdautil.interactions.dispatching.InteractionHub;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.threading.ThreadFactories;
import de.chojo.logutil.marker.LogNotify;
import de.chojo.shepquotes.commands.add.Add;
import de.chojo.shepquotes.commands.edit.Edit;
import de.chojo.shepquotes.commands.info.Info;
import de.chojo.shepquotes.commands.manage.Manage;
import de.chojo.shepquotes.commands.quote.Quote;
import de.chojo.shepquotes.commands.remove.Remove;
import de.chojo.shepquotes.commands.source.Source;
import de.chojo.shepquotes.commands.transfer.Transfer;
import de.chojo.shepquotes.config.Configuration;
import de.chojo.shepquotes.data.QuoteData;
import de.chojo.shepquotes.listener.SaveQuote;
import de.chojo.sqlutil.databases.SqlType;
import de.chojo.sqlutil.datasource.DataSourceCreator;
import de.chojo.sqlutil.exceptions.ExceptionTransformer;
import de.chojo.sqlutil.logging.LoggerAdapter;
import de.chojo.sqlutil.updater.QueryReplacement;
import de.chojo.sqlutil.updater.SqlUpdater;
import de.chojo.sqlutil.wrapper.QueryBuilderConfig;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.slf4j.LoggerFactory.getLogger;

public class ShepQuotes {
    private static final Logger log = getLogger(ShepQuotes.class);
    private static ShepQuotes instance = null;
    private Configuration configuration;

    private final ThreadGroup worker = new ThreadGroup("ShepQuotesWorker");
    private ExecutorService executor;
    private HikariDataSource dataSource;
    private ShardManager shardManager;
    private QuoteData quoteData;

    public static void main(String[] args) throws SQLException, IOException {
        ShepQuotes.instance = new ShepQuotes();
        instance.start();
    }

    private void start() throws SQLException, IOException {
        configuration = Configuration.create();

        RestAction.setDefaultFailure(err -> log.error(LogNotify.NOTIFY_ADMIN, "Unexpected error in async callback.", err));

        initDb();

        try {
            initShardManager();
        } catch (LoginException e) {
            log.error(LogNotify.NOTIFY_ADMIN, "Login failed.", e);
            return;
        }

        initCommands();
    }

    private void initCommands() {
        var localizer = Localizer.builder(DiscordLocale.ENGLISH_US)
                .addLanguage(DiscordLocale.GERMAN)
                .build();

        InteractionHub.builder(shardManager)
                .withCommands(
                        new Add(quoteData),
                        new Edit(quoteData),
                        new Manage(quoteData),
                        new Quote(quoteData),
                        new Remove(quoteData),
                        new Source(quoteData),
                        new Info(configuration),
                        new Transfer(quoteData)
                )
                .withDefaultModalService()
                .withDefaultMenuService()
                .withDefaultPagination()
                .withLocalizer(localizer)
                .build();

        var saveAsQuote = Commands.message("Save as quote").setGuildOnly(true);
        var quoteStart = Commands.message("Quote Start").setGuildOnly(true);
        var quoteEnd = Commands.message("Quote End").setGuildOnly(true);
        shardManager.getShards().get(0).upsertCommand(saveAsQuote).queue();
        shardManager.getShards().get(0).upsertCommand(quoteStart).queue();
        shardManager.getShards().get(0).upsertCommand(quoteEnd).queue();
    }

    private void initDb() throws IOException, SQLException {
        var dbLogger = getLogger("DbLogger");
        QueryBuilderConfig.setDefault(QueryBuilderConfig.builder()
                .withExceptionHandler(err -> dbLogger.error(LogNotify.NOTIFY_ADMIN, "An error occured: {}", ExceptionTransformer.prettyException(err), err))
                .build());

        var dbc = configuration.database();
        dataSource = DataSourceCreator.create(SqlType.POSTGRES)
                .configure(builder -> builder
                        .host(dbc.host())
                        .port(dbc.port())
                        .database(dbc.database())
                        .user(dbc.user())
                        .password(dbc.password()))
                .create()
                .build();


        SqlUpdater.builder(dataSource, SqlType.POSTGRES)
                .withLogger(LoggerAdapter.wrap(dbLogger))
                .setSchemas(dbc.schema())
                .setReplacements(new QueryReplacement("shep_quotes", dbc.schema()))
                .execute();

        dataSource.close();

        dataSource = DataSourceCreator.create(SqlType.POSTGRES)
                .configure(builder -> builder
                        .host(dbc.host())
                        .port(dbc.port())
                        .database(dbc.database())
                        .user(dbc.user())
                        .password(dbc.password()))
                .create()
                .forSchema(dbc.schema())
                .withMaximumPoolSize(5)
                .build();


        quoteData = new QuoteData(dataSource);
    }

    private void initShardManager() throws LoginException {

        var threadLogger = getLogger("ThreadLogger");
        executor = Executors.newCachedThreadPool(ThreadFactories.builder()
                .exceptionHandler((t, e) -> threadLogger.error(LogNotify.NOTIFY_ADMIN, ThreadFactories.threadErrorMessage(t), e))
                .group(worker)
                .build());

        shardManager = DefaultShardManagerBuilder.createLight(configuration.baseSettings().token())
                .enableIntents(GatewayIntent.GUILD_MESSAGES)
                .enableCache(CacheFlag.MEMBER_OVERRIDES)
                .setEventPool(executor)
                .addEventListeners(new SaveQuote(quoteData))
                .build();
    }

    public void shutdown() {
        shardManager.shutdown();
        dataSource.close();
        executor.shutdown();
    }
}
