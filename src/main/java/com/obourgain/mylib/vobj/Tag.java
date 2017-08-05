package com.obourgain.mylib.vobj;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

/**
 * Represents a tag on a book.
 */
@Entity
@Table(name="Tag")
public class Tag {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String userId;

	private String text;
	private String color;
	private String backgroundColor;
	private String borderColor;
	
    @ManyToMany(mappedBy = "tags")
	private Set<Book> books;

	@Override
	public String toString() {
		return String.format("Tag[id=%d, text='%s', {background-color:%s; color:%s; border-color:%s}]", id, text,
				backgroundColor, color, borderColor);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(String backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public String getBorderColor() {
		return borderColor;
	}

	public void setBorderColor(String borderColor) {
		this.borderColor = borderColor;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Set<Book> getBooks() {
		return books;
	}

}
