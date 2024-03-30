package com.obourgain.mylib.util.search;

import com.obourgain.mylib.vobj.Book;
import com.obourgain.mylib.vobj.Book.BookStatus;
import com.obourgain.mylib.vobj.Tag;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Util class for search with Lucene.
 */
@Component
public class LuceneSearch {
    /**
     * Path of the index.
     * <p>
     * The index is stored in directory:
     * <p>
     * <pre>
     * $HOME/mylib/search/y/
     * </pre>
     * <p>
     * where y is a version number (e.g. v3), which allow to handle change in the structure of the index, while keeping the possibility to rollback.
     */
    private static final String INDEX_VERSION = "/v0/";
    private static final String ROOT = System.getProperty("user.home") + "/mylib";
    private static final String INDEX_DIRECTORY = ROOT + "/search/" + INDEX_VERSION;
    /**
     * Analyser
     */
    private static final Analyzer ANALYSER = new FrenchAnalyzer();
    /**
     * List of fields used in the index.
     */
    private static final String FIELD_USER_ID = "userid";
    private static final String FIELD_ID = "id";
    private static final String FIELD_TITLE = "title";
    private static final String FIELD_SUBTITLE = "subtitle";
    private static final String FIELD_AUTHOR = "author";
    private static final String FIELD_ISBN = "isbn";
    private static final String FIELD_PAGES = "pages";
    private static final String FIELD_TAG = "tag";
    // Tags as a list of tag Ids (e.g. "12,4,5").
    private static final String FIELD_RAW_TAG = "rawtag";
    private static final String FIELD_PUBLISHER = "publisher";
    private static final String FIELD_COMMENT = "comment";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_UPDATED = "updated";
    private static Logger log = LoggerFactory.getLogger(LuceneSearch.class);
    /**
     * Index Lucene
     */
    private Directory index;
    private DirectoryReader reader;

    /**
     * Public constructor
     */
    public LuceneSearch() {
        openIndex(INDEX_DIRECTORY);
    }

    /**
     * Create an instance of this class, using a specific index. To be used for unit test, without interference with the real index.
     */
    public LuceneSearch(String dir) {
        openIndex(dir);
    }

    private void openIndex(String dir) {
        try {
            Files.createDirectories(Paths.get(dir));
        } catch (IOException e) {
            log.error("Impossible de créer le répertoire " + dir);
            return;
        }
        openDirectory(dir);
    }

    private void openDirectory(String dir) {
        Path path = FileSystems.getDefault().getPath(dir);

        try {
            log.info("Lucene using index in " + dir);
            index = FSDirectory.open(path);
            if (!DirectoryReader.indexExists(index)) {
                log.info("Lucene index doesn't exists, Creating it");
                // Pour créer l'index, on ouvre juste un writer, et on le ferme
                // tout
                // de suite.
                IndexWriterConfig config = new IndexWriterConfig(ANALYSER);
                try (IndexWriter writer = new IndexWriter(index, config)) {
                }
            }
            reader = DirectoryReader.open(index);
        } catch (IOException e) {
            log.error("IOException à l'ouverture de l'index", e);
        }
    }

    /**
     * Reset Lucene index.
     *
     * @throws RuntimeException if the index is locked
     */
    public void clearIndex() {
        log.info("Lucene index reset");
        IndexWriterConfig config = new IndexWriterConfig(ANALYSER);
        try (IndexWriter writer = new IndexWriter(index, config)) {
            writer.deleteAll();
        } catch (IOException e) {
            throw new RuntimeException("Impossible de reset l'index Lucene", e);
        }
    }

    /**
     * Add a list of books to index.
     */
    public void addAll(List<Book> books) {
        log.info("Ecriture dans l'index. Nombre de livres " + books.size());
        IndexWriterConfig config = new IndexWriterConfig(ANALYSER);
        try (IndexWriter writer = new IndexWriter(index, config)) {
            for (Book book : books) {
                log.info("Adding " + book);
                addOneDocToIndex(writer, book);
            }
        } catch (IOException e) {
            throw new SearchUnavailableException("Index non dispo", e);
        }

    }

    /**
     * Add a book to the index.
     *
     * @throws SearchUnavailableException If the index is not available
     */
    public void addToIndex(Book book) {
        IndexWriterConfig config = new IndexWriterConfig(ANALYSER);
        try (IndexWriter writer = new IndexWriter(index, config)) {
            addOneDocToIndex(writer, book);
        } catch (IOException e) {
            throw new SearchUnavailableException("Index non disponible", e);
        }
    }

    // Add one document to index, using the writer passed as an argument.
    private void addOneDocToIndex(IndexWriter w, Book book) throws IOException {
        if (book.getTitle() == null) {
            log.warn("Ignored - Book {} has no title {}", book.getId(), book);
            return;
        }

        Document doc = new Document();

        // Fields with index
        doc.add(new StringField(FIELD_USER_ID, book.getUserId(), Field.Store.YES));
        doc.add(new StringField(FIELD_ID, "" + book.getId(), Field.Store.YES));
        doc.add(new TextField(FIELD_TITLE, book.getTitle(), Field.Store.YES));
        if (book.getSubtitle() != null) doc.add(new TextField(FIELD_SUBTITLE, book.getSubtitle(), Field.Store.YES));
        if (book.getAuthor() != null) doc.add(new TextField(FIELD_AUTHOR, book.getAuthor(), Field.Store.YES));
        if (book.getIsbn() != null) doc.add(new TextField(FIELD_ISBN, book.getIsbn(), Field.Store.YES));
        if (book.getPublisher() != null) doc.add(new TextField(FIELD_PUBLISHER, book.getPublisher(), Field.Store.YES));
        if (book.getComment() != null) doc.add(new TextField(FIELD_COMMENT, book.getComment(), Field.Store.YES));
        if (book.getStatus() != null)
            doc.add(new StringField(FIELD_STATUS, book.getStatus().toString(), Field.Store.YES));

        StringBuilder rawTag = new StringBuilder();
        for (Tag tag : book.getTags()) {
            doc.add(new TextField(FIELD_TAG, tag.getText(), Field.Store.NO));
            rawTag.append(tag.getId()).append(',');
        }
        // We store the list of tags (no indexing), to be able to display the
        // book list directly from
        // the search results
        doc.add(new StoredField(FIELD_RAW_TAG, rawTag.toString()));
        doc.add(new StoredField(FIELD_PAGES, book.getPages()));
        if (book.getUpdated() != null)
            doc.add(new StoredField(FIELD_UPDATED, book.getUpdated().format(DateTimeFormatter.ISO_DATE_TIME)));

        w.deleteDocuments(new Term(FIELD_ID, "" + book.getId()));
        w.addDocument(doc);
    }

    public List<Book> search(String userId, String query, int nbHits) {
        log.debug("Internal search for " + query);

        try {
            Query q = getQuery(userId, query);
            List<Book> res = executeQuery(q, nbHits);
            log.debug("MatchFound" + res.size());
            return res;
        } catch (IOException e) {
            throw new SearchUnavailableException("Index non disponible", e);
        } catch (ParseException e) {
            throw new SearchUnavailableException("Error parsing Query", e);
        }

    }

    /**
     * Query contain a mandatory userId, and then the search criterias.
     */
    private Query getQuery(String userId, String criteria) throws ParseException {
        Query q1 = new TermQuery(new Term(FIELD_USER_ID, "" + userId));

        if (StringUtils.isBlank(criteria)) {
            BooleanQuery.Builder qb = new BooleanQuery.Builder()
                    .add(q1, BooleanClause.Occur.MUST);
            return qb.build();
        }

        MultiFieldQueryParser parser = new MultiFieldQueryParser(
                new String[]{FIELD_TITLE, FIELD_SUBTITLE, FIELD_AUTHOR, FIELD_ISBN, FIELD_TAG},
                ANALYSER);
        Query q3 = parser.parse(criteria);

        BooleanQuery.Builder qb = new BooleanQuery.Builder()
                .add(q1, BooleanClause.Occur.MUST)
                .add(q3, BooleanClause.Occur.MUST);

        return qb.build();
    }

    /**
     * Execute lucene query.
     *
     * @param query  The query
     * @param nbHits Nb of hits to return.
     * @return The results.
     */
    protected List<Book> executeQuery(Query query, int nbHits) throws IOException {
        log.debug("Real query :" + query.toString());

        DirectoryReader newReader = DirectoryReader.openIfChanged(reader);
        if (newReader != null) {
            log.debug("Reopening the reader");
            reader.close();
            reader = newReader;
        }
        IndexSearcher searcher = new IndexSearcher(reader);

        TopDocs results = searcher.search(query, nbHits);
        ScoreDoc[] hits = results.scoreDocs;

        List<Book> res = new ArrayList<>();
        for (ScoreDoc hit : hits) {
            Document doc = searcher.doc(hit.doc);
            res.add(docToBook(doc));
        }
        return res;
    }

    private Book docToBook(Document doc) {
        Book book = new Book();
        book.setId(Long.parseLong(doc.get(FIELD_ID)));
        book.setUserId(doc.get(FIELD_USER_ID));
        book.setTitle(doc.get(FIELD_TITLE));
        book.setSubtitle(doc.get(FIELD_SUBTITLE));
        book.setIsbn(doc.get(FIELD_ISBN));
        book.setAuthor(doc.get(FIELD_AUTHOR));
        book.setPages(Integer.parseInt(doc.get(FIELD_PAGES)));
        book.setComment(doc.get(FIELD_COMMENT));
        book.setTagString(doc.get(FIELD_RAW_TAG));
        book.setUpdated(LocalDateTime.parse(doc.get(FIELD_UPDATED)));
        String status = doc.get(FIELD_STATUS);
        if (status != null) book.setStatus(BookStatus.valueOf(status));
        return book;
    }

}
