-- !Ups
ALTER TABLE tijdloze.list_entry ADD attribution text NULL;

-- !Downs
ALTER TABLE tijdloze.list_entry DROP COLUMN attribution;
