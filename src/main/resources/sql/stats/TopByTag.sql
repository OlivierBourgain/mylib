-- List # of books & # of pages read author
SELECT
  tag.text   AS tag,
  count(*)   AS nb,
  sum(pages) AS pages
FROM
  tag
  LEFT JOIN book_tag ON tag.id = book_tag.tags_id
  LEFT JOIN book ON book.id = book_tag.books_id
WHERE
  book.user_id = ?
  AND (book.status IS NULL OR book.status <> 'DISCARDED' OR ? = 1)
GROUP BY tag.text