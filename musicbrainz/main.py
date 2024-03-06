import psycopg2
from collections import Counter


def query(cursor, query):
    cursor.execute(query)
    cols = list(map(lambda x: x[0], cursor.description))
    return (
        {col: value for col, value in zip(cols, data)}
        for data in cursor.fetchall()
    )


class Song:
    def __init__(self, title, country_id, recording_id, recording_artist, release_id, release_group_id, release_group_name, release_type, release_secondary_types, release_year, is_single_from, artist_score, work_score):
        self.title = title
        self.country_id = country_id
        self.recording_id = recording_id
        self.recording_artist = recording_artist
        self.release_id = release_id
        self.release_group_id = release_group_id
        self.release_group_name = release_group_name
        self.release_type = release_type
        self.release_secondary_types = release_secondary_types
        self.release_year = release_year
        self.is_single_from = is_single_from
        self.artist_score = artist_score
        self.work_score = work_score

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


try:
    with psycopg2.connect(
        host="ec2-54-93-206-123.eu-central-1.compute.amazonaws.com",
        database="musicbrainz_db",
        user="musicbrainz",
        password="musicbrainz"
    ) as conn:
        with conn.cursor() as cursor:
            # where = """LENGTH("artist"."name") < 255 AND levenshtein_less_equal("artist"."name", 'The Jimmi Hendrix Experience', 1) < 2"""
            where = """"artist"."name" LIKE '%Jimi Hendrix%'"""

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
                   COALESCE(iso_3166_1.code, SUBSTRING(iso_3166_2.code, 1, 2)) as country_id, 
                   release_group.gid as release_group_id, 
                   release_group.name as release_group_name,
                   release_group.type as release_type,
                   release.gid as release_id,
                   (SELECT MIN(date_year) FROM "release_country" WHERE release_country.release = release.id) as release_year,
                   (SELECT array_agg(secondary_type) FROM release_group_secondary_type_join WHERE release_group_secondary_type_join.release_group = release_group.id) as secondary_types,
                   "recording"."gid" as recording_id,
                   "recording"."name" as recording_name,
                   recording_artist_credit."name" as recording_artist_name,
                   (SELECT COUNT(*) FROM "l_artist_url" WHERE "entity0" = "artist"."id") as artist_score,
                   (SELECT COUNT(*) FROM "l_recording_work" WHERE "entity1" = "work"."id") as work_score
                FROM "artist"
                LEFT JOIN "area" ON "area"."id" = "artist"."area"
                LEFT JOIN "iso_3166_1" ON "area"."id" = "iso_3166_1"."area"
                LEFT JOIN "iso_3166_2" ON "area"."id" = "iso_3166_2"."area"
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
                    recording_artist=entry['recording_artist_name'],
                    release_group_id=release_group_id,
                    release_group_name=entry['release_group_name'],
                    release_type=entry['release_type'],
                    release_secondary_types=entry['secondary_types'],
                    release_year=entry['release_year'],
                    is_single_from=is_single_from,
                    artist_score=entry['artist_score'],
                    work_score=entry['work_score']
                )

                if song.release_group_id not in release_group_release:
                    release_group_release[song.release_group_id] = set()
                release_group_release[song.release_group_id].add(song.release_id)

                if song.release_group_id not in recording_title_count:
                    recording_title_count[song.release_group_id] = Counter()
                recording_title_count[song.release_group_id].update([song.title])

                songs.append(song)

            song_data = {}
            for song in songs:
                # song title found in at least 50% of all recordings of the release_group
                if recording_title_count[song.release_group_id][song.title] >= 0.5 * len(release_group_release[song.release_group_id]):
                    if song.title not in song_data or song_data[song.title].overridden_by(song):
                        song_data[song.title] = song

            for song in sorted(
                    song_data.values(),
                    key=lambda song: (song.artist_score, song.work_score), reverse=True
            ):
                print("{} {}: {} ({}) - {} {} {} ({}, {}, {})".format(
                    song.artist_score,
                    song.work_score,
                    song.recording_artist,
                    song.country_id,
                    song.title,
                    song.recording_id,
                    song.release_group_id,
                    song.release_group_name,
                    song.release_year,
                    song.release_type,
                    song.release_secondary_types
                ))
except psycopg2.DatabaseError as error:
    print("Error: {}".format(error))

