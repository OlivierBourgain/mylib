package com.obourgain.mylib.web;

import com.obourgain.mylib.service.BookService;
import com.obourgain.mylib.service.TagService;
import com.obourgain.mylib.util.HttpRequestUtil;
import com.obourgain.mylib.util.ISBNConvertor;
import com.obourgain.mylib.util.search.LuceneSearch;
import com.obourgain.mylib.vobj.Book;
import com.obourgain.mylib.vobj.Tag;
import com.obourgain.mylib.vobj.User;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controllers for the book list page.
 */
@Controller
public class BookListController extends AbstractController {

    private static Logger log = LoggerFactory.getLogger(BookListController.class);

    @Autowired
    private BookService bookService;
    @Autowired
    private HttpSession httpSession;
    @Autowired
    private LuceneSearch luceneSearch;

    /**
     * Search criteria for book list.
     */
    @RequestMapping(value = "/books", method = RequestMethod.GET)
    public String bookList(
            HttpServletRequest request,
            Model model,
            @SortDefault.SortDefaults({@SortDefault(sort = "Updated", direction = Sort.Direction.DESC)}) Pageable page) {
        log.info("Controller bookList");

        String searchCriteria = request.getParameter("criteria");

        log.info("Search criteria " + searchCriteria);

        // Intercept the identification request with an ISBN or an ASIN
        if (StringUtils.isNotEmpty(searchCriteria) &&
                (ISBNConvertor.isISBN(searchCriteria) ||
                        searchCriteria.toUpperCase().startsWith("ASIN:"))) {
            return isbnlookup(request, searchCriteria, model, page);
        }

        // Else return the standard booklist
        return internalBookList(request, model, page, searchCriteria);
    }

    private String internalBookList(HttpServletRequest request, Model model, Pageable page, String searchCriteria) {
        User user = getUserDetail();
        Boolean showDiscarded = HttpRequestUtil.getParamAsBoolean(request, "showDisc");
        Pageable cachedPage = (Pageable) httpSession.getAttribute("bookListPageable");
        String cachedSearchCriteria = (String) httpSession.getAttribute("bookListSearchCriteria");
        Boolean cachedShowDiscarded = (Boolean) httpSession.getAttribute("bookListShowDiscarded");

        log.info("Request Pageable is : " + page);
        log.info("Cached  Pageable is : " + cachedPage);
        log.info("SearchCriteria   is : " + searchCriteria);

        if (request.getParameter("page") == null
                && request.getParameter("sort") == null
                && request.getParameter("size") == null
                && searchCriteria == null
                && cachedPage != null) {
            String params = getParams(cachedPage, cachedSearchCriteria, cachedShowDiscarded);
            return "redirect:/books?" + params;
        }
        httpSession.setAttribute("bookListPageable", page);
        httpSession.setAttribute("bookListSearchCriteria", searchCriteria);
        httpSession.setAttribute("bookListShowDiscarded", showDiscarded);

        Page<Book> books = bookService.getBooks(searchCriteria, showDiscarded, page, user.getId());

        List<Integer> pagination = computePagination(books);
        model.addAttribute("pagination", pagination);
        model.addAttribute("searchCriteria", searchCriteria);
        model.addAttribute("showDiscarded", showDiscarded);
        model.addAttribute("sort", books.getSort() == null ? null : books.getSort().iterator().next());
        model.addAttribute("books", books);
        model.addAttribute("user", user);
        return "bookList";
    }

    /**
     * Generate the URL parameters that are equivalent to the given Pageable.
     * Used when a Pageable is in session, and we want to reapply it.
     */
    private String getParams(Pageable cachedPage, String searchCriteria, Boolean showDiscarded) {
        String params = "page=" + cachedPage.getPageNumber() + "&size=" + cachedPage.getPageSize();
        if (cachedPage.getSort() != null) {
            Order order = cachedPage.getSort().iterator().next();
            params += "&sort=" + order.getProperty();
            if (order.isDescending()) params += ",DESC";
        }
        if (StringUtils.isNotBlank(searchCriteria)) {
            try {
                params += "&criteria=" + URLEncoder.encode(searchCriteria, StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException e) {
            }
        }
        if (showDiscarded == true) {
            params += "&showDisc=true";
        }
        return params;
    }

    /**
     * Go to create a book page.
     */
    @RequestMapping(value = "/books/create", method = RequestMethod.GET)
    public String bookCreate(Model model) {
        log.info("Controller bookCreate");
        return "bookCreate";
    }

    /**
     * Lookup a book with ISBN
     */
    private String isbnlookup(HttpServletRequest request, String isbn, Model model, Pageable page) {
        log.info("ISBN lookup for  " + isbn);
        User user = getUserDetail();

        if (StringUtils.isBlank(isbn)) {
            log.info("No param");
            return internalBookList(request, model, page, "");
        }

        Book book = null;
        if (isbn.toLowerCase().startsWith("asin")) {
            String asin = isbn.substring(isbn.indexOf(':') + 1).trim();
            book = bookService.asinLookup(user.getId(), asin);
        } else {
            book = bookService.isbnLookup(user.getId(), isbn);
        }
        if (book == null) {
            model.addAttribute("alertWarn", "No book found for isbn <strong>" + isbn + "</strong>");
            return internalBookList(request, model, page, "");
        }
        return "redirect:/book/" + book.getId();
    }

    /**
     * Export de la liste des livres au format CSV.
     */
    @RequestMapping(value = "/books/exportcsv", method = RequestMethod.GET)
    public void exportcsv(HttpServletResponse response) throws IOException {
        log.info("Controller exportcsv");
        User user = getUserDetail();

        response.setContentType("text/csv");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=\"Library.csv\"";
        response.setHeader(headerKey, headerValue);

        List<Book> books = bookService.findByUserId(user.getId());
        StringBuilder sb = booksToCsv(books);
        response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
        response.getWriter().print(sb.toString());
        return;
    }

    private StringBuilder booksToCsv(List<Book> books) {
        StringBuilder sb = new StringBuilder();
        sb.append(
                "Id;Status;Title;Subtitle;Author;ISBN;Publisher;PublicationDate;Pages;Tags;Lang;Created;Updated;SmallImage;MediumImage;LargeImage;Description;Comment\n");
        for (Book book : books) {
            sb.append(book.getId()).append(";");
            sb.append(book.getStatus() == null ? "" : book.getStatus()).append(";");
            sb.append(book.getTitle()).append(";");
            sb.append(string(book.getSubtitle())).append(";");
            sb.append(book.getAuthor()).append(";");
            sb.append(book.getIsbn()).append(";");
            sb.append(book.getPublisher()).append(";");
            sb.append(book.getPublicationDate()).append(";");
            sb.append(book.getPages()).append(";");
            for (Tag tag : book.getTags())
                sb.append(tag.getText()).append(",");
            sb.append(";");
            sb.append(string(book.getLang())).append(";");
            sb.append(date(book.getCreated())).append(";");
            sb.append(date(book.getUpdated())).append(";");
            sb.append(string(book.getSmallImage())).append(";");
            sb.append(string(book.getMediumImage())).append(";");
            sb.append(string(book.getLargeImage())).append(";");
            sb.append(string(book.getComment())).append(";");

            sb.append("\n");
        }
        return sb;
    }

    private String date(LocalDateTime date) {
        if (date == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
        return date.format(formatter);
    }

    private String string(String text) {
        return text == null ? "" : text;
    }

    /**
     * Recrée l'index de recherche.
     */
    @RequestMapping(value = "/books/rebuildindex", method = RequestMethod.GET)
    public String rebuildindex(HttpServletResponse response) throws IOException {
        log.info("Controller rebuildindex");
        User user = getUserDetail();

        List<Book> books = bookService.findAll();
        luceneSearch.clearIndex();
        luceneSearch.addAll(books);
        return "redirect:/books/";
    }

}
