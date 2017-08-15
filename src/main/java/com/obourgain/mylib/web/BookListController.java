package com.obourgain.mylib.web;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.obourgain.mylib.service.BookService;
import com.obourgain.mylib.vobj.Book;
import com.obourgain.mylib.vobj.Tag;
import com.obourgain.mylib.vobj.User;

@Controller
public class BookListController extends AbstractController {

	private static Logger log = LogManager.getLogger(BookListController.class);

	private BookService bookService;

	@Autowired
	public BookListController(BookService bookService) {
		this.bookService = bookService;
	}

	/**
	 * List of books for a reader.
	 */
	@RequestMapping(value = "/books", method = RequestMethod.GET)
	public String bookList(Model model, Pageable page) {
		log.info("Controller bookList");
		User user = getUserDetail();

		log.info("Pageable is " + page);

		Page<Book> books = bookService.findByUserId(user.getId(), page);

		List<Integer> pagination = computePagination(books);
		log.info(pagination);
		model.addAttribute("books", books);
		model.addAttribute("user", user);
		model.addAttribute("pagination", pagination);
		return "bookList";
	}

	private static final int PAGINATION_GAP = -1;

	/**
	 * Return a list of pages to be display in the pagination bar.
	 * 
	 * This method is in the controller to avoid to many code in the template. It could be moved to an utility class if more lists are added in the application.
	 */
	private List<Integer> computePagination(Page<? extends Object> list) {
		if (list.getTotalPages() <= 7)
			// Will print all page number between 0 and totalPage - 1
			return IntStream.rangeClosed(0, list.getTotalPages() - 1).boxed().collect(Collectors.toList());

		// More than 7 pages, will return:
		// (- is a place holder indicating a gap in the sequence)
		// 012345- if page <= 3
		// -23456- if page > 3 and < total - 4
		// -456789 if page >= total - 4
		int page = list.getNumber();
		int last = list.getTotalPages() - 1;
		List<Integer> res = new ArrayList<>();
		if (page <= 3) {
			res.addAll(IntStream.rangeClosed(0, 5).boxed().collect(Collectors.toList()));
			res.add(PAGINATION_GAP);
		} else if (page < list.getTotalPages() - 4) {
			res.add(PAGINATION_GAP);
			res.addAll(IntStream.rangeClosed(page - 2, page + 2).boxed().collect(Collectors.toList()));
			res.add(PAGINATION_GAP);
		} else {
			res.add(PAGINATION_GAP);
			res.addAll(IntStream.rangeClosed(last - 5, last).boxed().collect(Collectors.toList()));
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
	public String isbnlookup(String isbn, Model model) {
		log.info("Controller isbnlookup with " + isbn);
		User user = getUserDetail();

		if (StringUtils.isBlank(isbn)) {
			log.info("No param");
			return "redirect:/books/";
		}

		Book book = bookService.isbnLookup(user, isbn);
		if (book == null) {
			model.addAttribute("alertWarn", "No book found for isbn <strong>" + isbn + "</strong>");
			return bookList(model, null);
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

}
