package com.obourgain.mylib.util;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

public class HttpRequestUtil {

	/**
	 * Return the value of the request parameter as Boolean.
	 * 
	 * The result is true if the parameter exists, and is equal to <code>true</code>.
	 */
	public static Boolean getParamAsBoolean(HttpServletRequest request, String paramName) {
		String s = request.getParameter(paramName);
		if (StringUtils.isBlank(s)) return false;
		if (s.equals("true")) return true;
		return false;
	}
}
