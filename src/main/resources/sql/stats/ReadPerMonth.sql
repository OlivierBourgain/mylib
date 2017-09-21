SELECT
	reading.month,
	count(*) as nb,
	sum(book.pages) as pages
FROM
	reading, book
WHERE 
	book.id = reading.book_id
	and book.user_id = ?
GROUP BY reading.month
ORDER BY reading.month