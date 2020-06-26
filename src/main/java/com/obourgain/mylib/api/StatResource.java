package com.obourgain.mylib.api;

import com.obourgain.mylib.service.BookService;
import com.obourgain.mylib.service.StatService;
import com.obourgain.mylib.service.TagService;
import com.obourgain.mylib.vobj.Book;
import com.obourgain.mylib.vobj.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = {"http://localhost:3000"})
@RestController
@RequestMapping("/api")
public class StatResource extends AbstractResource {
    private static Logger log = LoggerFactory.getLogger(StatResource.class);

    @Autowired
    private StatService statService;

    /**
     * @return the stats for a given year.
     */
    @GetMapping(value = "/stats/{year}")
    public ResponseEntity<Map<String, List<StatService.StatData>>> getStats(HttpServletRequest request, @PathVariable Integer year) throws Exception {
        log.info("REST - stats");
        var userId = getClientId(request).orElseThrow(() -> new SecurityException("User not authenticated"));
        var res = statService.getAllStat(userId, false, year);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    /**
     * @return global stats on the content of the library.
     */
    @GetMapping(value = "/stats")
    public ResponseEntity<Map<String, List<StatService.StatData>>> getStats(HttpServletRequest request) throws Exception {
        log.info("REST - stats");
        var userId = getClientId(request).orElseThrow(() -> new SecurityException("User not authenticated"));
        var res = statService.getAllStat(userId, false, null);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }
}

