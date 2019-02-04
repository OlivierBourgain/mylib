package com.obourgain.mylib.api;

import com.obourgain.mylib.service.BookService;
import com.obourgain.mylib.vobj.Book;
import com.obourgain.mylib.web.BookListController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api")
public class BookResource extends AbstractResource {
    private static Logger log = LoggerFactory.getLogger(BookListController.class);

    @Autowired
    private BookService bookService;

    /**
     * @return the list of books for a user.
     */
    @GetMapping(value = "/books")
    public ResponseEntity<List<Book>> getBooks(HttpServletRequest request) throws Exception {
        log.debug("REST - books");
        String userId = getClient(request).orElseThrow(() -> new SecurityException("User not authenticated"));
        List<Book> books = bookService.findByUserId(userId);
        return new ResponseEntity<>(books, HttpStatus.OK);
    }
}

