#!/bin/bash

mysql -B "$@" < flatten.sql > tijdloze.tsv

mysqldump --single-transaction "$@" artist album song list_entry > tijdloze.sql
