package org.spatiallibs.esrigeometry.queries;

import java.util.List;
import java.util.ArrayList;
import com.esri.core.geometry.Envelope2D;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.QuadTree;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.geometry.Operator;
import com.esri.core.geometry.OperatorFactoryLocal;
import com.esri.core.geometry.OperatorContains;
import com.esri.core.geometry.OperatorDistance;

public class Queries {

	public static List<Geometry> rangeQuery(QuadTree tree, Envelope2D env, List<Geometry> points) {
		List<Geometry> result = new ArrayList<Geometry>();
		QuadTree.QuadTreeIterator itr = tree.getIterator();
		itr.resetIterator(env, 0);
		int element;
		while( (element = itr.next()) >= 0) {
			int index = tree.getElement(element);
			result.add(points.get(index));
		}
		return result;
	}

	public static int [] join(QuadTree tree, List<Geometry> polygons, List<Geometry> points) {
		int count[] = new int[290];
		int element, index;
		SpatialReference sr = SpatialReference.create(32118);
		QuadTree.QuadTreeIterator itr = tree.getIterator();
		OperatorFactoryLocal factory = OperatorFactoryLocal.getInstance();
		OperatorContains op = (OperatorContains) factory.getOperator(Operator.Type.Contains);

		for (int i = 0; i < polygons.size(); i++) {
			Geometry polygon = polygons.get(i);
			Envelope2D env = new Envelope2D();
			polygon.queryEnvelope2D(env);
			itr.resetIterator(env, 0);
			while( (element = itr.next()) >= 0) {
				index = tree.getElement(element);
				if (op.execute(polygon, points.get(index), sr, null))
				count[i]++;
			}
		}

		return count;
	}

	public static List<Geometry> distanceQuery(QuadTree tree, List<Geometry> points, Envelope2D env, Geometry point, double distance) {
		List<Geometry> result = new ArrayList<Geometry>();
		int element, index;
		SpatialReference sr = SpatialReference.create(32118);
		QuadTree.QuadTreeIterator itr = tree.getIterator();
		OperatorFactoryLocal factory = OperatorFactoryLocal.getInstance();
		OperatorDistance op = (OperatorDistance) factory.getOperator(Operator.Type.Distance);
		op.accelerateGeometry(point, sr, Geometry.GeometryAccelerationDegree.enumHot);

		itr.resetIterator(env, 0);
		while( (element = itr.next()) >= 0) {
			index = tree.getElement(element);
			if (op.execute(point, points.get(index), null) <= distance) {
				result.add(points.get(index));
			}
		}
		return result;
	}
}
