package org.spatiallibs.jsi.index;

import java.util.Iterator;
import java.util.List;

import com.infomatiq.jsi.Point;
import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.rtree.RTree;
import com.infomatiq.jsi.SpatialIndex;

public class BuildIndex {

	public static  SpatialIndex buildRtree(List<Point> geoms) {
		SpatialIndex si = new RTree();
		si.init(null);
		Iterator<Point> itr = geoms.iterator();
		int i = 0;
		while (itr.hasNext()) {
			Point p = itr.next();
			float x = p.x;
			float y = p.y;
			Rectangle r = new Rectangle(x, y, x, y);
			si.add(r, i);
			i++;
		}
		return si;
	}

}
