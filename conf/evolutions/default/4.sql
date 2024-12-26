-- !Ups
ALTER TABLE tijdloze.list_entry DROP CONSTRAINT idx_16457_song_id_year;

-- !Downs
ALTER TABLE tijdloze.list_entry ADD CONSTRAINT idx_16457_song_id_year UNIQUE (song_id,"year");
