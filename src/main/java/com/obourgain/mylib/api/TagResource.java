package com.obourgain.mylib.api;

import com.obourgain.mylib.service.TagService;
import com.obourgain.mylib.vobj.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api")
public class TagResource extends AbstractResource {
    private static Logger log = LoggerFactory.getLogger(TagResource.class);

    @Autowired
    private TagService tagService;

    /**
     * Update a tag
     */
    @PostMapping(value = "/tag")
    public ResponseEntity<List<Tag>> update(HttpServletRequest request, @RequestBody Tag tag) throws Exception {
        log.info("REST - tag update " + tag);
        String userId = getClientId(request).orElseThrow(() -> new SecurityException("User not authenticated"));
        tagService.updateTag(tag, userId);
        List<Tag> tags = tagService.findByUserId(userId);
        return new ResponseEntity<>(tags, HttpStatus.OK);
    }

    /**
     * Delete a tag
     */
    @DeleteMapping(value = "/tag/{id}")
    public ResponseEntity<List<Tag>> delete(HttpServletRequest request, @PathVariable Long id) throws Exception {
        log.info("REST - tag delete " + id);
        String userId = getClientId(request).orElseThrow(() -> new SecurityException("User not authenticated"));
        tagService.deleteTag(id, userId);
        List<Tag> tags = tagService.findByUserId(userId);
        return new ResponseEntity<>(tags, HttpStatus.OK);
    }

    /**
     * @return the list of tags.
     */
    @GetMapping(value = "/tags")
    public ResponseEntity<List<Tag>> getTags(HttpServletRequest request) throws Exception {
        log.info("REST - tags");
        String userId = getClientId(request).orElseThrow(() -> new SecurityException("User not authenticated"));
        List<Tag> tags = tagService.findByUserId(userId);
        return new ResponseEntity<>(tags, HttpStatus.OK);
    }

}

