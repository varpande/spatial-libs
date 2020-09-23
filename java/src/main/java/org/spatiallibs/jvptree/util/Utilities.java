package org.spatiallibs.jvptree.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.spatiallibs.jvptree.cartesianpoint.SimpleCartesianPoint;

import org.spatiallibs.common.ResourceLoader;

public class Utilities {

	/* Reading Points related functions */

	public static List<SimpleCartesianPoint> readBinaryCartesianPoints(String fileName) {
		byte[] data = ResourceLoader.readBinaryFile(fileName);
		List<SimpleCartesianPoint> geoms = new ArrayList<SimpleCartesianPoint>();
		int times = Double.SIZE / Byte.SIZE;
		for(int i = 0; i < (data.length / times); i = i + 2) {
			double x = ByteBuffer.wrap(data, i * times, times).getDouble();
			double y = ByteBuffer.wrap(data, ((i + 1) * times), times).getDouble();
			SimpleCartesianPoint geom = new SimpleCartesianPoint(x, y);
			geoms.add(geom);
		}
		return geoms;
	}

	public static List<SimpleCartesianPoint> readPointsBinary(String fileName) {
		List<SimpleCartesianPoint> geoms = new ArrayList<SimpleCartesianPoint>();
		int times = Double.SIZE / Byte.SIZE;

		try {
			FileInputStream fileInputStream = new FileInputStream(fileName);
			BufferedInputStream in = new BufferedInputStream(fileInputStream);

			byte[] data = new byte[8192];
			int len;

			while ((len = in.read(data)) != -1) {
				for(int i = 0; i < (data.length / times); i = i + 2) {
					double x = ByteBuffer.wrap(data, i * times, times).getDouble();
					double y = ByteBuffer.wrap(data, ((i + 1) * times), times).getDouble();
					SimpleCartesianPoint geom = new SimpleCartesianPoint(x, y);
					geoms.add(geom);
				}
			}
			fileInputStream.close();
		}
		catch (FileNotFoundException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }

		return geoms;
	}

	/* Utility functions */

	public static List<List<Double>> getEnvelopes(String fileName) {
		List<String> lines = ResourceLoader.readFileInList(fileName);
		Iterator<String> itr = lines.iterator();
		List<List<Double>> ranges = new ArrayList<List<Double>>();

		while (itr.hasNext()) {
			String line = itr.next();
			ArrayList<Double> coords = getCoords(line);
//			SimpleCartesianPoint point = new SimpleCartesianPoint(coords[0],coords[1]);
			ranges.add(coords);
		}
		return ranges;
	}

	public static List<SimpleCartesianPoint> getQueryPoints(String fileName) {
		List<String> lines = ResourceLoader.readFileInList(fileName);
		Iterator<String> itr = lines.iterator();
		List<SimpleCartesianPoint> queryPoints = new ArrayList<SimpleCartesianPoint>();

		while(itr.hasNext()) {
			String line = itr.next();
			ArrayList<Double> coords = getCoords(line);
			SimpleCartesianPoint point = new SimpleCartesianPoint(coords.get(0),coords.get(1));
			queryPoints.add(point);
		}
		return queryPoints;
	}

	public static List<Double> getDistances(String fileName) {
		List<String> lines = ResourceLoader.readFileInList(fileName);
		Iterator<String> itr = lines.iterator();
		List<Double> distances = new ArrayList<Double>();

		while(itr.hasNext()) {
			String line = itr.next();
			ArrayList<Double> coords = getCoords(line);
			distances.add(coords.get(2));
		}
		return distances;
	}

	// read coordinates from a string into a list
	public static ArrayList<Double> getCoords(String line) {
		ArrayList<Double> al = new ArrayList<Double>();
		StringTokenizer defaultTokenizer = new StringTokenizer(line, ",");
		while (defaultTokenizer.hasMoreTokens()) {
			String s = defaultTokenizer.nextToken();
			double d = Double.parseDouble(s);
			al.add(d);
		}
		return al;
	}

	// converts a byte array to a double array
	public static double[] toDoubleArray(byte[] byteArray) {
		int times = Double.SIZE / Byte.SIZE;
		double[] doubles = new double[byteArray.length / times];
		for(int i = 0; i < doubles.length; i++)
		doubles[i] = ByteBuffer.wrap(byteArray, i * times, times).getDouble();
		return doubles;
	}

	public static List<Double> getCentroid(List<Double> coords) {
		List<Double> c = new ArrayList<Double>();
		double x = (coords.get(0) + coords.get(2)) / 2;
		double y = (coords.get(1) + coords.get(3)) / 2;
		c.add(x);
		c.add(y);
		return c;
	}

	public static double getMaxDistance(List<Double> range, List<Double> centroid) {
		double xdiff = centroid.get(0) - range.get(0);
		double ydiff = centroid.get(1) - range.get(1);
		double d = Math.sqrt((xdiff * xdiff) + (ydiff * ydiff));
		return d;
	}
}
