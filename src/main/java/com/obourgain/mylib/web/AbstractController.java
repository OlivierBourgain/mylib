package com.obourgain.mylib.web;

import com.obourgain.mylib.service.UserService;
import com.obourgain.mylib.util.auth.WebUser;
import com.obourgain.mylib.vobj.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class AbstractController {

    @Autowired
    private UserService userService;

    protected static final int PAGINATION_GAP = -1;

    protected WebUser getUserDetail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        DefaultOidcUser userDetails = (DefaultOidcUser) authentication.getPrincipal();
        WebUser webUser = new WebUser();
        webUser.setId(userDetails.getAttributes().get("sub").toString());
        webUser.setEmail(userDetails.getAttributes().get("email").toString());

        User user = userService.getByEmail(webUser.getEmail());
        if (user !=null && user.getRole() == User.UserRole.ADMIN) webUser.setAdmin(true);

        return webUser;
    }

    /**
     * Return a list of pages to be display in the pagination bar.
     * <p>
     * This method is in the controller to avoid to many code in the template. It could be moved to an utility class if more lists are added in the application.
     */
    protected List<Integer> computePagination(Page<? extends Object> list) {
        if (list.getTotalPages() <= 9)
            // Will print all page number between 0 and totalPage - 1
            return IntStream.rangeClosed(0, list.getTotalPages() - 1).boxed().collect(Collectors.toList());

        // More than 9 pages, will return:
        // (- is a place holder indicating a gap in the sequence)
        // 01234567- if page <= 4
        // -3456789- if page > 4 and < total - 5
        // -23456789 if page >= total - 5
        int page = list.getNumber();
        int last = list.getTotalPages() - 1;
        List<Integer> res = new ArrayList<>();
        if (page <= 4) {
            res.addAll(IntStream.rangeClosed(0, 7).boxed().collect(Collectors.toList()));
            res.add(PAGINATION_GAP);
        } else if (page < list.getTotalPages() - 5) {
            res.add(PAGINATION_GAP);
            res.addAll(IntStream.rangeClosed(page - 3, page + 3).boxed().collect(Collectors.toList()));
            res.add(PAGINATION_GAP);
        } else {
            res.add(PAGINATION_GAP);
            res.addAll(IntStream.rangeClosed(last - 7, last).boxed().collect(Collectors.toList()));
        }
        return res;
    }
}