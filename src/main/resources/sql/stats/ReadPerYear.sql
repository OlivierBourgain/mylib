SELECT
	reading.year as year,
	count(*) as nb,
	sum(book.pages) as pages
FROM
	reading, book
WHERE 
	book.id = reading.book_id
	and book.user_id = ?
GROUP BY reading.year
ORDER BY reading.year