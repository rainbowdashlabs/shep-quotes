package de.chojo.shepquotes.data;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.chojo.shepquotes.data.dao.Quotes;
import de.chojo.shepquotes.data.elements.SourceSnapshot;
import de.chojo.shepquotes.data.elements.QuoteSnapshot;
import de.chojo.sqlutil.base.QueryFactoryHolder;
import de.chojo.sqlutil.exceptions.ExceptionTransformer;
import de.chojo.sqlutil.wrapper.QueryBuilderConfig;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

public class QuoteData extends QueryFactoryHolder {
    private static final Logger log = getLogger(QuoteData.class);
    private final Cache<Integer, SourceSnapshot> authorCache = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build();
    private final Cache<Integer, Optional<QuoteSnapshot>> quoteCache = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build();

    /**
     * Create a new QueryFactoryholder
     *
     * @param dataSource datasource
     */
    public QuoteData(DataSource dataSource) {
        super(dataSource, QueryBuilderConfig.builder().withExceptionHandler(err -> log.error(ExceptionTransformer.prettyException(err))).build());
    }


    public Quotes quotes(Guild guild) {
        return new Quotes(guild, source());
    }
}
