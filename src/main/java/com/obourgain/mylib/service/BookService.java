package com.obourgain.mylib.service;

import com.obourgain.mylib.db.BookRepository;
import com.obourgain.mylib.ext.abebooks.AbeBooksLookup;
import com.obourgain.mylib.ext.isbndb.IsbnDbLookup;
import com.obourgain.mylib.ext.payot.PayotItemLookup;
import com.obourgain.mylib.util.ISBNConvertor;
import com.obourgain.mylib.util.auth.WebUser;
import com.obourgain.mylib.util.search.LuceneSearch;
import com.obourgain.mylib.vobj.Book;
import com.obourgain.mylib.vobj.Reading;
import com.obourgain.mylib.vobj.Tag;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Business logic for book management.
 */
@Service
public class BookService {
    private static Logger log = LoggerFactory.getLogger(BookService.class);

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private TagService tagService;

    @Autowired
    private LuceneSearch luceneSearch;

    /**
     * Return the list of books for a user.
     */
    public List<Book> findByUserId(String userId) {
        return bookRepository
                .findByUserId(userId)
                .stream()
                .filter(b -> b.getTitle() != null)
                .collect(Collectors.toList());
    }

    /**
     * Return the list of books for a user.
     */
    public Page<Book> findByUserId(String userId, Pageable page) {
        return bookRepository.findByUserId(userId, page);
    }

    /**
     * Return a book.
     * <p>
     * Returns null if the book is not found, or if the book is not linked to the user.
     */
    public Book findBook(String userId, long bookId) {
        var b = bookRepository.findById(bookId);
        if (!b.isPresent()) return null;
        if (!b.get().getUserId().equals(userId)) {
            log.warn("Access error to " + b + " from " + userId);
            throw new IllegalArgumentException("User " + userId + " cannot delete " + b);
        }
        return b.get();
    }

    /**
     * Delete a book.
     */
    public void deleteBook(String userId, Long bookId) {
        Book b = findBook(userId, bookId);
        if (b == null) return;
        bookRepository.delete(b);
    }

    /**
     * If the book has an Id, and exists in database, update it. If not, create it.
     */
    public void createOrUpdateBook(Book book, WebUser user, Set<Tag> tags) {
        Book existing = book.getId() == null ? null : findBook(user.getId(), book.getId());
        if (existing != null) {
            existing.setStatus(book.getStatus());
            existing.setTitle(book.getTitle());
            existing.setSubtitle(book.getSubtitle());
            existing.setAuthor(book.getAuthor());
            existing.setIsbn(book.getIsbn());
            existing.setPages(book.getPages());
            existing.setPublisher(book.getPublisher());
            existing.setPublicationDate(book.getPublicationDate());
            existing.setTags(tags);
            existing.setLang(book.getLang());
            existing.setComment(book.getComment());
            existing.setUpdated(LocalDateTime.now());
            existing.setSummary(book.getSummary());
            log.info("Updating book " + existing.deepToString());
            bookRepository.save(existing);
            luceneSearch.addToIndex(existing);
        } else {
            book.setUserId(user.getId());
            book.setCreated(LocalDateTime.now());
            book.setUpdated(LocalDateTime.now());
            book.setTags(tags);
            log.info("Creating book " + book.deepToString());
            bookRepository.save(book);
            luceneSearch.addToIndex(book);
        }
    }

    /**
     * Create a book from its ISBN Number. The ISBN can be an ISBN10 or ISBN13.
     * <p>
     * Return null if the amazon's page for this book can't be found.
     */
    public Book isbnLookup(WebUser user, String isbn) {
        Book book = PayotItemLookup.lookup(isbn);
        if (book == null) {
            log.info("No book found");
            return null;
        }
        book.setUserId(user.getId());
        book.setCreated(LocalDateTime.now());
        book.setUpdated(LocalDateTime.now());

        bookRepository.save(book);
        return book;
    }

    /**
     * Update the reading list of a book.
     */
    public Book updateBookReading(WebUser user, Long bookId, int year) {
        Book b = findBook(user.getId(), bookId);
        if (b == null) return b;

        Reading reading = new Reading();
        reading.setYear(year);
        b.getReadings().add(reading);
        bookRepository.save(b);
        return b;
    }

    private static final int MAX_RESULTS = 100000;

    /**
     * Get the list of book from Lucene, as a Page<Book>.
     */
    public Page<Book> getBooks(String criteria, Pageable page, String userId) {
        // First get the list of tags.
        Map<Long, Tag> alltags = tagService
                .findByUserId(userId).stream()
                .collect(Collectors.toMap(Tag::getId, Function.identity()));

        List<Book> luceneBooks = luceneSearch.search(userId, criteria, MAX_RESULTS);
        fixTags(luceneBooks, alltags);

        // Apply the pageable (sort)
        if (page != null && page.getSort() != null && page.getSort() != Sort.unsorted()) {
            Sort.Order order = page.getSort().iterator().next();
            Comparator<Book> bookComparator = getBookComparator(order);
            if (order.isDescending()) bookComparator = bookComparator.reversed();
            Collections.sort(luceneBooks, bookComparator);
        }

        // Apply the pageable (size, and page)
        int start = (int) page.getOffset();
        Pageable newPage = page;
        if (start > luceneBooks.size()) {
            start = 0;
            newPage = PageRequest.of(0, page.getPageSize(), page.getSort());
        }
        int end = Math.min(start + page.getPageSize(), luceneBooks.size());
        Page<Book> books = new PageImpl<Book>(luceneBooks.subList(start, end), newPage, luceneBooks.size());
        return books;
    }

    private Comparator<Book> getBookComparator(Sort.Order order) {
        switch (order.getProperty()) {
            case "Title" : return Comparator.comparing(Book::getTitle);
            case "Author" : return Comparator.comparing(Book::getAuthor);
            case "Pages" : return Comparator.comparing(Book::getPages);
            case "Updated" : return Comparator.comparing(Book::getUpdated);
            case "Tags" : return new TagListComparator();
            default : return Comparator.comparing(Book::getTitle);
        }
    }

    /**
     * Fix the tags.
     * <p>
     * In the lucene search, tags are stored in Book.tagString, as a list of Ids
     * (e.g. "1,23,54"). We need to replace that and compute Book.tag, which
     * contains a Set of Tag objects. In order to have a nice display, we also
     * want the Book.tag set to be ordered by Tag.priority desc.
     */
    private void fixTags(List<Book> books, Map<Long, Tag> alltags) {
        books.stream().forEach(book -> book.setTags(getTags(alltags, book)));
    }

    private Set<Tag> getTags(Map<Long, Tag> alltags, Book book) {
        return Stream.of(book.getTagString().split(","))
                .filter(s -> StringUtils.isNumeric(s))
                .map(Long::valueOf)
                .map(x -> alltags.get(x))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    /**
     * @return all the books in the database (from all users).
     */
    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    public void updateImg(Long bookId, String userId, String source) throws IOException {
        Book book = findBook(userId, bookId);
        switch (source) {
            case "isbndb":
                book.setLargeImage(IsbnDbLookup.saveImage(ISBNConvertor.clean(book.getIsbn())));
                bookRepository.save(book);
                break;
            case "abebooks":
                book.setLargeImage(AbeBooksLookup.saveImage(ISBNConvertor.clean(book.getIsbn())));
                bookRepository.save(book);
                break;
        }
    }

    public void removeImg(Long bookId, String userId) throws IOException {
        Book book = findBook(userId, bookId);
        book.setLargeImage(null);
        bookRepository.save(book);
    }

    /**
     * Compare two books based on their list of tags
     */
    public class TagListComparator implements Comparator<Book> {

        @Override
        public int compare(Book o1, Book o2) {
            var tags1 = o1.getTags().iterator();
            var tags2 = o2.getTags().iterator();
            while (tags1.hasNext() && tags2.hasNext()) {
                int c = tags1.next().compareTo(tags2.next());
                if (c != 0) return c;
            }
            if (tags1.hasNext()) return 1;
            if (tags2.hasNext()) return -1;
            return 0;
        }

    }
}
