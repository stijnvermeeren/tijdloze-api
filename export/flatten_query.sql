with subquery as (
    SELECT
        string_agg(DISTINCT
	    CONCAT(
	      'coalesce((SELECT min(position) FROM list_entry WHERE song_id = s.id AND year = ',
	      year,
	      ')::text, '''') as position',
	      year
	    )
	  , E',\n  ') as value
    FROM
        (SELECT DISTINCT year FROM list_entry ORDER BY year ASC) as years
)
select '
COPY (
select
  s.id as song_id,
  s.title as song_title,
  a.id as artist_id,
  a.name as name,
  coalesce(a2.id::text, '''') as second_artist_id,
  coalesce(a2.name, '''') as second_name,
  c.id as album_id,
  c.title as album_title,
  c.release_year as release_year,
  ' || subquery.value || '
FROM song s
JOIN artist a ON s.artist_id = a.id
LEFT JOIN artist a2 ON s.second_artist_id = a2.id
JOIN album c ON s.album_id = c.id
) TO STDOUT CSV HEADER DELIMITER E''\t'';' as query from subquery;
