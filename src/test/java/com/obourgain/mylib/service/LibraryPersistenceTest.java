package com.obourgain.mylib.service;

import com.obourgain.mylib.util.auth.WebUser;
import com.obourgain.mylib.util.search.LuceneSearch;
import com.obourgain.mylib.vobj.Book;
import com.obourgain.mylib.vobj.Reading;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:rest-api-tests;NON_KEYWORDS=YEAR,MONTH,USER",
        "spring.jpa.hibernate.ddl-auto=create-drop"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({BookService.class, ReadingService.class})
class LibraryPersistenceTest {
    @Autowired BookService books;
    @Autowired ReadingService readings;
    @Autowired EntityManager entityManager;
    @MockBean LuceneSearch index;
    @MockBean TagService tags;

    @Test
    void searchLoadsCompleteBooksAndExcludesForeignAndStaleIndexEntries() {
        WebUser user = new WebUser();
        user.setId("google-user");
        Book book = new Book();
        book.setTitle("Dune");
        book.setSummary("Complete database metadata");
        books.createOrUpdateBook(book, user, Set.of());
        WebUser other = new WebUser();
        other.setId("other-user");
        Book foreignBook = new Book();
        foreignBook.setTitle("Dune");
        books.createOrUpdateBook(foreignBook, other, Set.of());
        Book indexedBook = new Book();
        indexedBook.setId(book.getId());
        Book stale = new Book();
        stale.setId(9999L);
        when(index.search("google-user", "Dune", 100000))
                .thenReturn(java.util.List.of(indexedBook, foreignBook, stale));
        var page = books.searchBooks("Dune", PageRequest.of(0, 10), "google-user");
        assertEquals(1, page.getTotalElements());
        assertEquals("Complete database metadata", page.getContent().getFirst().getSummary());
        assertEquals(book.getId(), page.getContent().getFirst().getId());
        var beyondEnd = books.searchBooks("Dune", PageRequest.of(3, 10), "google-user");
        assertTrue(beyondEnd.isEmpty());
        assertEquals(3, beyondEnd.getNumber());
        assertEquals(1, beyondEnd.getTotalElements());
        when(index.search("google-user", "unknown", 100000)).thenReturn(java.util.List.of());
        assertTrue(books.searchBooks("unknown", PageRequest.of(0, 10), "google-user").isEmpty());
    }

    @Test
    void persistsBooksAndReadingsWithOwnershipAndDerivedDates() {
        WebUser user = new WebUser();
        user.setId("google-user");
        Book book = new Book();
        book.setTitle("Dune");
        books.createOrUpdateBook(book, user, Set.of());
        verify(index).addToIndex(book);
        assertNotNull(book.getId());
        assertNotNull(book.getCreated());
        assertEquals("google-user", book.getUserId());

        Reading reading = readings.save(user, book, LocalDate.of(2026, 11, 17));
        entityManager.flush();
        entityManager.clear();
        assertTrue(books.findOwnedBook("other-user", book.getId()).isEmpty());
        assertTrue(books.findOwnedBook("google-user", book.getId()).isPresent());
        assertEquals(0, books.findByUserId("other-user", PageRequest.of(0, 10)).getTotalElements());
        assertEquals(1, books.findByUserId("google-user", PageRequest.of(0, 10)).getTotalElements());
        assertEquals(0, readings.findByUserId("other-user", PageRequest.of(0, 10)).getTotalElements());
        Reading saved = readings.findByUserId("google-user", PageRequest.of(0, 10)).getContent().getFirst();
        assertEquals(reading.getId(), saved.getId());
        assertEquals(book.getId(), saved.getBook().getId());
        assertEquals(LocalDate.of(2026, 11, 17), saved.getDate());
        assertEquals(2026, saved.getYear());
        assertEquals(11, saved.getMonth());
        assertNotNull(saved.getCreated());

        WebUser other = new WebUser();
        other.setId("other-user");
        assertThrows(IllegalArgumentException.class, () -> readings.save(other, book, saved.getDate()));
    }
}
