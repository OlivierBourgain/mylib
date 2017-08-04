package com.obourgain.mylib.db;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.obourgain.mylib.vobj.Book;

public interface BookRepository extends JpaRepository<Book, Long> {
	List<Book> findByUserId(String userId);
}
