package com.obourgain.mylib.util.img;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * FileStore management.
 */
public class FileStore {
	private static Logger log = LogManager.getLogger(FileStore.class);
	private static final String SEP = "/";

	public static final String ROOT = System.getProperty("user.home") + "/mylib/store/";

	/**
	 * Save the content in the file store as a file.
	 * 
	 * @return The name of the file, relative to FileStore.ROOT.
	 */
	public static String saveFile(String isbn, char size, byte[] bytes, String ext) throws IOException {
		File f = createFile(isbn, size, ext);
		log.info("Saving " + f.getAbsolutePath());
		FileUtils.writeByteArrayToFile(f, bytes);
		return f.getCanonicalPath().substring(ROOT.length());
	}

	/**
	 * Return a file, in the daily directory. The name of the file is
	 * 'isbn_size.ext'.
	 */
	private static File createFile(String isbn, char size, String ext) {
		String realPath = getDirectory();
		String fileName = isbn + '_' + size + "." + ext;
		File file = new File(realPath + fileName);
		return file;
	}

	/**
	 * Return the current daily directory full path.
	 */
	private static String getDirectory() {
		Calendar calendar = Calendar.getInstance();
		Date date = calendar.getTime();
		// création du répertoire "année mois"
		String dirYearMonth = new SimpleDateFormat("yyyy_MM").format(date);
		String dirDay = new SimpleDateFormat("dd").format(date);
		String realPath = ROOT + dirYearMonth + SEP;
		createDirectory(realPath);
		// création du répertoire "jour"
		realPath += dirDay + SEP;
		createDirectory(realPath);
		return realPath;
	}

	/**
	 * Check if a directory exist. If not, create it.
	 */
	private static void createDirectory(String path) {
		File f = new File(path);
		if (!f.exists()) {
			if (!f.mkdirs()) {
				log.error("Can't create directory " + f);
				throw new RuntimeException("Can't create directory " + f);
			}
		}
	}
}
