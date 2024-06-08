-- !Ups

CREATE SEQUENCE album_id_seq INCREMENT 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1;

CREATE TABLE "tijdloze"."album" (
    "id" integer DEFAULT nextval('album_id_seq') NOT NULL,
    "title" character varying(255) NOT NULL,
    "artist_id" integer NOT NULL,
    "release_year" smallint NOT NULL,
    "url_wikien" character varying(255),
    "url_wikinl" character varying(255),
    "url_allmusic" character varying(255),
    "spotify_id" character varying(255),
    "wikidata_id" character varying(255),
    "musicbrainz_id" character varying(255),
    "cover" character varying(255),
    CONSTRAINT "idx_16388_primary" PRIMARY KEY ("id")
) WITH (oids = false);

CREATE INDEX "idx_16388_artist_id" ON "tijdloze"."album" USING btree ("artist_id");


CREATE SEQUENCE analyse_id_seq INCREMENT 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1;

CREATE TABLE "tijdloze"."analyse" (
    "id" integer DEFAULT nextval('analyse_id_seq') NOT NULL,
    "jaar" smallint NOT NULL,
    "bericht" text NOT NULL,
    CONSTRAINT "idx_16395_primary" PRIMARY KEY ("id")
) WITH (oids = false);


CREATE SEQUENCE artist_id_seq INCREMENT 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1;

CREATE TABLE "tijdloze"."artist" (
    "id" integer DEFAULT nextval('artist_id_seq') NOT NULL,
    "name_prefix" character varying(255),
    "name" character varying(255) NOT NULL,
    "aliases" character varying(255),
    "country_id" character(3),
    "notes" text,
    "url_official" character varying(255),
    "url_wikien" character varying(255),
    "url_wikinl" character varying(255),
    "url_allmusic" character varying(255),
    "spotify_id" character varying(255),
    "wikidata_id" character varying(255),
    "musicbrainz_id" character varying(255),
    CONSTRAINT "idx_16402_primary" PRIMARY KEY ("id")
) WITH (oids = false);


CREATE SEQUENCE chat_message_id_seq INCREMENT 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1;

CREATE TABLE "tijdloze"."chat_message" (
    "id" integer DEFAULT nextval('chat_message_id_seq') NOT NULL,
    "user_id" character varying(255) NOT NULL,
    "message" text NOT NULL,
    "created" timestamptz DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT "idx_16409_primary" PRIMARY KEY ("id")
) WITH (oids = false);

CREATE INDEX "idx_16409_user_id" ON "tijdloze"."chat_message" USING btree ("user_id");


CREATE TABLE "tijdloze"."chat_online" (
    "user_id" character varying(255) NOT NULL,
    "last_seen" timestamptz DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT "idx_16416_primary" PRIMARY KEY ("user_id")
) WITH (oids = false);


CREATE TABLE "tijdloze"."chat_ticket" (
    "ticket" character varying(255) NOT NULL,
    "user_id" character varying(255) NOT NULL,
    "created" timestamptz NOT NULL,
    "used" timestamptz,
    CONSTRAINT "idx_16420_primary" PRIMARY KEY ("ticket")
) WITH (oids = false);

CREATE INDEX "idx_16420_user_id" ON "tijdloze"."chat_ticket" USING btree ("user_id");


CREATE SEQUENCE comment_id_seq INCREMENT 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1;

CREATE TABLE "tijdloze"."comment" (
    "id" integer DEFAULT nextval('comment_id_seq') NOT NULL,
    "user_id" character varying(255),
    "naam" character varying(255) DEFAULT '',
    "bericht" text,
    "ip" character varying(15) DEFAULT '',
    "tijdstip" timestamptz DEFAULT CURRENT_TIMESTAMP NOT NULL,
    "date_deleted" timestamptz,
    "version_id" integer,
    CONSTRAINT "idx_16426_primary" PRIMARY KEY ("id")
) WITH (oids = false);

CREATE INDEX "idx_16426_fi_version_id" ON "tijdloze"."comment" USING btree ("version_id");

CREATE INDEX "idx_16426_user_id" ON "tijdloze"."comment" USING btree ("user_id");


CREATE SEQUENCE comment_version_id_seq INCREMENT 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1;

CREATE TABLE "tijdloze"."comment_version" (
    "id" integer DEFAULT nextval('comment_version_id_seq') NOT NULL,
    "comment_id" integer NOT NULL,
    "message" text,
    "created" timestamptz,
    CONSTRAINT "idx_16436_primary" PRIMARY KEY ("id")
) WITH (oids = false);

CREATE INDEX "idx_16436_fi_comment_id" ON "tijdloze"."comment_version" USING btree ("comment_id");


CREATE SEQUENCE crawl_album_id_seq INCREMENT 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1;

CREATE TABLE "tijdloze"."crawl_album" (
    "id" integer DEFAULT nextval('crawl_album_id_seq') NOT NULL,
    "album_id" integer NOT NULL,
    "crawl_date" timestamptz NOT NULL,
    "field" character varying(255) NOT NULL,
    "value" character varying(255),
    "comment" character varying(255),
    "is_auto" boolean NOT NULL,
    "is_accepted" boolean,
    CONSTRAINT "idx_16443_crawl_album_album_id_field_value" UNIQUE ("album_id", "field", "value"),
    CONSTRAINT "idx_16443_primary" PRIMARY KEY ("id")
) WITH (oids = false);


CREATE SEQUENCE crawl_artist_id_seq INCREMENT 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1;

CREATE TABLE "tijdloze"."crawl_artist" (
    "id" integer DEFAULT nextval('crawl_artist_id_seq') NOT NULL,
    "artist_id" integer NOT NULL,
    "crawl_date" timestamptz NOT NULL,
    "field" character varying(255) NOT NULL,
    "value" character varying(255),
    "comment" character varying(255),
    "is_auto" boolean NOT NULL,
    "is_accepted" boolean,
    CONSTRAINT "idx_16450_crawl_artist_artist_id_field_value" UNIQUE ("artist_id", "field", "value"),
    CONSTRAINT "idx_16450_primary" PRIMARY KEY ("id")
) WITH (oids = false);


CREATE SEQUENCE list_entry_id_seq INCREMENT 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1;

CREATE TABLE "tijdloze"."list_entry" (
    "id" integer DEFAULT nextval('list_entry_id_seq') NOT NULL,
    "song_id" integer NOT NULL,
    "year" smallint NOT NULL,
    "position" smallint NOT NULL,
    CONSTRAINT "idx_16457_primary" PRIMARY KEY ("id"),
    CONSTRAINT "idx_16457_song_id_year" UNIQUE ("song_id", "year"),
    CONSTRAINT "idx_16457_year_position" UNIQUE ("year", "position")
) WITH (oids = false);


CREATE SEQUENCE list_exit_id_seq INCREMENT 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1;

CREATE TABLE "tijdloze"."list_exit" (
    "id" integer DEFAULT nextval('list_exit_id_seq') NOT NULL,
    "song_id" integer NOT NULL,
    "year" smallint NOT NULL,
    CONSTRAINT "idx_16462_primary" PRIMARY KEY ("id"),
    CONSTRAINT "idx_16462_song_id_year" UNIQUE ("song_id", "year")
) WITH (oids = false);

CREATE INDEX "idx_16462_year" ON "tijdloze"."list_exit" USING btree ("year");


CREATE SEQUENCE log_user_display_name_id_seq INCREMENT 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1;

CREATE TABLE "tijdloze"."log_user_display_name" (
    "id" integer DEFAULT nextval('log_user_display_name_id_seq') NOT NULL,
    "user_id" character varying(255) NOT NULL,
    "display_name" character varying(255) NOT NULL,
    "created" timestamptz NOT NULL,
    CONSTRAINT "idx_16467_primary" PRIMARY KEY ("id")
) WITH (oids = false);

CREATE INDEX "idx_16467_user_id" ON "tijdloze"."log_user_display_name" USING btree ("user_id");


CREATE SEQUENCE poll_id_seq INCREMENT 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1;

CREATE TABLE "tijdloze"."poll" (
    "id" integer DEFAULT nextval('poll_id_seq') NOT NULL,
    "year" smallint NOT NULL,
    "question" character varying(255) NOT NULL,
    "is_active" boolean DEFAULT true NOT NULL,
    "is_deleted" boolean DEFAULT false NOT NULL,
    "timestamp" timestamptz DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT "idx_16479_primary" PRIMARY KEY ("id")
) WITH (oids = false);


CREATE SEQUENCE poll_answer_id_seq INCREMENT 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1;

CREATE TABLE "tijdloze"."poll_answer" (
    "id" integer DEFAULT nextval('poll_answer_id_seq') NOT NULL,
    "poll_id" integer NOT NULL,
    "answer" character varying(255) NOT NULL,
    "vote_count" integer DEFAULT '0' NOT NULL,
    CONSTRAINT "idx_16487_primary" PRIMARY KEY ("id")
) WITH (oids = false);

CREATE INDEX "idx_16487_poll_id" ON "tijdloze"."poll_answer" USING btree ("poll_id");


CREATE SEQUENCE poll_vote_id_seq INCREMENT 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1;

CREATE TABLE "tijdloze"."poll_vote" (
    "id" integer DEFAULT nextval('poll_vote_id_seq') NOT NULL,
    "user_id" character varying(255),
    "poll_id" integer NOT NULL,
    "answer_id" integer NOT NULL,
    "session_id" character varying(255),
    "ip" character varying(255),
    "timestamp" timestamptz DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT "idx_16493_primary" PRIMARY KEY ("id")
) WITH (oids = false);

CREATE INDEX "idx_16493_answer_id" ON "tijdloze"."poll_vote" USING btree ("answer_id");

CREATE INDEX "idx_16493_poll_id" ON "tijdloze"."poll_vote" USING btree ("poll_id");

CREATE INDEX "idx_16493_user_id" ON "tijdloze"."poll_vote" USING btree ("user_id");


CREATE SEQUENCE reactie_bu_id_seq INCREMENT 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1;

CREATE TABLE "tijdloze"."reactie_bu" (
    "id" integer DEFAULT nextval('reactie_bu_id_seq') NOT NULL,
    "user_id" character varying(255),
    "naam" character varying(255) DEFAULT '',
    "email" character varying(255) DEFAULT '' NOT NULL,
    "bericht" text NOT NULL,
    "ip" character varying(15) DEFAULT '',
    "tijdstip" timestamptz DEFAULT CURRENT_TIMESTAMP NOT NULL,
    "code" character varying(255),
    CONSTRAINT "idx_16501_primary" PRIMARY KEY ("id")
) WITH (oids = false);

CREATE INDEX "idx_16501_user_id" ON "tijdloze"."reactie_bu" USING btree ("user_id");


CREATE SEQUENCE song_id_seq INCREMENT 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1;

CREATE TABLE "tijdloze"."song" (
    "id" integer DEFAULT nextval('song_id_seq') NOT NULL,
    "artist_id" integer NOT NULL,
    "second_artist_id" integer,
    "title" character varying(255) NOT NULL,
    "aliases" character varying(255),
    "album_id" integer NOT NULL,
    "lyrics" text,
    "language_id" character(3),
    "lead_vocals_id" character(1),
    "notes" text,
    "url_wikien" character varying(255),
    "url_wikinl" character varying(255),
    "spotify_id" character varying(255),
    "last_update" timestamptz DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT "idx_16512_primary" PRIMARY KEY ("id")
) WITH (oids = false);

CREATE INDEX "idx_16512_album" ON "tijdloze"."song" USING btree ("album_id");

CREATE INDEX "idx_16512_artiest" ON "tijdloze"."song" USING btree ("artist_id");

CREATE INDEX "idx_16512_second_artist_id" ON "tijdloze"."song" USING btree ("second_artist_id");


CREATE TABLE "tijdloze"."text" (
    "key" character varying(255) NOT NULL,
    "value" text NOT NULL,
    CONSTRAINT "idx_16519_primary" PRIMARY KEY ("key")
) WITH (oids = false);


CREATE TABLE "tijdloze"."user" (
    "id" character varying(255) NOT NULL,
    "display_name" character varying(255),
    "name" character varying(255),
    "first_name" character varying(255),
    "last_name" character varying(255),
    "nickname" character varying(255),
    "email" character varying(255),
    "email_verified" boolean NOT NULL,
    "created" timestamptz NOT NULL,
    "last_seen" timestamptz NOT NULL,
    "is_admin" boolean DEFAULT false NOT NULL,
    "is_blocked" boolean DEFAULT false NOT NULL,
    CONSTRAINT "idx_16524_primary" PRIMARY KEY ("id")
) WITH (oids = false);


CREATE TABLE "tijdloze"."year" (
    "year" smallint NOT NULL,
    CONSTRAINT "idx_16531_primary" PRIMARY KEY ("year")
) WITH (oids = false);


ALTER TABLE ONLY "tijdloze"."album" ADD CONSTRAINT "album_ibfk_2" FOREIGN KEY (artist_id) REFERENCES artist(id) ON UPDATE CASCADE ON DELETE CASCADE NOT DEFERRABLE;

ALTER TABLE ONLY "tijdloze"."chat_message" ADD CONSTRAINT "chat_message_ibfk_2" FOREIGN KEY (user_id) REFERENCES "user"(id) ON UPDATE CASCADE ON DELETE CASCADE NOT DEFERRABLE;

ALTER TABLE ONLY "tijdloze"."chat_online" ADD CONSTRAINT "chat_online_ibfk_2" FOREIGN KEY (user_id) REFERENCES "user"(id) ON UPDATE CASCADE ON DELETE CASCADE NOT DEFERRABLE;

ALTER TABLE ONLY "tijdloze"."chat_ticket" ADD CONSTRAINT "chat_ticket_ibfk_1" FOREIGN KEY (user_id) REFERENCES "user"(id) ON UPDATE CASCADE ON DELETE CASCADE NOT DEFERRABLE;

ALTER TABLE ONLY "tijdloze"."comment" ADD CONSTRAINT "comment_ibfk_1" FOREIGN KEY (user_id) REFERENCES "user"(id) ON UPDATE CASCADE ON DELETE CASCADE NOT DEFERRABLE;
ALTER TABLE ONLY "tijdloze"."comment" ADD CONSTRAINT "comment_ibfk_2" FOREIGN KEY (version_id) REFERENCES comment_version(id) ON UPDATE CASCADE ON DELETE CASCADE NOT DEFERRABLE;

ALTER TABLE ONLY "tijdloze"."comment_version" ADD CONSTRAINT "fk_comment_id" FOREIGN KEY (comment_id) REFERENCES comment(id) ON UPDATE CASCADE ON DELETE CASCADE NOT DEFERRABLE;

ALTER TABLE ONLY "tijdloze"."crawl_album" ADD CONSTRAINT "crawl_album_album_id" FOREIGN KEY (album_id) REFERENCES album(id) ON UPDATE CASCADE ON DELETE CASCADE NOT DEFERRABLE;

ALTER TABLE ONLY "tijdloze"."crawl_artist" ADD CONSTRAINT "crawl_artist_artist_id" FOREIGN KEY (artist_id) REFERENCES artist(id) ON UPDATE CASCADE ON DELETE CASCADE NOT DEFERRABLE;

ALTER TABLE ONLY "tijdloze"."list_entry" ADD CONSTRAINT "list_entry_ibfk_1" FOREIGN KEY (song_id) REFERENCES song(id) ON UPDATE CASCADE ON DELETE CASCADE NOT DEFERRABLE;
ALTER TABLE ONLY "tijdloze"."list_entry" ADD CONSTRAINT "list_entry_ibfk_2" FOREIGN KEY (year) REFERENCES year(year) ON UPDATE CASCADE ON DELETE CASCADE NOT DEFERRABLE;

ALTER TABLE ONLY "tijdloze"."list_exit" ADD CONSTRAINT "list_exit_ibfk_2" FOREIGN KEY (song_id) REFERENCES song(id) ON UPDATE CASCADE ON DELETE CASCADE NOT DEFERRABLE;
ALTER TABLE ONLY "tijdloze"."list_exit" ADD CONSTRAINT "list_exit_ibfk_3" FOREIGN KEY (year) REFERENCES year(year) ON UPDATE CASCADE ON DELETE CASCADE NOT DEFERRABLE;

ALTER TABLE ONLY "tijdloze"."log_user_display_name" ADD CONSTRAINT "log_user_display_name_ibfk_2" FOREIGN KEY (user_id) REFERENCES "user"(id) ON UPDATE CASCADE ON DELETE CASCADE NOT DEFERRABLE;

ALTER TABLE ONLY "tijdloze"."poll_answer" ADD CONSTRAINT "poll_answer_ibfk_2" FOREIGN KEY (poll_id) REFERENCES poll(id) ON UPDATE CASCADE ON DELETE CASCADE NOT DEFERRABLE;

ALTER TABLE ONLY "tijdloze"."poll_vote" ADD CONSTRAINT "poll_vote_ibfk_2" FOREIGN KEY (poll_id) REFERENCES poll(id) ON UPDATE CASCADE ON DELETE CASCADE NOT DEFERRABLE;
ALTER TABLE ONLY "tijdloze"."poll_vote" ADD CONSTRAINT "poll_vote_ibfk_3" FOREIGN KEY (answer_id) REFERENCES poll_answer(id) ON UPDATE CASCADE ON DELETE CASCADE NOT DEFERRABLE;
ALTER TABLE ONLY "tijdloze"."poll_vote" ADD CONSTRAINT "poll_vote_ibfk_4" FOREIGN KEY (user_id) REFERENCES "user"(id) ON UPDATE CASCADE ON DELETE CASCADE NOT DEFERRABLE;

ALTER TABLE ONLY "tijdloze"."reactie_bu" ADD CONSTRAINT "reactie_bu_ibfk_1" FOREIGN KEY (user_id) REFERENCES "user"(id) ON UPDATE RESTRICT ON DELETE RESTRICT NOT DEFERRABLE;

ALTER TABLE ONLY "tijdloze"."song" ADD CONSTRAINT "song_ibfk_1" FOREIGN KEY (album_id) REFERENCES album(id) ON UPDATE CASCADE ON DELETE CASCADE NOT DEFERRABLE;
ALTER TABLE ONLY "tijdloze"."song" ADD CONSTRAINT "song_ibfk_2" FOREIGN KEY (artist_id) REFERENCES artist(id) ON UPDATE CASCADE ON DELETE CASCADE NOT DEFERRABLE;
ALTER TABLE ONLY "tijdloze"."song" ADD CONSTRAINT "song_ibfk_3" FOREIGN KEY (second_artist_id) REFERENCES artist(id) ON UPDATE CASCADE ON DELETE CASCADE NOT DEFERRABLE;

-- !Downs
