package com.obourgain.mylib.ext.payot;

import com.obourgain.mylib.vobj.Book;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;

public class PayotHtmlParser {
    private static final Logger log = LoggerFactory.getLogger(PayotHtmlParser.class);

    Document doc;

    public PayotHtmlParser(String html) {
        String cleanHtml = html.replaceAll("[\\n\\t]", "");
        cleanHtml = cleanHtml.replaceAll("\\s{2,}", " ");
        cleanHtml = cleanHtml.replaceAll("<span> Editeur:</span>", "<span class='editeur'/>");
        cleanHtml = cleanHtml.replaceAll("<span> Pages:</span>", "<span class='pages'/>");
        cleanHtml = cleanHtml.replaceAll("<span> EAN13:</span>", "<span class='ean13'/>");
        cleanHtml = cleanHtml.replaceAll("<span> Parution: </span>", "<span class='parution'/>");
        //log.debug(cleanHtml);
        Document doc = Jsoup.parse(cleanHtml);
        this.doc = doc;
    }

    public Book parseBook() {
        Book res = new Book();
        res.setTitle(getTitle());
        res.setSubtitle(getSubtitle());
        res.setAuthor(getAuthor());
        res.setPublisher(getPublisher());
        res.setPages(getPages());
        res.setPublicationDate(getPublicationDate());
        res.setIsbn(getIsbn());
        return res;
    }

    /**
     * <div itemprop="name">
     *     Title
     * </div>
     */
    private String getTitle() {
        Elements elts = doc.select("[itemprop='name']");
        if (elts.size() == 0) return null;
        else return elts.get(0).text();
    }

    /**
     * <p class="detailSubTitle">
     * Subtitle
     * </p>
     */
    private String getSubtitle() {
        Elements elts = doc.select(".detailSubTitle");
        if (elts.size() == 0) return null;
        else return elts.get(0).text();
    }

    /**
     * <ul class="infoLink li-autheur li-autheur--main">
     *    <li><a id="ContentPlaceHolder1_C001_Authors1_rptAuthorPrimary_hlnkauthor_0" href="/Dynamics/Result?author=Florence Aubenas&amp;cId=0">Florence Aubenas</a></li>
     * </ul>
     */
    private String getAuthor() {
        Elements elts = doc.select(".li-autheur--main li a");
        return elts.stream()
                .map(e -> e.text())
                .map(e -> e.replace("(Auteur)", ""))
                .map(e -> e.trim())
                .collect(Collectors.joining (", "));
    }

    /**
     * <span> Editeur:</span>
     * <span>
     * <a id="ContentPlaceHolder1_C001_hlnkEditor" href="/Dynamics/Result?edId=d5f7829708f692580108f6b75e8657ed&amp;ed=Seuil">Seuil</a>
     * </span>
     */
    private String getPublisher() {
        Elements elts = doc.select(".editeur + span");
        if (elts.size() == 0) return null;
        else return elts.get(0).text();
    }

    /**
     * <span>Parution:</span><span>octobre 2005</span>
     */
    private String getPublicationDate() {
        Elements elts = doc.select(".parution + span");
        if (elts.size() == 0) return "";
        return elts.get(0).text();
    }

    /**
     * <span>Pages:</span><span>254 pages</span>
     */
    private int getPages() {
        Elements elts = doc.select(".pages + span");
        if (elts.size() == 0) return 0;
        String val = elts.get(0).text();
        val = val.substring(0, val.indexOf(' '));
        return Integer.parseInt(val);
    }

    /**
     * <span> EAN13:</span>
     */
    private String getIsbn() {
        Elements elts = doc.select(".ean13 + span");
        if (elts.size() == 0) return null;
        String val = elts.get(0).text();
        if (val.startsWith("978")) return "978-" + val.substring(3);
        else if (val.startsWith("979")) return "979-" + val.substring(3);
        return val;
    }

}
