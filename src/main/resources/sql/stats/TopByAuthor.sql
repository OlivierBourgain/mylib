-- noinspection SqlNoDataSourceInspectionForFile

-- List # of books & # of pages read author
SELECT
  author,
  count(*)   AS nb,
  sum(pages) AS pages
FROM
  book
  LEFT JOIN reading ON book.id = reading.book_id
WHERE
  book.user_id = ?
  AND (? IS NULL OR reading.year = ?)
GROUP BY author