package com.obourgain.mylib.web;

import com.obourgain.mylib.service.BookService;
import com.obourgain.mylib.service.StatService;
import com.obourgain.mylib.service.StatService.StatData;
import com.obourgain.mylib.util.HttpRequestUtil;
import com.obourgain.mylib.vobj.Book;
import com.obourgain.mylib.vobj.Book.BookStatus;
import com.obourgain.mylib.vobj.Reading;
import com.obourgain.mylib.vobj.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

/**
 * Controller for the stat page.
 */
@Controller
public class StatController extends AbstractController {
    private static Logger log = LoggerFactory.getLogger(StatController.class);

    @Autowired
    private BookService bookService;
    @Autowired
    private StatService statService;


    /**
     * Stats.
     */
    @RequestMapping(value = "/stats", method = RequestMethod.GET)
    public String stats(HttpServletRequest request, Model model) {
        log.info("Controller stats");
        User user = getUserDetail();

        Integer year = HttpRequestUtil.getParamAsInteger(request, "year");

        List<Book> allBooks = bookService.findByUserId(user.getId());

        // If the year is set, filter the list
        if (year != null)
            allBooks = allBooks.stream().filter(b -> readInYear(b, year)).collect(Collectors.toList());


        model.addAttribute("nbBooks", allBooks.size());
        model.addAttribute("nbPages", allBooks.stream().mapToInt(Book::getPages).sum());

        Map<String, List<StatData>> stats = statService.getAllStat(user.getId(), year);
        model.addAttribute("pagesByTag", toHighChartJs(stats.get("pagesByTag")));
        model.addAttribute("booksByTag", toHighChartJs(stats.get("booksByTag")));
        model.addAttribute("pagesByAuthor", toHighChartJs(stats.get("pagesByAuthor")));
        model.addAttribute("booksByAuthor", toHighChartJs(stats.get("booksByAuthor")));
        model.addAttribute("pagesByYear", toHighChartJs(stats.get("pagesByYear")));
        model.addAttribute("booksByYear", toHighChartJs(stats.get("booksByYear")));
        model.addAttribute("pagesByMonth", toHighChartJs(stats.get("pagesByMonth")));
        model.addAttribute("booksByMonth", toHighChartJs(stats.get("booksByMonth")));
        model.addAttribute("year", year);

        return "stats";
    }

    /**
     * Return true if the book b was read in a given year.
     */
    private boolean readInYear(Book b, int year) {
        return b.getReadings().stream().anyMatch(r -> r.getYear() == year);
    }


    /**
     * Stat detail.
     */
    @RequestMapping(value = "/stat/{statName}", method = RequestMethod.GET)
    public void stat(HttpServletRequest request, HttpServletResponse response, @PathVariable("statName") String statName, Model model) throws IOException {
        log.info("Controller stat " + statName);
        Integer year = HttpRequestUtil.getParamAsInteger(request, "year");

        User user = getUserDetail();

        List<StatData> stat = statService.getStatDetail(user.getId(), year, statName);
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
        response.getWriter().print(toHighChartJs(stat));
        return;
    }


    /**
     * Getting the stat data in the Chart JS format.
     * <p>
     * We want to generate:
     * <p>
     * <pre>
     *      {
     *          "labels": ["SF", "Policier", "..."],
     *          "datasets": [{
     *              "data": [3, 2, ...],
     *              "backgroundColor": 'rgb(255, 255, 255)',
     *              "borderWidth": 0
     *          }]
     *       }
     * </pre>
     */
    protected String toHighChartJs(List<StatData> datas) {
        if (datas.isEmpty()) return "{}";

        var keys = datas.stream()
                .map(e ->  "\"" + e.key.replace('\'', ' ') + "\"")
                .collect(joining(","));
        var vals = datas.stream()
                .map(e -> e.value)
                .map(String::valueOf)
                .collect(joining(","));

        return """
                {
                    "labels": [{keys}],
                    "datasets": [{
                        "data": [{values}],
                        "backgroundColor": "rgb(255, 255, 255)",
                        "borderWidth": 0
                    }]
                }
                """.replace("{keys}", keys)
                .replace("{values}", vals);
    }


}
