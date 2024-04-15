package com.obourgain.mylib.ext.isbndb;

import com.obourgain.mylib.ext.payot.PayotItemLookup;
import com.obourgain.mylib.util.img.FileStore;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class IsbnDbLookup {
    private final static String ISBN_DB_URL = "https://images.isbndb.com/covers/";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:66.0) Gecko/20100101 Firefox/66.0";
    private static final Logger log = LoggerFactory.getLogger(IsbnDbLookup.class);

    /**
     * Gets the image from IsbnDbLookup, and store result.
     */
    public static String saveImage(String isbn) throws IOException {
        String url = ISBN_DB_URL + isbn.substring(9, 11) + "/" + isbn.substring(11, 13) + "/" + isbn + ".jpg";
        log.info("Fetching image isbndb " + url);
        try {
            URLConnection connection = new URL(url).openConnection();
            connection.setRequestProperty("User-Agent", USER_AGENT);

            InputStream response = connection.getInputStream();
            byte[] bytes = IOUtils.toByteArray(response);
            // Don't store if it is less than 100 bytes.
            if (bytes.length < 100) {
                log.warn("Isbn image received too small, size=" + bytes.length);
                log.debug(response.toString());
                log.debug(new String(bytes));
                return null;
            }
            return FileStore.saveFile(isbn, 'L', bytes, "jpg");
        } catch (Exception e) {
            log.warn("Pb fetching image @isbndb", e);
            return null;
        }
    }
}
