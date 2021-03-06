package com.obourgain.mylib.api;

import com.obourgain.mylib.service.BookService;
import com.obourgain.mylib.service.ReadingService;
import com.obourgain.mylib.vobj.Book;
import com.obourgain.mylib.vobj.Reading;
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
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;

@CrossOrigin(origins = {"http://localhost:3000", "https://book.obourgain.com"})
@RestController
@RequestMapping("/api")
public class ReadingResource extends AbstractResource {
    private static Logger log = LoggerFactory.getLogger(ReadingResource.class);

    @Autowired
    private ReadingService readingService;

    @Autowired
    private BookService bookService;

    /**
     * @return the list of books for a user.
     */
    @GetMapping(value = "/readings")
    public ResponseEntity<Page<Reading>> getReadings(HttpServletRequest request, Pageable page) throws Exception {
        log.info("REST - readings");
        String userId = checkAccess(request);
        Page<Reading> readings = readingService.findByUserId(userId, page);
        return new ResponseEntity<>(readings, HttpStatus.OK);
    }

    /**
     * @return the list of books for a book.
     */
    @GetMapping(value = "/bookreadings/{id}")
    public ResponseEntity<List<Reading>> getBookReadings(HttpServletRequest request, @PathVariable Long id) throws Exception {
        log.info("REST - readings for book {}", id);
        String userId = checkAccess(request);
        List<Reading> readings = readingService.findByBook(userId, id);
        return new ResponseEntity<>(readings, HttpStatus.OK);
    }


    /**
     * Delete a reading.
     */
    @DeleteMapping(value = "/reading/{id}")
    public void deleteReading(HttpServletRequest request, @PathVariable Long id) throws Exception {
        String userId = checkAccess(request);
        log.info("Deleting reading " + id + " from " + userId);
        readingService.delete(id);
    }

    /**
     * Add a reading.
     */
    @PostMapping(value = "/reading")
    public void postReading(HttpServletRequest request, @RequestBody ReadingParam reading) throws Exception {
        String userId = checkAccess(request);
        Book book = bookService.findBook(userId, Long.parseLong(reading.book));
        LocalDate localDate = LocalDate.parse(reading.date);

        log.info("Add reading for book" + book.getId() + " and date " + localDate);
        readingService.save(userId, book, localDate);
        return;
    }

    /**
     * Paramètre pour post reading
     */
    public static class ReadingParam {
        public String book;
        public String date;
    }

}

