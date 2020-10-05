package com.obourgain.mylib.api;

import com.obourgain.mylib.service.BookService;
import com.obourgain.mylib.service.TagService;
import com.obourgain.mylib.util.search.LuceneSearch;
import com.obourgain.mylib.vobj.Book;
import com.obourgain.mylib.vobj.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = {"http://localhost:3000", "https://book.obourgain.com"})
@RestController
@RequestMapping("/api")
public class BookResource extends AbstractResource {
    private static Logger log = LoggerFactory.getLogger(BookResource.class);

    @Autowired
    private BookService bookService;

    @Autowired
    private TagService tagService;

    @Autowired
    private LuceneSearch luceneSearch;

    /**
     * Lookup for a new book (on Amazon, based on the ASIN or the ISBN).
     *
     * @return the book, or null if not found.
     */
    @GetMapping(value = "/lookup")
    public ResponseEntity<Book> lookup(HttpServletRequest request, @RequestParam String term) throws Exception {
        String userId = checkAccess(request);
        Book book = bookService.lookup(userId, term);
        return new ResponseEntity<>(book, HttpStatus.OK);
    }

    /**
     * @return the list of books for a user.
     */
    @GetMapping(value = "/books")
    public ResponseEntity<Page<Book>> getBooks(HttpServletRequest request, Pageable page, String criteria, boolean discarded) throws Exception {
        log.info("REST - books, " + Objects.toString(page));
        String userId = checkAccess(request);
        Page<Book> books = bookService.getBooks(criteria, discarded, page, userId);
        log.info("REST - books, returned {} books ", books.getTotalElements());
        return new ResponseEntity<>(books, HttpStatus.OK);
    }

    /**
     * @return the list of book titles for a user.
     */
    @GetMapping(value = "/booktitles")
    public ResponseEntity<Map<Long, String>> getBooks(HttpServletRequest request) throws Exception {
        log.info("REST - booktitles");
        String userId = checkAccess(request);
        Map<Long, String> res = bookService.findByUserId(userId).stream()
                .filter(b -> b.getTitle() != null)
                .collect(Collectors.toMap(Book::getId, Book::getTitle));
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    /**
     * @return the detail of a book.
     */
    @GetMapping(value = "/book/{id}")
    public ResponseEntity<Book> getBook(HttpServletRequest request, @PathVariable Long id) throws Exception {
        log.info("REST - book " + id);
        String userId = checkAccess(request);
        Book book = bookService.findBook(userId, id);
        return new ResponseEntity<>(book, HttpStatus.OK);
    }

    /**
     * Updates a book.
     * The tag list is updated from the tagstring value of the book.
     */
    @PostMapping(value = "/book")
    public ResponseEntity<Book> update(HttpServletRequest request, @RequestBody Book book) throws Exception {
        String userId = checkAccess(request);
        Set<Tag> tags = tagService.getTags(book.getTagString(), userId);
        bookService.createOrUpdateBook(book, userId, tags);
        return new ResponseEntity<>(book, HttpStatus.OK);
    }

    /**
     * Delete a book
     */
    @DeleteMapping(value = "/book/{id}")
    public void delete(HttpServletRequest request, @PathVariable Long id) throws Exception {
        String userId = checkAccess(request);
        bookService.deleteBook(userId, id);
        return;
    }

    @PostMapping(value = "/bookdiscard")
    public ResponseEntity<Book> updateDiscard(HttpServletRequest request, @RequestParam("book") Long id, @RequestParam("discard") boolean discard) throws Exception {
        log.info("(un)Discard book {} to {}", id, discard);
        String userId = checkAccess(request);
        Book book = bookService.findBook(userId, id);
        book.setStatus(discard ? Book.BookStatus.DISCARDED : null);
        bookService.createOrUpdateBook(book, userId, book.getTags());
        return new ResponseEntity<>(book, HttpStatus.OK);
    }

    /**
     * Recrée l'index de recherche.
     */
    @GetMapping(value = "/rebuildindex")
    public void rebuildindex(HttpServletRequest request) throws Exception {
        log.info("REST - rebuildindex");
        String userId = checkAccess(request);

        List<Book> books = bookService.findAll();
        luceneSearch.clearIndex();
        luceneSearch.addAll(books);
        List<Book> check = bookService.findByUserId(userId);
        log.info("After rebuild, {} books", check.size());
        log.info("REST - rebuildindex done");
        return;
    }

    /**
     * Export book list to CSV
     */
    @GetMapping(value = "/exportbooks")
    public void exportcsv(HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.info("REST - export csv");
        String userId = checkAccess(request);

        response.setContentType("text/csv");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=\"Library.csv\"";
        response.setHeader(headerKey, headerValue);

        List<Book> books = bookService.findByUserId(userId);
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


}

