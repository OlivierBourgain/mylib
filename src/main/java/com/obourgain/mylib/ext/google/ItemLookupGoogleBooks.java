package com.obourgain.mylib.ext.google;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.obourgain.mylib.vobj.Book;


/**
 * Interface with the Google books API.
 * Non utilisée pour l'instant.
 */
public class ItemLookupGoogleBooks {
	private static final Logger log = LogManager.getLogger(ItemLookupGoogleBooks.class);
	private static final String URL = "https://www.googleapis.com/books/v1/volumes?q=isbn:";

	public static void main(String[] args) {

		lookup("1617292540");
	}

	public static Book lookup(String isbn) {
		try {
			String url = getLookupUrl(isbn);
			log.info("Calling google @ " +url);

			String response = callGoogle(url);
			return parseResponse(isbn, response);

		} catch (Exception e) {
			throw new RuntimeException("Erreur fetching this book", e);
		}

	}

	private static Book parseResponse(String searchisbn, String response) {
		JSONObject obj = new JSONObject(response);
		int nbResults = obj.getInt("totalItems");
		System.out.println("Nb results " + nbResults);
		if (nbResults == 0) return null; 
		
		Book book = new Book();
		JSONArray items = obj.getJSONArray("items");
		JSONObject volumeInfo = items.getJSONObject(0).getJSONObject("volumeInfo");

		String title = volumeInfo.optString("title");
		String publisher = volumeInfo.optString("publisher");
		String lang = volumeInfo.optString("language");
		int pages = volumeInfo.getInt("pageCount");
		String description = volumeInfo.optString("description");
		String googleLink = volumeInfo.optString("infoLink");

		String publicationDate = volumeInfo.getString("publishedDate");
		// Fix publication date '2014-10-08T00:00:00+02:00'
		if (publicationDate.contains("T00:00:00")) {
			int idx = publicationDate.indexOf('T');
			publicationDate = publicationDate.substring(0, idx);
		}

		JSONArray industryIdentifiers = volumeInfo.getJSONArray("industryIdentifiers");
		String isbn = null;
		for (int i = 0; i < industryIdentifiers.length(); i++) {
			String type = industryIdentifiers.getJSONObject(i).getString("type");
			// We take ISBN_13 if it exits, and ISBN_10 as a backup.
			if (isbn == null && "ISBN_10".equals(type))
				isbn = industryIdentifiers.getJSONObject(i).getString("identifier");
			if ("ISBN_13".equals(type))
				isbn = industryIdentifiers.getJSONObject(i).getString("identifier");
		}
		// Sometimes the ISBN is not in the response (https://www.googleapis.com/books/v1/volumes?q=isbn:9782070409341)
		if (isbn == null) isbn = searchisbn;

		String smallImage = null;
		String mediumImage = null;
		if (volumeInfo.optJSONObject("imageLinks") != null) {
			smallImage = volumeInfo.getJSONObject("imageLinks").optString("smallThumbnail");
			mediumImage = volumeInfo.getJSONObject("imageLinks").optString("thumbnail");
		}

		StringBuilder auth = new StringBuilder();
		JSONArray authors = volumeInfo.getJSONArray("authors");
		authors.forEach(t -> auth.append(", ").append(t));
		String author = auth.toString().substring(2);

		book.setTitle(title);
		book.setAuthor(author);
		book.setPublisher(publisher);
		book.setPublicationDate(publicationDate);
		book.setPages(pages);
		book.setSmallImage(smallImage);
		book.setMediumImage(mediumImage);
		book.setIsbn(isbn);
		book.setLang(lang);
		book.setDescription(description);
		book.setGoogleURL(googleLink);
		return book;
	}

	private static String callGoogle(String url) throws Exception {
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer res = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			res.append(inputLine);
		}
		in.close();

		if (responseCode != 200) {
			System.out.println("Bad response code " + responseCode);
			System.err.println(res);
			return "";
		}

		return res.toString();
	}

	private static String getLookupUrl(String isbn) {
		return URL + isbn;
	}

}
