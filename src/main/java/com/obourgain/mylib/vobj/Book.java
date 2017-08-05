package com.obourgain.mylib.vobj;

import java.time.LocalDateTime;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * A book !
 */
@Entity
@Table(name="Book")
public class Book {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String isbn;
	private String title;
	private String author;
	private String publisher;
	private String publicationDate;
	private int pages;
	private String lang;
	@Column(length = 4096)
	private String description;

	private String largeImage;
	private String mediumImage;
	private String smallImage;

	private String googleURL;

	private String userId;

	private String comment;

	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "book_tag")
	private Set<Tag> tags;

	@Transient
	private String tagString; // Used in the HTML form

	private LocalDateTime created;
	private LocalDateTime updated;


	public Set<Tag> getTags() {
		return tags;
	}

	public void setTags(Set<Tag> tags) {
		this.tags = tags;
	}

	@Override
	public String toString() {
		return "Book " + id + " - " + title + " - " + isbn;
	}

	public String deepToString() {
		StringBuilder res = new StringBuilder();
		res.append("------\n");
		res.append("Id       : ").append(id).append("\n");
		res.append("ISBN     : ").append(isbn).append("\n");
		res.append("TITLE    : ").append(title).append("\n");
		res.append("LANG     : ").append(lang).append("\n");
		res.append("AUTHOR   : ").append(author).append("\n");
		res.append("PUBLISHER: ").append(publisher).append("\n");
		res.append("DATE     : ").append(publicationDate).append("\n");
		res.append("GOOGLE   : ").append(googleURL).append("\n");
		res.append("PAGES    : ").append(pages).append("\n");
		res.append("Small IMG: ").append(smallImage).append("\n");
		res.append("Med   IMG: ").append(mediumImage).append("\n");
		res.append("Large IMG: ").append(largeImage).append("\n");
		res.append("DESC     : ").append(description).append("\n");
		res.append("------\n");
		return res.toString();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public int getPages() {
		return pages;
	}

	public void setPages(int pages) {
		this.pages = pages;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public LocalDateTime getCreated() {
		return created;
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

	public String getLargeImage() {
		return largeImage;
	}

	public void setLargeImage(String largeImage) {
		this.largeImage = largeImage;
	}

	public String getMediumImage() {
		return mediumImage;
	}

	public void setMediumImage(String mediumImage) {
		this.mediumImage = mediumImage;
	}

	public String getSmallImage() {
		return smallImage;
	}

	public void setSmallImage(String smallImage) {
		this.smallImage = smallImage;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getPublicationDate() {
		return publicationDate;
	}

	public void setPublicationDate(String publicationDate) {
		this.publicationDate = publicationDate;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getGoogleURL() {
		return googleURL;
	}

	public void setGoogleURL(String googleURL) {
		this.googleURL = googleURL;
	}

	public String getTagString() {
		return tagString;
	}

	public void setTagString(String tagString) {
		this.tagString = tagString;
	}

}
