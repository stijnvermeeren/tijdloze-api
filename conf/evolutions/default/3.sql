-- !Ups
ALTER TABLE "tijdloze"."song" ALTER COLUMN "language_id" TYPE varchar(3);
ALTER TABLE "tijdloze"."song" ALTER COLUMN "lead_vocals_id" TYPE varchar(3);
ALTER TABLE "tijdloze"."artist" ALTER COLUMN "country_id" TYPE varchar(3);

-- !Downs
