SELECT
	tag.text as tag,
	count(*) as nb,
	sum(pages) as pages
FROM
	tag, book, book_tag
WHERE 
	tag.id = book_tag.tags_id
	and book.id = book_tag.books_id
	and book.user_id = ?
	and (book.status is null or book.status <> 'DISCARDED' or ? = 1)
GROUP BY tag.text