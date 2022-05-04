package de.chojo.shepquotes;

import de.chojo.jdautil.command.dispatching.CommandHub;
import de.chojo.jdautil.localization.ILocalizer;
import de.chojo.jdautil.util.Consumers;
import de.chojo.shepquotes.commands.Add;
import de.chojo.shepquotes.commands.Edit;
import de.chojo.shepquotes.commands.Manage;
import de.chojo.shepquotes.commands.Quote;
import de.chojo.shepquotes.commands.Remove;
import de.chojo.shepquotes.commands.Source;
import de.chojo.shepquotes.config.Configuration;
import de.chojo.shepquotes.data.QuoteData;
import de.chojo.shepquotes.util.LogNotify;
import de.chojo.sqlutil.datasource.DataSourceCreator;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;

import javax.security.auth.login.LoginException;
import javax.sql.DataSource;

import java.util.concurrent.Executors;

import static org.slf4j.LoggerFactory.getLogger;

public class ShepQuotes {
    private static final Logger log = getLogger(ShepQuotes.class);
    private static ShepQuotes instance;
    private Configuration configuration;
    private DataSource dataSource;
    private ShardManager shardManager;
    private QuoteData quoteData;

    public static void main(String[] args) {
        instance = new ShepQuotes();
        instance.start();
    }

    private void start() {
        configuration = Configuration.create();

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
        CommandHub.builder(shardManager)
                .useGuildCommands()
                .withCommands(
                        new Add(quoteData),
                        new Edit(quoteData),
                        new Manage(quoteData),
                        new Quote(quoteData),
                        new Remove(quoteData),
                        new Source(quoteData)
                )
                .withPermissionCheck((event, simpleCommand) -> true)
                .withModalService(Consumers.empty())
                .withButtonService(Consumers.empty())
                .withPagination(Consumers.empty())
                .withLocalizer(ILocalizer.DEFAULT)
                .build();
    }

    private void initDb() {
        var db = configuration.database();
        dataSource = DataSourceCreator.create(PGSimpleDataSource.class)
                .withAddress(db.host())
                .withPort(db.port())
                .forDatabase(db.database())
                .withUser(db.user())
                .withPassword(db.password())
                .create()
                .forSchema(db.schema())
                .withMaximumPoolSize(5)
                .build();

        this.quoteData = new QuoteData(dataSource);
    }

    private void initShardManager() throws LoginException {
        shardManager = DefaultShardManagerBuilder
                .create(configuration.baseSettings().token(), GatewayIntent.GUILD_MESSAGES)
                .setEventPool(Executors.newCachedThreadPool())
                .build();
    }

    public void shutdown() {

    }
}
