package de.chojo.shepquotes.data.dao;

import de.chojo.sqlutil.base.QueryFactoryHolder;
import de.chojo.sqlutil.wrapper.QueryBuilderConfig;
import org.apache.commons.collections4.iterators.AbstractUntypedIteratorDecorator;
import org.slf4j.Logger;

import javax.sql.DataSource;

import static org.slf4j.LoggerFactory.getLogger;

public class Source extends QueryFactoryHolder {
    private static final Logger log = getLogger(Source.class);

    private Links links;
    int id;

    public Source(Links links, int id) {
        super(links.source(), QueryBuilderConfig.builder()
                .withExceptionHandler(err -> log.error("Unhandled exception", err))
                .build());
        this.links = links;
        this.id = id;
    }

    public int id() {
        return id;
    }

    public void merge(Source source) {

    }

    public void rename(String name) {

    }

    public void delete() {

    }

    public void link(Quote quote) {
        links.link(quote, this);
    }

    public String name() {
        return builder(String.class).query("""
                SELECT name FROM source WHERE id = ?
                """).paramsBuilder(stmt -> stmt.setInt(id))
                .readRow(r -> r.getString("name"))
                .firstSync().get();
    }
}
