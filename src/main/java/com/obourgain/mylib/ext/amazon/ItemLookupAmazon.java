package com.obourgain.mylib.ext.amazon;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.obourgain.mylib.util.ISBNConvertor;
import com.obourgain.mylib.util.img.FileStore;
import com.obourgain.mylib.vobj.Book;

/**
 * Lookup d'un item sur Amazon.
 * 
 * L'appel des API nécessite d'être partenaire, on se rabat donc sur le scrapping de la page publique.
 * 
 * Pour les images, voir: http://aaugh.com/imageabuse.html. http://images.amazon.com/images/P/${isbn}.08.T.jpg --> height 110 px
 * http://images.amazon.com/images/P/${isbn}.08.Z.jpg --> height 160 px http://images.amazon.com/images/P/${isbn}.08.L.jpg --> height 490 ou 500 px
 */
public class ItemLookupAmazon {
	private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36";
	private static final String AMAZON_URL = "https://www.amazon.fr/gp/product/";
	private static final String AMAZON_IMG = "http://images.amazon.com/images/P/";
	private static final Logger log = LogManager.getLogger(ItemLookupAmazon.class);

	public static void main(String[] args) throws Exception {
		Book res = lookup("2290120332");
		System.out.println(res.deepToString());
	}

	public static Book lookup(String isbn) {
		try {
			String isbn10 = isbn;
			if (isbn.length() >= 13) isbn10 = ISBNConvertor.isbn13to10(isbn);

			String url = getUrl(isbn10);
			log.info("Calling amazon {} at {}", isbn10, url);
			Document doc = Jsoup.connect(url).userAgent(USER_AGENT).get();
			Book book = new Book();
			book.setTitle(getTitle(doc));
			book.setAuthor(getAuthor(doc));

			parseDetail(book, doc);

			book.setSmallImage(saveImage(isbn10, 'T'));
			book.setMediumImage(saveImage(isbn10, 'Z'));
			book.setLargeImage(saveImage(isbn10, 'L'));

			log.info(() -> book.deepToString());
			return book;
		} catch (Exception e) {
			throw new RuntimeException("Erreur fetching this book", e);
		}
	}

	/**
	 * Gets the image from Amazon, and store result.
	 */
	private static String saveImage(String isbn, char size) throws IOException {
		String url = AMAZON_IMG + isbn + ".08." + size + ".jpg";
		System.out.println("Calling " + url);
		URLConnection connection = new URL(url).openConnection();
		connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36");

		InputStream response = connection.getInputStream();
		byte[] bytes = IOUtils.toByteArray(response);
		// Don't store if it is less than 100 bytes.
		if (bytes.length < 100) return null;
		return FileStore.saveFile(isbn, size, bytes, "jpg");
	}

	/**
	 * Détail contient une liste de
	 * <li>avec un contenu textuel.
	 * 
	 * Parmi ces données, on veut retrouver :
	 * 
	 * - Le nombre de pages
	 * 
	 * <pre>
	 * Broché: 624 pages
	 * Relié: 162 pages
	 * Poche: 480 pages
	 * </pre>
	 * 
	 * - La langue
	 * 
	 * <pre>
	 * Langue : Anglais
	 * Langue : Français
	 * </pre>
	 * 
	 * - L'éditeur et la date d'édition
	 * 
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

		Elements elts = doc.select("#detail_bullets_id li");
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
			} else if (elt.text().startsWith("Editeur")) {
				if (elt.text().contains(";")) {
					String[] t = elt.text().split(";");
					String publisher = t[0].substring(t[0].indexOf(':') + 1).trim();
					String publication = t[1].substring(t[1].indexOf('(') + 1, t[1].indexOf(')')).trim();
					book.setPublisher(publisher);
					book.setPublicationDate(publication);
				} else {
					String publisher = elt.text().substring(elt.text().indexOf(':') + 1, elt.text().indexOf('('))
							.trim();
					String publication = elt.text().substring(elt.text().indexOf('(') + 1, elt.text().indexOf(')'))
							.trim();
					book.setPublisher(publisher);
					book.setPublicationDate(publication);
				}
			} else if (elt.text().startsWith("ISBN-13")) {
				book.setIsbn(elt.text().substring(elt.text().indexOf(':') + 1).trim());
			}

		}

	}

	/**
	 * Cas 1 : <a data-asin="B001JOVOZ6" class="a-link-normal contributorNameID" href= "(...)">Craig Walls</a>
	 * 
	 * Cas 2: <span class="author notFaded" data-width=""> <a class="a-link-normal" href= "(...)">Aldous Huxley</a> (...) </span>
	 */
	private static String getAuthor(Document doc) {
		Elements elts = doc.select(".contributorNameID");
		if (elts.size() > 0) {
			String res = "";
			for (Element elt : elts)
				res += ", " + elt.text();
			return res.substring(2);
		}

		elts = doc.select("span.author>a");
		if (elts.size() > 0) {
			String res = "";
			for (Element elt : elts)
				res += ", " + elt.text();
			return res.substring(2);
		}

		return null;
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
	 * 
	 * "vous pouvez insérer un lien vers notre catalogue à l'aide de cette URL : http://www.amazon.fr/gp/product/0000000000 Remplacez les zéros par le numéro ISBN à
	 * 10 chiffres (en omettant les traits d'union) ou ASIN (présent sur la page d'informations détaillées du produit commençant par un B) pour le titre
	 * correspondant. "
	 */
	private static String getUrl(String isbn) {
		return AMAZON_URL + isbn;
	}

}