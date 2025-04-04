#!/bin/bash

mkdir -p tijdloze
rm -r tijdloze/*
cp -r static/* tijdloze/

psql "$@" -t -A < flatten_query.sql | psql "$@" > tijdloze.tsv

echo "COPY (SELECT * FROM song) TO STDOUT CSV HEADER DELIMITER E'\t';" | psql "$@" -t -A > tijdloze/song.tsv
echo "COPY (SELECT * FROM artist) TO STDOUT CSV HEADER DELIMITER E'\t';" | psql "$@" -t -A > tijdloze/artist.tsv
echo "COPY (SELECT * FROM album) TO STDOUT CSV HEADER DELIMITER E'\t';" | psql "$@" -t -A > tijdloze/album.tsv
echo "COPY (SELECT * FROM list_entry) TO STDOUT CSV HEADER DELIMITER E'\t';" | psql "$@" -t -A > tijdloze/list_entry.tsv
echo "COPY (SELECT * FROM year) TO STDOUT CSV HEADER DELIMITER E'\t';" | psql "$@" -t -A > tijdloze/year.tsv

pg_dump "$@" --schema-only -t artist -t album -t song -t list_entry -t year > tijdloze/postgres-schema.sql

zip -r tijdloze.zip tijdloze/
