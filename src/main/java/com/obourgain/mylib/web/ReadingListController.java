package com.obourgain.mylib.web;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
	 * List of tags.
	 */
	@RequestMapping(value = "/tags", method = RequestMethod.GET)
	public String tagList(Model model) {
		log.info("Controller tagList");
		User user = getUserDetail();

		List<Tag> tags = tagRepository.findByUserId(user.getId());
		model.addAttribute("tags", tags);
		model.addAttribute("user", user);
		return "tagList";
	}

	/**
	 * Update of tag colors.
	 */
	@RequestMapping(value = "/updateTag", method = RequestMethod.GET)
	public String updateTag(Long tagId, String backgroundColor, String color, String borderColor) {
		User user = getUserDetail();

		log.info("Controller updateTag " + tagId + " with " + backgroundColor + "/" + color + "/" + borderColor);
		Tag tag = tagRepository.getOne(tagId);
		if (!tag.getUserId().equals(user.getId())) {
			log.error("Bad user id " + tag.getUserId() + " vs " + user.getId());
			throw new IllegalArgumentException("Not your stuff");
		}
		tag.setBackgroundColor(backgroundColor);
		tag.setColor(color);
		tag.setBorderColor(borderColor);
		tagRepository.save(tag);
		return "empty";
	}

	/**
	 * Delete a tag.
	 */
	@RequestMapping(value = "/deleteTag", method = RequestMethod.GET)
	public String deleteTag(Long tagId) {
		User user = getUserDetail();
		log.info("Controller deleteTag  " + tagId);
		Tag tag = tagRepository.getOne(tagId);
		if (tag == null) {
			log.warn("Tag not found for deletion " + tagId);
			return "empty";
		}
		if (!tag.getUserId().equals(user.getId())) {
			log.error("Bad user id " + tag.getUserId() + " vs " + user.getId());
			throw new IllegalArgumentException("Not your stuff");
		}

		tagService.deleteTag(tag);
		return "empty";
	}

	/**
	 * List of books for a reader.
	 */
	@RequestMapping(value = "/books", method = RequestMethod.GET)
	public String bookList(Model model, Pageable page) {
		log.info("Controller bookList");
		User user = getUserDetail();

		log.info("Pageable is " + page);

		Page<Book> books = bookRepository.findByUserId(user.getId(), page);
		model.addAttribute("books", books);
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
		List<Tag> alltags = tagRepository
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

		// Managing tags
		log.info("And the tags are : " + book.getTags());

		createOrUpdateBook(book, user);
		return "redirect:/books/";
	}

	/**
	 * If the book has an Id, and exists in database, update it. If not, create it.
	 */
	private void createOrUpdateBook(Book book, User user) {
		Book existing = bookRepository.findOne(book.getId());
		Set<Tag> tags = tagService.getTags(book.getTagString(), user.getId());

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
		LinkedHashMap<String, String> userdetail = (LinkedHashMap<String, String>) aut
				.getUserAuthentication()
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

	
	/**
	 * Stats.
	 */
	@RequestMapping(value = "/stats", method = RequestMethod.GET)
	public String stats(Model model) {
		log.info("Controller stats");
		User user = getUserDetail();

		return "stats";
	}
}
