# --- !Ups
ALTER TABLE `artist`
    ADD `wikidata_id` varchar(255) COLLATE 'utf8mb4_unicode_520_ci' NULL AFTER `spotify_id`;

# --- !Downs

ALTER TABLE `artist`
   DROP `wikidata_id`;