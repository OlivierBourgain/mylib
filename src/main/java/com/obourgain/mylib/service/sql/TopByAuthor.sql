SELECT
	author,
	count(*) as nb,
	sum(pages) as pages
FROM
	book
WHERE 
	user_id = ?
GROUP BY author