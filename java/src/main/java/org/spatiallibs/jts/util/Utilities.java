package org.spatiallibs.jts.util;

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

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.util.GeometricShapeFactory;

import org.spatiallibs.common.ResourceLoader;

public class Utilities {

	/* Reading Points related functions */

	// reads a  binary file and coverts the byte array to a list of geometries
	public static List<Geometry> readBinaryPoints(String fileName) {
		byte[] data = ResourceLoader.readBinaryFile(fileName);
		List<Geometry> geoms = new ArrayList<Geometry>();
		GeometryFactory gf = new GeometryFactory();

		int times = Double.SIZE / Byte.SIZE;

		for(int i = 0; i < (data.length / times); i = i + 2) {
			double x = ByteBuffer.wrap(data, i * times, times).getDouble();
			double y = ByteBuffer.wrap(data, ((i + 1) * times), times).getDouble();
			Coordinate coord = new Coordinate(x, y);
			Geometry geom = gf.createPoint(coord);
			geoms.add(geom);
		}
		return geoms;
	}

	// Converts a list of strings (each of which represents a co-ordinate) to a list of geometries
	public static List<Geometry> readPoints(List<String> points) {
		List<Geometry> geoms = new ArrayList<Geometry>();
		Iterator<String> itr = points.iterator();
		String delims = ",";
		GeometryFactory gf = new GeometryFactory();

		while (itr.hasNext()) {
			String t = itr.next();
			String[] tokens = t.split(delims);
			Coordinate coord = new Coordinate(Double.parseDouble(tokens[0]), Double.parseDouble(tokens[1]));
			Geometry geom = gf.createPoint(coord);
			geoms.add(geom);
		}
		return geoms;
	}

	public static List<Geometry> readPointsBinary(String fileName) {
		List<Geometry> geoms = new ArrayList<Geometry>();
		GeometryFactory gf = new GeometryFactory();
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
					Coordinate coord = new Coordinate(x, y);
					Geometry geom = gf.createPoint(coord);
					geoms.add(geom);
				}
			}
			fileInputStream.close();
		}
		catch (FileNotFoundException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }

		return geoms;
	}


	// converts a list of wkt strings to jts geometries
	public static List<Geometry> readPolygons(List<String> polygons) {
		List<Geometry> geoms = new ArrayList<Geometry>();
		Iterator<String> itr = polygons.iterator();

		while (itr.hasNext()) {
			String t = itr.next();
			WKTReader reader = new WKTReader();
			Geometry geom = null;
			try {
				geom = reader.read(t);
			}
			catch (ParseException e) {
				e.printStackTrace();
			}
			geom.setSRID(32118);
			geoms.add(geom);
		}
		return geoms;
	}

	public static List<Envelope> getEnvelopes(String fileName) {
		List<String> lines = ResourceLoader.readFileInList(fileName);
		Iterator<String> itr = lines.iterator();
		List<Envelope> ranges = new ArrayList<Envelope>();

		while (itr.hasNext()) {
			String line = itr.next();
			ArrayList<Double> coords = getCoords(line);
			Coordinate c1 = new Coordinate(coords.get(0), coords.get(1));
			Coordinate c2 = new Coordinate(coords.get(2), coords.get(3));
			Envelope env = new Envelope(c1, c2);
			ranges.add(env);
		}
		return ranges;
	}

	public static Object[] getQueryPointsAndDistances(String fileName) {
		List<String> lines = ResourceLoader.readFileInList(fileName);
		Iterator<String> itr = lines.iterator();

		List<Geometry> queryPoints = new ArrayList<Geometry>();
		List<Double> distances = new ArrayList<Double>();

		while(itr.hasNext()) {
			String line = itr.next();
			ArrayList<Double> coords = getCoords(line);
			Geometry circle = createCircle(coords.get(0), coords.get(1), coords.get(2));
			queryPoints.add(circle);
			distances.add(coords.get(2));
		}

		return new Object[] {queryPoints, distances};
	}

	public static List<Geometry> getQueryPoints(String fileName) {
		List<String> lines = ResourceLoader.readFileInList(fileName);
		Iterator<String> itr = lines.iterator();

		List<Geometry> queryPoints = new ArrayList<Geometry>();

		while(itr.hasNext()) {
			String line = itr.next();
			ArrayList<Double> coords = getCoords(line);
			Geometry point = createPoint(coords.get(0), coords.get(1));
			queryPoints.add(point);
		}

		return queryPoints;	
	}


	/* Utility functions */

	// converts a byte array to a double array
	public static double[] toDoubleArray(byte[] byteArray) {
		int times = Double.SIZE / Byte.SIZE;
		double[] doubles = new double[byteArray.length / times];
		for(int i = 0; i < doubles.length; i++)
		doubles[i] = ByteBuffer.wrap(byteArray, i * times, times).getDouble();
		return doubles;
	}

	// create and envelope from two co-ordinates
	public static Envelope createEnvelope(double x1, double y1, double x2, double y2) {
		Coordinate c1 = new Coordinate(x1,y1);
		Coordinate c2 = new Coordinate(x2,y2);
		return new Envelope(c2, c1);
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

	public static Geometry createBuffer(Geometry input, double distance) {
		Geometry geom = input.buffer(distance);
		geom.setSRID(32118);
		return geom;
	}

	public static Geometry createPoint(double x, double y) {
		GeometryFactory gf = new GeometryFactory();
		Coordinate coord = new Coordinate(x, y);
		Geometry geom = gf.createPoint(coord);
		geom.setSRID(32118);
		return geom;
	}

	public static Geometry createCircle(double x, double y, double radius) {
		GeometricShapeFactory gf = new GeometricShapeFactory();
		Coordinate c = new Coordinate(x, y);
		gf.setCentre(c);
		gf.setSize(2 * radius);
		Geometry geom = gf.createCircle();
		geom.setSRID(32118);
        return geom;
    }
}
