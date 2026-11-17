package com.obourgain.mylib.web.api;

import com.obourgain.mylib.vobj.Book;
import com.obourgain.mylib.vobj.Reading;
import com.obourgain.mylib.vobj.Tag;
import jakarta.validation.constraints.*;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Comparator;

public final class ApiModels {
    private ApiModels() { }

    public record CreateBookRequest(
            @NotBlank @Size(max = 255) String title,
            @Size(max = 255) String subtitle,
            @Size(max = 255) String author,
            @Size(max = 255) String isbn,
            @Size(max = 255) String publisher,
            @Size(max = 255) String publicationDate,
            Book.BookStatus status,
            @PositiveOrZero int pages,
            @Size(max = 255) String lang,
            @Size(max = 4096) String comment,
            @Size(max = 255) String summary) {
        Book toBook() {
            Book book = new Book();
            book.setTitle(title);
            book.setSubtitle(subtitle);
            book.setAuthor(author);
            book.setIsbn(isbn);
            book.setPublisher(publisher);
            book.setPublicationDate(publicationDate);
            book.setStatus(status);
            book.setPages(pages);
            book.setLang(lang);
            book.setComment(comment);
            book.setSummary(summary);
            return book;
        }
    }

    public record CreateReadingRequest(@NotNull @Positive Long bookId, @NotNull LocalDate date) { }

    // Explicit response models avoid entity recursion and exposing internal ownership fields.
    public record BookResponse(Long id, String title, String subtitle, String author, String isbn,
                               String publisher, String publicationDate, Book.BookStatus status,
                               int pages, String lang, String comment, String summary,
                               LocalDateTime created, LocalDateTime updated, List<TagResponse> tags) {
        static BookResponse from(Book book) {
            return new BookResponse(book.getId(), book.getTitle(), book.getSubtitle(), book.getAuthor(),
                    book.getIsbn(), book.getPublisher(), book.getPublicationDate(), book.getStatus(),
                    book.getPages(), book.getLang(), book.getComment(), book.getSummary(),
                    book.getCreated(), book.getUpdated(),
                    book.getTags() == null ? List.of() : book.getTags().stream()
                            .sorted(Comparator.comparing(Tag::getId))
                            .map(TagResponse::from).toList());
        }
    }

    public record TagResponse(Long id, String text, String color, String backgroundColor,
                              String borderColor, Integer priority) {
        static TagResponse from(Tag tag) {
            return new TagResponse(tag.getId(), tag.getText(), tag.getColor(), tag.getBackgroundColor(),
                    tag.getBorderColor(), tag.getPriority());
        }
    }

    public record ReadingResponse(Long id, Long bookId, LocalDate date, int year, int month,
                                  LocalDateTime created, LocalDateTime updated) {
        static ReadingResponse from(Reading reading) {
            return new ReadingResponse(reading.getId(), reading.getBook().getId(), reading.getDate(),
                    reading.getYear(), reading.getMonth(), reading.getCreated(), reading.getUpdated());
        }
    }

    public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
        static <T> PageResponse<T> from(Page<T> page) {
            return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(),
                    page.getTotalElements(), page.getTotalPages());
        }
    }
}
