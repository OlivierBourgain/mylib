package com.obourgain.mylib.db;

import com.obourgain.mylib.vobj.Reading;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReadingRepository extends JpaRepository<Reading, Long> {
    List<Reading> findByUserId(String userId);

    Page<Reading> findByUserId(String userId, Pageable page);
}
