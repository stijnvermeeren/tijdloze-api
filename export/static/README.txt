De relationale database van tijdloze.rocks is geëxporteerd naar vijf TSV-bestanden
 (tab-separated values), die elk de gegevens van een bepaalde tabel uit de database
 bevatten:

artist.tsv
- alle artiesten

album.tsv
- alle albums
- de "foreign key" "artist_id" verwijst naar de "id" uit artist.tsv

song.tsv
- alle nummers
- de "foreign key" "artist_id" verwijst naar de "id" uit artist.tsv
- de (optionale) "foreign key" "second_artist_id" verwijst ook naar de "id"
 uit artist.tsv
- de "foreign key" "album_id" verwijst naar de "id" uit album.tsv

year.tsv
- de jaartallen van alle edities van de Tijdloze

list_entry.tsv
- alle noteringen
- de "foreign key" "song_id" verwijst naar de "id" uit song.tsv
- de "foreign key" "year" verwijst naar de "year" uit year.tsv


Wie met PostgreSQL werkt, kan de relationele database als volgt reconstrueren:

1. Maak een schema met de naam "tijdloze" in de database

echo "CREATE SCHEMA tijdloze;" | psql [connection_params]

1. Creëer de tabellen (indien nog niet gedaan)

psql [connection_params] < postgres-schema.sql

2. Gegevens uit de TSV-bestanden laden (bestaande gegevens worden verwijderd!)

psql [connection_params] < postgres-import.sql
