package com.obourgain.mylib.web;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import com.obourgain.mylib.vobj.User;

public abstract class AbstractController {

	protected static final int PAGINATION_GAP = -1;

	protected User getUserDetail() {
		OAuth2Authentication aut = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
		@SuppressWarnings("unchecked")
		LinkedHashMap<String, String> userdetail = (LinkedHashMap<String, String>) aut
				.getUserAuthentication()
				.getDetails();
		User user = new User();
		user.setId(userdetail.get("sub"));
		user.setName(userdetail.get("name"));
		user.setFirstName(userdetail.get("given_name"));
		user.setLastName(userdetail.get("family_name"));
		user.setEmail(userdetail.get("email"));
		user.setPicture(userdetail.get("picture"));
		user.setProfile(userdetail.get("profile"));
		user.setLocale(userdetail.get("locale"));
		user.setGender(userdetail.get("gender"));
		return user;
	}

	/**
	 * Return a list of pages to be display in the pagination bar.
	 * 
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