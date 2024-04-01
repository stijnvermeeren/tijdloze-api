import re


def query(cursor, query):
    cursor.execute(query)
    cols = list(map(lambda x: x[0], cursor.description))
    return (
        {col: value for col, value in zip(cols, data)}
        for data in cursor.fetchall()
    )


def search_key(value: str) -> str:
    # TODO: when "(live)" in title, then restrict search to live albums
    return re.sub(r'[^a-zA-Z0-9]+', '', value.lower().replace("(live)", ""))
