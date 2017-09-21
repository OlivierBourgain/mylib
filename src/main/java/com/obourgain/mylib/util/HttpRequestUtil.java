package com.obourgain.mylib.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HttpRequestUtil {
	private static Logger log = LogManager.getLogger(HttpRequestUtil.class);

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

	/**
	 * Return the value of the request parameter as String.
	 * 
	 * If the param doesn't exists, return an empty String.
	 */
	public static String getParamAsString(HttpServletRequest request, String paramName) {
		String s = request.getParameter(paramName);
		if (StringUtils.isBlank(s)) return "";
		return s;
	}

	/**
	 * Return the value of the request parameter as Long.
	 * 
	 * If the param doesn't exists or is not numeric, returns null.
	 */
	public static Long getParamAsLong(HttpServletRequest request, String paramName) {
		String s = request.getParameter(paramName);
		if (StringUtils.isBlank(s)) return null;
		try {
			return Long.valueOf(s);
		} catch (NumberFormatException e) {
			log.warn("Trying to get a long, but value is " + s, e);
			return null;
		}
	}

	/**
	 * Return the value of the request parameter as a LocalDate.
	 * 
	 * If the param doesn't exists or is not a date with format "yyyy-mm-dd", returns null.
	 */
	public static LocalDate getParamAsLocalDate(HttpServletRequest request, String paramName) {
		String s = request.getParameter(paramName);
		if (StringUtils.isBlank(s)) return null;
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			return LocalDate.parse(s, formatter);
		} catch (DateTimeParseException e) {
			log.warn("Trying to get a date, but value is " + s, e);
			return null;
		}
	}
}
