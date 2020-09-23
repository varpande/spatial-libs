package org.spatiallibs.jts.strtree;

import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.index.strtree.STRtree;

import org.spatiallibs.jts.index.BuildIndex;
import org.spatiallibs.jts.util.Utilities;

import org.openjdk.jol.info.GraphLayout;

public class IndexSize {

	private static List<Geometry> points;
	private static STRtree strtree;

	public static void index_size(String dataset, String numPoints) {
		points = Utilities.readBinaryPoints(System.getProperty("user.dir") + "/resources/datasets/projected/binary/java/" + numPoints + "M_" + dataset + ".bin");
		strtree = BuildIndex.buildRtree(points);
		//explicit build
		int size = strtree.size();
		points.clear();

		System.out.println(GraphLayout.parseInstance(strtree).toFootprint());
	}

	public static void main(String[] args) {
		index_size(args[0], args[1]);
	}
}
