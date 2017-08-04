package com.obourgain.mylib.db;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.obourgain.mylib.vobj.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {
	List<Tag> findByUserId(String userId);
}
