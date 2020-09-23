package org.spatiallibs.jts.index;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.index.kdtree.KdTree;
import org.locationtech.jts.index.quadtree.Quadtree;
import org.locationtech.jts.index.strtree.STRtree;

public class BuildIndex {

	public static List<PreparedGeometry> buildPreparedGeometries(List<Geometry> geoms) {
		PreparedGeometryFactory pgf = new PreparedGeometryFactory();
		Iterator<Geometry> itr = geoms.iterator();
		List<PreparedGeometry> pgs = new ArrayList<PreparedGeometry>();
		while (itr.hasNext()) {
			Geometry g = itr.next();
			PreparedGeometry pg = pgf.prepare(g);
			pgs.add(pg);
		}
		return pgs;
	}

	public static STRtree buildRtree(List<Geometry> geoms) {
		STRtree tree = new STRtree();
		Iterator<Geometry> itr = geoms.iterator();
		Geometry g;
		int i = 0;
		while (itr.hasNext()) {
			g = itr.next();
			tree.insert(g.getEnvelopeInternal(), i);
			i++;
		}
		return tree;
	}

	public static STRtree buildRtreePoints(List<Geometry> geoms) {
		STRtree tree = new STRtree();
		Iterator<Geometry> itr = geoms.iterator();
		Geometry g;
		while (itr.hasNext()) {
			g = itr.next();
			tree.insert(g.getEnvelopeInternal(), g);
		}
		return tree;
	}

	public static Quadtree buildQuadtree(List<Geometry> geoms) {
		Quadtree tree = new Quadtree();
		Iterator<Geometry> itr = geoms.iterator();
		Geometry g;
		int i = 0;
		while (itr.hasNext()) {
			g = itr.next();
			tree.insert(g.getEnvelopeInternal(), i);
			i++;
		}
		return tree;
	}

	public static KdTree buildKdtree(List<Geometry> geoms) {
		KdTree tree = new KdTree();
		Iterator<Geometry> itr = geoms.iterator();
		Geometry g;
		int i = 0;
		while (itr.hasNext()) {
			g = itr.next();
			Coordinate c = g.getCoordinate();
			tree.insert(c, i);
			i++;
		}
		return tree;
	}
}
