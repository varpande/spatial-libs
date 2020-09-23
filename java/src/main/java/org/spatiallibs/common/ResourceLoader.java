package org.spatiallibs.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Collections;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;

public class ResourceLoader {

	// returns lines of a file in a list of strings
	public static List<String> readFileInList(String fileName) {
		List<String> lines = Collections.emptyList();
		try {
			lines = Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return lines;
	}

	// reads the whole binary file to a byte array
	public static byte[] readBinaryFile(String fileName) {
		File file = new File(fileName);
		byte[] data = new byte[(int) file.length()];
		try {
			FileInputStream fis = new FileInputStream(file);
			fis.read(data);
			fis.close();
		}
		catch (FileNotFoundException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
		return data;
	}
}
