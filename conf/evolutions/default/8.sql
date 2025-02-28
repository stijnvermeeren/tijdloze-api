-- !Ups
ALTER TABLE tijdloze.song ADD musicbrainz_recording_id text NULL;
ALTER TABLE tijdloze.song ADD musicbrainz_work_id text NULL;

CREATE SEQUENCE crawl_song_id_seq INCREMENT 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1;

CREATE TABLE "tijdloze"."crawl_song" (
                                          "id" integer DEFAULT nextval('crawl_song_id_seq') NOT NULL,
                                          "song_id" integer NOT NULL,
                                          "crawl_date" timestamptz NOT NULL,
                                          "field" character varying(255) NOT NULL,
                                          "value" character varying(255),
                                          "comment" character varying(255),
                                          "is_auto" boolean NOT NULL,
                                          "is_accepted" boolean,
                                          CONSTRAINT "idx_crawl_song_song_id_field_value" UNIQUE ("song_id", "field", "value"),
                                          CONSTRAINT "idx_crawl_song_primary" PRIMARY KEY ("id")
) WITH (oids = false);

ALTER TABLE ONLY "tijdloze"."crawl_song" ADD CONSTRAINT "crawl_song_song_id" FOREIGN KEY (song_id) REFERENCES song(id) ON UPDATE CASCADE ON DELETE CASCADE NOT DEFERRABLE;


-- !Downs
ALTER TABLE tijdloze.song DROP COLUMN musicbrainz_recording_id;
ALTER TABLE tijdloze.song DROP COLUMN musicbrainz_work_id;

DROP TABLE "tijdloze"."wikipedia_content";
DROP SEQUENCE "crawl_song_id_seq";