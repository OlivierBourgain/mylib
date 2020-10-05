package com.obourgain.mylib.ext.amazon;

import com.obourgain.mylib.vobj.Book;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class ItemLookupAmazonTest {

    @Test
    public void parseHtmlPageHurlevent() throws IOException {
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

    @Test
    public void parseHtmlPageUserStory() throws IOException {
        InputStream is = ItemLookupAmazon.class.getResourceAsStream("/amazon/1491904909.html");
        String html = IOUtils.toString(is, StandardCharsets.UTF_8);
        Document doc = Jsoup.parse(html);
        Book book = ItemLookupAmazon.parseHtmlPage("1491904909", doc);
        assertEquals("User Story Mapping", book.getTitle());
        assertEquals("Jeff Patton, Peter Economy, Martin Fowler, Marty Cagan, Alan Cooper", book.getAuthor());
        assertEquals("O′Reilly", book.getPublisher());
        assertEquals(324, book.getPages());
        assertEquals("26 septembre 2014",book.getPublicationDate());
        assertEquals("en", book.getLang());
        assertEquals("978-1491904909", book.getIsbn());
    }

    @Test
    public void parseHtmlPageMoustique() throws IOException {
        InputStream is = ItemLookupAmazon.class.getResourceAsStream("/amazon/225307389X.html");
        String html = IOUtils.toString(is, StandardCharsets.UTF_8);
        Document doc = Jsoup.parse(html);
        Book book = ItemLookupAmazon.parseHtmlPage("225307389X", doc);
        assertEquals("Géopolitique du moustique", book.getTitle());
        assertEquals("Isabelle de Saint-Aubin, Erik Orsenna", book.getAuthor());
        assertEquals("Le Livre de Poche", book.getPublisher());
        assertEquals(288, book.getPages());
        assertEquals("29 mai 2019",book.getPublicationDate());
        assertEquals("fr", book.getLang());
        assertEquals("978-2253073895", book.getIsbn());
    }

    @Test
    public void parseHtmlPageElogeForce() throws IOException {
        InputStream is = ItemLookupAmazon.class.getResourceAsStream("/amazon/2379340048.html");
        String html = IOUtils.toString(is, StandardCharsets.UTF_8);
        Document doc = Jsoup.parse(html);
        Book book = ItemLookupAmazon.parseHtmlPage("2379340048", doc);
        assertEquals("Eloge de la force", book.getTitle());
        assertEquals("Laurent Obertone", book.getAuthor());
        assertEquals("Ring", book.getPublisher());
        assertEquals(0, book.getPages());
        assertEquals("24 septembre 2020",book.getPublicationDate());
        assertEquals("fr", book.getLang());
        assertEquals("978-2379340048", book.getIsbn());
    }

    @Test
    public void parseHtmlPageGuerreFormique() throws IOException {
        InputStream is = ItemLookupAmazon.class.getResourceAsStream("/amazon/2841726525.html");
        String html = IOUtils.toString(is, StandardCharsets.UTF_8);
        Document doc = Jsoup.parse(html);
        Book book = ItemLookupAmazon.parseHtmlPage("2841726525", doc);
        assertEquals("Avertir la terre", book.getTitle());
        assertEquals("Aaron Johnston, Orson Scott Card, Florence Bury", book.getAuthor());
        assertEquals("ATALANTE (L')", book.getPublisher());
        assertEquals(480, book.getPages());
        // Content in the page is "ATALANTE (L') (24 octobre 2013)"
        assertEquals("24 octobre 2013",book.getPublicationDate());
        assertEquals("fr", book.getLang());
        assertEquals("978-2841726523", book.getIsbn());
    }

    @Test
    public void testLaMeprise() throws IOException {
        Book book = ItemLookupAmazon.lookup("9782020789516");
        assertEquals("La Méprise : L'Affaire d'Outreau", book.getTitle());
        assertEquals("Florence Aubenas", book.getAuthor());
    }


    @Test
    public void testORing() throws IOException {
        Book book = ItemLookupAmazon.lookup("0813066034");
        assertEquals("Truth, Lies, and O-Rings: Inside the Space Shuttle Challenger Disaster", book.getTitle());
        assertEquals("Allan J. McDonald, James R. Hansen", book.getAuthor());

    }
}