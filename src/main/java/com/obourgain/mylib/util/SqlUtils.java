package com.obourgain.mylib.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class SqlUtils {
    private static final Logger log = LoggerFactory.getLogger(SqlUtils.class);

    /**
     * Read a SQL request from an input stream.
     */
    public static String readSql(InputStream is) {
        try {
            List<String> lines = IOUtils.readLines(is, StandardCharsets.UTF_8);
            return lines.stream()
                    // Filter comments & empty lines.
                    .filter(line -> StringUtils.isNotBlank(line) && !line.trim().startsWith("--"))
                    .collect(Collectors.joining(" "));
        } catch (UncheckedIOException e) {
            log.error("SQL file not found");
            throw new RuntimeException("Should not happen");
        }
    }
}
