package com.obourgain.mylib.web;

import java.util.LinkedHashMap;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import com.obourgain.mylib.vobj.User;

public abstract class AbstractController {

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

}