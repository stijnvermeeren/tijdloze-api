# --- !Ups
CREATE TABLE IF NOT EXISTS `crawl_artist` (
   `id` int NOT NULL AUTO_INCREMENT,
   `artist_id` int NOT NULL,
   `crawl_date` datetime NOT NULL,
   `field` varchar(255) COLLATE utf8mb4_unicode_520_ci NOT NULL,
   `value` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
   `comment` varchar(255) COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
   `is_auto` boolean COLLATE utf8mb4_unicode_520_ci NOT NULL,
   `is_accepted` boolean COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,
   PRIMARY KEY (`id`),
   CONSTRAINT `crawl_artist_artist_id_field_value` UNIQUE (`artist_id`, `field`, `value`),
   CONSTRAINT `crawl_artist_artist_id` FOREIGN KEY (`artist_id`) REFERENCES `artist` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;

ALTER TABLE `artist`
    ADD `spotify_id` varchar(255) COLLATE 'utf8mb4_unicode_520_ci' NULL AFTER `url_allmusic`;

# --- !Downs

DROP TABLE `crawl_artist`;

ALTER TABLE `artist`
   DROP `spotify_id`;