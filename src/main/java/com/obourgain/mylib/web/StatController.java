package com.obourgain.mylib.web;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.obourgain.mylib.service.BookService;
import com.obourgain.mylib.service.StatService;
import com.obourgain.mylib.service.StatService.StatData;
import com.obourgain.mylib.vobj.Book;
import com.obourgain.mylib.vobj.User;

/**
 * Controller for the stat page.
 */
@Controller
public class StatController extends AbstractController {
	private static Logger log = LogManager.getLogger(StatController.class);

	private BookService bookService;
	private StatService statService;

	@Autowired
	public StatController(BookService bookService, StatService statService) {
		this.bookService = bookService;
		this.statService = statService;
	}

	/**
	 * Stats.
	 */
	@RequestMapping(value = "/stats", method = RequestMethod.GET)
	public String stats(Model model) {
		log.info("Controller stats");
		User user = getUserDetail();

		List<Book> allBooks = bookService.findByUserId(user.getId());
		model.addAttribute("nbBooks", allBooks.size());
		model.addAttribute("nbPages", allBooks.stream().mapToInt(Book::getPages).sum());

		Map<String, List<StatData>> stats = statService.getAllStat(user.getId());
		model.addAttribute("pagesByTag", toHighChartJs(stats.get("pagesByTag")));
		model.addAttribute("booksByTag", toHighChartJs(stats.get("booksByTag")));
		model.addAttribute("pagesByAuthor", toHighChartJs(stats.get("pagesByAuthor")));
		model.addAttribute("booksByAuthor", toHighChartJs(stats.get("booksByAuthor")));
		return "stats";
	}

	/**
	 * Getting the data into the thymeleaf format is a bit painful (especially
	 * because I'm a newbie in thymeleaf). Lets do it in Java.
	 * 
	 * We want to generate:
	 * 
	 * <pre>
	 *      labels: ["SF", "Policier", "..."],
	 *       datasets: [{
	 *           data: [3, 2, ...],
	 *           backgroundColor: 'rgb(255, 255, 255)',
	 *           borderWidth: 0
	 *       }]
	 * </pre>
	 */
	private String toHighChartJs(List<StatData> datas) {
		StringBuilder sb = new StringBuilder();
		sb.append("labels: [");
		for (StatData data : datas)
			sb.append("'").append(data.key.replace('\'', ' ')).append("',");
		sb.append("],");
		sb.append("datasets: [{");
		sb.append("data: [");
		for (StatData data : datas)
			sb.append(data.value).append(",");
		sb.append("],");
		sb.append("backgroundColor: 'rgb(255, 255, 255)',");
		sb.append("borderWidth: 0");
		sb.append("}]");
		log.info(sb.toString());
		return sb.toString();
	}
}