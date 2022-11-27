package com.obourgain.mylib.ext.amazon;

import com.obourgain.mylib.util.img.FileStore;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Lookup d'un item sur Amazon.
 * <p>
 * Pour les images, voir: http://aaugh.com/imageabuse.html.
 * http://images.amazon.com/images/P/${isbn}.08.L.jpg --> height 490 ou 500 px
 */
public class ItemLookupAmazon {
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:66.0) Gecko/20100101 Firefox/66.0";
    private static final String AMAZON_IMG = "http://images.amazon.com/images/P/";
    private static final Logger log = LoggerFactory.getLogger(ItemLookupAmazon.class);


    /**
     * Gets the image from Amazon, and store result.
     */
    public static String saveImage(String isbn, char size) throws IOException {
        String url = AMAZON_IMG + isbn + ".08." + size + ".jpg";
        log.debug("Calling " + url);
        try {
            URLConnection connection = new URL(url).openConnection();
            connection.setRequestProperty("User-Agent", USER_AGENT);

            InputStream response = connection.getInputStream();
            byte[] bytes = IOUtils.toByteArray(response);
            // Don't store if it is less than 100 bytes.
            if (bytes.length < 100) return null;
            return FileStore.saveFile(isbn, size, bytes, "jpg");
        } catch (Exception e) {
            log.warn("Pb fetching image @amazon", e);
            return null;
        }
    }
}