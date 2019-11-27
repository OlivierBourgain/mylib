package com.obourgain.mylib.ext.amazon;

import com.obourgain.mylib.vobj.Book;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class ItemLookupAmazonTest {

    @Test
    public void parseHtmlPage() throws IOException {
        InputStream is = ItemLookupAmazon.class.getResourceAsStream("/amazon/hurlevent.html");
        String html = IOUtils.toString(is, StandardCharsets.UTF_8);
        Document doc = Jsoup.parse(html);
        Book book = ItemLookupAmazon.parseHtmlPage("2070446050", doc);
        assertEquals("Les Hauts de Hurle-Vent", book.getTitle());
        assertEquals("Emily Brontë", book.getAuthor());
        assertEquals("Le Livre de Poche", book.getPublisher());
        assertEquals(413, book.getPages());
        assertEquals("1 juillet 1974",book.getPublicationDate());
        assertEquals("fr", book.getLang());
        assertEquals("978-2253004752", book.getIsbn());

    }
}