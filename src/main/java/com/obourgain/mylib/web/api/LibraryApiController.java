package com.obourgain.mylib.web.api;

import com.obourgain.mylib.service.BookService;
import com.obourgain.mylib.service.ReadingService;
import com.obourgain.mylib.util.auth.WebUser;
import com.obourgain.mylib.vobj.Book;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.Set;

import static com.obourgain.mylib.web.api.ApiModels.*;

@RestController
@RequestMapping("/api")
public class LibraryApiController {
    private final BookService books;
    private final ReadingService readings;

    public LibraryApiController(BookService books, ReadingService readings) {
        this.books = books;
        this.readings = readings;
    }

    @GetMapping("/books")
    public PageResponse<BookResponse> listBooks(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "100") int size) {
        return PageResponse.from(books.findByUserId(jwt.getSubject(), pagination(page, size))
                .map(BookResponse::from));
    }

    @GetMapping("/books/{id}")
    public BookResponse bookDetail(@AuthenticationPrincipal Jwt jwt, @PathVariable long id) {
        return BookResponse.from(ownedBook(jwt, id));
    }

    @PostMapping("/books")
    public ResponseEntity<BookResponse> createBook(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateBookRequest request) {
        Book book = request.toBook();
        books.createOrUpdateBook(book, user(jwt), Set.of());
        return ResponseEntity.created(URI.create("/api/books/" + book.getId())).body(BookResponse.from(book));
    }

    @GetMapping("/readings")
    public PageResponse<ReadingResponse> listReadings(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "100") int size) {
        return PageResponse.from(readings.findByUserId(jwt.getSubject(), pagination(page, size))
                .map(ReadingResponse::from));
    }

    @PostMapping("/readings")
    public ResponseEntity<ReadingResponse> addReading(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateReadingRequest request) {
        Book book = ownedBook(jwt, request.bookId());
        ReadingResponse reading = ReadingResponse.from(readings.save(user(jwt), book, request.date()));
        return ResponseEntity.status(HttpStatus.CREATED).body(reading);
    }

    private Book ownedBook(Jwt jwt, long id) {
        return books.findOwnedBook(jwt.getSubject(), id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
    }

    private PageRequest pagination(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page must be >= 0; size must be 1 to 100");
        }
        return PageRequest.of(page, size, Sort.by("id"));
    }

    private WebUser user(Jwt jwt) {
        WebUser user = new WebUser();
        user.setId(jwt.getSubject());
        user.setEmail(jwt.getClaimAsString("email"));
        return user;
    }
}
