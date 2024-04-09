package com.obourgain.mylib.vobj;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Represents a tag on a book.
 */
@Entity
@Table(name = "Tag")
public class Tag implements Comparable<Tag> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;

    private String text;
    private String color;
    private String backgroundColor;
    private String borderColor;
    private Integer priority;
    private LocalDateTime created;
    private LocalDateTime updated;

    @ManyToMany(mappedBy = "tags")
    private Set<Book> books;

    @Override
    public String toString() {
        return String.format("Tag[id=%d, text='%s', {background-color:%s; color:%s; border-color:%s}]", id, text,
                backgroundColor, color, borderColor);
    }

    @Override
    public int compareTo(Tag o) {
        if (getPriority() == o.getPriority()) return getText().compareTo(o.getText());
        if (getPriority() == null) return 1;
        if (o.getPriority() == null) return -1;
        // Else by priority desc
        return -getPriority().compareTo(o.getPriority());
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

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }


}
