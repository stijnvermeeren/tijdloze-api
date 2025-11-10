TRUNCATE TABLE "tijdloze"."artist" CASCADE;
TRUNCATE TABLE "tijdloze"."year" CASCADE;

\copy "tijdloze"."artist" FROM 'artist.tsv' DELIMITER E'\t' CSV HEADER
\copy "tijdloze"."album" FROM 'album.tsv' DELIMITER E'\t' CSV HEADER
\copy "tijdloze"."song" FROM 'song.tsv' DELIMITER E'\t' CSV HEADER
\copy "tijdloze"."year" FROM 'year.tsv' DELIMITER E'\t' CSV HEADER
\copy "tijdloze"."list_entry" FROM 'list_entry.tsv' DELIMITER E'\t' CSV HEADER

SELECT setval(pg_get_serial_sequence('"tijdloze"."artist"', 'id'), (SELECT MAX("id") + 1 FROM "tijdloze"."artist"));
SELECT setval(pg_get_serial_sequence('"tijdloze"."album"', 'id'), (SELECT MAX("id") + 1 FROM "tijdloze"."album"));
SELECT setval(pg_get_serial_sequence('"tijdloze"."song"', 'id'), (SELECT MAX("id") + 1 FROM "tijdloze"."song"));
SELECT setval(pg_get_serial_sequence('"tijdloze"."list_entry"', 'id'), (SELECT MAX("id") + 1 FROM "tijdloze"."list_entry"));
