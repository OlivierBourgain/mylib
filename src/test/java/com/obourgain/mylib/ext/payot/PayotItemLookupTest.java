package com.obourgain.mylib.ext.payot;

import com.obourgain.mylib.vobj.Book;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PayotItemLookupTest {

    @Test
    public void parseHtmlPageHurlevent() throws IOException {
        InputStream is = PayotItemLookup.class.getResourceAsStream("/payot/978-2253004752.html");
        String html = IOUtils.toString(is, StandardCharsets.UTF_8);
        Book book = new PayotHtmlParser(html).parseBook();
        assertEquals("Les Hauts de Hurle-Vent", book.getTitle());
        assertEquals("Emily Brontë", book.getAuthor());
        assertEquals("LGF/Le Livre de Poche", book.getPublisher());
        assertEquals(413, book.getPages());
        assertEquals("juin 2007",book.getPublicationDate());
        assertEquals("978-2253004752", book.getIsbn());
    }

    @Test
    public void parseHtmlPageUserStory() throws IOException {
        InputStream is = PayotItemLookup.class.getResourceAsStream("/payot/978-1491904909.html");
        String html = IOUtils.toString(is, StandardCharsets.UTF_8);
        Book book = new PayotHtmlParser(html).parseBook();
        assertEquals("User Story Mapping", book.getTitle());
        assertEquals("Jeff Patton", book.getAuthor());
        assertEquals("O'Reilly", book.getPublisher());
        assertEquals(324, book.getPages());
        assertEquals("septembre 2014",book.getPublicationDate());
        assertEquals("978-1491904909", book.getIsbn());
    }

    @Test
    public void parseHtmlPageMoustique() throws IOException {
        InputStream is = PayotItemLookup.class.getResourceAsStream("/payot/978-2253073895.html");
        String html = IOUtils.toString(is, StandardCharsets.UTF_8);
        Book book = new PayotHtmlParser(html).parseBook();
        assertEquals("Petit précis de mondialisation", book.getTitle());
        assertEquals("Tome 4, Géopolitique du moustique", book.getSubtitle());
        assertEquals("Isabelle Saint-Aubin, Erik Orsenna", book.getAuthor());
        assertEquals("LGF/Le Livre de Poche", book.getPublisher());
        assertEquals(288, book.getPages());
        assertEquals("mai 2019",book.getPublicationDate());
        assertEquals("978-2253073895", book.getIsbn());
    }

    @Test
    public void parseHtmlPageElogeForce() throws IOException {
        InputStream is = PayotItemLookup.class.getResourceAsStream("/payot/978-2379340048.html");
        String html = IOUtils.toString(is, StandardCharsets.UTF_8);
        Book book = new PayotHtmlParser(html).parseBook();
        assertEquals("Eloge de la force", book.getTitle());
        assertEquals("Laurent Obertone", book.getAuthor());
        assertEquals("Editions Ring", book.getPublisher());
        assertEquals(240, book.getPages());
        assertEquals("septembre 2020",book.getPublicationDate());
        assertEquals("978-2379340048", book.getIsbn());
    }

    @Test
    public void parseHtmlPageGuerreFormique() throws IOException {
        InputStream is = PayotItemLookup.class.getResourceAsStream("/payot/978-2841726523.html");
        String html = IOUtils.toString(is, StandardCharsets.UTF_8);
        Book book = new PayotHtmlParser(html).parseBook();
        assertEquals("Avertir la terre", book.getTitle());
        assertEquals("(La Première Guerre formique, Tome 1)", book.getSubtitle());
        assertEquals("Orson Scott Card, Aaron Johnston", book.getAuthor());
        assertEquals("L'Atalante Editions", book.getPublisher());
        assertEquals(474, book.getPages());
        assertEquals("octobre 2013",book.getPublicationDate());
        assertEquals("978-2841726523", book.getIsbn());
    }


    @Test
    public void parseHtmlEspionTraitre() throws IOException {
        InputStream is = PayotItemLookup.class.getResourceAsStream("/payot/978-2266309844.html");
        String html = IOUtils.toString(is, StandardCharsets.UTF_8);
        Book book = new PayotHtmlParser(html).parseBook();
        assertEquals("L'espion et le traître", book.getTitle());
        assertEquals("Ben MacIntyre", book.getAuthor());
        assertEquals("Pocket", book.getPublisher());
        assertEquals(537, book.getPages());
        assertEquals("septembre 2020",book.getPublicationDate());
        assertEquals("978-2266309844", book.getIsbn());
    }

    @Test
    public void testLaMeprise() throws IOException {
        Book book = PayotItemLookup.lookup("9782020789516");
        assertEquals("La méprise", book.getTitle());
        assertEquals("L'affaire d'Outreau", book.getSubtitle());
        assertEquals("Florence Aubenas", book.getAuthor());
    }


    @Test
    public void testAccelerate() throws IOException {
        InputStream is = PayotItemLookup.class.getResourceAsStream("/payot/978-1942788331.html");
        String html = IOUtils.toString(is, StandardCharsets.UTF_8);
        Book book = new PayotHtmlParser(html).parseBook();
        assertEquals("Accelerate", book.getTitle());
        assertEquals("Nicole Forsgren Phd", book.getAuthor());
        assertEquals("It Revolution Press", book.getPublisher());
        assertEquals(288, book.getPages());
        assertEquals("décembre 2018",book.getPublicationDate());
        assertEquals("978-1942788331", book.getIsbn());
    }


    @Test
    public void testLookupWithIsbn10() throws IOException {
        Book book = PayotItemLookup.lookup("0813066034");
        assertEquals("Truth, Lies, and O-Rings", book.getTitle());
        assertEquals("Allan J. McDonald", book.getAuthor());
        assertEquals("Univ Pr Of Florida", book.getPublisher());
        assertEquals(648, book.getPages());
        assertEquals("décembre 2018", book.getPublicationDate());
        assertEquals("978-0813066035", book.getIsbn());

    }
}