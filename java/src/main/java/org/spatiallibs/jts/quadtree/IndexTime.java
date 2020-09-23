package org.spatiallibs.jts.quadtree;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import static org.openjdk.jmh.annotations.Scope.Benchmark;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.quadtree.Quadtree;

import org.spatiallibs.jts.index.BuildIndex;
import org.spatiallibs.jts.util.Utilities;
import org.spatiallibs.jts.queries.Queries;

@State(Scope.Benchmark)
public class IndexTime {

	@State(value = Benchmark)
	public static class Data {
		// numPoints in millions
		@Param({"83M_tweets", "300M_rides"})
		private String numPoints;

		private List<Geometry> points;
		private Quadtree quadtree;

		@Setup
		public void setup() {
			if(numPoints.equals("83M_tweets")) {
				points = Utilities.readPointsBinary(System.getProperty("user.dir") + "/resources/datasets/projected/binary/java/" + numPoints + ".bin");
			}
			if(numPoints.equals("300M_rides")) {
				points = Utilities.readPointsBinary(System.getProperty("user.dir") + "/resources/datasets/projected/binary/java/" + numPoints + ".bin");
			}
			if(numPoints.equals("200M_osm")) {
				points = Utilities.readPointsBinary(System.getProperty("user.dir") + "/resources/datasets/raw/binary/java/" + numPoints + ".bin");
			}			
		}

		@TearDown
		public void teardown() {
			points.clear();
			quadtree = null;
		}
	}

	@Benchmark
	@BenchmarkMode(Mode.SingleShotTime)
	@Warmup(iterations = 0)
	@OutputTimeUnit(TimeUnit.SECONDS)
	@Timeout(time = 8, timeUnit = TimeUnit.HOURS)
	@Fork(value = 1)
	public void benchmark(Data d, Blackhole bh) {
		d.quadtree = BuildIndex.buildQuadtree(d.points);
                int size = d.quadtree.size();
		bh.consume(d.quadtree);
	}
}
