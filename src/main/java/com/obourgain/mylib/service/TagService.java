package com.obourgain.mylib.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.obourgain.mylib.db.TagRepository;
import com.obourgain.mylib.vobj.Book;
import com.obourgain.mylib.vobj.Tag;

/**
 * Business logic for tag management.
 */
@Service
public class TagService {
	private static Logger log = LogManager.getLogger(TagService.class);

	private TagRepository tagRepository;

	@Autowired
	public TagService(TagRepository tagRepository) {
		this.tagRepository = tagRepository;
	}

	/**
	 * Transform a list of tag names, to a list a tag Ids. If a tag doesn't
	 * exists, create it in the Tag table.
	 * 
	 * @param tags
	 *            The tag list, e.g. "SF,Cycle Fondation"
	 * @return the list of Ids of the tags.
	 */
	public List<String> getTagIds(String in, String userId) {
		if (in == null || in.trim().length() == 0)
			return new ArrayList<>();
		List<String> texts = Arrays.asList(in.split(","));
		List<String> res = new ArrayList<>();

		List<Tag> alltags = tagRepository.findByUserId(userId);

		alltags.stream().forEach(System.out::println);
		for (String text : texts) {
			Tag t = alltags.stream()
					.filter(x -> x.getText().equals(text.trim()))
					.findFirst()
					.orElseGet(() -> createNewTag(text, userId));
			res.add(t.getId().toString());
		}
		return res;
	}

	/**
	 * Create a new tag in database, and return it.
	 */
	public Tag createNewTag(String text, String userId) {
		Tag t = new Tag();
		t.setText(text.trim());
		t.setUserId(userId);
		t.setBackgroundColor("#E7E7E7");
		t.setColor("#464646");
		t.setBorderColor("#464646");
		tagRepository.save(t);
		log.info("Created tag " + t);
		return t;
	}

	/**
	 * Return the list of tags of a book, as a List<Long>.
	 */
	public List<Long> getTagIdList(Book book) {
		if (StringUtils.isBlank(book.getTags())) return new ArrayList<>();
		return Stream.of(book.getTags().split(","))
				.map(Long::parseLong)
				.collect(Collectors.toList());
	}
}
