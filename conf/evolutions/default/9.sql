-- !Ups
ALTER TABLE tijdloze.comment
    ADD COLUMN "parent_id" integer NULL REFERENCES tijdloze.comment (id) ON DELETE CASCADE,
    ADD COLUMN "last_reply_1_id" integer NULL REFERENCES tijdloze.comment (id) ON DELETE CASCADE,
    ADD COLUMN "last_reply_2_id" integer NULL REFERENCES tijdloze.comment (id) ON DELETE CASCADE,
    ADD COLUMN "last_reply_3_id" integer NULL REFERENCES tijdloze.comment (id) ON DELETE CASCADE,
    ADD COLUMN "sort_date" timestamptz DEFAULT CURRENT_TIMESTAMP NOT NULL;

CREATE INDEX "idx_comment_parent_id" ON "tijdloze"."comment" USING btree ("parent_id");
CREATE INDEX "idx_comment_sort_date" ON "tijdloze"."comment" USING btree ("sort_date" DESC);

UPDATE tijdloze.comment SET sort_date = tijdstip;

-- !Downs
ALTER TABLE tijdloze.comment
    DROP COLUMN "parent_id",
    DROP COLUMN "last_reply_1_id",
    DROP COLUMN "last_reply_2_id",
    DROP COLUMN "last_reply_3_id",
    DROP COLUMN "sort_date";

DROP INDEX "tijdloze"."idx_comment_parent_id";
DROP INDEX "tijdloze"."idx_comment_sort_date";