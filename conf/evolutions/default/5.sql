# --- !Ups
ALTER TABLE `artist`
    ADD `musicbrainz_id` varchar(255) COLLATE 'utf8mb4_unicode_520_ci' NULL AFTER `wikidata_id`;

# --- !Downs

ALTER TABLE `artist`
   DROP `musicbrainz_id`;