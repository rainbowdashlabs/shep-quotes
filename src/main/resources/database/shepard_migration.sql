-- Leave some space for the migrated ids

ALTER SEQUENCE shep_quotes.quote_id_seq INCREMENT BY 5000;
SELECT nextval('shep_quotes.quote_id_seq');
ALTER SEQUENCE shep_quotes.quote_id_seq INCREMENT BY 1;


-- Extract distinct sources from quotes by guild id
SELECT ROW_NUMBER() OVER (), name, guild_id
FROM (SELECT guild_id, INITCAP(name) AS name
      FROM (SELECT guild_id, UNNEST(REGEXP_SPLIT_TO_ARRAY(source, '\s*,\s*')) AS name
            FROM shep_quotes.quotes_old
            WHERE source IS NOT NULL) b) a
GROUP BY guild_id, name;

-- Insert distinct sources into sources table
INSERT INTO shep_quotes.source
SELECT ROW_NUMBER() OVER (), name, guild_id
FROM (SELECT guild_id, INITCAP(name) AS name
      FROM (SELECT guild_id, UNNEST(REGEXP_SPLIT_TO_ARRAY(source, '\s*,\s*')) AS name
            FROM shep_quotes.quotes_old
            WHERE source IS NOT NULL) b) a
GROUP BY guild_id, name;

-- Insert quotes to registry
INSERT INTO shep_quotes.quote
SELECT quote_id, created, edited, -1, guild_id
FROM shep_quotes.quotes_old;

-- Insert quote content
INSERT INTO shep_quotes.content
SELECT quote_id, quote
FROM shep_quotes.quotes_old;

-- Build source quote links
SELECT quote_id, a.guild_id, a.name AS name, s.id, s.guild_id AS source_guild_id
FROM (SELECT quote_id, guild_id, INITCAP(UNNEST(REGEXP_SPLIT_TO_ARRAY(source, '\s*,\s*'))) AS name
      FROM shep_quotes.quotes_old q
      WHERE source IS NOT NULL) a
         LEFT JOIN shep_quotes.source s ON a.name = s.name
WHERE a.guild_id = s.guild_id;

-- Insert links
INSERT INTO shep_quotes.source_links
SELECT quote_id, s.id AS source_guild_id
FROM (SELECT quote_id, guild_id, INITCAP(UNNEST(REGEXP_SPLIT_TO_ARRAY(source, '\s*,\s*'))) AS name
      FROM shep_quotes.quotes_old q
      WHERE source IS NOT NULL) a
         LEFT JOIN shep_quotes.source s ON a.name = s.name
WHERE a.guild_id = s.guild_id;
