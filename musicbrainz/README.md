## Create new postgres tables

```postgresql
CREATE TABLE stijn_area_country_id (
  "area_id" integer, 
  "country_id" varchar,
  PRIMARY KEY ("area_id")
);

DELETE FROM stijn_area_country_id;

INSERT INTO stijn_area_country_id
SELECT
    "area"."id" as "area_id",
    LOWER(COALESCE("iso_3166_1"."code", i1."code", i2."code", i3."code")) AS "county_id"
FROM "area"
         LEFT JOIN "iso_3166_1" ON "area"."id" = "iso_3166_1"."area"
         LEFT JOIN "l_area_area" AS l1 ON l1."link" = 118734 AND l1."entity1" = "area"."id"
         LEFT JOIN "iso_3166_1" AS i1 ON i1."area" = l1."entity0"
         LEFT JOIN "l_area_area" AS l2 ON l2."link" = 118734 AND l2."entity1" = "l1"."entity0"
         LEFT JOIN "iso_3166_1" AS i2 ON i2."area" = l2."entity0"
         LEFT JOIN "l_area_area" AS l3 ON l3."link" = 118734 AND l3."entity1" = "l2"."entity0"
         LEFT JOIN "iso_3166_1" AS i3 ON i3."area" = l3."entity0"
ON CONFLICT ("area_id") DO NOTHING;
```

```postgresql

CREATE TABLE stijn_artist (
                              "name" text,
                              "artist_id" int,
                              "score" int,
                              UNIQUE ("name", "artist_id")
);

DELETE FROM stijn_artist;

WITH data AS (
    SELECT id, "name"
    FROM artist

    UNION

    SELECT artist.id, artist_alias."name"
    FROM artist
    JOIN artist_alias ON artist_alias.artist = artist.id

    UNION

    SELECT artist.id, artist_credit_name."name"
    FROM artist
    JOIN artist_credit_name ON artist_credit_name.artist = artist.id

    UNION

    SELECT artist.id, artist2.name
    FROM artist
    JOIN l_artist_artist ON entity1 = artist.id
    JOIN "link" ON l_artist_artist.link = "link".id AND "link_type" = 103
    JOIN artist AS artist2 ON artist2.id = l_artist_artist.entity0
    JOIN link_attribute ON link_attribute."link" = "link".id AND link_attribute.attribute_type = 1094
)
INSERT INTO stijn_artist
SELECT 
    LOWER(REGEXP_REPLACE(name, '\W', '', 'g')), 
    id,
    (SELECT COUNT(*) FROM "l_artist_url" WHERE "entity0" = data."id")
FROM data
ON CONFLICT ("name", "artist_id") DO NOTHING;
```


```postgresql
CREATE TABLE stijn_recording (
                              "artist_id" int,
                              "name" text,
                              "recording_id" int
);

CREATE INDEX idx_recording_artist ON stijn_recording(artist_id);

DELETE FROM stijn_recording;

WITH data AS (
    SELECT artist_credit_name.artist, recording.name, "recording"."id" as recording_id
    FROM recording
    JOIN artist_credit ON artist_credit.id = recording.artist_credit
    JOIN "artist_credit_name" ON "artist_credit_name"."artist_credit" = "artist_credit"."id"
    
    UNION

    SELECT artist_credit_name.artist, recording_alias.name, "recording"."id" as recording_id
    FROM recording
    JOIN artist_credit ON artist_credit.id = recording.artist_credit
    JOIN "artist_credit_name" ON "artist_credit_name"."artist_credit" = "artist_credit"."id"
    JOIN "recording_alias" ON "recording_alias"."recording" = "recording"."id" 

    UNION

    SELECT artist_credit_name.artist, work.name, "recording"."id" as recording_id
    FROM recording
    JOIN artist_credit ON artist_credit.id = recording.artist_credit
    JOIN "artist_credit_name" ON "artist_credit_name"."artist_credit" = "artist_credit"."id"
    JOIN "l_recording_work" ON "l_recording_work"."entity0" = "recording"."id"
    JOIN "work" ON "work"."id" = "l_recording_work"."entity1"

    UNION

    SELECT artist_credit_name.artist, work_alias.name, "recording"."id" as recording_id
    FROM recording
    JOIN artist_credit ON artist_credit.id = recording.artist_credit
    JOIN "artist_credit_name" ON "artist_credit_name"."artist_credit" = "artist_credit"."id"
    JOIN "l_recording_work" ON "l_recording_work"."entity0" = "recording"."id"
    JOIN "work" ON "work"."id" = "l_recording_work"."entity1"
    JOIN "work_alias" ON "work_alias"."work" = "work"."id"
)
INSERT INTO stijn_recording
SELECT artist, LOWER(REGEXP_REPLACE(name, '\W', '', 'g')), recording_id
FROM data;
```

## Tables for export

```postgresql
CREATE TABLE mb_artist (
    "id" int PRIMARY KEY,
    "mb_id" varchar,
    "name" varchar,
    "country_id" varchar,
    "score" int
);

CREATE TABLE mb_artist_alias (
    "artist_id" int,
    "alias" varchar,
    UNIQUE ("artist_id", "alias")
);

CREATE INDEX idx_mb_artist_alias_artist_id ON mb_artist_alias(artist_id);
CREATE INDEX idx_mb_artist_alias_alias ON mb_artist_alias(alias);

CREATE TABLE mb_album (
    "id" int PRIMARY KEY,
    "mb_id" varchar,
    "title" varchar,
    "release_year" int,
    "is_single" boolean
);

CREATE TABLE mb_song (
    "id" int PRIMARY KEY,
    "mb_id" varchar,
    "title" varchar,
    "artist_id" int,
    "album_id" int,
    "is_single" boolean,
    "score" int
);

CREATE INDEX idx_mb_song_artist_id ON mb_song(artist_id);

CREATE TABLE mb_song_alias (
    "song_id" int,
    "alias" varchar,
    UNIQUE ("song_id", "alias")
);

CREATE INDEX idx_mb_song_alias_song_id ON mb_song_alias(song_id);
CREATE INDEX idx_mb_song_alias_alias ON mb_song_alias(alias);
```

Artist data:
```postgresql
WITH data AS (
    SELECT
      id, gid, name, country_id, (SELECT COUNT(*) FROM "l_artist_url" WHERE "entity0" = "artist"."id") as score
    FROM "artist"
    LEFT JOIN "stijn_area_country_id" ON "stijn_area_country_id"."area_id" = "artist"."area"
)
INSERT INTO mb_artist (id, mb_id, name, country_id, score)
SELECT id, gid, name, country_id, score
FROM data
WHERE country_id = 'be' OR score > 8;


WITH data AS (
    SELECT id, "name"
    FROM mb_artist

    UNION

    SELECT mb_artist.id, artist_alias."name"
    FROM mb_artist
             JOIN artist_alias ON artist_alias.artist = mb_artist.id

    UNION

    SELECT mb_artist.id, artist_credit_name."name"
    FROM mb_artist
             JOIN artist_credit_name ON artist_credit_name.artist = mb_artist.id

    UNION

    SELECT mb_artist.id, artist2.name
    FROM mb_artist
             JOIN l_artist_artist ON entity1 = mb_artist.id
             JOIN "link" ON l_artist_artist.link = "link".id AND "link_type" = 103
             JOIN artist AS artist2 ON artist2.id = l_artist_artist.entity0
             JOIN link_attribute ON link_attribute."link" = "link".id AND link_attribute.attribute_type = 1094
)
INSERT INTO mb_artist_alias (artist_id, alias)
SELECT
    id,
    LOWER(REGEXP_REPLACE(name, '\W', '', 'g'))
FROM data
ON CONFLICT ("artist_id", "alias") DO NOTHING;
```

Recording data (after running script):
```postgresql
-- TODO
-- ERROR:  operator does not exist: uuid = character varying
-- LINE 7:     JOIN recording ON recording.gid = mb_song.mb_id

DELETE FROM mb_song_alias;

WITH data AS (
    SELECT mb_song.id, recording.name
    FROM mb_song
    JOIN recording ON recording.gid = mb_song.mb_id
    
    UNION

    SELECT mb_song.id, recording_alias.name
    FROM mb_song
    JOIN recording ON recording.gid = mb_song.mb_id
    JOIN "recording_alias" ON "recording_alias"."recording" = "recording"."id" 

    UNION

    SELECT mb_song.id, work.name
    FROM mb_song
    JOIN recording ON recording.gid = mb_song.mb_id
    JOIN "l_recording_work" ON "l_recording_work"."entity0" = "recording"."id"
    JOIN "work" ON "work"."id" = "l_recording_work"."entity1"

    UNION

    SELECT mb_song.id, work_alias.name
    FROM mb_song
    JOIN recording ON recording.gid = mb_song.mb_id
    JOIN "l_recording_work" ON "l_recording_work"."entity0" = "recording"."id"
    JOIN "work" ON "work"."id" = "l_recording_work"."entity1"
    JOIN "work_alias" ON "work_alias"."work" = "work"."id"
)
INSERT INTO mb_song_alias
SELECT id, LOWER(REGEXP_REPLACE(name, '\W', '', 'g'))
FROM data;
```

## Query for creating tijdlozedb.csv dataset

```postgresql
SELECT
song.id, song.title,
album.id as "album_id", album.title as "album_title", album.release_year, album.musicbrainz_id,
artist.id as "artist_id", IFNULL(CONCAT(artist.name_prefix, " ", artist.name), artist.name) as "artist_name", artist.country_id as "artist_country_id", artist.musicbrainz_id as "artist_musicbrainz_id",
artist2.id as "artist2_id", IFNULL(CONCAT(artist2.name_prefix, " ", artist2.name), artist2.name) as "artist2_name", artist2.country_id as "artist2_country_id", artist2.musicbrainz_id as "artist2_musicbrainz_id"
FROM song
JOIN album ON album.id = song.album_id 
JOIN artist ON artist.id = song.artist_id
LEFT JOIN artist AS artist2 ON artist2.id = song.second_artist_id
```