package com.obourgain.mylib.web;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import com.obourgain.mylib.vobj.Book;
import com.obourgain.mylib.vobj.Tag;
import com.obourgain.mylib.vobj.User;

@Controller
@RequestMapping("/")
public class ReadingListController {
	private static Logger log = LogManager.getLogger(ReadingListController.class);

	private BookRepository bookRepository;
	private TagRepository tagRepository;

	@Autowired
	public ReadingListController(BookRepository bookRepository, TagRepository tagRepository) {
		this.bookRepository = bookRepository;
		this.tagRepository = tagRepository;
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
		// - a list of tags
		// - a map Tag.Id -> Tag
		// - a map Book.Id -> List of Tags.Id
		List<Tag> allTags = tagRepository.findByUserId(user.getId());
		Map<Long, Tag> tagMap = allTags.stream().collect(Collectors.toMap(Tag::getId, Function.identity()));
		Map<Long, List<Long>> bookTagMap = books.stream()
				.collect(Collectors.toMap(Book::getId, book -> getTagIdList(book)));

		model.addAttribute("alltags", allTags);
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

		List<Tag> tagList = tagRepository.findByUserId(user.getId()).stream()
				.sorted(Comparator.comparing(Tag::getText))
				.collect(Collectors.toList());

		model.addAttribute("alltags", tagList);
		model.addAttribute("tags", getTagIdList(b));
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
		List<String> tagIds = getTagIds(book.getTags(), user.getId());

		// Creating book
		Book b = bookRepository.findOne(book.getId());
		b.setTitle(book.getTitle());
		b.setAuthor(book.getAuthor());
		b.setIsbn(book.getIsbn());
		b.setPages(book.getPages());
		b.setPublisher(book.getPublisher());
		b.setTags(String.join(",", tagIds));
		b.setComment(book.getComment());
		b.setUpdated(LocalDateTime.now());

		bookRepository.save(b);
		return "redirect:/books/";
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

	/**
	 * Transform a list of tag names, to a list a tag Ids. If a tag doesn't
	 * exists, create it in the Tag table.
	 * 
	 * @param tags
	 *            The tag list, e.g. "SF,Cycle Fondation"
	 * @return the list of Ids of the tags.
	 */
	private List<String> getTagIds(String in, String userId) {
		if (in == null || in.trim().length() == 0)
			return new ArrayList<>();
		List<String> texts = Arrays.asList(in.split(","));
		List<String> res = new ArrayList<>();

		List<Tag> alltags = tagRepository.findByUserId(userId);

		alltags.stream().forEach(System.out::println);
		for (String text : texts) {
			Tag t = alltags.stream()
					.filter(x -> x.getText().equals(text.trim()))
					.findFirst()
					.orElseGet(() -> createNewTag(text, userId));
			res.add(t.getId().toString());
		}
		return res;
	}

	/**
	 * Create a new tag in database, and return it.
	 */
	private Tag createNewTag(String text, String userId) {
		Tag t = new Tag();
		t.setText(text.trim());
		t.setUserId(userId);
		t.setBackgroundColor("#E7E7E7");
		t.setColor("#464646");
		t.setBorderColor("#464646");
		log.info("Creating tag " + t);
		tagRepository.save(t);
		log.info("Created tag " + t);
		return t;
	}

	/**
	 * Return the list of tags of a book, as a List<Long>.
	 */
	private List<Long> getTagIdList(Book book) {
		if (StringUtils.isBlank(book.getTags()))
			return new ArrayList<>();
		return Stream.of(book.getTags().split(","))
				.map(Long::parseLong)
				.collect(Collectors.toList());
	}

}
