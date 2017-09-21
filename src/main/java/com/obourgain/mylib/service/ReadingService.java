package com.obourgain.mylib.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.obourgain.mylib.db.ReadingRepository;
import com.obourgain.mylib.vobj.Book;
import com.obourgain.mylib.vobj.Reading;
import com.obourgain.mylib.vobj.User;

@Service
public class ReadingService {
	private static Logger log = LogManager.getLogger(TagService.class);

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
	 * Return a reading.
	 * 
	 * Returns null if the reading is not found, or if the reading is not linked to the user.
	 */
	public Reading findReading(String userId, long readingId) {
		Reading r = readingRepository.findOne(readingId);
		if (r == null) return null;
		if (!r.getUserId().equals(userId)) {
			log.warn("Access error to " + r + " from " + userId);
			throw new IllegalArgumentException("User " + userId + " cannot access " + r);
		}
		return r;
	}

	/**
	 * Delete reading.
	 * 
	 * @param readingId
	 */
	public void delete(Long readingId) {
		log.info("Deleting reading " + readingId);
		readingRepository.delete(readingId);
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
