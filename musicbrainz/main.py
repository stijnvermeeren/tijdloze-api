from util import query, search_key
import psycopg2
import re
import dataclasses


@dataclasses.dataclass
class Entry:
    title: str
    recording_id: int
    recording_mb_id: str
    release_group_id: int
    release_group_mb_id: str
    release_group_name: str
    release_type: int
    release_secondary_types: list[int]
    release_year: int
    is_single_from: int
    recording_score: int

    def is_main_album(self):
        return self.release_type == 1 and not self.release_secondary_types

    def is_compilation_album(self):
        return self.release_type == 1 and 1 in self.release_secondary_types

    def is_soundtrack_album(self):
        return self.release_type == 1 and 2 in self.release_secondary_types

    def is_exact_match(self, query):
        return search_key(self.title) == search_key(query)

    def relevance_for_query(self, query):
        if self.is_exact_match(query):
            # exact match
            return self.recording_score
        else:
            # e.g. "Hotellounge (Be the Death of Me)" instead of "Hotellounge"
            return self.recording_score / 10

    def sort_key(self):
        if self.release_year is None:
            year_value = 9999
        elif self.is_single_from or self.is_main_album():
            year_value = self.release_year
        else:
            year_value = self.release_year + 1

        reference_priorty = 1
        if self.is_single_from:
            reference_priorty = 0

        if self.is_main_album():
            type_priority = 1
        elif self.is_soundtrack_album():
            type_priority = 2
        elif self.is_compilation_album():
            # Note that we have already ensured in the SQL query that we only take compilation albums from the artist,
            # no "various artists" compilation albums.
            type_priority = 3
        else:
            type_priority = 4

        return (year_value, reference_priorty, type_priority)


def process_artist(cursor, artist_id: int):
    singlesQuery = """
        SELECT
            release_group.name AS title,
            release_group_album."gid" AS album_id
        FROM "artist_credit_name"
        JOIN "artist_credit" ON "artist_credit"."id" = "artist_credit_name"."artist_credit"
        JOIN "release_group" ON "release_group"."artist_credit" = "artist_credit"."id"
        JOIN "l_release_group_release_group" ON "l_release_group_release_group"."entity0" = "release_group".id
        JOIN "link" ON "link"."id" = "l_release_group_release_group"."link"
        JOIN "release_group" AS release_group_album ON release_group_album.id = "l_release_group_release_group"."entity1"
        WHERE "artist_credit_name"."artist" = {} AND "link"."link_type" = 11  -- "single_from"
    """.format(artist_id)
    single_from_relations = {}
    for entry in query(cursor, singlesQuery):
        single_title = entry["title"]
        if single_title not in single_from_relations:
            single_from_relations[single_title] = set()
        single_from_relations[single_title].add(entry['album_id'])

    recordingsQuery = """
        SELECT
           release_group.id as release_group_id, 
           release_group.gid as release_group_mb_id, 
           release_group.name as release_group_name,
           release_group.type as release_type,
           MIN((SELECT MIN(date_year) FROM "release_country" WHERE release_country.release = release.id)) as release_year,
           (SELECT array_agg(secondary_type) FROM release_group_secondary_type_join WHERE release_group_secondary_type_join.release_group = release_group.id) as secondary_types,
           "recording"."id" as recording_id,
           "recording"."gid" as recording_mb_id,
           "recording"."name" as recording_name,
           (SELECT COUNT(*) FROM "release" r2 JOIN "medium" m2 ON m2."release" = r2."id" JOIN "track" t2 ON t2."medium" = m2."id" WHERE t2."recording" = "recording"."id") as recording_score
        FROM "recording"
        JOIN "track" ON "recording"."id" = "track"."recording"
        JOIN "medium" ON "track"."medium" = "medium"."id" 
        JOIN "release" ON "medium"."release" = "release"."id"
        JOIN "release_group" ON "release"."release_group" = "release_group"."id"
        JOIN "artist_credit" AS artist_credit_rg ON artist_credit_rg.id = "release_group"."artist_credit"
        JOIN "artist_credit_name" ON "artist_credit_name"."artist_credit" = artist_credit_rg."id"
        JOIN "artist_credit" ON "artist_credit".id = "recording"."artist_credit"
        WHERE "artist_credit_name"."artist" = {} AND "release"."status" = 1 -- official
        GROUP BY recording.id, release_group.id
    """.format(artist_id)

    songs = {}
    for entry in query(cursor, recordingsQuery):
        if entry['release_year'] is None:
            continue

        title = entry['recording_name']
        release_group_mb_id = entry['release_group_mb_id']
        is_single_from = title in single_from_relations and release_group_mb_id in single_from_relations[title]

        song = Entry(
            title=title,
            recording_id=entry['recording_id'],
            recording_mb_id=entry['recording_mb_id'],
            release_group_id=entry['release_group_id'],
            release_group_mb_id=release_group_mb_id,
            release_group_name=entry['release_group_name'],
            release_type=entry['release_type'],
            release_secondary_types=entry['secondary_types'],
            release_year=entry['release_year'],
            is_single_from=is_single_from,
            recording_score=entry['recording_score']
        )

        if song.recording_id not in songs:
            songs[song.recording_id] = []
        songs[song.recording_id].append(song)

    album_values = {}
    song_values = {}
    for recording_id, songs in songs.items():
        best_match = min(songs, key=lambda song: song.sort_key())

        if best_match.release_type == 2:
            is_single = 'TRUE'
        else:
            is_single = 'FALSE'

        album_values[best_match.release_group_id] = "({}, '{}', '{}', {}, {})".format(
            best_match.release_group_id,
            best_match.release_group_mb_id,
            best_match.release_group_name.replace("'", "''"),
            best_match.release_year,
            is_single
        )

        song_values[best_match.recording_id] = "({}, '{}', '{}', {}, {}, {}, {})".format(
            best_match.recording_id,
            best_match.recording_mb_id,
            best_match.title.replace("'", "''"),
            artist_id,
            best_match.release_group_id,
            best_match.is_single_from,
            best_match.recording_score
        )

    if len(album_values):
        insert_album = """
            INSERT INTO mb_album (id, mb_id, title, release_year, is_single)
            VALUES {}
            ON CONFLICT(id) DO UPDATE SET
             mb_id = EXCLUDED.mb_id, 
             title = EXCLUDED.title, 
             release_year = EXCLUDED.release_year,
             is_single = EXCLUDED.is_single;
        """.format(", ".join(album_values.values()))
        cursor.execute(insert_album)

    if len(song_values):
        insert_song = """
            INSERT INTO mb_song (id, mb_id, title, artist_id, album_id, is_single, score)
            VALUES {}
            ON CONFLICT(id) DO UPDATE SET
             mb_id = EXCLUDED.mb_id,
             title = EXCLUDED.title, 
             artist_id = EXCLUDED.artist_id,
             album_id = EXCLUDED.album_id,
             is_single = EXCLUDED.is_single,
             score = EXCLUDED.score;
        """.format(", ".join(song_values.values()))
        cursor.execute(insert_song)


try:
    with psycopg2.connect(
            host="ec2-54-93-206-123.eu-central-1.compute.amazonaws.com",
            database="musicbrainz_db",
            user="musicbrainz",
            password="musicbrainz"
    ) as conn:
        with conn.cursor() as cursor:
            for artist in query(cursor, "SELECT id, name FROM mb_artist ORDER BY score DESC;"):
                print(artist['name'])
                process_artist(cursor, artist['id'])
                conn.commit()
except psycopg2.DatabaseError as error:
    print("Error: {}".format(error))
