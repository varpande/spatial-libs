package org.spatiallibs.esrigeometry.util;

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

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Envelope2D;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.WktImportFlags;
import com.esri.core.geometry.Operator;
import com.esri.core.geometry.OperatorContains;
import com.esri.core.geometry.OperatorFactoryLocal;
import com.esri.core.geometry.SpatialReference;

import org.spatiallibs.common.ResourceLoader;

public class Utilities {

	/* Reading Points related functions */

	// reads a  binary file and coverts the byte array to a list of geometries
	public static List<Geometry> readBinaryPoints(String fileName) {
		byte[] data = ResourceLoader.readBinaryFile(fileName);
		List<Geometry> geoms = new ArrayList<Geometry>();
		int times = Double.SIZE / Byte.SIZE;
		for(int i = 0; i < (data.length / times); i = i + 2) {
			double x = ByteBuffer.wrap(data, i * times, times).getDouble();
			double y = ByteBuffer.wrap(data, ((i + 1) * times), times).getDouble();
			Geometry geom = new Point(x, y);
			geoms.add(geom);
		}
		return geoms;
	}

	// converts a list of wkt strings to esri geometries
	public static List<Geometry> readPolygons(List<String> polygons) {
		List<Geometry> geoms = new ArrayList<Geometry>();
		Iterator<String> itr = polygons.iterator();

		while (itr.hasNext()) {
			String t = itr.next();
			Geometry g = GeometryEngine.geometryFromWkt(t, WktImportFlags.wktImportDefaults, Geometry.Type.Polygon);
			geoms.add(g);
		}
		return geoms;
	}

	public static List<Geometry> readPointsBinary(String fileName) {
		List<Geometry> geoms = new ArrayList<Geometry>();
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
					Geometry geom = new Point(x, y);
					geoms.add(geom);
				}
			}
			fileInputStream.close();
		}
		catch (FileNotFoundException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }

		return geoms;
	}

	// return a list of envelopes
	public static List<Envelope2D> getEnvelopes(String fileName) {
		List<String> lines = ResourceLoader.readFileInList(fileName);
		Iterator<String> itr = lines.iterator();
		List<Envelope2D> ranges = new ArrayList<Envelope2D>();

		while (itr.hasNext()) {
			String line = itr.next();
			ArrayList<Double> coords = getCoords(line);
			ranges.add(new Envelope2D(coords.get(0), coords.get(1), coords.get(2), coords.get(3)));
		}
		return ranges;
	}

	public static List<Geometry> getQueryPoints(String fileName) {
		List<String> lines = ResourceLoader.readFileInList(fileName);
		Iterator<String> itr = lines.iterator();
		List<Geometry> queryPoints = new ArrayList<Geometry>();

		while (itr.hasNext()) {
			String line = itr.next();
			ArrayList<Double> coords = getCoords(line);
			queryPoints.add(new Point(coords.get(0), coords.get(1)));
		}
		return queryPoints;
	}

	public static List<Double> getDistances(String fileName) {
		List<String> lines = ResourceLoader.readFileInList(fileName);
		Iterator<String> itr = lines.iterator();
		List<Double> distances = new ArrayList<Double>();

		while (itr.hasNext()) {
			String line = itr.next();
			ArrayList<Double> coords = getCoords(line);
			distances.add(coords.get(2));
		}
		return distances;
	}

	public static List<Envelope2D> getEnvelopes(List<Geometry> points, List<Double> distances) {
		List<Envelope2D> envs = new ArrayList<Envelope2D>();
		for(int i = 0; i < points.size(); i++) {
			double d = distances.get(i);
			Point p = (Point) points.get(i);
			double x = p.getX();
			double y = p.getY();
			envs.add(new Envelope2D(x-d, y-d, x+d, y+d));
		}
		return envs;
	}

	public static Envelope2D getExtent(List<Geometry> points) {
		double minx, miny, maxx, maxy;
		minx = miny = Double.MAX_VALUE;
		maxx = maxy = Double.MIN_VALUE;
		 
		for(int i = 0; i < points.size(); i++) {
			Point p = (Point) points.get(i);
			double x = p.getX();
			double y = p.getY();

			if(minx > x) minx = x;
			if(miny > y) miny = y;
			if(maxx < x) maxx = x;
			if(maxy < y) maxy = y;			
		}
		return (new Envelope2D(minx, miny, maxx, maxy));
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

	// helper function to accelerate(index) individual esri geometries
	public static void accelerateGeometryContains(List<Geometry> geoms) {
		OperatorFactoryLocal factory = OperatorFactoryLocal.getInstance();
		OperatorContains op = (OperatorContains) factory.getOperator(Operator.Type.Contains);
		SpatialReference sr = SpatialReference.create(32118);
		for(int i = 0; i < geoms.size(); i++) {
			op.accelerateGeometry(geoms.get(i), sr, Geometry.GeometryAccelerationDegree.enumHot);
		} 
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
}
