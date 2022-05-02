CREATE TABLE IF NOT EXISTS shep_quotes.source
(
    id       SERIAL NOT NULL,
    name     TEXT   NOT NULL,
    guild_id BIGINT NOT NULL,
    CONSTRAINT source_pk
        UNIQUE (id)
);

CREATE INDEX IF NOT EXISTS source_guild_id_index
    ON shep_quotes.source (guild_id);

CREATE UNIQUE INDEX IF NOT EXISTS "source_guild_id_lower(name)_uindex"
    ON shep_quotes.source (guild_id, LOWER(name));

CREATE TABLE IF NOT EXISTS shep_quotes.quote
(
    id       SERIAL,
    created  TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    modified TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    owner    BIGINT                                 NOT NULL,
    guild_id BIGINT                                 NOT NULL,
    CONSTRAINT quote_pk
        PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS shep_quotes.content
(
    quote_id INTEGER,
    content  TEXT,
    CONSTRAINT content_quote_id_fk
        FOREIGN KEY (quote_id) REFERENCES shep_quotes.quote
            ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS shep_quotes.source_links
(
    quote_id  INTEGER,
    author_id INTEGER,
    CONSTRAINT source_links_quote_id_fk
        FOREIGN KEY (quote_id) REFERENCES shep_quotes.quote
            ON DELETE CASCADE,
    CONSTRAINT source_links_source_id_fk
        FOREIGN KEY (author_id) REFERENCES shep_quotes.source (id)
            ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS author_links_author_id_index
    ON shep_quotes.source_links (author_id COLLATE ??? ???);

CREATE UNIQUE INDEX IF NOT EXISTS author_links_quote_id_author_id_uindex
    ON shep_quotes.source_links (quote_id, author_id);

CREATE INDEX IF NOT EXISTS author_links_quote_id_index
    ON shep_quotes.source_links (quote_id COLLATE ??? ???);

CREATE TABLE IF NOT EXISTS shep_quotes.quotes_old
(
    quote_id INTEGER NOT NULL,
    quote    TEXT    NOT NULL,
    guild_id BIGINT  NOT NULL,
    source   TEXT,
    created  TIMESTAMP DEFAULT NOW(),
    edited   TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (quote_id, guild_id)
);

CREATE OR REPLACE VIEW shep_quotes.source_ids(quote_id, ids) AS
SELECT source_links.quote_id,
       ARRAY_AGG(source_links.source_id) AS ids
FROM shep_quotes.source_links
GROUP BY source_links.quote_id;

CREATE OR REPLACE VIEW shep_quotes.guild_quote_ids(quote_id, guild_quote_id) AS
SELECT quote.id                                        AS quote_id,
       ROW_NUMBER() OVER (PARTITION BY quote.guild_id) AS guild_quote_id
FROM shep_quotes.quote
ORDER BY quote.id;
