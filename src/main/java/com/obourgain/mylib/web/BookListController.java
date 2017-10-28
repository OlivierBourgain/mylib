package com.obourgain.mylib.web;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.obourgain.mylib.service.BookService;
import com.obourgain.mylib.service.TagService;
import com.obourgain.mylib.util.HttpRequestUtil;
import com.obourgain.mylib.util.search.LuceneSearch;
import com.obourgain.mylib.vobj.Book;
import com.obourgain.mylib.vobj.Tag;
import com.obourgain.mylib.vobj.User;

/**
 * Controllers for the book list page.
 */
@Controller
public class BookListController extends AbstractController {

	private static final int MAX_RESULTS = 100000;

	private static Logger log = LogManager.getLogger(BookListController.class);

	@Autowired
	private BookService bookService;
	@Autowired
	private TagService tagService;
	@Autowired
	private HttpSession httpSession;
	@Autowired
	private LuceneSearch luceneSearch;

	/**
	 * List of books for a reader.
	 */
	@RequestMapping(value = "/books", method = RequestMethod.GET)
	public String bookList(
			HttpServletRequest request,
			Model model,
			@SortDefault.SortDefaults({ @SortDefault(sort = "Updated", direction = Sort.Direction.DESC) }) Pageable page) {
		log.info("Controller bookList");
		User user = getUserDetail();

		String searchCriteria = request.getParameter("criteria");
		Boolean showDiscarded = HttpRequestUtil.getParamAsBoolean(request, "showDisc");
		Pageable cachedPage = (Pageable) httpSession.getAttribute("bookListPageable");
		String cachedSearchCriteria = (String) httpSession.getAttribute("bookListSearchCriteria");
		Boolean cachedShowDiscarded = (Boolean) httpSession.getAttribute("bookListShowDiscarded");

		log.info("Request Pageable is " + page);
		log.info("Cached  Pageable is " + cachedPage);
		log.info("searchCriteria " + searchCriteria);

		if (request.getParameter("page") == null
				&& request.getParameter("sort") == null
				&& request.getParameter("size") == null
				&& searchCriteria == null
				&& cachedPage != null) {
			String params = getParams(cachedPage, cachedSearchCriteria, cachedShowDiscarded);
			return "redirect:/books?" + params;
		}
		httpSession.setAttribute("bookListPageable", page);
		httpSession.setAttribute("bookListSearchCriteria", searchCriteria);
		httpSession.setAttribute("bookListShowDiscarded", showDiscarded);

		Page<Book> books;
		// Use Lucene
		log.info("Getting the list of books from Lucene");
		books = getBooks(searchCriteria, showDiscarded, page, user);

		List<Integer> pagination = computePagination(books);
		model.addAttribute("pagination", pagination);
		model.addAttribute("searchCriteria", searchCriteria);
		model.addAttribute("showDiscarded", showDiscarded);
		model.addAttribute("sort", books.getSort() == null ? null : books.getSort().iterator().next());
		model.addAttribute("books", books);
		model.addAttribute("user", user);
		return "bookList";
	}

	// Generate the URL parameters that are equivalent to the given Pageable.
	// Used when a Pageable is in session, and we want to reapply it.
	private String getParams(Pageable cachedPage, String searchCriteria, Boolean showDiscarded) {
		String params = "page=" + cachedPage.getPageNumber() + "&size=" + cachedPage.getPageSize();
		if (cachedPage.getSort() != null) {
			Order order = cachedPage.getSort().iterator().next();
			params += "&sort=" + order.getProperty();
			if (order.isDescending()) params += ",DESC";
		}
		if (StringUtils.isNotBlank(searchCriteria)) {
			try {
				params += "&criteria=" + URLEncoder.encode(searchCriteria, StandardCharsets.UTF_8.name());
			} catch (UnsupportedEncodingException e) {
			}
		}
		if (showDiscarded == true) {
			params += "&showDisc=true";
		}
		return params;
	}

	/**
	 * Get the list of book from Lucene, as a Page<Book>.
	 */
	private Page<Book> getBooks(String criteria, boolean showDiscarded, Pageable page, User user) {
		Map<Long, Tag> alltags = tagService
				.findByUserId(user.getId()).stream()
				.collect(Collectors.toMap(Tag::getId, Function.identity()));

		List<Book> luceneBooks = luceneSearch.search(user.getId(), criteria, showDiscarded, MAX_RESULTS);
		fixTags(luceneBooks, alltags);

		// Apply the pageable (sort)
		if (page.getSort() != null) {
			Order order = page.getSort().iterator().next();
			Comparator<Book> comparator = Comparator.comparing(Book::getTitle);
			switch (order.getProperty()) {
			case "Title":
				comparator = Comparator.comparing(Book::getTitle);
				break;
			case "Author":
				comparator = Comparator.comparing(Book::getAuthor);
				break;
			case "Pages":
				comparator = Comparator.comparing(Book::getPages);
				break;
			case "Updated":
				comparator = Comparator.comparing(Book::getUpdated);
				break;
			case "Tags":
				comparator = new TagListComparator();
				break;
			}
			if (order.isDescending()) comparator = comparator.reversed();
			Collections.sort(luceneBooks, comparator);
		}

		// Apply the pageable (size, and page)
		int start = page.getOffset();
		Pageable newPage = page;
		if (start > luceneBooks.size()) {
			start = 0;
			newPage = new PageRequest(0, page.getPageSize(), page.getSort());
		}
		int end = (start + page.getPageSize()) > luceneBooks.size() ? luceneBooks.size() : (start + page.getPageSize());
		log.info(page.getOffset() + "/" + page.getPageNumber() + "/" + page.getPageSize());
		log.info(start + "/" + end);
		Page<Book> books = new PageImpl<Book>(luceneBooks.subList(start, end), newPage, luceneBooks.size());
		return books;
	}

	/**
	 * Fix the tags.
	 * 
	 * In the lucene search, tags are stored in Book.tagString, as a list of Ids (e.g. "1,23,54"). We need to replace that and compute Book.tag, which contains
	 * a Set of Tag objects. In order to have a nice display, we also want the Book.tag set to be ordered by Tag.priority desc.
	 */
	private void fixTags(List<Book> luceneBooks, Map<Long, Tag> alltags) {

		Pattern pattern = Pattern.compile(",");
		luceneBooks.stream().forEach(
				book -> {
					Set<Tag> tags = pattern
							.splitAsStream(book.getTagString())
							.map(Long::valueOf)
							.map(x -> alltags.get(x))
							.collect(Collectors.toSet());
					book.setTags(new TreeSet<>(tags));
				});
	}

	/**
	 * Compare two books based on the their list of tags
	 */
	public class TagListComparator implements Comparator<Book> {

		@Override
		public int compare(Book o1, Book o2) {
			Iterator<Tag> tags1 = o1.getTags().iterator();
			Iterator<Tag> tags2 = o2.getTags().iterator();
			while (tags1.hasNext() && tags2.hasNext()) {
				int c = tags1.next().compareTo(tags2.next());
				if (c != 0) return c;
			}
			if (tags1.hasNext()) return 1;
			if (tags2.hasNext()) return -1;
			return 0;
		}

	}

	/**
	 * Go to create a book page.
	 */
	@RequestMapping(value = "/books/create", method = RequestMethod.GET)
	public String bookCreate(Model model) {
		log.info("Controller bookNew");
		return "bookCreate";
	}

	/**
	 * Lookup a book with ISBN
	 */
	@RequestMapping(value = "/isbnlookup", method = RequestMethod.POST)
	public String isbnlookup(HttpServletRequest request, String isbn, Model model) {
		log.info("Controller isbnlookup with " + isbn);
		User user = getUserDetail();

		if (StringUtils.isBlank(isbn)) {
			log.info("No param");
			return "redirect:/books/";
		}

		Book book = null;
		if (isbn.startsWith("asin")) {
			String asin = isbn.substring(isbn.indexOf('=') + 1);
			book = bookService.asinLookup(user, asin);
		} else {
			book = bookService.isbnLookup(user, isbn);
		}
		if (book == null) {
			model.addAttribute("alertWarn", "No book found for isbn <strong>" + isbn + "</strong>");
			return bookList(request, model, null);
		}
		return "redirect:/book/" + book.getId();
	}

	/**
	 * Export de la liste des livres au format CSV.
	 */
	@RequestMapping(value = "/books/exportcsv", method = RequestMethod.GET)
	public void exportcsv(HttpServletResponse response) throws IOException {
		log.info("Controller export");
		User user = getUserDetail();

		response.setContentType("text/csv");
		String headerKey = "Content-Disposition";
		String headerValue = "attachment; filename=\"Library.csv\"";
		response.setHeader(headerKey, headerValue);

		List<Book> books = bookService.findByUserId(user.getId());
		StringBuilder sb = booksToCsv(books);
		response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
		response.getWriter().print(sb.toString());
		return;
	}

	private StringBuilder booksToCsv(List<Book> books) {
		StringBuilder sb = new StringBuilder();
		sb.append(
				"Id;Status;Title;Subtitle;Author;ISBN;Publisher;PublicationDate;Pages;Tags;Lang;Created;Updated;SmallImage;MediumImage;LargeImage;Description;Comment\n");
		for (Book book : books) {
			sb.append(book.getId()).append(";");
			sb.append(book.getStatus() == null ? "" : book.getStatus()).append(";");
			sb.append(book.getTitle()).append(";");
			sb.append(string(book.getSubtitle())).append(";");
			sb.append(book.getAuthor()).append(";");
			sb.append(book.getIsbn()).append(";");
			sb.append(book.getPublisher()).append(";");
			sb.append(book.getPublicationDate()).append(";");
			sb.append(book.getPages()).append(";");
			for (Tag tag : book.getTags())
				sb.append(tag.getText()).append(",");
			sb.append(";");
			sb.append(string(book.getLang())).append(";");
			sb.append(date(book.getCreated())).append(";");
			sb.append(date(book.getUpdated())).append(";");
			sb.append(string(book.getSmallImage())).append(";");
			sb.append(string(book.getMediumImage())).append(";");
			sb.append(string(book.getLargeImage())).append(";");
			sb.append(string(book.getDescription())).append(";");
			sb.append(string(book.getComment())).append(";");

			sb.append("\n");
		}
		return sb;
	}

	private String date(LocalDateTime date) {
		if (date == null) return "";
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
		return date.format(formatter);
	}

	private String string(String text) {
		return text == null ? "" : text;
	}

	/**
	 * Recrée l'index de recherche.
	 */
	@RequestMapping(value = "/books/rebuildindex", method = RequestMethod.GET)
	public String rebuildindex(HttpServletResponse response) throws IOException {
		log.info("Controller rebuildindex");
		User user = getUserDetail();

		List<Book> books = bookService.findByUserId(user.getId());
		luceneSearch.clearIndex();
		luceneSearch.addAll(books);
		return "redirect:/books/";
	}

}
