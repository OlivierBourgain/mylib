package com.obourgain.mylib.web;

import com.obourgain.mylib.service.BookService;
import com.obourgain.mylib.service.TagService;
import com.obourgain.mylib.util.auth.WebUser;
import com.obourgain.mylib.vobj.Book;
import com.obourgain.mylib.vobj.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller for the book detail page.
 */
@Controller
public class BookDetailController extends AbstractController {
    private static Logger log = LoggerFactory.getLogger(BookDetailController.class);

    @Autowired
    private BookService bookService;
    @Autowired
    private TagService tagService;

    /**
     * Get the detail of a book.
     */
    @RequestMapping(value = "/book/{bookId}", method = RequestMethod.GET)
    public String bookDetail(@PathVariable("bookId") long bookId, Model model) {
        log.info("Controller bookDetail");
        WebUser user = getUserDetail();

        Book b = bookService.findBook(user.getId(), bookId);
        if (b == null) {
            b = new Book();
        }
        log.info("Found " + b);

        // Tag list, sorted by Text.
        List<Tag> alltags = tagService
                .findByUserId(user.getId()).stream()
                .sorted(Comparator.comparing(Tag::getText))
                .collect(Collectors.toList());

        // List of tag Ids for this book
        Set<Long> tagids = b.getTags() == null ? new HashSet<>() : b.getTags().stream().map(Tag::getId).collect(Collectors.toSet());

        model.addAttribute("user", user);
        model.addAttribute("book", b);
        model.addAttribute("alltags", alltags);
        model.addAttribute("tagids", tagids);
        return "bookDetail";
    }

    /**
     * Get a tooltip popup of a book.
     */
    @RequestMapping(value = "/book/tooltip/{bookId}", method = RequestMethod.GET)
    public String bookTooltip(@PathVariable("bookId") long bookId, Model model) {
        log.info("Controller bookTooltip");
        WebUser user = getUserDetail();

        Book b = bookService.findBook(user.getId(), bookId);
        if (b == null) {
            b = new Book();
        }
        log.info("Found " + b);
        model.addAttribute("book", b);
        return "bookTooltip";
    }

    /**
     * Update book.
     */
    @RequestMapping(value = "/book/{bookId}", method = RequestMethod.POST, params = "action=save")
    public String updateBook(@PathVariable("bookId") Long bookId, Book book) {
        log.info("Controller updateBook");
        WebUser user = getUserDetail();

        log.info("And the tags are : " + book.getTags());
        Set<Tag> tags = tagService.getTags(book.getTagString(), user.getId());

        bookService.createOrUpdateBook(book, user, tags);
        return "redirect:/books";
    }

    /**
     * Update book.
     */
    @RequestMapping(value = "/book/{bookId}", method = RequestMethod.POST, params = "action=readnow")
    public String updateBookReading(@PathVariable("bookId") Long bookId) {
        log.info("Controller updateBookReading");
        WebUser user = getUserDetail();

        int year = LocalDate.now().getYear();
        log.info("Book " + bookId + " was read in " + year);

        Book book = bookService.updateBookReading(user, bookId, year);
        if (book == null) {
            return "redirect:/books";
        }
        return "redirect:/book/" + book.getId();
    }

    /**
     * Delete book.
     */
    @RequestMapping(value = "/book/{bookId}", method = RequestMethod.POST, params = "action=delete")
    public String deleteBook(@PathVariable("bookId") Long bookId) {
        log.info("Controller deleteBook " + bookId);
        WebUser user = getUserDetail();
        bookService.deleteBook(user.getId(), bookId);
        return "redirect:/books";
    }

    /**
     * Create book.
     */
    @RequestMapping(value = "/book", method = RequestMethod.POST, params = "action=create")
    public String createBook(Book book) {
        log.info("Controller createBook");
        WebUser user = getUserDetail();

        log.info("And the tags are : " + book.getTagString());
        Set<Tag> tags = tagService.getTags(book.getTagString(), user.getId());

        bookService.createOrUpdateBook(book, user, tags);
        return "redirect:/books";
    }

    @RequestMapping(value = "/book/{bookId}/updateimgwithisbndb", method = RequestMethod.GET)
    public String updateImageIsnbDb(@PathVariable("bookId") Long bookId) throws IOException {
        log.info("Controller createBook");
        WebUser user = getUserDetail();
        Book book = bookService.findBook(user.getId(), bookId);
        if (book != null) {
            bookService.updateImg(bookId, user.getId(), "isbndb");
        }
        return "redirect:/book/" + book.getId();
    }

    @RequestMapping(value = "/book/{bookId}/updateimg/{source}", method = RequestMethod.GET)
    public String updateImageAbe(@PathVariable("bookId") Long bookId, @PathVariable("source") String source) throws IOException {
        log.info("Controller update image with " + source);
        WebUser user = getUserDetail();
        Book book = bookService.findBook(user.getId(), bookId);
        if (book != null) {
            bookService.updateImg(bookId, user.getId(), source);
        }
        return "redirect:/book/" + book.getId();
    }

    @RequestMapping(value = "/book/{bookId}/removeimg", method = RequestMethod.GET)
    public String updateImageAbe(@PathVariable("bookId") Long bookId) throws IOException {
        log.info("Controller remove image");
        WebUser user = getUserDetail();
        Book book = bookService.findBook(user.getId(), bookId);
        if (book != null) {
            bookService.removeImg(bookId, user.getId());
        }
        return "redirect:/book/" + book.getId();
    }
}
