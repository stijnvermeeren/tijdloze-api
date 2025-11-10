-- !Ups

CREATE TABLE "tijdloze"."wikipedia_content" (
    "url" character varying(255) NOT NULL,
    "content" text,
    "last_update" timestamptz DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT "idx_wikipedia_content_primary" PRIMARY KEY ("url")
) WITH (oids = false);

ALTER TABLE "tijdloze"."song" ADD "wikidata_id" character varying(255) NULL;

-- !Downs
DROP TABLE "tijdloze"."wikipedia_content";

ALTER TABLE "tijdloze"."song" DROP COLUMN "wikidata_id";
