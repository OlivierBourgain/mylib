-- List # of books & # of pages read author
SELECT
  author,
  count(*)   AS nb,
  sum(pages) AS pages
FROM
  book
WHERE
  user_id = ?
  AND (status IS NULL OR status <> 'DISCARDED' OR ? = 1)
GROUP BY author