package com.obourgain.mylib.api;

import com.obourgain.mylib.service.TagService;
import com.obourgain.mylib.vobj.Reading;
import com.obourgain.mylib.vobj.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
     * @return the list of books for a user.
     */
    @GetMapping(value = "/tags")
    public ResponseEntity<List<Tag>> getTags(HttpServletRequest request) throws Exception {
        log.info("REST - tagss");
        String userId = getClientId(request).orElseThrow(() -> new SecurityException("User not authenticated"));
        List<Tag> tags = tagService.findByUserId(userId);
        return new ResponseEntity<>(tags, HttpStatus.OK);
    }
}

