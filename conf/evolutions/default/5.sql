-- !Ups
ALTER TABLE tijdloze.album ADD is_single bool DEFAULT False NOT NULL;
ALTER TABLE tijdloze.album ADD is_soundtrack bool DEFAULT False NOT NULL;
update tijdloze.album set is_single = true, title = substr(title, 1, length(title) - length(' (single)')) where title like '% (single)';
update tijdloze.album set is_soundtrack = true, title = substr(title, 1, length(title) - length(' (soundtrack)')) where title like '% (soundtrack)';

-- !Downs
update tijdloze.album set title = concat(title, ' (single)') where is_single;
update tijdloze.album set title = concat(title, ' (soundtrack)') where is_soundtrack;
ALTER TABLE tijdloze.album DROP COLUMN is_single;
ALTER TABLE tijdloze.album DROP COLUMN is_soundtrack;
