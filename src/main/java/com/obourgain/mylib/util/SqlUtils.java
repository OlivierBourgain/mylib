package com.obourgain.mylib.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SqlUtils {
	private static final Logger log = LogManager.getLogger(SqlUtils.class);

	/**
	 * Read a SQL request from an input stream. TODO Trim white lines & comments
	 */
	public static String readSql(InputStream is) {
		try {
			String sql = IOUtils.toString(is, StandardCharsets.UTF_8);
			return sql;
		} catch (IOException e) {
			log.error("SQL file not found");
			throw new RuntimeException("Should not happen");
		}
	}
}
