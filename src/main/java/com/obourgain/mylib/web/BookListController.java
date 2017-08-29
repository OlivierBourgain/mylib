package com.obourgain.mylib.web;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
import java.util.stream.IntStream;

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
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.obourgain.mylib.service.BookService;
import com.obourgain.mylib.service.TagService;
import com.obourgain.mylib.util.search.LuceneSearch;
import com.obourgain.mylib.vobj.Book;
import com.obourgain.mylib.vobj.Tag;
import com.obourgain.mylib.vobj.User;

@Controller
public class BookListController extends AbstractController {

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
	public String bookList(HttpServletRequest request, Model model, Pageable page) {
		log.info("Controller bookList");
		User user = getUserDetail();

		Pageable cachedPage = (Pageable) httpSession.getAttribute("bookListPageable");
		String searchCriteria = request.getParameter("criteria");

		log.info("Request Pageable is " + page);
		log.info("Cached  Pageable is " + cachedPage);
		log.info("searchCriteria " + searchCriteria);

		if (request.getParameter("page") == null
				&& request.getParameter("sort") == null
				&& request.getParameter("size") == null
				&& searchCriteria == null
				&& cachedPage != null) {
			String params = getParams(cachedPage);
			return "redirect:/books?" + params;
		}
		httpSession.setAttribute("bookListPageable", page);

		Page<Book> books;
		// Use Lucene
		log.info("Getting the list of books from Lucene");
		books = getBooks(searchCriteria, page, user);

		List<Integer> pagination = computePagination(books);
		model.addAttribute("pagination", pagination);
		model.addAttribute("searchCriteria", searchCriteria);
		model.addAttribute("sort", books.getSort() == null ? null : books.getSort().iterator().next());
		model.addAttribute("books", books);
		model.addAttribute("user", user);
		return "bookList";
	}

	// Generate the URL parameters that are equivalent to the given Pageable.
	// Used when a Pageable is in session, and we want to reapply it.
	private String getParams(Pageable cachedPage) {
		String params = "page=" + cachedPage.getPageNumber() + "&size=" + cachedPage.getPageSize();
		if (cachedPage.getSort() != null) {
			Order order = cachedPage.getSort().iterator().next();
			params += "&sort=" + order.getProperty();
			if (order.isDescending()) params += ",DESC";
		}
		return params;
	}

	/**
	 * Get the list of book from Lucene, as a Page<Book>.
	 */
	private Page<Book> getBooks(String criteria, Pageable page, User user) {
		List<Book> luceneBooks = luceneSearch.search(user.getId(), criteria, 100000);

		// Fix the tags
		Map<Long, Tag> alltags = tagService
				.findByUserId(user.getId()).stream()
				.collect(Collectors.toMap(Tag::getId, Function.identity()));

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

	/** Compare two books based on the their list of tags */
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

	private static final int PAGINATION_GAP = -1;

	/**
	 * Return a list of pages to be display in the pagination bar.
	 * 
	 * This method is in the controller to avoid to many code in the template. It could be moved to an utility class if more lists are added in the application.
	 */
	private List<Integer> computePagination(Page<? extends Object> list) {
		if (list.getTotalPages() <= 9)
			// Will print all page number between 0 and totalPage - 1
			return IntStream.rangeClosed(0, list.getTotalPages() - 1).boxed().collect(Collectors.toList());

		// More than 9 pages, will return:
		// (- is a place holder indicating a gap in the sequence)
		// 01234567- if page <= 4
		// -3456789- if page > 4 and < total - 5
		// -23456789 if page >= total - 5
		int page = list.getNumber();
		int last = list.getTotalPages() - 1;
		List<Integer> res = new ArrayList<>();
		if (page <= 4) {
			res.addAll(IntStream.rangeClosed(0, 7).boxed().collect(Collectors.toList()));
			res.add(PAGINATION_GAP);
		} else if (page < list.getTotalPages() - 5) {
			res.add(PAGINATION_GAP);
			res.addAll(IntStream.rangeClosed(page - 3, page + 3).boxed().collect(Collectors.toList()));
			res.add(PAGINATION_GAP);
		} else {
			res.add(PAGINATION_GAP);
			res.addAll(IntStream.rangeClosed(last - 7, last).boxed().collect(Collectors.toList()));
		}
		return res;
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
