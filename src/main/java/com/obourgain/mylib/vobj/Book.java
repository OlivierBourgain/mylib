package com.obourgain.mylib.vobj;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * A book !
 */
@Entity
@Table(name = "Book")
public class Book {

    /**
     * Enum for book status
     * Not used anymore, should be replaced with a signe "Discarded" flag.
     */
    public enum BookStatus {
        TO_READ, // à lire
        DISCARDED, // jeté
        STORED, // dans un carton, en stockage
        ON_SHELF // dans la bibliothèque
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String userId;

    private String isbn;
    private String title;
    private String subtitle;
    private String author;
    private String publisher;
    private String publicationDate;
    @Enumerated(EnumType.STRING)
    private BookStatus status;

    private int pages;
    private String lang;
    @Column(length = 4096)
    private String comment;
    private String largeImage;
    private String mediumImage;
    private String smallImage;

    private LocalDateTime created;
    private LocalDateTime updated;

    /**
     * List of dates this book has been read.
     */
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "book_id", referencedColumnName = "id")
    @JsonIgnore
    private List<Reading> readings;

    /**
     * List of tags for this book.
     */
    @ManyToMany
    @JoinTable(name = "book_tag")
    private Set<Tag> tags;

    @Transient
    private String tagString; // Used in the HTML form

    // ----------------------------------------------------------
    // ToStrings (we define a quick one and a 'deep' toString
    // ----------------------------------------------------------
    @Override
    public String toString() {
        return "Book " + id + " - " + title + " - " + isbn;
    }

    public String deepToString() {
        StringBuilder res = new StringBuilder();
        res.append("------\n");
        res.append("Id       : ").append(id).append("\n");
        res.append("User     : ").append(userId).append("\n");
        res.append("Status   : ").append(status).append("\n");
        res.append("ISBN     : ").append(isbn).append("\n");
        res.append("TITLE    : ").append(title).append("\n");
        res.append("LANG     : ").append(lang).append("\n");
        res.append("AUTHOR   : ").append(author).append("\n");
        res.append("PUBLISHER: ").append(publisher).append("\n");
        res.append("DATE     : ").append(publicationDate).append("\n");
        res.append("PAGES    : ").append(pages).append("\n");
        res.append("Small IMG: ").append(smallImage).append("\n");
        res.append("Med   IMG: ").append(mediumImage).append("\n");
        res.append("Large IMG: ").append(largeImage).append("\n");
        res.append("------\n");
        return res.toString();
    }

    // ----------------------------------------------------------
    // Autogenerated getters and setters
    // ----------------------------------------------------------
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

    public String getTagString() {
        return tagString;
    }

    public void setTagString(String tagString) {
        this.tagString = tagString;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public List<Reading> getReadings() {
        return readings;
    }

    public void setReadings(List<Reading> readings) {
        this.readings = readings;
    }

    public BookStatus getStatus() {
        return status;
    }

    public void setStatus(BookStatus status) {
        this.status = status;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }
}
