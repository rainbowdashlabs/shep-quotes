package de.chojo.shepquotes.data.dao;

import de.chojo.sadu.base.QueryFactory;
import de.chojo.sadu.wrapper.QueryBuilderConfig;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class Source extends QueryFactory {
    private static final Logger log = getLogger(Source.class);

    private final Sources sources;
    private final Links links;
    private final int id;
    private String name;

    public Source(Sources sources, Links links, int id, String name) {
        super(links.source(), QueryBuilderConfig.builder()
                .withExceptionHandler(err -> log.error("Unhandled exception", err))
                .build());
        this.sources = sources;
        this.links = links;
        this.id = id;
        this.name = name;
    }

    public int id() {
        return id;
    }

    /**
     * Merge the source into this source.
     *
     * @param old source to merge into this source
     */
    public void merge(Source old) {
        if (old.equals(this)) return;

        builder().query("""
                        WITH assigned AS (
                            SELECT quote_id, COUNT(1) AS count
                            FROM source_links
                            WHERE source_id = ? OR source_id = ?
                            GROUP BY quote_id
                        ),
                        to_delete AS (
                            SELECT quote_id
                            FROM assigned
                            WHERE  count = 2
                        )
                        DELETE FROM source_links
                        WHERE quote_id = ANY (SELECT quote_id FROM to_delete)
                            AND source_id = ?
                        """)
                .parameter(stmt -> stmt.setInt(old.id()).setInt(id).setInt(old.id()))
                .append()
                .query("""
                        UPDATE source_links SET source_id = ? WHERE source_id = ?
                        """)
                .parameter(stmt -> stmt.setInt(id).setInt(old.id()))
                .update()
                .send();
        old.delete();
        sources.invalidate(this);
    }

    public void rename(String name) {
        if (this.name.equals(name)) return;
        if (this.name.equalsIgnoreCase(name)) {
            setName(name);
            return;
        }

        sources.get(name).ifPresentOrElse(
                source -> source.merge(this),
                () -> setName(name));
    }

    private void setName(String name) {
        this.name = name;
        builder().query("""
                        UPDATE source SET name = ? WHERE id = ?;
                        """)
                .parameter(stmt -> stmt.setString(name).setInt(id))
                .update()
                .send();
    }

    public void delete() {
        builder().query("DELETE FROM source WHERE id = ?")
                .parameter(stmt -> stmt.setInt(id))
                .delete()
                .send();
        sources.invalidate(this);
    }

    public void link(Quote quote) {
        links.link(quote, this);
    }

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return name();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Source)) return false;

        var source = (Source) o;

        if (id != source.id) return false;
        return name.equals(source.name);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + name.hashCode();
        return result;
    }

    Source update(String name) {
        this.name = name;
        return this;
    }
}
