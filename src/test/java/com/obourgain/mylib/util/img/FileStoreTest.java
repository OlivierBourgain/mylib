package com.obourgain.mylib.util.img;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class FileStoreTest {

	@Test
	public void test() throws IOException {
		String path = FileStore.saveFile("0123456789", 'S', "test".getBytes(), "txt");
		System.out.println(path);
		String res = FileUtils.readFileToString(new File(FileStore.ROOT + path), StandardCharsets.UTF_8);
		assertEquals("test", res);
	}

}
