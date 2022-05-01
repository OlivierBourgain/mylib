package com.obourgain.mylib.util.search;

import com.obourgain.mylib.vobj.Book;
import com.obourgain.mylib.vobj.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LuceneSearchTest {

    private static LuceneSearch fixture;

    @BeforeEach
    public void initIndex() {
        fixture = new LuceneSearch(System.getProperty("user.home") + "/mylib/search/testfull");
        fixture.clearIndex();
    }

    @Test
    public void testSearchOnEachField() {

        Set<Tag> tags = new HashSet<>();
        Tag tag = new Tag();
        tag.setId(12L);
        tag.setText("TestTag");
        tags.add(tag);

        Book book = new Book();
        book.setId(35L);
        book.setUserId("UserABC");
        book.setTitle("TestTitle");
        book.setSubtitle("TestSubTitle");
        book.setAuthor("TestAuthor");
        book.setIsbn("TestISBN");
        book.setTags(tags);
        book.setUpdated(LocalDateTime.now());
        fixture.addToIndex(book);

        List<Book> res1 = fixture.search("UserABC", "testtitle", false, 10);
        assertEquals(35L, res1.get(0).getId().longValue());

        List<Book> res2 = fixture.search("UserABC", "testauthor", false, 10);
        assertEquals(35L, res2.get(0).getId().longValue());

        List<Book> res3 = fixture.search("UserABC", "testtag", false, 10);
        assertEquals(35L, res3.get(0).getId().longValue());
        assertEquals("12,", res3.get(0).getTagString());

        List<Book> res4 = fixture.search("UserABC", "testisbn", false, 10);
        assertEquals(35L, res4.get(0).getId().longValue());

        List<Book> res5 = fixture.search("UserABC", "testsubtitle", false, 10);
        assertEquals(35L, res5.get(0).getId().longValue());
    }

    @Test
    public void accentTest() {
        Book book = new Book();
        book.setId(32L);
        book.setUserId("UserABC");
        book.setTitle("TELECOM");
        book.setTags(new HashSet<>());
        book.setUpdated(LocalDateTime.now());
        fixture.addToIndex(book);

        List<Book> res1 = fixture.search("UserABC", "télécom", false, 10);
        assertEquals(32L, res1.get(0).getId().longValue());

        List<Book> res2 = fixture.search("UserABC", "TÉlÉCOM", false, 10);
        assertEquals(32L, res2.get(0).getId().longValue());
    }

    @Test
    public void accentTest2() {
        Book book = new Book();
        book.setId(37L);
        book.setUserId("UserABC");
        book.setTitle("télécom");
        book.setTags(new HashSet<>());
        book.setUpdated(LocalDateTime.now());
        fixture.addToIndex(book);

        List<Book> res1 = fixture.search("UserABC", "TELECOM", false, 10);
        assertEquals(37L, res1.get(0).getId().longValue());

        List<Book> res2 = fixture.search("UserABC", "TÉlÉCOM", false, 10);
        assertEquals(37L, res2.get(0).getId().longValue());
    }

    @Test
    public void mutliWord() {
        Book book = new Book();
        book.setId(39L);
        book.setUserId("UserABC");
        book.setTitle("EN TERRE ETRANGERE");
        book.setTags(new HashSet<>());
        book.setUpdated(LocalDateTime.now());
        fixture.addToIndex(book);

        List<Book> res1 = fixture.search("UserABC", "En terre etrangère", false, 10);
        assertEquals(39L, res1.get(0).getId().longValue());

    }

    @Test
    public void wildcard() {
        Book book = new Book();
        book.setId(40L);
        book.setUserId("UserABC");
        book.setTitle("CRYPTONOMICON");
        book.setTags(new HashSet<>());
        book.setUpdated(LocalDateTime.now());
        fixture.addToIndex(book);

        List<Book> res1 = fixture.search("UserABC", "CRYPTO*", false, 10);
        assertEquals(40L, res1.get(0).getId().longValue());

    }

    @Test
    public void apostropheTest() {
        Book book = new Book();
        book.setId(41L);
        book.setUserId("UserABC");
        book.setTitle("L'étoile et le fouet");
        book.setTags(new HashSet<>());
        book.setUpdated(LocalDateTime.now());
        fixture.addToIndex(book);

        List<Book> res1 = fixture.search("UserABC", "étoile", false, 10);
        assertEquals(41L, res1.get(0).getId().longValue());
    }

    @Test
    public void userIdVisibilityTest() {
        Book book = new Book();
        book.setId(44L);
        book.setUserId("UserABC");
        book.setTitle("ABCDEFG");
        book.setTags(new HashSet<>());
        book.setUpdated(LocalDateTime.now());
        fixture.addToIndex(book);

        List<Book> res1 = fixture.search("AnotherUser", "ABCDEFG", false, 10);
        assertTrue(res1.isEmpty());
    }

    @Test
    public void returnAll() {
        Book book = new Book();
        book.setId(41L);
        book.setUserId("UserABC");
        book.setTitle("L'étoile et le fouet");
        book.setTags(new HashSet<>());
        book.setUpdated(LocalDateTime.now());
        fixture.addToIndex(book);

        List<Book> res1 = fixture.search("UserABC", "", false, 10);
        assertEquals(41L, res1.get(0).getId().longValue());
    }


    @Test
    public void addAndDelete() {
        Book book = new Book();
        book.setId(78L);
        book.setUserId("TEST");
        book.setTitle("WRONG BOOK");
        book.setTags(new HashSet<>());
        book.setUpdated(LocalDateTime.now());
        fixture.addToIndex(book);

        List<Book> res1 = fixture.search("TEST", "WRONG BOOK", false, 10);
        assertEquals(78L, res1.get(0).getId().longValue());

        fixture.removeFromIndex(78L);
        List<Book> res2 = fixture.search("TEST", "WRONG BOOK", false, 10);
        assertEquals(0, res2.size());
    }


}
