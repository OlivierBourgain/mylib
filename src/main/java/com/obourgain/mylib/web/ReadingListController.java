package com.obourgain.mylib.web;

import com.obourgain.mylib.service.BookService;
import com.obourgain.mylib.service.ReadingService;
import com.obourgain.mylib.util.HttpRequestUtil;
import com.obourgain.mylib.vobj.Book;
import com.obourgain.mylib.vobj.Reading;
import com.obourgain.mylib.vobj.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Controller for the reading list management.
 */
@Controller
public class ReadingListController extends AbstractController {
    private static Logger log = LoggerFactory.getLogger(ReadingListController.class);

    @Autowired
    private ReadingService readingService;
    @Autowired
    private BookService bookService;

    /**
     * Get the reading list.
     */
    @RequestMapping(value = "/readinglist", method = RequestMethod.GET)

    public String readingList(
            HttpServletRequest request,
            Model model,
            @SortDefault.SortDefaults({@SortDefault(sort = "Date", direction = Sort.Direction.DESC)}) Pageable page) {
        log.info("Controller bookList");
        User user = getUserDetail();

        Page<Reading> readings = readingService.findByUserId(user.getId(), page);
        var books = bookService.findByUserId(user.getId());
        books.sort(Comparator.comparing(Book::getTitle));

        model.addAttribute("pagination", computePagination(readings));
        model.addAttribute("readings", readings);
        model.addAttribute("books", books);
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("sort", readings.getSort() == null ? null : readings.getSort().iterator().next());

        return "readingList";
    }

    /**
     * New reading
     */
    @RequestMapping(value = "/reading", method = RequestMethod.POST)
    public String newReading(HttpServletRequest request, Model model) {
        log.info("Controller newReading");
        User user = getUserDetail();
        LocalDate date = HttpRequestUtil.getParamAsLocalDate(request, "readdate");
        Long bookId = HttpRequestUtil.getParamAsLong(request, "bookId");

        Book book = bookService.findBook(user.getId(), bookId);
        readingService.save(user, book, date);
        return "redirect:/readinglist/";
    }

    /**
     * Delete a reading.
     */
    @RequestMapping(value = "/deleteReading", method = RequestMethod.POST)
    public String deleteReading(Long readingId) {
        log.info("Delete newReading");
        User user = getUserDetail();

        Reading reading = readingService.findReading(user.getId(), readingId);
        if (reading != null) readingService.delete(readingId);
        return "empty";

    }
}
