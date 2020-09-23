package org.spatiallibs.esrigeometry.index;

import java.util.List;
import java.util.Iterator;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.QuadTree;
import com.esri.core.geometry.Envelope2D;

public class BuildIndex {

	public static QuadTree buildQuadtree(List<Geometry> geoms, int depth, boolean gps) {
		Envelope2D extent;
		// extent of EPSG:32118 NAD83 / New York Long Island
		// see: https://spatialreference.org/ref/epsg/nad83-new-york-long-island/
		if(!gps) {
			extent = new Envelope2D(277102.1637, 33718.9600, 490794.6230, 129387.2653);
		}
		else {
			extent = new Envelope2D(-180.0, -90.0, 180.0, 90);
		}
		QuadTree tree = new QuadTree(extent, depth);
		Iterator<Geometry> itr = geoms.iterator();
		Geometry g;
		int geomId = 0;
		while (itr.hasNext()) {
			g = itr.next();
			Envelope2D env = new Envelope2D();
			g.queryEnvelope2D(env);
			tree.insert(geomId, env);
			geomId++;
		}
		tree.getElementCount();
		return tree;
	}
}
