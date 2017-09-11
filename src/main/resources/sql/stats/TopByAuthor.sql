SELECT
	author,
	count(*) as nb,
	sum(pages) as pages
FROM
	book
WHERE 
	user_id = ?
	and (status is null or status <> 'DISCARDED' or ? = 1)
GROUP BY author