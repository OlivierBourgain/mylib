package com.obourgain.mylib.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	 * Transform a list of tag names, to a list a tags. If a tag doesn't exists, create it in the Tag table.
	 * 
	 * @param tags
	 *            The tag list, e.g. "SF,Cycle Fondation"
	 * @return the list of Tag
	 */
	public Set<Tag> getTags(String in, String userId) {
		if (in == null || in.trim().length() == 0)
			return new HashSet<>();
		List<String> texts = Arrays.asList(in.split(","));
		Set<Tag> res = new HashSet<>();

		List<Tag> alltags = tagRepository.findByUserId(userId);

		alltags.stream().forEach(System.out::println);
		for (String text : texts) {
			Tag t = alltags
					.stream()
					.filter(x -> x.getText().equals(text.trim()))
					.findFirst()
					.orElseGet(() -> createNewTag(text, userId));
			res.add(t);
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
	 * Delete a tag.
	 */
	public void deleteTag(Tag tag) {
		for (Book book : tag.getBooks()) {
			log.info("Deleting tag " + tag.getText() + " from book " + book.getTitle());
			book.getTags().remove(tag);
		}
		log.info("Deleting " + tag);
		tagRepository.delete(tag);
		return;
	}

}
