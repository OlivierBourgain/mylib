package com.obourgain.mylib.db;

import com.obourgain.mylib.vobj.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findByUserId(String userId);
}
