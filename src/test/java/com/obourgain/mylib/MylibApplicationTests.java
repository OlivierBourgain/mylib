package com.obourgain.mylib;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

import com.obourgain.mylib.db.BookRepository;
import com.obourgain.mylib.vobj.Book;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MylibApplicationTests {

	@Test
	public void contextLoads() {
	}
	
	
	@Autowired
	public BookRepository bookRepository;
	
	@Test
	public void testBooks() {
		PageRequest page = new PageRequest(0, 5);
		Page<Book> books = bookRepository.findByUserId("111025875593718684632", page);
		assertTrue(books.getSize() > 0);
	}

}
