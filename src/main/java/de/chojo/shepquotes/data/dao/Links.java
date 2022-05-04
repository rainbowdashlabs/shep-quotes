package de.chojo.shepquotes.data.dao;

import de.chojo.sqlutil.base.QueryFactoryHolder;
import de.chojo.sqlutil.wrapper.QueryBuilderConfig;
import org.slf4j.Logger;

import javax.sql.DataSource;

import static org.slf4j.LoggerFactory.getLogger;

public class Links extends QueryFactoryHolder {
    private static final Logger log = getLogger(Links.class);

    public Links(DataSource dataSource) {
        super(dataSource, QueryBuilderConfig.builder()
                .withExceptionHandler(err -> log.error("Unhandled exception", err))
                .build());
    }

    public void link(Quote quote, Source source) {
        builder().query("""
                        INSERT INTO source_links(quote_id, source_id) VALUES(?,?)
                        ON CONFLICT
                        DO NOTHING
                        """)
                .paramsBuilder(stmt -> stmt.setInt(quote.id()).setInt(source.id()))
                .insert()
                .execute();
    }

    public void clear(Quote quote) {
        builder().query("""
                        DELETE FROM source_links WHERE quote_id = ?;
                        """)
                .paramsBuilder(stmt -> stmt.setInt(quote.id()))
                .delete()
                .executeSync();
    }

    public void clear(Source quote) {
        builder().query("""
                        DELETE FROM source_links WHERE quote_id = ?;
                        """)
                .paramsBuilder(stmt -> stmt.setInt(quote.id()))
                .delete()
                .execute();
    }
}
