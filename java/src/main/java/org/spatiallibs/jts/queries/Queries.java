package org.spatiallibs.jts.queries;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.index.kdtree.KdTree;
import org.locationtech.jts.index.kdtree.KdNode;
import org.locationtech.jts.index.quadtree.Quadtree;
import org.locationtech.jts.index.strtree.STRtree;
import org.locationtech.jts.index.strtree.GeometryItemDistance;

public class Queries {

	public static int[] preparedJoin(List<PreparedGeometry> pgs, List<Geometry> points) {
		int count[] = new int[290];
		for (int i = 0; i < pgs.size(); i++) {
			PreparedGeometry polygon = pgs.get(i);
			for(int j = 0; j < points.size(); j++) {
				Geometry point = points.get(j);
				if(polygon.contains(point))
				count[i]++;
			}
		}
		return count;
	}

	public static int[] rtreeOnPolygonsJoin(STRtree strtree, List<PreparedGeometry> pgs, List<Geometry> points) {
		int count[] = new int[290];
		Iterator<Geometry> itr = points.iterator();

		while (itr.hasNext()) {
			Geometry g = itr.next();
			Envelope ev = g.getEnvelopeInternal();
			List<Object> ls = strtree.query(ev);
			Iterator<Object> ls_itr = ls.iterator();

			while(ls_itr.hasNext()) {
				int index = (int) ls_itr.next();
//				TreeNode node = (TreeNode) (ls_itr.next());
//				int x = node.getID();
				if(pgs.get(index).contains(g))
					count[index]++;
			}
		}
		return count;
	}

	public static int[] rtreeOnPointsJoin(STRtree strtree, List<Geometry> points, List<Geometry> polygons, List<PreparedGeometry> preparedPolygons) {
		int count[] = new int[290];

		for (int i = 0; i < polygons.size(); i++) {
			Geometry g = polygons.get(i);
			Envelope ev = g.getEnvelopeInternal();
			List<Object> ls = strtree.query(ev);
			Iterator<Object> ls_itr = ls.iterator();

			while(ls_itr.hasNext()) {
				int index = (int) ls_itr.next();
//				TreeNode node = (TreeNode) (ls_itr.next());
//				int x = node.getID();
				if(preparedPolygons.get(i).contains(points.get(index)))
					count[i]++;
			}
		}
		return count;
	}

	public static int[] quadtreeOnPolygonsJoin(Quadtree quadtree, List<PreparedGeometry> pgs, List<Geometry> points) {
		int count[] = new int[290];
		Iterator<Geometry> itr = points.iterator();

		while (itr.hasNext()) {
			Geometry g = itr.next();
			Envelope ev = g.getEnvelopeInternal();
			List<Object> ls = quadtree.query(ev);
			Iterator<Object> ls_itr = ls.iterator();

			while(ls_itr.hasNext()) {
				int idx = (int) (ls_itr.next());
//				TreeNode node = (TreeNode) (ls_itr.next());
//				int x = node.getID();
				if(pgs.get(idx).contains(g))
					count[idx]++;
			}
		}
		return count;
	}

	public static int[] quadtreeOnPointsJoin(Quadtree quadtree, List<Geometry> points, List<Geometry> polygons, List<PreparedGeometry> preparedPolygons) {
		int count[] = new int[290];

		for (int i = 0; i < polygons.size(); i++) {
			Geometry g = polygons.get(i);
			Envelope ev = g.getEnvelopeInternal();
			List<Object> ls = quadtree.query(ev);
			Iterator<Object> ls_itr = ls.iterator();

			while(ls_itr.hasNext()) {
				int idx = (int) (ls_itr.next());
//				TreeNode node = (TreeNode) (ls_itr.next());
//				int x = node.getID();
				if(preparedPolygons.get(i).contains(points.get(idx)))
					count[i]++;
			}
		}
		return count;
	}

	public static int[] kdtreeOnPointsJoin(KdTree kdtree, List<Geometry> points, List<Geometry> polygons, List<PreparedGeometry> preparedPolygons) {
		int count[] = new int[290];

		for (int i = 0; i < polygons.size(); i++) {
			Geometry g = polygons.get(i);
			Envelope ev = g.getEnvelopeInternal();
			List<Object> ls = kdtree.query(ev);
			Iterator<Object> ls_itr = ls.iterator();

			while(ls_itr.hasNext()) {
				KdNode node = (KdNode) (ls_itr.next());
				int idx = (int) node.getData();
				int local_count = node.getCount();
				if(preparedPolygons.get(i).contains(points.get(idx)))
					count[i] = count[i] + local_count;
			}
		}
		return count;
	}

	public static Object[] knnQuery(STRtree strtree, Geometry point, int k) {
		Envelope ev = point.getEnvelopeInternal();
		Object obj = (Object) point;
		Object[] ls = strtree.nearestNeighbour(ev, obj, new GeometryItemDistance(), k);
		return ls;
	}

	public static List<Object> rangeQueryRtree(STRtree strtree, Envelope env) {
		return strtree.query(env);
	}

	public static List<Object> rangeQueryQuadtree(Quadtree quadtree, Envelope env, List<Geometry> points) {
		// TODO: further refinement needed for quadtree
		List<Object> candidates = quadtree.query(env);
		List<Object> result = new ArrayList<Object>();
		Iterator<Object> ls_itr = candidates.iterator();

		while(ls_itr.hasNext()) {
			int idx = (int) (ls_itr.next());
			if(env.contains(points.get(idx).getEnvelopeInternal()))
				result.add(points.get(idx));
		}
		
		return result;
	}

	// A range query on kdtree return a list of nodes that are contained in a given range
	// Each node contains a "Count" of snapped points (duplicate points upto a defined tolerance)
	// Please refer: https://locationtech.github.io/jts/javadoc/org/locationtech/jts/index/kdtree/KdTree.html
	// We iterate over the node return by the tree and add each node "Count" number of times, where count is
	// number of (same) points in that particular node
	public static List<Object> rangeQueryKdtree(KdTree kdtree, Envelope env, List<Geometry> points) {
		List<Object> result =  new ArrayList<Object>();
		List<Object> list = new ArrayList<Object>();

		kdtree.query(env, list);
		Iterator<Object> ls_itr = list.iterator();

		while(ls_itr.hasNext()) {
			KdNode node = (KdNode) (ls_itr.next());
			int idx = (int) node.getData();
			for(int i = 0; i < node.getCount(); i++)
				result.add(points.get(idx));
		}
		return result;
	}

	public static List<Object> distanceQueryRtree(STRtree strtree, Geometry buffer, List<Geometry> points, double distance) {
		List<Object> result =  new ArrayList<Object>();
		Geometry centroid = buffer.getCentroid();
		Envelope env = buffer.getEnvelopeInternal();
		
		List<Object> ls = strtree.query(env);
		Iterator<Object> ls_itr = ls.iterator();

		while(ls_itr.hasNext()) {
			int index = (int) ls_itr.next();
//			TreeNode node = (TreeNode) (ls_itr.next());
//			int x = node.getID();
			if(centroid.isWithinDistance(points.get(index), distance))
				result.add(points.get(index));
		}

		return result;
	}

	public static List<Object> distanceQueryQuadtree(Quadtree quadtree, Geometry buffer, List<Geometry> points, double distance) {
		List<Object> result =  new ArrayList<Object>();
		Geometry centroid = buffer.getCentroid();
		Envelope env = buffer.getEnvelopeInternal();
		
		List<Object> ls = quadtree.query(env);
		Iterator<Object> ls_itr = ls.iterator();

		while(ls_itr.hasNext()) {
//			TreeNode node = (TreeNode) (ls_itr.next());
//			int x = node.getID();
			int idx = (int) ls_itr.next();
			if(centroid.isWithinDistance(points.get(idx), distance))
				result.add(points.get(idx));
		}

		return result;
	}

	public static List<Object> distanceQueryKdtree(KdTree kdtree, Geometry buffer, List<Geometry> points, double distance) {
		List<Object> result =  new ArrayList<Object>();
		List<Object> list = new ArrayList<Object>();
		Geometry centroid = buffer.getCentroid();
		Envelope env = buffer.getEnvelopeInternal();
		
		kdtree.query(env, list);
		Iterator<Object> ls_itr = list.iterator();

		while(ls_itr.hasNext()) {
			KdNode node = (KdNode) (ls_itr.next());
			int idx = (int) node.getData();
			if(centroid.isWithinDistance(points.get(idx), distance)) {
				for(int i = 0; i < node.getCount(); i++)
					result.add(points.get(idx));
			}
		}

		return result;
	}
}
