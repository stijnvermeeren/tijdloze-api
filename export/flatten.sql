SET @sql = NULL;
SET SESSION group_concat_max_len = 1000000;

SELECT
  GROUP_CONCAT(DISTINCT
    CONCAT(
      'IFNULL((SELECT position FROM list_entry WHERE song_id = s.id AND year = ',
      year,
      '), "") as position',
      year
    )
  ) INTO @sql
FROM
  (SELECT DISTINCT year FROM list_entry ORDER BY year ASC) as years;
SET @sql = CONCAT('SELECT s.id as song_id, s.title as song_title, a.id as artist_id, IFNULL(a.name_prefix, "") as name_prefix, a.name as name, IFNULL(a2.id, "") as second_artist_id, IFNULL(a2.name_prefix, "") as second_name_prefix, IFNULL(a2.name, "") as second_name, c.id as album_id, c.title as album_title, c.release_year as release_year, ', @sql, ' FROM song s JOIN artist a ON s.artist_id = a.id LEFT JOIN artist a2 ON s.second_artist_id = a2.id JOIN album c ON s.album_id = c.id');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
