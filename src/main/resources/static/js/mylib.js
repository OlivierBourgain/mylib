$(function() {
				
	/**
	 * Table pagination - Selection of a new page size.
	 */
	$("#pagination-size").change(function() {
		var newSize = $("#pagination-size").val()
		// When size is changed, reset page number to 0
		var newUrl = URI(window.location.href)
					.setSearch("size", newSize)
					.setSearch("page", 0)
					.toString()
			
		window.location.href = newUrl
	});
	
	/**
	 * Table pagination - Select a page number.
	 */
	$("li.page-item a").click(function() {
		var page = $(this).attr("data-page")
		var newUrl = URI(window.location.href)
					.setSearch("page", page)
					.toString()
		
		window.location.href = newUrl
	});
	
	/**
	 * Click on a Tag adds a filter criteria.
	 */
	$("span.tag").click(function() {
		var tagtext = $(this).text();
		var newUrl = URI(window.location.href)
		.setSearch("criteria", tagtext.replace(/[^A-zÀ-ÿ0-9-_\s]/g, ''))
		.toString()
		window.location.href = newUrl
	});
	
	/** 
	 * Table header - Click to sort
	 */
	$("th a").click(function() {
		var col = $(this).attr("data-col")
		
		var cursort = URI(window.location.href).query(true).sort
		var newsort = col
		if (cursort == col) newsort = col + ",DESC"
		var newUrl = URI(window.location.href)
					.setSearch("sort", newsort)
					.toString()
		
		window.location.href = newUrl
	});
	
	/**
	 * Table pagination - Remove the filter.
	 */
	$("a#removeFilter").click(function() {
		var newUrl = URI(window.location.href)
					.setSearch("criteria", "")
					.toString()
		
		window.location.href = newUrl
	});
});