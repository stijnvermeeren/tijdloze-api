-- !Ups
UPDATE artist SET name = name_prefix || ' ' || name WHERE name_prefix is not null;
ALTER TABLE artist DROP COLUMN name_prefix;

-- !Downs
