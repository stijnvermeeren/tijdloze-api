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

## Query for creating mbdata.csv dataset

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