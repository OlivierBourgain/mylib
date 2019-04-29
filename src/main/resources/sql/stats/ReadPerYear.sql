-- noinspection SqlNoDataSourceInspectionForFile

-- List # of books & # of pages read per year
SELECT
  reading.year    AS year,
  count(*)        AS nb,
  sum(book.pages) AS pages
FROM
  reading
  INNER JOIN book ON book.id = reading.book_id
WHERE
  book.user_id = ?
  AND (? is NULL OR reading.year = ?)
GROUP BY reading.year
ORDER BY reading.year