-- List # of books & # of pages read per month
SELECT
  reading.month   AS month,
  count(*)        AS nb,
  sum(book.pages) AS pages
FROM
  reading
  INNER JOIN book ON book.id = reading.book_id
WHERE
  book.user_id = ?
GROUP BY reading.month
ORDER BY reading.month