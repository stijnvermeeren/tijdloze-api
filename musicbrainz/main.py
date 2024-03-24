import psycopg2
from collections import Counter
import csv
import re
import dataclasses


def query(cursor, query):
    cursor.execute(query)
    cols = list(map(lambda x: x[0], cursor.description))
    return (
        {col: value for col, value in zip(cols, data)}
        for data in cursor.fetchall()
    )


def search_key(value: str) -> str:
    return re.sub(r'[^a-zA-Z0-9]+', '', value.lower())

def clean(value: str) -> str:
    return (value
            .replace("…", "...")
            .replace("’", "'")
            .replace("“", "\"")
            .replace("”", "\"")
            .replace("‐", "-")
            )


@dataclasses.dataclass
class Song:
    title: str
    country_id: str
    recording_id: str
    recording_artist_id: str
    recording_artist: str
    release_id: str
    release_group_id: str
    release_group_name: str
    release_type: str
    release_secondary_types: str
    release_year: int
    is_single_from: int
    artist_score: int
    recording_score: int
    work_score: int

    def is_main_album(self):
        return self.release_type == 1 and not self.release_secondary_types

    def is_compilation_album(self):
        return self.release_type == 1 and 1 in self.release_secondary_types

    def is_soundtrack_album(self):
        return self.release_type == 1 and 2 in self.release_secondary_types

    def relevance_for_query(self, query):
        if search_key(self.title) == search_key(query):
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


def search_artist(cursor, artist: str) -> list[int]:
    artist_query = """
        SELECT
            artist_id 
        FROM "stijn_artist"
        WHERE LENGTH("stijn_artist"."name") < 255 
        AND levenshtein_less_equal("stijn_artist"."name", LOWER(REGEXP_REPLACE('{}', '\W', '', 'g')), 1) < 2
    """.format(artist.replace("'", "''"))

    return [entry["artist_id"] for entry in query(cursor, artist_query)]


def search(cursor, artist_ids: list[int], search_title: str) -> Song:
    if not len(artist_ids):
        return None

    where = """("artist"."id" IN ({}))""".format(",".join([str(id) for id in artist_ids]))
    singlesQuery = """
        SELECT
            release_group.name AS title,
            release_group_album."gid" AS album_id 
        FROM "artist"
        JOIN "artist_credit_name" ON "artist_credit_name"."artist" = "artist"."id"
        JOIN "artist_credit" ON "artist_credit"."id" = "artist_credit_name"."artist_credit"
        JOIN "release_group" ON "release_group"."artist_credit" = "artist_credit"."id"
        JOIN "l_release_group_release_group" ON "l_release_group_release_group"."entity0" = "release_group".id
        JOIN "link" ON "link"."id" = "l_release_group_release_group"."link"
        JOIN "release_group" AS release_group_album ON release_group_album.id = "l_release_group_release_group"."entity1"
        WHERE {} AND "link"."link_type" = 11  -- "single_from"
    """.format(where)
    single_from_relations = {}
    for entry in query(cursor, singlesQuery):
        single_title = entry["title"]
        if single_title not in single_from_relations:
            single_from_relations[single_title] = set()
        single_from_relations[single_title].add(entry['album_id'])

    where = """
    ("stijn_recording"."artist_id" IN ({}) AND "stijn_recording"."name" LIKE '{}%')
    """.format(",".join([str(id) for id in artist_ids]), search_key(search_title))

    recordingsQuery = """
        SELECT
           artist.gid,
           artist.name,
           stijn_area_country_id.country_id as country_id, 
           release_group.gid as release_group_id, 
           release_group.name as release_group_name,
           release_group.type as release_type,
           release.gid as release_id,
           (SELECT MIN(date_year) FROM "release_country" WHERE release_country.release = release.id) as release_year,
           (SELECT array_agg(secondary_type) FROM release_group_secondary_type_join WHERE release_group_secondary_type_join.release_group = release_group.id) as secondary_types,
           "recording"."gid" as recording_id,
           "recording"."name" as recording_name,
           "work"."name" as work_name,
           "artist_credit"."name" as recording_artist_name,
           (SELECT COUNT(*) FROM "l_artist_url" WHERE "entity0" = "artist"."id") as artist_score,
           (SELECT COUNT(*) FROM "release" r2 JOIN "medium" m2 ON m2."release" = r2."id" JOIN "track" t2 ON t2."medium" = m2."id" WHERE t2."recording" = "recording"."id") as recording_score,
           (SELECT COUNT(*) FROM "l_recording_work" WHERE "entity1" = "work"."id") as work_score
        FROM "stijn_recording"
        JOIN "artist" ON "artist"."id" = "stijn_recording"."artist_id"
        LEFT JOIN "stijn_area_country_id" ON "stijn_area_country_id"."area_id" = "artist"."area"
        JOIN "recording" ON "recording"."id" = "stijn_recording"."recording_id"
        JOIN "track" ON "recording"."id" = "track"."recording"
        JOIN "medium" ON "track"."medium" = "medium"."id" 
        JOIN "release" ON "medium"."release" = "release"."id"
        JOIN "release_group" ON "release"."release_group" = "release_group"."id"
        JOIN "artist_credit" AS artist_credit_rg ON artist_credit_rg.id = "release_group"."artist_credit"
        JOIN "artist_credit_name" ON "artist_credit_name"."artist_credit" = artist_credit_rg."id" AND "artist_credit_name"."artist" = "artist"."id"
        JOIN "artist_credit" ON "artist_credit".id = "recording"."artist_credit"
        LEFT JOIN "l_recording_work" ON "l_recording_work"."entity0" = "recording"."id"
        LEFT JOIN "work" ON "work"."id" = "l_recording_work"."entity1"
        WHERE {} AND "release"."status" = 1  -- official
    """.format(where)
    songs = []
    release_group_release = {}
    for entry in query(cursor, recordingsQuery):
        title = entry['recording_name']
        release_group_id = entry['release_group_id']
        is_single_from = title in single_from_relations and release_group_id in single_from_relations[title]
        country_id = entry['country_id'].lower() if entry['country_id'] else None

        song = Song(
            title=title,
            country_id=country_id,
            recording_id=entry['recording_id'],
            release_id=entry['release_id'],
            recording_artist_id=entry['gid'],
            recording_artist=entry['recording_artist_name'],
            release_group_id=release_group_id,
            release_group_name=entry['release_group_name'],
            release_type=entry['release_type'],
            release_secondary_types=entry['secondary_types'],
            release_year=entry['release_year'],
            is_single_from=is_single_from,
            artist_score=entry['artist_score'],
            recording_score=entry['recording_score'],
            work_score=entry['work_score']
        )

        if song.release_group_id not in release_group_release:
            release_group_release[song.release_group_id] = set()
        release_group_release[song.release_group_id].add(song.release_id)

        songs.append(song)

        if entry['work_name'] is not None and entry['work_name'] != song.title:
            song2 = dataclasses.replace(song)
            song2.title = entry['work_name']
            songs.append(song2)

    if len(songs):
        max_recording_score = max([song.relevance_for_query(search_title) for song in songs])
        # ignore obsure early demo's of a common song (e.g. Evanescence - My Immortal)
        filtered_songs = [
            song
            for song in songs
            if song.relevance_for_query(search_title) > max_recording_score / 10 or song.is_single_from
        ]

        # scored = sorted(filtered_songs, key=lambda song: song.sort_key())
        # for song in scored:
        #     print(song.sort_key(), song)

        best_match = min(filtered_songs, key=lambda song: song.sort_key())
        return best_match

def process_song(cursor, row):
    if row["artist2_name"]:
        artist_name = "{} & {}".format(row["artist_name"], row["artist2_name"])
    else:
        artist_name = row["artist_name"]
    title = row["title"]
    print()
    print("{} - {}".format(artist_name, title))

    artist_ids = search_artist(cursor, row["artist_name"])
    song = search(cursor, artist_ids, title)

    artist_db = "{} {} ({})".format(row["artist_musicbrainz_id"], row["artist_name"], row["artist_country_id"])
    print("DB: {}".format(artist_db))
    if song is not None:
        artist_mb = "{} {} ({})".format(
            song.recording_artist_id,
            clean(song.recording_artist),
            song.country_id
        )
        if artist_db == artist_mb:
            print("MB: ok")
        else:
            print("MB: {}".format(artist_mb))

    album_db = "{} {} ({})".format(row["musicbrainz_id"], row["album_title"], row["release_year"])
    print("DB: {}".format(album_db))
    if song is not None:
        album_title = clean(song.release_group_name)
        if song.release_type == 2:
            album_title += " (single)"
        album_mb = "{} {} ({})".format(song.release_group_id, album_title, song.release_year)
        if album_db == album_mb:
            print("MB: ok")
        else:
            print("MB: {}".format(album_mb))



try:
    with psycopg2.connect(
            host="ec2-54-93-206-123.eu-central-1.compute.amazonaws.com",
            database="musicbrainz_db",
            user="musicbrainz",
            password="musicbrainz"
    ) as conn:
        with conn.cursor() as cursor:
            with open('tijdlozedb.csv', newline='') as csvfile:
                reader = csv.DictReader(csvfile)
                for row in reader:
                    if row['title'] != 'Hotellounge':
                        pass
                    process_song(cursor, row)
except psycopg2.DatabaseError as error:
    print("Error: {}".format(error))
