package org.spatiallibs.jvptree.cartesianpoint;

import org.spatiallibs.jvptree.cartesianpoint.CartesianPoint;

public class SimpleCartesianPoint implements CartesianPoint {
	public double x;
	public double y;

	public SimpleCartesianPoint(double x, double y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public double getX() {
		return this.x;
	}

	@Override
	public double getY() {
		return this.y;
	}
}
