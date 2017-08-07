package com.obourgain.mylib.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.obourgain.mylib.db.BookRepository;
import com.obourgain.mylib.ext.amazon.ItemLookupAmazon;
import com.obourgain.mylib.vobj.Book;
import com.obourgain.mylib.vobj.Tag;
import com.obourgain.mylib.vobj.User;

/**
 * Business logic for book management.
 */
@Service
public class BookService {
	private static Logger log = LogManager.getLogger(BookService.class);

	private BookRepository bookRepository;

	@Autowired
	public BookService(BookRepository bookRepository) {
		this.bookRepository = bookRepository;
	}

	/**
	 * Return the list of books for a user.
	 */
	public List<Book> findByUserId(String userId) {
		return bookRepository.findByUserId(userId);
	}

	/**
	 * Return the list of books for a user.
	 */
	public Page<Book> findByUserId(String userId, Pageable page) {
		return bookRepository.findByUserId(userId, page);
	}

	/**
	 * Return a book.
	 * 
	 * Returns null if the book is not found, or if the book is not linked to
	 * the user.
	 */
	public Book findBook(String userId, long bookId) {
		Book b = bookRepository.findOne(bookId);
		if (b == null) return null;
		if (!b.getUserId().equals(userId)) {
			log.warn("Access error to " + b + " from " + userId);
			throw new IllegalArgumentException("User " + userId + " cannot delete " + b);
		}
		return b;
	}

	/**
	 * Delete a book.
	 */
	public void deleteBook(String userId, Long bookId) {
		Book b = findBook(userId, bookId);
		if (b == null) return;
		bookRepository.delete(b);
	}
	
	
	/**
	 * If the book has an Id, and exists in database, update it. If not, create
	 * it.
	 */
	public void createOrUpdateBook(Book book, User user, Set<Tag> tags) {
		Book existing = findBook(user.getId(), book.getId());
		if (existing != null) {
			existing.setTitle(book.getTitle());
			existing.setAuthor(book.getAuthor());
			existing.setIsbn(book.getIsbn());
			existing.setPages(book.getPages());
			existing.setPublisher(book.getPublisher());
			existing.setTags(tags);
			existing.setComment(book.getComment());
			existing.setUpdated(LocalDateTime.now());
			log.info("Updating book " + existing.deepToString());
			bookRepository.save(existing);
		} else {
			book.setUserId(user.getId());
			book.setCreated(LocalDateTime.now());
			book.setUpdated(LocalDateTime.now());
			book.setTags(new HashSet<Tag>());
			log.info("Creating book " + book.deepToString());
			bookRepository.save(existing);
		}
	}
	
	/**
	 * Create a book from its ISBN Number.
	 * The ISBN can be an ISBN10 or ISBN13.
	 * 
	 * Return null if the amazon's page for this book can't be found.
	 */
	public Book isbnLookup(User user, String isbn) {
		Book book = ItemLookupAmazon.lookup(isbn);
		if (book == null) {
			log.info("No book found");
			return null;
		}
		book.setUserId(user.getId());
		book.setCreated(LocalDateTime.now());
		book.setUpdated(LocalDateTime.now());

		bookRepository.save(book);
		return book;
	}
}
