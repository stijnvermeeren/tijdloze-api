# --- !Ups

ALTER TABLE `song`
    ADD `aliases` varchar(255) COLLATE 'utf8mb4_unicode_520_ci' NULL AFTER `title`;

ALTER TABLE `artist`
    ADD `aliases` varchar(255) COLLATE 'utf8mb4_unicode_520_ci' NULL AFTER `name`;

# --- !Downs

ALTER TABLE `song`
    DROP `aliases`;

ALTER TABLE `artist`
    DROP `aliases`;