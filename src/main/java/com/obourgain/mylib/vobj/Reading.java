package com.obourgain.mylib.vobj;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * This class represents the date a book was read.
 * As reading a book is not immediate, the only mandatory field is the year (to allow for stats).
 * 
 * And as I'm a bit lazy, there is no other field (maybe later I will add month and day).
 */
@Entity
@Table(name="Reading")
public class Reading {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private int year;

	// Foreign key to book
	  @Column(name="book_id")
	  private Long bookId;
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public Long getBookId() {
		return bookId;
	}

	public void setBookId(Long bookId) {
		this.bookId = bookId;
	}


}
