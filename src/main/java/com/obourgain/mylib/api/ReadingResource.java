package com.obourgain.mylib.api;

import com.obourgain.mylib.service.ReadingService;
import com.obourgain.mylib.vobj.Book;
import com.obourgain.mylib.vobj.Reading;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ReadingResource extends AbstractResource {
    private static Logger log = LoggerFactory.getLogger(ReadingResource.class);

    @Autowired
    private ReadingService readingService;

    /**
     * @return the list of books for a user.
     */
    @GetMapping(value = "/readings")
    public ResponseEntity<List<Reading>> getBooks(HttpServletRequest request) throws Exception {
        log.info("REST - readings");
        String userId = getClient(request).orElseThrow(() -> new SecurityException("User not authenticated"));
        List<Reading> readings = readingService.findByUserId(userId);
        return new ResponseEntity<>(readings, HttpStatus.OK);
    }
}

