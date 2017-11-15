package com.obourgain.mylib.vobj;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * This class represents the date a book was read.
 * 
 * In addition to the date, we store the year and month to simplify get stats.
 */
@Entity
@Table(name = "Reading")
public class Reading {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private String userId;
	
	// Foreign key to book
	@ManyToOne
	private Book book;
	
	// Date the book was read
	private LocalDate date;
	private int year;
	private int month;
	
	private LocalDateTime created;
	private LocalDateTime updated;
	
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


	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
        this.date = date;
	}

	public LocalDateTime getCreated() {
		return  created;
	}

	public void setCreated(LocalDateTime created) {
       this.created = created;
	}

	public LocalDateTime getUpdated() {
		return updated;
	}

	public void setUpdated(LocalDateTime updated) {
		this.updated = updated;
	}
	
	public Book getBook() {
		return book;
	}

	public void setBook(Book book) {
		this.book = book;
	}

}
