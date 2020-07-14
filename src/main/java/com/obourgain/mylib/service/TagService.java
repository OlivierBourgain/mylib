package com.obourgain.mylib.service;

import com.obourgain.mylib.db.TagRepository;
import com.obourgain.mylib.vobj.Book;
import com.obourgain.mylib.vobj.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Business logic for tag management.
 */
@Service
public class TagService {
    private static Logger log = LoggerFactory.getLogger(TagService.class);

    private TagRepository tagRepository;

    @Autowired
    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    /**
     * Transform a list of tag names, to a list a tags. If a tag doesn't exists, create it in the Tag table.
     *
     * @param in The tag list, e.g. "SF,Cycle Fondation"
     * @param userId The user id
     * @return the list of Tag
     */
    @Transactional(readOnly = true)
    public Set<Tag> getTags(String in, String userId) {
        if (in == null || in.trim().length() == 0)
            return new HashSet<>();

        var texts = Arrays.asList(in.split(","));
        Set<Tag> res = new HashSet<>();

        var alltags = tagRepository.findByUserId(userId);
        for (String text : texts) {
            Tag t = alltags
                    .stream()
                    .filter(tag -> tag.getText().equals(text.trim()))
                    .findFirst()
                    .orElseGet(() -> createNewTag(text, userId));
            res.add(t);
        }
        return res;
    }

    /**
     * Return the list of tags for a user.
     */
    public List<Tag> findByUserId(String userId) {
        List<Tag> res=  tagRepository.findByUserId(userId);
        Collections.sort(res);
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
        t.setCreated(LocalDateTime.now());
        t.setUpdated(LocalDateTime.now());
        tagRepository.save(t);
        log.info("Created tag " + t);
        return t;
    }

    /**
     * Update a tag.
     */
    public Tag updateTag(Tag tag, String userId) {
        Tag existing = tagRepository.getOne(tag.getId());
        if (!existing.getUserId().equals(userId) || !tag.getUserId().equals(existing.getUserId())) {
            log.error("Bad user id " + existing.getUserId() + " vs " + userId);
            throw new IllegalArgumentException("Not your stuff");
        }
        existing.setUpdated(LocalDateTime.now());
        tagRepository.save(tag);
        return tag;
    }

    /**
     * Delete a tag.
     */
    public void deleteTag(Long tagId, String userId) {
        Tag tag = tagRepository.getOne(tagId);
        if (tag == null) {
            log.warn("Tag not found for deletion " + tagId);
            return;
        }
        if (!tag.getUserId().equals(userId)) {
            log.error("Bad user id " + tag.getUserId() + " vs " + userId);
            throw new IllegalArgumentException("Not your stuff");
        }

        for (Book book : tag.getBooks()) {
            log.info("Deleting tag " + tag.getText() + " from book " + book.getTitle());
            book.getTags().remove(tag);
        }
        log.info("Deleting " + tag);
        tagRepository.delete(tag);
        return;
    }


}
