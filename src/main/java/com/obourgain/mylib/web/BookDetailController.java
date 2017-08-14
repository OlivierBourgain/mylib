package com.obourgain.mylib.web;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.obourgain.mylib.service.BookService;
import com.obourgain.mylib.service.TagService;
import com.obourgain.mylib.vobj.Book;
import com.obourgain.mylib.vobj.Tag;
import com.obourgain.mylib.vobj.User;

/**
 * Controller for the book detail page.
 */
@Controller
public class BookDetailController extends AbstractController {
	private static Logger log = LogManager.getLogger(BookDetailController.class);

	private BookService bookService;
	private TagService tagService;

	@Autowired
	public BookDetailController(BookService bookService, TagService tagService) {
		this.bookService = bookService;
		this.tagService = tagService;
	}

	/**
	 * Get the detail of a book.
	 */
	@RequestMapping(value = "/book/{bookId}", method = RequestMethod.GET)
	public String bookDetail(@PathVariable("bookId") long bookId, Model model) {
		log.info("Controller bookDetail");
		User user = getUserDetail();

		Book b = bookService.findBook(user.getId(), bookId);
		if (b == null) {
			b = new Book();
		}
		log.info("Book detail " + b);

		// Tag list, sorted by Text.
		List<Tag> alltags = tagService
				.findByUserId(user.getId()).stream()
				.sorted(Comparator.comparing(Tag::getText))
				.collect(Collectors.toList());

		// List of tag Ids for this book
		Set<Long> tagids = b.getTags().stream().map(Tag::getId).collect(Collectors.toSet());

		model.addAttribute("user", user);
		model.addAttribute("book", b);
		model.addAttribute("alltags", alltags);
		model.addAttribute("tagids", tagids);
		return "bookDetail";
	}

	/**
	 * Update book.
	 */
	@RequestMapping(value = "/book/{bookId}", method = RequestMethod.POST, params = "action=save")
	public String updateBook(@PathVariable("bookId") Long bookId, Book book) {
		log.info("Controller updateBook");
		User user = getUserDetail();

		log.info("And the tags are : " + book.getTags());
		Set<Tag> tags = tagService.getTags(book.getTagString(), user.getId());

		bookService.createOrUpdateBook(book, user, tags);
		return "redirect:/books/";
	}

	/**
	 * Update book.
	 */
	@RequestMapping(value = "/book/{bookId}", method = RequestMethod.POST, params = "action=readnow")
	public String updateBookReading(@PathVariable("bookId") Long bookId) {
		log.info("Controller updateBookReading");
		User user = getUserDetail();

		int year = LocalDate.now().getYear();
		log.info("Book " + bookId + " was read in " + year);

		Book book = bookService.updateBookReading(user, bookId, year);
		if (book == null) { return "redirect:/books/"; }
		return "redirect:/book/" + book.getId();
	}

	/**
	 * Delete book.
	 */
	@RequestMapping(value = "/book/{bookId}", method = RequestMethod.POST, params = "action=delete")
	public String deleteBook(@PathVariable("bookId") Long bookId) {
		log.info("Controller deleteBook " + bookId);
		User user = getUserDetail();
		bookService.deleteBook(user.getId(), bookId);
		return "redirect:/books/";
	}
	

	/**
	 * Create book.
	 */
	@RequestMapping(value = "/book", method = RequestMethod.POST, params = "action=create")
	public String createBook(Book book) {
		log.info("Controller createBook");
		User user = getUserDetail();

		log.info("And the tags are : " + book.getTagString());
		Set<Tag> tags = tagService.getTags(book.getTagString(), user.getId());

		bookService.createOrUpdateBook(book, user, tags);
		return "redirect:/books/";
	}

}
