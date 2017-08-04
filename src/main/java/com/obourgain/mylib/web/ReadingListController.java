package com.obourgain.mylib.web;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.obourgain.mylib.db.BookRepository;
import com.obourgain.mylib.db.TagRepository;
import com.obourgain.mylib.ext.amazon.ItemLookupAmazon;
import com.obourgain.mylib.service.TagService;
import com.obourgain.mylib.vobj.Book;
import com.obourgain.mylib.vobj.Tag;
import com.obourgain.mylib.vobj.User;

@Controller
@RequestMapping("/")
public class ReadingListController {
	private static Logger log = LogManager.getLogger(ReadingListController.class);

	private BookRepository bookRepository;
	private TagRepository tagRepository;
	private TagService tagService;

	@Autowired
	public ReadingListController(BookRepository bookRepository, TagRepository tagRepository, TagService tagService) {
		this.bookRepository = bookRepository;
		this.tagRepository = tagRepository;
		this.tagService = tagService;
	}

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home() {
		return "home";
	}

	/**
	 * List of books for a reader.
	 */
	@RequestMapping(value = "/books", method = RequestMethod.GET)
	public String readersBooks(Model model) {
		log.info("Controller readersBooks");
		User user = getUserDetail();

		List<Book> books = bookRepository.findByUserId(user.getId());
		model.addAttribute("books", books);

		// Tag management
		// To simplify the template code, we need:
		// - a map Tag.Id -> Tag
		// - a map Book.Id -> List of Tags.Id
		List<Tag> allTags = tagRepository.findByUserId(user.getId());
		Map<Long, Tag> tagMap = allTags.stream().collect(Collectors.toMap(Tag::getId, Function.identity()));
		Map<Long, List<Long>> bookTagMap = books.stream()
				.collect(Collectors.toMap(Book::getId, book -> tagService.getTagIdList(book)));

		model.addAttribute("tagMap", tagMap);
		model.addAttribute("bookTagMap", bookTagMap);

		model.addAttribute("user", user);
		return "bookList";
	}

	/**
	 * Get the detail of a book.
	 */
	@RequestMapping(value = "/book/{bookId}", method = RequestMethod.GET)
	public String bookDetail(@PathVariable("bookId") long bookId, Model model) {
		log.info("Controller bookDetail");
		User user = getUserDetail();

		Book b = bookRepository.findOne(bookId);
		if (b == null) {
			b = new Book();
		}
		log.info("Book detail " + b);

		// Tag list, sorted by Text.
		List<Tag> tagList = tagRepository.findByUserId(user.getId()).stream()
				.sorted(Comparator.comparing(Tag::getText))
				.collect(Collectors.toList());

		model.addAttribute("alltags", tagList);
		model.addAttribute("tags", tagService.getTagIdList(b));
		model.addAttribute("book", b);
		model.addAttribute("user", user);
		return "bookDetail";
	}

	/**
	 * Update book.
	 */
	@RequestMapping(value = "/book/{bookId}", method = RequestMethod.POST, params = "action=save")
	public String updateBook(@PathVariable("bookId") Long bookId, Book book) {
		log.info("Controller updateBook");
		User user = getUserDetail();

		// Managing tags
		log.info("And the tags are : " + book.getTags());

		createOrUpdateBook(book, user);
		return "redirect:/books/";
	}

	/**
	 * If the book has an Id, and exists in database, update it.
	 * If not, create it.
	 */
	private void createOrUpdateBook(Book book, User user) {
		Book existing = bookRepository.findOne(book.getId());
		List<String> tagIds = tagService.getTagIds(book.getTags(), user.getId());

		if (existing != null) {
			existing.setTitle(book.getTitle());
			existing.setAuthor(book.getAuthor());
			existing.setIsbn(book.getIsbn());
			existing.setPages(book.getPages());
			existing.setPublisher(book.getPublisher());
			existing.setTags(String.join(",", tagIds));
			existing.setComment(book.getComment());
			existing.setUpdated(LocalDateTime.now());
			log.info("Updating book " + existing.deepToString());
			bookRepository.save(existing);
		} else {
			book.setUserId(user.getId());
			book.setCreated(LocalDateTime.now());
			book.setUpdated(LocalDateTime.now());
			book.setTags(String.join(",", tagIds));
			log.info("Creating book " + book.deepToString());
			bookRepository.save(existing);
		}
	}

	/**
	 * Delete book.
	 */
	@RequestMapping(value = "/book/{bookId}", method = RequestMethod.POST, params = "action=delete")
	public String deleteBook(@PathVariable("bookId") Long bookId) {
		log.info("Controller deleteBook " + bookId);
		bookRepository.delete(bookId);
		return "redirect:/books/";
	}

	/**
	 * Lookup a book with ISBN
	 */
	@RequestMapping(value = "/isbnlookup", method = RequestMethod.POST)
	public String isbnlookup(String isbn) {
		log.info("Controller isbnlookup with " + isbn);
		User user = getUserDetail();

		if (StringUtils.isBlank(isbn)) {
			log.info("No param");
			return "redirect:/books/";
		}

		Book book = ItemLookupAmazon.lookup(isbn);
		if (book == null) {
			log.info("No book found");
			return "redirect:/books/";
		}
		book.setUserId(user.getId());
		book.setCreated(LocalDateTime.now());
		book.setUpdated(LocalDateTime.now());

		bookRepository.save(book);
		return "redirect:/book/" + book.getId();
	}

	// Business logic.
	private User getUserDetail() {
		OAuth2Authentication aut = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
		@SuppressWarnings("unchecked")
		LinkedHashMap<String, String> userdetail = (LinkedHashMap<String, String>) aut.getUserAuthentication()
				.getDetails();
		User user = new User();
		user.setId(userdetail.get("sub"));
		user.setName(userdetail.get("name"));
		user.setFirstName(userdetail.get("given_name"));
		user.setLastName(userdetail.get("family_name"));
		user.setEmail(userdetail.get("email"));
		user.setPicture(userdetail.get("picture"));
		user.setProfile(userdetail.get("profile"));
		user.setLocale(userdetail.get("locale"));
		user.setGender(userdetail.get("gender"));
		return user;
	}

}
