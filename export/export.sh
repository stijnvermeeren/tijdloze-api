#!/bin/bash

psql "$@" -t -A < flatten_query.sql | psql "$@" > tijdloze.tsv

pg_dump "$@" --data-only -t artist -t album -t song -t year -t list_entry > tijdloze-data.sql
pg_dump "$@" -t artist -t album -t song -t year -t list_entry > tijdloze-schema-data.sql