package org.spatiallibs.jsi.queries;

import java.util.List;
import java.util.ArrayList;

import gnu.trove.TIntProcedure;
import com.infomatiq.jsi.Point;
import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.SpatialIndex;

public class Queries {

	public static List<Integer> rangeQuery(SpatialIndex si, Rectangle rect, long dataset_size, List<Point> points) {

		class SaveToListProcedure implements TIntProcedure {
			private List<Integer> ids = new ArrayList<Integer>();
			public boolean execute(int id) {
				ids.add(id);
				return true;
			};
			private List<Integer> getIds() {
				return ids;
			}
		};

		SaveToListProcedure myProc = new SaveToListProcedure();
		si.contains(rect, myProc);
		List<Integer> ids = myProc.getIds();
		return ids;
	}

	public static List<Integer> distanceQuery(SpatialIndex si, long dataset_size, List<Point> points, Point queryPoint, float distance) {

		class SaveToListProcedure implements TIntProcedure {
			private List<Integer> ids = new ArrayList<Integer>();
			public boolean execute(int id) {
				ids.add(id);
				return true;
			};
			private List<Integer> getIds() {
				return ids;
			}
		};

		Rectangle rect = new Rectangle(queryPoint.x - distance, queryPoint.y - distance, queryPoint.x + distance, queryPoint.y + distance);
		float testDistance = distance * distance;

		// R-tree filter
		SaveToListProcedure myProc = new SaveToListProcedure();
		si.contains(rect, myProc);
		List<Integer> ids = myProc.getIds();

		List<Integer> result = new ArrayList<Integer>();
		// Refinement
		for (Integer id : ids) {
			Point p = points.get(id);
			float dx = p.x - queryPoint.x;
			float dy = p.y - queryPoint.y;
			float d = (float) ((dx * dx) + (dy * dy));
			if(d <= testDistance)
				result.add(id);
		}
		return result;
	}

	public static List<Integer> knnQuery(SpatialIndex si, Point point, int k) {

		class SaveToListProcedure implements TIntProcedure {
			private List<Integer> ids = new ArrayList<Integer>();
			public boolean execute(int id) {
				ids.add(id);
				return true;
			};
			private List<Integer> getIds() {
				return ids;
			}
		};

		SaveToListProcedure myProc = new SaveToListProcedure();
		si.nearestN(point, myProc, k, Float.MAX_VALUE);
		List<Integer> ids = myProc.getIds();

		return ids;
	}
}
