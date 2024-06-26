package com.obourgain.mylib.web;

import com.obourgain.mylib.service.TagService;
import com.obourgain.mylib.util.auth.WebUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Collections;

@Controller
public class TagController extends AbstractController {
    private static Logger log = LoggerFactory.getLogger(TagController.class);

    @Autowired
    private TagService tagService;

    /**
     * List of tags.
     */
    @RequestMapping(value = "/tags", method = RequestMethod.GET)
    public String tagList(Model model) {
        log.info("Controller tagList");
        WebUser user = getUserDetail();

        var tags = tagService.findByUserId(user.getId());
        Collections.sort(tags);
        model.addAttribute("tags", tags);
        model.addAttribute("user", user);
        return "tagList";
    }

    /**
     * Update of tag colors.
     */
    @RequestMapping(value = "/updateTagColor", method = RequestMethod.POST)
    public String updateTagColor(Long tagId, String backgroundColor, String color, String borderColor) {
        WebUser user = getUserDetail();
        log.info("Controller updateTag " + tagId + " with " + backgroundColor + "/" + color + "/" + borderColor);

        tagService.updateTag(tagId, backgroundColor, color, borderColor, user.getId());
        return "empty";
    }

    /**
     * Update of tag priority.
     */
    @RequestMapping(value = "/updateTagPriority", method = RequestMethod.POST)
    public String updateTagPriority(Long tagId, Integer priority) {
        WebUser user = getUserDetail();
        log.info("Controller updateTag " + tagId + " with priority " + priority);

        tagService.updateTag(tagId, priority, user.getId());
        return "empty";
    }

    /**
     * Delete a tag.
     */
    @RequestMapping(value = "/deleteTag", method = RequestMethod.POST)
    public String deleteTag(Long tagId) {
        WebUser user = getUserDetail();
        log.info("Controller deleteTag  " + tagId);
        tagService.deleteTag(tagId, user.getId());
        return "empty";
    }

}
