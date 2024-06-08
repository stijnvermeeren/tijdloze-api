
INSERT INTO "user" ("id", "display_name", "name", "first_name", "last_name", "nickname", "email", "email_verified", "created", "last_seen", "is_admin", "is_blocked") VALUES
('auth0|5e63ecaa320c120d43e6574a',	'User 2',	'user2@example.com',	NULL,	NULL,	'user2',	'user2@example.com',	TRUE,	'2020-03-09 20:18:16',	'2020-03-09 20:19:46',	FALSE,	FALSE),
('auth0|5e63ecdd320c120d43e6577d',	NULL,	'admin1@example.com',	NULL,	NULL,	'admin1',	'admin1@example.com',	TRUE,	'2020-03-09 20:18:27',	'2020-03-09 20:18:27',	TRUE,	FALSE),
('auth0|5e63ed00320c120d43e65799',	NULL,	'admin2@example.com',	NULL,	NULL,	'admin2',	'admin2@example.com',	TRUE,	'2020-03-09 20:18:39',	'2020-03-09 20:19:17',	TRUE,	FALSE),
('auth0|5e66986f1efc7a0d639fec45',	'User 1',	'user1@example.com',	NULL,	NULL,	'user1',	'user1@example.com',	TRUE,	'2020-03-09 20:18:03',	'2020-03-09 20:28:45',	FALSE,	FALSE)
ON CONFLICT DO NOTHING;

INSERT INTO "comment" ("user_id", "naam", "tijdstip", "date_deleted", "version_id") VALUES
('auth0|5e66986f1efc7a0d639fec45', NULL, '2020-03-09 19:20:33', NULL, NULL);

INSERT INTO "comment_version" ("comment_id", "message", "created") VALUES
(currval('comment_id_seq'), 'Load up on guns, bring your friends', '2020-03-09 19:20:33');

UPDATE "comment" SET "version_id" = currval('comment_version_id_seq') WHERE "id" = currval('comment_id_seq');

INSERT INTO "comment" ("user_id", "naam", "tijdstip", "date_deleted", "version_id") VALUES
('auth0|5e63ecaa320c120d43e6574a', NULL, '2020-03-09 19:20:41', NULL, NULL);

INSERT INTO "comment_version" ("comment_id", "message", "created") VALUES
(currval('comment_id_seq'), 'It''s fun to lose and to pretend', '2020-03-09 19:20:41');

UPDATE "comment" SET "version_id" = currval('comment_version_id_seq') WHERE "id" = currval('comment_id_seq');

INSERT INTO "comment" ("user_id", "naam", "tijdstip", "date_deleted", "version_id") VALUES
('auth0|5e66986f1efc7a0d639fec45', NULL, '2020-03-09 19:20:50', NULL, NULL);

INSERT INTO "comment_version" ("comment_id", "message", "created") VALUES
(currval('comment_id_seq'), 'She''s over-bored and self-assured', '2020-03-09 19:20:50');

UPDATE "comment" SET "version_id" = currval('comment_version_id_seq') WHERE "id" = currval('comment_id_seq');

INSERT INTO "comment" ("user_id", "naam", "tijdstip", "date_deleted", "version_id") VALUES
('auth0|5e63ecaa320c120d43e6574a', NULL, '2020-03-09 19:20:55', NULL, NULL);

INSERT INTO "comment_version" ("comment_id", "message", "created") VALUES
(currval('comment_id_seq'), 'Oh no, I know a dirty word', '2020-03-09 19:20:55');

UPDATE "comment" SET "version_id" = currval('comment_version_id_seq') WHERE "id" = currval('comment_id_seq');

INSERT INTO "comment" ("user_id", "naam", "tijdstip", "date_deleted", "version_id") VALUES
('auth0|5e66986f1efc7a0d639fec45', NULL, '2020-03-09 19:21:03', NULL, NULL);

INSERT INTO "comment_version" ("comment_id", "message", "created") VALUES
(currval('comment_id_seq'), 'Hello, hello, hello, how low', '2020-03-09 19:21:03');

UPDATE "comment" SET "version_id" = currval('comment_version_id_seq') WHERE "id" = currval('comment_id_seq');

INSERT INTO "comment" ("user_id", "naam", "tijdstip", "date_deleted", "version_id") VALUES
('auth0|5e63ecaa320c120d43e6574a', NULL, '2020-03-09 19:21:09', NULL, NULL);

INSERT INTO "comment_version" ("comment_id", "message", "created") VALUES
(currval('comment_id_seq'), 'Hello, hello, hello, how low', '2020-03-09 19:21:09');

UPDATE "comment" SET "version_id" = currval('comment_version_id_seq') WHERE "id" = currval('comment_id_seq');

INSERT INTO "comment" ("user_id", "naam", "tijdstip", "date_deleted", "version_id") VALUES
('auth0|5e66986f1efc7a0d639fec45', NULL, '2020-03-09 19:21:13', NULL, NULL);

INSERT INTO "comment_version" ("comment_id", "message", "created") VALUES
(currval('comment_id_seq'), 'Hello, hello, hello, how low', '2020-03-09 19:21:13');

UPDATE "comment" SET "version_id" = currval('comment_version_id_seq') WHERE "id" = currval('comment_id_seq');

INSERT INTO "comment" ("user_id", "naam", "tijdstip", "date_deleted", "version_id") VALUES
('auth0|5e63ecaa320c120d43e6574a', NULL, '2020-03-09 19:21:22', NULL, NULL);

INSERT INTO "comment_version" ("comment_id", "message", "created") VALUES
(currval('comment_id_seq'), 'Hello, hello, hello', '2020-03-09 19:21:22');

UPDATE "comment" SET "version_id" = currval('comment_version_id_seq') WHERE "id" = currval('comment_id_seq');

INSERT INTO "comment" ("user_id", "naam", "tijdstip", "date_deleted", "version_id") VALUES
('auth0|5e66986f1efc7a0d639fec45', NULL, '2020-03-09 19:21:27', NULL, NULL);

INSERT INTO "comment_version" ("comment_id", "message", "created") VALUES
(currval('comment_id_seq'), 'With the lights out, it''s less dangerous', '2020-03-09 19:21:27');

UPDATE "comment" SET "version_id" = currval('comment_version_id_seq') WHERE "id" = currval('comment_id_seq');

INSERT INTO "comment" ("user_id", "naam", "tijdstip", "date_deleted", "version_id") VALUES
('auth0|5e63ecaa320c120d43e6574a', NULL, '2020-03-09 19:21:33', NULL, NULL);

INSERT INTO "comment_version" ("comment_id", "message", "created") VALUES
(currval('comment_id_seq'), 'Here we are now, entertain us', '2020-03-09 19:21:33');

UPDATE "comment" SET "version_id" = currval('comment_version_id_seq') WHERE "id" = currval('comment_id_seq');

INSERT INTO "comment" ("user_id", "naam", "tijdstip", "date_deleted", "version_id") VALUES
('auth0|5e66986f1efc7a0d639fec45', NULL, '2020-03-09 19:21:38', NULL, NULL);

INSERT INTO "comment_version" ("comment_id", "message", "created") VALUES
(currval('comment_id_seq'), 'I feel stupid and contagious', '2020-03-09 19:21:38');

UPDATE "comment" SET "version_id" = currval('comment_version_id_seq') WHERE "id" = currval('comment_id_seq');

INSERT INTO "comment" ("user_id", "naam", "tijdstip", "date_deleted", "version_id") VALUES
('auth0|5e63ecaa320c120d43e6574a', NULL, '2020-03-09 19:21:44', NULL, NULL);

INSERT INTO "comment_version" ("comment_id", "message", "created") VALUES
(currval('comment_id_seq'), 'Here we are now, entertain us', '2020-03-09 19:21:44');

UPDATE "comment" SET "version_id" = currval('comment_version_id_seq') WHERE "id" = currval('comment_id_seq');

INSERT INTO "comment" ("user_id", "naam", "tijdstip", "date_deleted", "version_id") VALUES
('auth0|5e66986f1efc7a0d639fec45', NULL, '2020-03-09 19:22:10', NULL, NULL);

INSERT INTO "comment_version" ("comment_id", "message", "created") VALUES
(currval('comment_id_seq'), 'A mulatto', '2020-03-09 19:22:10');

UPDATE "comment" SET "version_id" = currval('comment_version_id_seq') WHERE "id" = currval('comment_id_seq');

INSERT INTO "comment" ("user_id", "naam", "tijdstip", "date_deleted", "version_id") VALUES
('auth0|5e63ecaa320c120d43e6574a', NULL, '2020-03-09 19:23:00', NULL, NULL);

INSERT INTO "comment_version" ("comment_id", "message", "created") VALUES
(currval('comment_id_seq'), 'An albino', '2020-03-09 19:23:00');

UPDATE "comment" SET "version_id" = currval('comment_version_id_seq') WHERE "id" = currval('comment_id_seq');

INSERT INTO "comment" ("user_id", "naam", "tijdstip", "date_deleted", "version_id") VALUES
('auth0|5e66986f1efc7a0d639fec45', NULL, '2020-03-09 19:23:10', NULL, NULL);

INSERT INTO "comment_version" ("comment_id", "message", "created") VALUES
(currval('comment_id_seq'), 'A mosquito', '2020-03-09 19:23:10');

UPDATE "comment" SET "version_id" = currval('comment_version_id_seq') WHERE "id" = currval('comment_id_seq');

INSERT INTO "comment" ("user_id", "naam", "tijdstip", "date_deleted", "version_id") VALUES
('auth0|5e63ecaa320c120d43e6574a', NULL, '2020-03-09 19:23:17', NULL, NULL);

INSERT INTO "comment_version" ("comment_id", "message", "created") VALUES
(currval('comment_id_seq'), 'My libido', '2020-03-09 19:23:17');

UPDATE "comment" SET "version_id" = currval('comment_version_id_seq') WHERE "id" = currval('comment_id_seq');


INSERT INTO "text" ("key", "value") VALUES
('commentsOn', 'on');
