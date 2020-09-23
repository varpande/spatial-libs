package org.spatiallibs.jvptree.queries;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import com.eatthepath.jvptree.VPTree;

import org.spatiallibs.jvptree.util.Utilities;
import org.spatiallibs.jvptree.cartesianpoint.CartesianPoint;
import org.spatiallibs.jvptree.cartesianpoint.SimpleCartesianPoint;

public class Queries {

	public static List<SimpleCartesianPoint> rangeQuery(VPTree<CartesianPoint, SimpleCartesianPoint> vptree, List<Double> range) {
			List<SimpleCartesianPoint> result = new ArrayList<SimpleCartesianPoint>();

			List<Double> centroid = Utilities.getCentroid(range);
			double distance = Utilities.getMaxDistance(range, centroid);
			SimpleCartesianPoint geom = new SimpleCartesianPoint(centroid.get(0), centroid.get(1));

			List<SimpleCartesianPoint> candidates = vptree.getAllWithinDistance(geom, distance);
			Iterator<SimpleCartesianPoint> itr = candidates.iterator();

			while(itr.hasNext()) {
				SimpleCartesianPoint point = itr.next();
				double minx = Math.min(range.get(0), range.get(2));
				double maxx = Math.max(range.get(0), range.get(2));
				double miny = Math.min(range.get(1), range.get(3));
				double maxy = Math.max(range.get(1), range.get(3));
			if(point.x >= minx && point.x <= maxx && point.y >= miny && point.y <= maxy) {
				result.add(point);
			}
		}
		return result;
	}

	public static List<SimpleCartesianPoint> distanceQuery(VPTree<CartesianPoint, SimpleCartesianPoint> vptree, SimpleCartesianPoint queryPoint, double distance) {
		return vptree.getAllWithinDistance(queryPoint, distance);
	}

	public static List<SimpleCartesianPoint> knnQuery(VPTree<CartesianPoint, SimpleCartesianPoint> vptree, SimpleCartesianPoint queryPoint, int k) {
		return vptree.getNearestNeighbors(queryPoint, k);
	}
}
