package de.chojo.shepquotes.data;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.chojo.shepquotes.data.dao.Links;
import de.chojo.shepquotes.data.dao.Quotes;
import de.chojo.shepquotes.data.dao.Sources;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

public class QuoteData {
    private final Links links;
    private static final Logger log = getLogger(QuoteData.class);
    private final Cache<Long, Quotes> quotes = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build();

    /**
     * Create a new QueryFactoryholder
     *
     * @param dataSource datasource
     */
    public QuoteData(DataSource dataSource) {
        links = new Links(dataSource);
    }


    public Quotes quotes(Guild guild) {
        try {
            return quotes.get(guild.getIdLong(), () -> new Quotes(guild, new Sources(guild, links), links));
        } catch (ExecutionException e) {
            log.error("Could not create quotes access", e);
            throw new RuntimeException(e);
        }
    }
}
