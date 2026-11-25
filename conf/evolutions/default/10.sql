-- !Ups
ALTER TABLE tijdloze.song ADD youtube_video_id text NULL;

-- !Downs
ALTER TABLE tijdloze.song DROP COLUMN youtube_video_id;
