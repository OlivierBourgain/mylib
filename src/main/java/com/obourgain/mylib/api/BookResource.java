package com.obourgain.mylib.api;

import com.obourgain.mylib.service.BookService;
import com.obourgain.mylib.vobj.Book;
import com.obourgain.mylib.web.BookListController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class BookResource {
    private static Logger log = LoggerFactory.getLogger(BookListController.class);

    @Autowired
    private BookService bookService;

    /**
     * @return the list of books for a user.
     */
    @GetMapping(value = "/books")
    public ResponseEntity<List<Book>> getBooks() {
        log.debug("REST - books");
        String userId = "108916333336515243586";
        List<Book> books = bookService.findByUserId(userId);
        return new ResponseEntity<>(books, HttpStatus.OK);
    }
}

