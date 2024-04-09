package com.obourgain.mylib.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class HttpRequestUtil {
    private static final Logger log = LoggerFactory.getLogger(HttpRequestUtil.class);

    /**
     * Return the value of the request parameter as Boolean.
     * <p>
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
     * <p>
     * If the param doesn't exist, return an empty String.
     */
    public static String getParamAsString(HttpServletRequest request, String paramName) {
        String s = request.getParameter(paramName);
        if (StringUtils.isBlank(s)) return "";
        return s;
    }

    /**
     * Return the value of the request parameter as Long.
     * <p>
     * If the param doesn't exist or is not numeric, returns null.
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
     * Return the value of the request parameter as Integer.
     * <p>
     * If the param doesn't exist or is not numeric, returns null.
     */
    public static Integer getParamAsInteger(HttpServletRequest request, String paramName) {
        String s = request.getParameter(paramName);
        if (StringUtils.isBlank(s)) return null;
        try {
            return Integer.valueOf(s);
        } catch (NumberFormatException e) {
            log.warn("Trying to get a long, but value is " + s, e);
            return null;
        }
    }

    /**
     * Return the value of the request parameter as a LocalDate.
     * <p>
     * If the param doesn't exist or is not a date with format "yyyy-mm-dd", returns null.
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
