package com.obourgain.mylib.util.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.obourgain.mylib.vobj.Book;
import com.obourgain.mylib.vobj.Tag;

public class LuceneSearchTest {

	private LuceneSearch fixture;

	@Before
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
		fixture.addToIndex(book);

		List<Book> res1 = fixture.search("UserABC", "testtitle", 10);
		assertEquals(35L, res1.get(0).getId().longValue());

		List<Book> res2 = fixture.search("UserABC", "testauthor", 10);
		assertEquals(35L, res2.get(0).getId().longValue());

		List<Book> res3 = fixture.search("UserABC", "testtag", 10);
		assertEquals(35L, res3.get(0).getId().longValue());
		assertEquals("12,", res3.get(0).getTagString());

		List<Book> res4 = fixture.search("UserABC", "testisbn", 10);
		assertEquals(35L, res4.get(0).getId().longValue());

		List<Book> res5 = fixture.search("UserABC", "testsubtitle", 10);
		assertEquals(35L, res5.get(0).getId().longValue());
	}

	@Test
	public void accentTest() {
		Book book = new Book();
		book.setId(32L);
		book.setUserId("UserABC");
		book.setTitle("TELECOM");
		book.setTags(new HashSet<>());
		fixture.addToIndex(book);

		List<Book> res1 = fixture.search("UserABC", "télécom", 10);
		assertEquals(32L, res1.get(0).getId().longValue());

		List<Book> res2 = fixture.search("UserABC", "TÉlÉCOM", 10);
		assertEquals(32L, res2.get(0).getId().longValue());
	}

	@Test
	public void accentTest2() {
		Book book = new Book();
		book.setId(37L);
		book.setUserId("UserABC");
		book.setTitle("télécom");
		book.setTags(new HashSet<>());
		fixture.addToIndex(book);

		List<Book> res1 = fixture.search("UserABC", "TELECOM", 10);
		assertEquals(37L, res1.get(0).getId().longValue());

		List<Book> res2 = fixture.search("UserABC", "TÉlÉCOM", 10);
		assertEquals(37L, res2.get(0).getId().longValue());
	}

	@Test
	public void mutliWord() {
		Book book = new Book();
		book.setId(39L);
		book.setUserId("UserABC");
		book.setTitle("EN TERRE ETRANGERE");
		book.setTags(new HashSet<>());
		fixture.addToIndex(book);

		List<Book> res1 = fixture.search("UserABC", "En terre etrangère", 10);
		assertEquals(39L, res1.get(0).getId().longValue());

	}

	@Test
	public void wildcard() {
		Book book = new Book();
		book.setId(40L);
		book.setUserId("UserABC");
		book.setTitle("CRYPTONOMICON");
		book.setTags(new HashSet<>());
		fixture.addToIndex(book);

		List<Book> res1 = fixture.search("UserABC", "CRYPTO*", 10);
		assertEquals(40L, res1.get(0).getId().longValue());

	}

	@Test
	public void apostropheTest() {
		Book book = new Book();
		book.setId(41L);
		book.setUserId("UserABC");
		book.setTitle("L'étoile et le fouet");
		book.setTags(new HashSet<>());
		fixture.addToIndex(book);

		List<Book> res1 = fixture.search("UserABC", "étoile", 10);
		assertEquals(41L, res1.get(0).getId().longValue());
	}
	
	@Test
	public void userIdVisibilityTest() {
		Book book = new Book();
		book.setId(44L);
		book.setUserId("UserABC");
		book.setTitle("ABCDEFG");
		book.setTags(new HashSet<>());
		fixture.addToIndex(book);

		List<Book> res1 = fixture.search("AnotherUser", "ABCDEFG", 10);
		assertTrue(res1.isEmpty());
	}

}