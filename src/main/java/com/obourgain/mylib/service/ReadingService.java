package com.obourgain.mylib.service;

import com.obourgain.mylib.db.ReadingRepository;
import com.obourgain.mylib.vobj.Book;
import com.obourgain.mylib.vobj.Reading;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class ReadingService {
    private static Logger log = LoggerFactory.getLogger(TagService.class);

    @Autowired
    private ReadingRepository readingRepository;

    /**
     * Return the list of tags for a user.
     */
    public Page<Reading> findByUserId(String userId, Pageable page) {
        return readingRepository.findByUserId(userId, page);
    }

    /**
     * Delete reading.
     *
     * @param readingId
     */
    public void delete(Long readingId) {
        log.info("Deleting reading " + readingId);
        readingRepository.deleteById(readingId);
    }

    /**
     * Save a new reading.
     */
    public void save(String userId, Book book, LocalDate date) {
        Reading r = new Reading();
        r.setUserId(userId);
        r.setBook(book);
        r.setDate(date);
        r.setYear(date.getYear());
        r.setMonth(date.getMonthValue());
        r.setCreated(LocalDateTime.now());
        r.setUpdated(LocalDateTime.now());

        r = readingRepository.save(r);
        log.info("Created reading " + r.getId());

    }

}
