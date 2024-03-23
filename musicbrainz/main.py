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
    return re.sub(r'[^A-Za-z0-9 ]+', '', value.lower())

def clean(value: str) -> str:
    return (value
            .replace("…", "...")
            .replace("’", "'")
            .replace("“", "\"")
            .replace("”", "\"")
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

    def priority(self):
        is_single_from_term = 0 if self.is_single_from else 10

        if self.is_main_album():
            return is_single_from_term + 1
        else:
            return is_single_from_term + 2

    def overridden_by(self, other_song):
        if self.release_year:
            if other_song.release_year:
                if self.priority() < other_song.priority():
                    # prefer album to single, even if the single is a year earlier
                    return other_song.release_year < self.release_year - 1
                if other_song.priority() < self.priority():
                    # prefer album to single, even if the single is a year earlier
                    return other_song.release_year < self.release_year + 2
                else:
                    return other_song.release_year < self.release_year
            else:
                return False
        else:
            return other_song.release_year is not None


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

    where = """"artist"."id" IN ({})""".format(",".join([str(id) for id in artist_ids]))
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
           recording_artist_credit."name" as recording_artist_name,
           (SELECT COUNT(*) FROM "l_artist_url" WHERE "entity0" = "artist"."id") as artist_score,
           (SELECT COUNT(*) FROM "release" r2 JOIN "medium" m2 ON m2."release" = r2."id" JOIN "track" t2 ON t2."medium" = m2."id" WHERE t2."recording" = "recording"."id") as recording_score,
           (SELECT COUNT(*) FROM "l_recording_work" WHERE "entity1" = "work"."id") as work_score
        FROM "artist"
        LEFT JOIN "stijn_area_country_id" ON "stijn_area_country_id"."area_id" = "artist"."area"
        JOIN "artist_credit_name" ON "artist_credit_name"."artist" = "artist"."id"
        JOIN "artist_credit" ON "artist_credit"."id" = "artist_credit_name"."artist_credit"
        JOIN "release_group" ON "release_group"."artist_credit" = "artist_credit"."id"
        JOIN "release" ON "release"."release_group" = "release_group"."id"
        JOIN "medium" ON "medium"."release" = "release"."id"
        JOIN "track" ON "track"."medium" = "medium"."id"
        JOIN "recording" ON "recording"."id" = "track"."recording"
        JOIN "artist_credit" recording_artist_credit ON recording_artist_credit.id = "recording"."artist_credit"
        LEFT JOIN "l_recording_work" ON "l_recording_work"."entity0" = "recording"."id"
        LEFT JOIN "work" ON "work"."id" = "l_recording_work"."entity1" 
        WHERE {} AND "release"."status" = 1  -- official
    """.format(where)
    songs = []
    release_group_release = {}
    recording_title_count = {}
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

        if song.release_group_id not in recording_title_count:
            recording_title_count[song.release_group_id] = Counter()
        recording_title_count[song.release_group_id].update([song.title])

        songs.append(song)

        if entry['work_name'] != song.title:
            song2 = dataclasses.replace(song)
            song2.title = entry['work_name']
            songs.append(song2)

    song_data = {}
    for song in songs:
        # song title found in at least 50% of all recordings of the release_group
        if recording_title_count[song.release_group_id][song.title] >= 0.5 * len(release_group_release[song.release_group_id]):
            key = search_key(song.title)
            if key not in song_data or song_data[key].overridden_by(song):
                song_data[key] = song

    # for song in sorted(
    #         song_data.values(),
    #         key=lambda song: (song.recording_score, song.artist_score), reverse=True
    # ):
    #     print("{} {}: {} ({}) - {} {} {} ({}, {}, {})".format(
    #         song.artist_score,
    #         song.recording_score,
    #         song.recording_artist,
    #         song.country_id,
    #         song.title,
    #         song.recording_id,
    #         song.release_group_id,
    #         song.release_group_name,
    #         song.release_year,
    #         song.release_type,
    #         song.release_secondary_types
    #     ))

    key = search_key(search_title)
    if key in song_data:
        return song_data[key]

def process_song(cursor, row):
    if row["artist2_name"]:
        artist_name = "{} & {}".format(row["artist_name"], row["artist2_name"])
    else:
        artist_name = row["artist_name"]
    title = row["title"]
    print()
    print("{} - {}".format(artist_name, title))

    artist_ids = search_artist(cursor, artist_name)
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
                    process_song(cursor, row)
except psycopg2.DatabaseError as error:
    print("Error: {}".format(error))
