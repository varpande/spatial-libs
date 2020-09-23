package org.spatiallibs.jvptree.index;

import java.util.List;

import com.eatthepath.jvptree.VPTree;

import org.spatiallibs.jvptree.cartesianpoint.CartesianPoint;
import org.spatiallibs.jvptree.cartesianpoint.CartesianDistanceFunction;
import org.spatiallibs.jvptree.cartesianpoint.SimpleCartesianPoint;

public class BuildIndex {

	public static VPTree<CartesianPoint, SimpleCartesianPoint> buildVpTree(List<SimpleCartesianPoint> points) {
		VPTree vptree = new VPTree<CartesianPoint, SimpleCartesianPoint>(new CartesianDistanceFunction());
		// addAll is way faster than adding points one by one
//		for(int i = 0; i < points.size(); i++) {
//			vptree.add(points.get(i));
//		}
		vptree.addAll(points);
		return vptree;
	}
}
