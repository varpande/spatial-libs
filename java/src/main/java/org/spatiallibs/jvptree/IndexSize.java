package org.spatiallibs.jvptree;
  
import java.util.List;

import com.eatthepath.jvptree.VPTree;

import org.spatiallibs.jvptree.util.Utilities;
import org.spatiallibs.jvptree.index.BuildIndex;
import org.spatiallibs.jvptree.cartesianpoint.CartesianPoint;
import org.spatiallibs.jvptree.cartesianpoint.SimpleCartesianPoint;

import org.openjdk.jol.info.GraphLayout;

public class IndexSize {

	private static List<SimpleCartesianPoint> points;
	private static VPTree<CartesianPoint, SimpleCartesianPoint> vptree;

	public static void index_size(String dataset, String numPoints) {
		points = Utilities.readBinaryCartesianPoints(System.getProperty("user.dir") + "/resources/datasets/projected/binary/java/" + numPoints + "M_" + dataset + ".bin");
		vptree = BuildIndex.buildVpTree(points);
		System.out.println(GraphLayout.parseInstance(vptree).toFootprint());
	}

	public static void main(String[] args) {
		index_size(args[0], args[1]);
	}
}
