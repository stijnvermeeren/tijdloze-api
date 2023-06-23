# --- !Ups
CREATE TABLE IF NOT EXISTS `crawl_album` (
   `id` int NOT NULL AUTO_INCREMENT,
   `album_id` int NOT NULL,
   `crawl_date` datetime NOT NULL,
   `field` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
   `value` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
   `comment` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
   `is_auto` boolean COLLATE utf8mb4_unicode_520_ci NOT NULL,
   `is_accepted` boolean COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
   PRIMARY KEY (`id`),
   CONSTRAINT `crawl_album_album_id_field_value` UNIQUE (`album_id`, `field`, `value`),
   CONSTRAINT `crawl_album_album_id` FOREIGN KEY (`album_id`) REFERENCES `album` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

ALTER TABLE `album`
    ADD `spotify_id` varchar(255) COLLATE 'utf8mb4_unicode_520_ci' NULL AFTER `url_allmusic`,
    ADD `wikidata_id` varchar(255) COLLATE 'utf8mb4_unicode_520_ci' NULL AFTER `spotify_id`,
    ADD `musicbrainz_id` varchar(255) COLLATE 'utf8mb4_unicode_520_ci' NULL AFTER `wikidata_id`,
    ADD `cover` varchar(255) COLLATE 'utf8mb4_unicode_520_ci' NULL AFTER `musicbrainz_id`;

# --- !Downs

DROP TABLE `crawl_album`;

ALTER TABLE `album`
   DROP `spotify_id`,
   DROP `wikidata_id`,
   DROP `musicbrainz_id`,
   DROP `cover`;