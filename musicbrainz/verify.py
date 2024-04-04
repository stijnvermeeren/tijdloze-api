from util import query, search_key
import psycopg2
import csv
import dataclasses


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
    matched_alias: str
    song_mb_id: str
    album_title: str
    album_mb_id: str
    release_year: int
    artist: str
    artist_mb_id: str
    country_id: str
    is_single_from: int
    is_single: int
    recording_score: int

    def is_exact_match(self, query):
        return search_key(self.matched_alias) == search_key(query)

    def relevance_for_query(self, query):
        if self.is_exact_match(query) and self.is_single_from:
            return 5 * self.recording_score

        if self.is_exact_match(query):
            # exact match
            return self.recording_score
        else:
            # e.g. "Hotellounge (Be the Death of Me)" instead of "Hotellounge"
            return self.recording_score / 10

def song_from_result(entry):
    return Song(
        title=entry['title'],
        matched_alias=entry['matched_alias'],
        song_mb_id=entry['song_mb_id'],
        artist=entry['name'],
        artist_mb_id=entry['artist_mb_id'],
        country_id=entry['country_id'],
        album_title=entry['album_title'],
        album_mb_id=entry['album_mb_id'],
        release_year=entry['release_year'],
        is_single_from=entry['single_relationship'],
        is_single=entry['is_single'],
        recording_score=entry['recording_score']
    )


def search(cursor, search_artist: str, search_title: str) -> Song:
    where = """
    ("mb_song_alias"."alias" LIKE '{}%') AND (
        LENGTH("mb_artist_alias"."alias") < 255 
        AND levenshtein_less_equal("mb_artist_alias"."alias", LOWER(REGEXP_REPLACE('{}', '\W', '', 'g')), 1) < 2
    )
    """.format(search_key(search_title), search_key(search_artist))

    where2 = """
    (
        LENGTH("mb_song_alias"."alias") < 255 AND levenshtein_less_equal("mb_song_alias"."alias", '{}', 1) < 2
    ) AND (
        LENGTH("mb_artist_alias"."alias") < 255 
        AND levenshtein_less_equal("mb_artist_alias"."alias", LOWER(REGEXP_REPLACE('{}', '\W', '', 'g')), 1) < 2
    )
    """.format(search_key(search_title), search_key(search_artist))

    recordings_query_template = """
        SELECT
           mb_song.mb_id as song_mb_id,
           mb_song_alias.alias as matched_alias,
           mb_song.title,
           mb_song.is_single AS single_relationship,
           mb_song.score AS recording_score,
           mb_album.title as album_title,
           mb_album.release_year,
           mb_album.is_single,
           mb_album.mb_id as album_mb_id,
           mb_artist.name,
           mb_artist.mb_id as artist_mb_id,
           mb_artist.country_id
        FROM "mb_song"
        JOIN "mb_song_alias" ON "mb_song"."id" = "mb_song_alias"."song_id"
        JOIN "mb_album" ON "mb_album"."id" = "mb_song"."album_id"
        JOIN "mb_artist" ON "mb_artist"."id" = "mb_song"."artist_id"
        JOIN "mb_artist_alias" ON "mb_artist"."id" = "mb_artist_alias"."artist_id"
        WHERE {}
    """

    recordings_query = recordings_query_template.format(where)
    songs = [song_from_result(entry) for entry in query(cursor, recordings_query)]

    if not len(songs):
        recordings_query = recordings_query_template.format(where2)
        songs = [song_from_result(entry) for entry in query(cursor, recordings_query)]

    if len(songs):
        min_relevance = max([song.relevance_for_query(search_title) for song in songs]) / 2
        best_match = max(
            [song for song in songs if song.relevance_for_query(search_title) >= min_relevance],
            key=lambda song: (-song.release_year, song.relevance_for_query(search_title))
        )
        return best_match

def process_song(cursor, row):
    if row["artist2_name"]:
        artist_name = "{} & {}".format(row["artist_name"], row["artist2_name"])
    else:
        artist_name = row["artist_name"]
    title = row["title"]
    print()
    print("{} - {}".format(artist_name, title))

    song = search(cursor, artist_name, title)

    artist_db = "{} {} ({})".format(row["artist_musicbrainz_id"], row["artist_name"], row["artist_country_id"])
    print("DB: {}".format(artist_db))
    if song is not None:
        artist_mb = "{} {} ({})".format(
            song.artist_mb_id,
            clean(song.artist),
            song.country_id
        )
        if artist_db == artist_mb:
            print("MB: ok")
        else:
            print("MB: {}".format(artist_mb))

    album_db = "{} {} ({})".format(row["musicbrainz_id"], row["album_title"], row["release_year"])
    print("DB: {}".format(album_db))
    if song is not None:
        album_title = clean(song.album_title)
        if song.is_single:
            album_title += " (single)"
        album_mb = "{} {} ({})".format(song.album_mb_id, album_title, song.release_year)
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
