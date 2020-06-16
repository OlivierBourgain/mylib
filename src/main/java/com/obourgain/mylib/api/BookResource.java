package com.obourgain.mylib.api;

import com.obourgain.mylib.service.BookService;
import com.obourgain.mylib.service.TagService;
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
import java.util.Objects;
import java.util.Set;

@CrossOrigin(origins = {"http://localhost:3000"})
@RestController
@RequestMapping("/api")
public class BookResource extends AbstractResource {
    private static Logger log = LoggerFactory.getLogger(BookResource.class);

    @Autowired
    private BookService bookService;

    @Autowired
    private TagService tagService;


    /**
     * Lookup for a new book (on Amazon, based on the ASIN or the ISBN).
     *
     * @return the book, or null if not found.
     */
    @GetMapping(value = "/lookup")
    public ResponseEntity<Book> lookup(HttpServletRequest request, @RequestParam String term) throws Exception {
        String userId = getClientId(request).orElseThrow(() -> new SecurityException("User not authenticated"));
        Book book = bookService.lookup(userId, term);
        return new ResponseEntity<>(book, HttpStatus.OK);
    }

    /**
     * @return the list of books for a user.
     */
    @GetMapping(value = "/books")
    public ResponseEntity<Page<Book>> getBooks(HttpServletRequest request, Pageable page, String criteria, boolean discarded) throws Exception {
        log.info("REST - books, " + Objects.toString(page));
        String userId = getClientId(request).orElseThrow(() -> new SecurityException("User not authenticated"));
        Page<Book> books = bookService.getBooks(criteria, discarded, page, userId);
        return new ResponseEntity<>(books, HttpStatus.OK);
    }

    /**
     * @return the detail of a book.
     */
    @GetMapping(value = "/book/{id}")
    public ResponseEntity<Book> getBook(HttpServletRequest request, @PathVariable Long id) throws Exception {
        log.info("REST - book " + id);
        String userId = getClientId(request).orElseThrow(() -> new SecurityException("User not authenticated"));
        Book book = bookService.findBook(userId, id);
        return new ResponseEntity<>(book, HttpStatus.OK);
    }

    /**
     * Updates a book.
     * The tag list is updated from the tagstring value of the book.
     */
    @PostMapping(value = "/book")
    public ResponseEntity<Book> update(HttpServletRequest request, @RequestBody Book book) throws Exception {
        String userId = getClientId(request).orElseThrow(() -> new SecurityException("User not authenticated"));
        Set<Tag> tags = tagService.getTags(book.getTagString(), userId);
        bookService.createOrUpdateBook(book, userId, tags);
        return new ResponseEntity<>(book, HttpStatus.OK);
    }

    /**
     * Delete a book
     */
    @DeleteMapping(value = "/book/{id}")
    public void delete(HttpServletRequest request, @PathVariable Long id) throws Exception {
        String userId = getClientId(request).orElseThrow(() -> new SecurityException("User not authenticated"));
        bookService.deleteBook(userId, id);
        return;
    }
}

