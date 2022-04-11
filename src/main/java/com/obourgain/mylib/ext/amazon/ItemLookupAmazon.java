package com.obourgain.mylib.ext.amazon;

import com.obourgain.mylib.util.ISBNConvertor;
import com.obourgain.mylib.util.img.FileStore;
import com.obourgain.mylib.vobj.Book;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lookup d'un item sur Amazon.
 * <p>
 * Pour les images, voir: http://aaugh.com/imageabuse.html.
 * http://images.amazon.com/images/P/${isbn}.08.T.jpg --> height 110 px
 * http://images.amazon.com/images/P/${isbn}.08.Z.jpg --> height 160 px
 * http://images.amazon.com/images/P/${isbn}.08.L.jpg --> height 490 ou 500 px
 */
public class ItemLookupAmazon {
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:66.0) Gecko/20100101 Firefox/66.0";
    private static final String AMAZON_URL = "https://www.amazon.fr/gp/product/";
    private static final String AMAZON_IMG = "http://images.amazon.com/images/P/";
    private static final Logger log = LoggerFactory.getLogger(ItemLookupAmazon.class);

    public static void main(String[] args) throws Exception {
        Book res = lookup("2290120332");
        System.out.println(res.deepToString());
    }

    /**
     * Lookup a book on Amazon, with its ISBN.
     * <p>
     * ISBN can be 10 or 13 characters.
     * <p>
     * This function won't work for ISBN 13 starting with 979 (as there is no corresponding ISBN 10)
     */
    public static Book lookup(String isbn) {
        try {
            String isbn10 = isbn;
            if (isbn.length() >= 13) isbn10 = ISBNConvertor.isbn13to10(isbn);
            String url = buildUrl(isbn10);

            log.info("Calling amazon {} at {}", isbn10, url);
            String html = fetchDocument(url);
            Document doc = Jsoup.parse(html);
            return parseHtmlPage(isbn10, doc);
        } catch (Exception e) {
            log.error("Book not found", e);
            throw new RuntimeException("Error getting info from Amazon");
        }
    }

    private static String fetchDocument(String urlstr) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(urlstr))
                .header("user-agent", USER_AGENT)
                .header("accept", "*/*")
                .header("Referrer-Policy","strict-origin-when-cross-origin")
                //  .header("Accept-Encoding", "gzip, deflate")
                .header("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("DNT","1")
                //.header("Connection","close")
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

    /**
     * Lookup a book on Amazon, given it's ASIN.
     */
    public static Book asinLookup(String asin) {
        try {
            String url = "https://www.amazon.fr/exec/obidos/ASIN/" + asin;

            log.info("Calling amazon asin={} at {}", asin, url);
            String html = fetchDocument(url);
            Document doc = Jsoup.parse(html);
            return parseHtmlPage(asin, doc);
        } catch (Exception e) {
            log.error("Book not found", e);
            return null;
        }
    }

    protected static Book parseHtmlPage(String asin, Document doc) throws IOException {
        Book book = new Book();
        book.setTitle(getTitle(doc));
        book.setAuthor(getAuthor(doc));

        parseDetail(book, doc);

        book.setSmallImage(saveImage(asin, 'T'));
        book.setMediumImage(saveImage(asin, 'Z'));
        book.setLargeImage(saveImage(asin, 'L'));

        log.info(book.deepToString());
        return book;
    }

    /**
     * Gets the image from Amazon, and store result.
     */
    private static String saveImage(String isbn, char size) throws IOException {
        String url = AMAZON_IMG + isbn + ".08." + size + ".jpg";
        System.out.println("Calling " + url);
        URLConnection connection = new URL(url).openConnection();
        connection.setRequestProperty("User-Agent", USER_AGENT);

        InputStream response = connection.getInputStream();
        byte[] bytes = IOUtils.toByteArray(response);
        // Don't store if it is less than 100 bytes.
        if (bytes.length < 100) return null;
        return FileStore.saveFile(isbn, size, bytes, "jpg");
    }

    /**
     * Détail contient une liste de
     * <li>avec un contenu textuel.
     * <p>
     * Parmi ces données, on veut retrouver :
     * <p>
     * - Le nombre de pages
     * <p>
     * <pre>
     * Broché: 624 pages
     * Relié: 162 pages
     * Poche: 480 pages
     * </pre>
     * <p>
     * - La langue
     * <p>
     * <pre>
     * Langue : Anglais
     * Langue : Français
     * </pre>
     * <p>
     * - L'éditeur et la date d'édition
     * <p>
     * <pre>
     * Editeur : Manning Publications; Édition : 4 (27 novembre 2014)
     * Editeur : Dupuis (7 janvier 2015)
     * Editeur : Simon & Schuster (23 juin 2009)
     * Editeur : Casterman (4 mai 1993)
     * </pre>
     */
    private static void parseDetail(Book book, Document doc) {
        Pattern patternNbPages = Pattern.compile(".* (\\d*) pages");
        Pattern patternLang = Pattern.compile("Langue.*:\\s(.*)");

        Elements elts = doc.select("#detailBullets_feature_div li");
        for (Element elt : elts) {
            Matcher matcherPages = patternNbPages.matcher(elt.text());
            Matcher matcherLang = patternLang.matcher(elt.text());

            if (matcherPages.find()) {
                book.setPages(Integer.parseInt(matcherPages.group(1)));
            } else if (matcherLang.find()) {
                String lang = matcherLang.group(1);
                switch (lang) {
                    case "Anglais":
                        book.setLang("en");
                        break;
                    case "Français":
                        book.setLang("fr");
                        break;
                    default:
                        System.err.println("Langue non trouvée " + lang);
                }
            } else if (elt.text().startsWith("Editeur") || elt.text().startsWith("Éditeur")) {
                if (elt.text().contains(";")) {
                    String[] t = elt.text().split(";");
                    String publisher = t[0].substring(t[0].indexOf(':') + 1).trim();
                    String publication = t[1].substring(t[1].indexOf('(') + 1, t[1].indexOf(')')).trim();
                    book.setPublisher(publisher);
                    book.setPublicationDate(publication);
                } else {
                    // Should cover cases like
                    // - ATALANTE (L') (24 octobre 2013)
                    // - Biblio (12 septembre 2012)
                    String publisher = elt.text().substring(elt.text().indexOf(':') + 1, elt.text().lastIndexOf('('))
                            .trim();
                    String publication = elt.text().substring(elt.text().lastIndexOf('(') + 1, elt.text().lastIndexOf(')'))
                            .trim();
                    book.setPublisher(publisher);
                    book.setPublicationDate(publication);
                }
            } else if (elt.text().startsWith("ISBN-13")) {
                String isbn = elt.text().substring(elt.text().indexOf(':') + 1).trim();
                // Correction of strange case where the ISBN start with Unicode Character 'LEFT-TO-RIGHT MARK' (U+200E)
                if (isbn.startsWith(Character.toString(8206))) isbn = isbn.substring(1).trim();
                book.setIsbn(isbn);
            }
        }
    }

    /**
     * Cas 1 : <a data-asin="B001JOVOZ6" class="a-link-normal contributorNameID" href= "(...)">Craig Walls</a>
     * <p>
     * Cas 2: <span class="author notFaded" data-width=""> <a class="a-link-normal" href= "(...)">Aldous Huxley</a> (...) </span>
     */
    private static String getAuthor(Document doc) {
        Elements elts = doc.select(".contributorNameID");
        List<String> res = new ArrayList<>();

        if (elts.size() > 0) {
            for (Element elt : elts)
                res.add(elt.text());
        }
        elts = doc.select("span.author>a");
        if (elts.size() > 0) {
            for (Element elt : elts)
                res.add(elt.text());
        }
        return String.join(", ", res);
    }

    /**
     * <span id="productTitle" class="a-size-large">Les Hauts de Hurle-Vent</span>
     */
    private static String getTitle(Document doc) {
        Elements elts = doc.select("#productTitle");
        if (elts.size() == 0) return null;
        else return elts.get(0).text();
    }

    /**
     * cf. https://www.amazon.fr/gp/help/customer/display.html?ie=UTF8&nodeId=200107620
     * <p>
     * "vous pouvez insérer un lien vers notre catalogue à l'aide de cette URL : http://www.amazon.fr/gp/product/0000000000 Remplacez les zéros par le numéro ISBN à
     * 10 chiffres (en omettant les traits d'union) ou ASIN (présent sur la page d'informations détaillées du produit commençant par un B) pour le titre
     * correspondant. "
     */
    private static String buildUrl(String isbn) {
        return AMAZON_URL + isbn;
    }
}