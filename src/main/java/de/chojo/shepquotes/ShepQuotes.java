package de.chojo.shepquotes;

import com.zaxxer.hikari.HikariDataSource;
import de.chojo.jdautil.command.dispatching.CommandHub;
import de.chojo.jdautil.localization.Localizer;
import de.chojo.jdautil.localization.util.Language;
import de.chojo.jdautil.threading.ThreadFactories;
import de.chojo.logutil.marker.LogNotify;
import de.chojo.shepquotes.commands.Add;
import de.chojo.shepquotes.commands.Edit;
import de.chojo.shepquotes.commands.Info;
import de.chojo.shepquotes.commands.Manage;
import de.chojo.shepquotes.commands.Quote;
import de.chojo.shepquotes.commands.Remove;
import de.chojo.shepquotes.commands.Source;
import de.chojo.shepquotes.commands.Transfer;
import de.chojo.shepquotes.config.Configuration;
import de.chojo.shepquotes.data.QuoteData;
import de.chojo.sqlutil.databases.SqlType;
import de.chojo.sqlutil.datasource.DataSourceCreator;
import de.chojo.sqlutil.exceptions.ExceptionTransformer;
import de.chojo.sqlutil.logging.LoggerAdapter;
import de.chojo.sqlutil.updater.QueryReplacement;
import de.chojo.sqlutil.updater.SqlUpdater;
import de.chojo.sqlutil.wrapper.QueryBuilderConfig;
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
        var localizer = Localizer.builder(Language.ENGLISH)
                .addLanguage(Language.GERMAN)
                .build();

        CommandHub.builder(shardManager)
                .useGuildCommands()
                .withCommands(
                        new Add(quoteData),
                        new Edit(quoteData),
                        new Manage(quoteData),
                        new Quote(quoteData),
                        new Remove(quoteData),
                        new Source(quoteData),
                        Info.create(configuration),
                        new Transfer(quoteData)
                )
                .withDefaultModalService()
                .withDefaultMenuService()
                .withDefaultPagination()
                .withLocalizer(localizer)
                .build();
    }

    private void initDb() throws IOException, SQLException {
        var dbLogger = getLogger("DbLogger");
        QueryBuilderConfig.setDefault(QueryBuilderConfig.builder()
                .withExceptionHandler(err -> dbLogger.error(LogNotify.NOTIFY_ADMIN,"An error occured: {}", ExceptionTransformer.prettyException(err)))
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
                .build();
    }

    public void shutdown() {
        shardManager.shutdown();
        dataSource.close();
        executor.shutdown();
    }
}
