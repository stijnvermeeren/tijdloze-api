#!/bin/bash

mysql -B "$@" < flatten.sql > export.tsv

mysqldump --single-transaction "$@" artist album song list_entry > export.sql
