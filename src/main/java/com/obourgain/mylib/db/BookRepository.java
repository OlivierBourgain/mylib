package com.obourgain.mylib.db;

import com.obourgain.mylib.vobj.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByIdAndUserId(Long id, String userId);
    List<Book> findByUserId(String userId);
    Page<Book> findByUserIdAndIdIn(String userId, List<Long> ids, Pageable page);
    Page<Book> findByUserId(String userId, Pageable page);
}
