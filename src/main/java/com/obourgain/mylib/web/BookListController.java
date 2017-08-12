package com.obourgain.mylib.web;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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
	public String bookList(Model model) {
		log.info("Controller bookList");
		User user = getUserDetail();

		List<Book> books = bookService.findByUserId(user.getId());
		model.addAttribute("books", books);
		model.addAttribute("user", user);
		return "bookList";
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
			model.addAttribute("alertWarn", "No book found for isbn <strong>" + isbn + "</strong>" );
			return bookList(model);
		}
		return "redirect:/book/" + book.getId();
	}



	/**
	 * Export de la liste des livres au format CSV.
	 */
	@RequestMapping(value = "/exportcsv", method = RequestMethod.GET)
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
			sb.append(book.getStatus()).append(";");
			sb.append(book.getTitle()).append(";");
			sb.append(book.getSubtitle()).append(";");
			sb.append(book.getAuthor()).append(";");
			sb.append(book.getIsbn()).append(";");
			sb.append(book.getPublisher()).append(";");
			sb.append(book.getPublicationDate()).append(";");
			sb.append(book.getPages()).append(";");
			for (Tag tag : book.getTags())
				sb.append(tag.getText()).append(",");
			sb.append(";");
			sb.append(book.getLang()).append(";");
			sb.append(book.getCreated()).append(";");
			sb.append(book.getUpdated()).append(";");
			sb.append(book.getSmallImage()).append(";");
			sb.append(book.getMediumImage()).append(";");
			sb.append(book.getLargeImage()).append(";");
			sb.append(string(book.getDescription())).append(";");
			sb.append(string(book.getComment())).append(";");

			sb.append("\n");
		}
		return sb;
	}

	private String string(String text) {
		return text == null ? "" : text;
	}

}
