package com.obourgain.mylib.util.search;

import com.obourgain.mylib.vobj.Book;
import com.obourgain.mylib.vobj.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BookSearchTest {
    @TempDir Path directory;

    @Test
    void searchesTitleAuthorIsbnAndTagsWithinAuthenticatedUsersIndex() {
        LuceneSearch search = new LuceneSearch(directory.toString());
        Book book = new Book();
        book.setId(1L);
        book.setUserId("google-user");
        book.setTitle("Dune");
        book.setAuthor("Frank Herbert");
        book.setIsbn("9780441172719");
        book.setUpdated(LocalDateTime.now());
        Tag tag = new Tag();
        tag.setId(2L);
        tag.setText("Science fiction");
        book.setTags(Set.of(tag));
        search.addToIndex(book);
        book.setId(3L);
        book.setUserId("other-user");
        search.addToIndex(book);

        for (String query : java.util.List.of("Dune", "author:Herbert", "9780441172719", "tag:fiction", "userid:other-user OR Dune")) {
            var matches = search.search("google-user", query, 100);
            assertEquals(1, matches.size(), query);
            assertEquals(1L, matches.getFirst().getId(), query);
        }
        assertTrue(search.search("google-user", "unknown", 100).isEmpty());
        assertThrows(SearchUnavailableException.class, () -> search.search("google-user", "title:", 100));
    }
}
