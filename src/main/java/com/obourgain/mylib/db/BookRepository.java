package com.obourgain.mylib.db;

import com.obourgain.mylib.vobj.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByUserId(String userId);
    Page<Book> findByUserId(String userId, Pageable page);
}
