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
    def __init__(self, title, recording_id, recording_artist, release_id, release_group_id, release_group_name, release_type, release_year):
        self.title = title
        self.recording_id = recording_id
        self.recording_artist = recording_artist
        self.release_id = release_id
        self.release_group_id = release_group_id
        self.release_group_name = release_group_name
        self.release_type = release_type
        self.release_year = release_year

    def overridden_by(self, other_song):
        if self.release_year:
            if other_song.release_year:
                if self.release_type == 1 and other_song.release_type != 1:
                    # prefer album to single, even if the single is a year earlier
                    return other_song.release_year < self.release_year - 1
                if self.release_type != 1 and other_song.release_type == 1:
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
            deusQuery = """
                SELECT
                   artist.gid, artist.name, 
                   release_group.gid as release_group_id, 
                   release_group.name as release_group_name,
                   release_group.type as release_type,
                   release.gid as release_id,
                   (SELECT MIN(date_year) FROM "release_country" WHERE release_country.release = release.id) as release_year,
                   "recording"."gid" as recording_id,
                   "recording"."name" as recording_name,
                   recording_artist_credit."name" as recording_artist_name 
                FROM "artist"
                JOIN "artist_credit_name" ON "artist_credit_name"."artist" = "artist"."id"
                JOIN "artist_credit" ON "artist_credit"."id" = "artist_credit_name"."artist_credit"
                JOIN "release_group" ON "release_group"."artist_credit" = "artist_credit"."id"
                JOIN "release" ON "release"."release_group" = "release_group"."id"
                JOIN "medium" ON "medium"."release" = "release"."id"
                JOIN "track" ON "track"."medium" = "medium"."id"
                JOIN "recording" ON "recording"."id" = "track"."recording"
                JOIN "artist_credit" recording_artist_credit ON recording_artist_credit.id = "recording"."artist_credit"
                WHERE "artist"."name" = 'Oasis' AND "release"."status" = 1  -- official
            """
            songs = []
            release_group_release = {}
            recording_title_count = {}
            for entry in query(cursor, deusQuery):
                song = Song(
                    title=entry['recording_name'],
                    recording_id=entry['recording_id'],
                    release_id=entry['release_id'],
                    recording_artist=entry['recording_artist_name'],
                    release_group_id=entry['release_group_id'],
                    release_group_name=entry['release_group_name'],
                    release_type=entry['release_type'],
                    release_year=entry['release_year']
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

            for title, song in song_data.items():
                print("{} {} {} {} {} ({}, {})".format(song.title, song.recording_id, song.recording_artist, song.release_group_id, song.release_group_name, song.release_year, song.release_type))
except psycopg2.DatabaseError as error:
    print("Error: {}".format(error))

