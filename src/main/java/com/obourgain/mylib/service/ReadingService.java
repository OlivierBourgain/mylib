package com.obourgain.mylib.service;

import com.obourgain.mylib.db.ReadingRepository;
import com.obourgain.mylib.vobj.Book;
import com.obourgain.mylib.vobj.Reading;
import com.obourgain.mylib.vobj.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReadingService {
    private static Logger log = LoggerFactory.getLogger(TagService.class);

    @Autowired
    private ReadingRepository readingRepository;

    /**
     * Return the list of tags for a user.
     */
    public List<Reading> findByUserId(String userId) {
        log.debug("Reading list for " + userId);
        return readingRepository.findByUserId(userId);
    }

    /**
     * Return the list of tags for a user.
     */
    public Page<Reading> findByUserId(String userId, Pageable page) {
        return readingRepository.findByUserId(userId, page);
    }

    /**
     * Return a reading.
     * <p>
     * Returns null if the reading is not found, or if the reading is not linked to the user.
     */
    public Reading findReading(String userId, long readingId) {
        Optional<Reading> r = readingRepository.findById(readingId);
        if (!r.isPresent()) return null;
        if (!r.get().getUserId().equals(userId)) {
            log.warn("Access error to " + r + " from " + userId);
            throw new IllegalArgumentException("User " + userId + " cannot access " + r);
        }
        return r.get();
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
    public void save(User user, Book book, LocalDate date) {
        Reading r = new Reading();
        r.setUserId(user.getId());
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
