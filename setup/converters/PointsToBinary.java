import java.util.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.io.*;
import java.util.StringTokenizer;
import java.util.Scanner.*;
import java.util.regex.*;

public class PointsToBinary {

	public static List<String> readFileInList(String fileName) {
		List<String> lines = Collections.emptyList();
		try {
			lines = Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Read " + lines.size() + " lines");
		return lines;
	}

	public static void writeBinaryPoints(List<String> points, String fileName) {
		DataOutputStream outFile = null;
		int count = 0;

		try {
			outFile = new DataOutputStream(new FileOutputStream(fileName));
			Iterator<String> itr = points.iterator();
			String delims = ",";
			while (itr.hasNext()) {
				String t = itr.next();
				String[] tokens = t.split(delims);
				outFile.writeDouble(Double.parseDouble(tokens[0]));
				outFile.writeDouble(Double.parseDouble(tokens[1]));
				count++;
			}
			outFile.close();
			System.out.println("Wrote out " + count + " line!\n");
		}
		catch (FileNotFoundException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
	}

	public static void main(String[] args) {

		if (args.length != 2) {
			System.out.println("usage: <input_points_file> <output_binary_file>");
			System.exit(-1);
		}

		List<String> points = readFileInList(args[0]);
		writeBinaryPoints(points, args[1]);
	}
}
