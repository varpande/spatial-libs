package org.spatiallibs.esrigeometry;
  
import java.util.List;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.QuadTree;

import org.spatiallibs.esrigeometry.index.BuildIndex;
import org.spatiallibs.esrigeometry.util.Utilities;

import org.openjdk.jol.info.GraphLayout;

public class IndexSize {

	private static List<Geometry> points;
	private static QuadTree quadtree;
	private static int indexDepth;

	public static void index_size(String dataset, String numPoints) {
		points = Utilities.readBinaryPoints(System.getProperty("user.dir") + "/resources/datasets/projected/binary/java/" + numPoints + "M_" + dataset + ".bin");

		if(dataset.equals("rides"))
			indexDepth = 18;
		else
			indexDepth = 9;

		quadtree = BuildIndex.buildQuadtree(points, indexDepth, false);
		quadtree.getElementCount();
		System.out.println(GraphLayout.parseInstance(quadtree).toFootprint());
	}

	public static void main(String[] args) {
		index_size(args[0], args[1]);
	}
}
