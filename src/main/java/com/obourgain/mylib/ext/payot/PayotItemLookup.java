package com.obourgain.mylib.ext.payot;

import com.obourgain.mylib.ext.amazon.ItemLookupAmazon;
import com.obourgain.mylib.ext.isbndb.IsbnDbLookup;
import com.obourgain.mylib.util.ISBNConvertor;
import com.obourgain.mylib.vobj.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Lookup the detail of a book at payot.ch.
 *
 *
 */
public class PayotItemLookup {
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:66.0) Gecko/20100101 Firefox/66.0";
    private static final Logger log = LoggerFactory.getLogger(PayotItemLookup.class);

    public static Book lookup(String rawIsbn) {
        try {
            String isbn = ISBNConvertor.clean(rawIsbn);
            String isbn13 = isbn.length() == 10 ? ISBNConvertor.isbn10to13(isbn) : isbn;
            String html = fetchDocument(isbn13);
            Book book = new PayotHtmlParser(html).parseBook();
            // Payot doesn't have a good quality image of the book, going to amazon.
            book.setLargeImage(IsbnDbLookup.saveImage(isbn13));
            return book;
        } catch (Exception e) {
            log.error("Book not found", e);
            throw new RuntimeException("Error getting info from Payot");
        }
    }

    private static String fetchDocument(String isbn) throws Exception {
        String urlstr = "https://www.payot.ch/Detail/" + isbn + "?cId=1";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(urlstr))
                .header("user-agent", USER_AGENT)
                .header("Referrer-Policy","strict-origin-when-cross-origin")
                //.header("Accept-Encoding", "gzip, deflate")
                .header("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("DNT","1")
                .header("Upgrade-Insecure-Requests", "1")
                .GET()
                .build();

        log.info("Request {}", request.toString());
        log.info("User agent {}", request.headers());
        HttpResponse<String> response = HttpClient
                .newBuilder()
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());
        log.info("Response {}", response.toString());
        if (response.statusCode() != 200)
            throw new RuntimeException("HTTP error fetching URL " + response.statusCode());
        return response.body();
    }
}