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
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.index.quadtree.Quadtree;

import org.spatiallibs.jts.index.BuildIndex;
import org.spatiallibs.jts.util.Utilities;
import org.spatiallibs.jts.queries.Queries;

import org.spatiallibs.common.ResourceLoader;

@State(Scope.Benchmark)
public class JoinQuery {

    // keep state between benchmarks, avoids setup between benchmarks
    // reduces benchmarks runtime dramatically
	@State(value = Benchmark)
	public static class Data {
		@Param({"83M_tweets", "300M_rides"})
		private String dataset;

		private List<Geometry> points;
		private List<Geometry> polygons;
		private List<PreparedGeometry> preparedPolygons;
		private Quadtree quadtreePoints;

		@Setup
		public void setup() {
			if(dataset.contains("tweets")) {
				points = Utilities.readPointsBinary(System.getProperty("user.dir") + "/resources/datasets/projected/binary/java/" + dataset + ".bin");
				polygons = Utilities.readPolygons(ResourceLoader.readFileInList(System.getProperty("user.dir") + "/resources/datasets/neighborhoods/projected/polygons_289.csv"));
				preparedPolygons = BuildIndex.buildPreparedGeometries(polygons);
			}
			if(dataset.contains("rides")) {
				points = Utilities.readPointsBinary(System.getProperty("user.dir") + "/resources/datasets/projected/binary/java/" + dataset + ".bin");
				polygons = Utilities.readPolygons(ResourceLoader.readFileInList(System.getProperty("user.dir") + "/resources/datasets/neighborhoods/projected/polygons_289.csv"));
				preparedPolygons = BuildIndex.buildPreparedGeometries(polygons);
			}
			if(dataset.contains("osm")) {
				points = Utilities.readPointsBinary(System.getProperty("user.dir") + "/resources/datasets/raw/binary/java/" + dataset + ".bin");
				polygons = Utilities.readPolygons(ResourceLoader.readFileInList(System.getProperty("user.dir") + "/resources/datasets/neighborhoods/projected/countries.csv"));
				preparedPolygons = BuildIndex.buildPreparedGeometries(polygons);
			}

			quadtreePoints = BuildIndex.buildQuadtree(points);
			quadtreePoints.size();
		}

		@TearDown
		public void teardown() {
			points.clear();
			polygons.clear();
			preparedPolygons.clear();
			quadtreePoints = null;
		}
	}
	
	@Benchmark
	@BenchmarkMode(Mode.SingleShotTime)
	@Warmup(iterations = 0)
	@OutputTimeUnit(TimeUnit.SECONDS)
	@Timeout(time = 8, timeUnit = TimeUnit.HOURS)
	@Fork(value = 1)
	public void benchmark(Data d, Blackhole bh) {
		int[] count = Queries.quadtreeOnPointsJoin(d.quadtreePoints, d.points, d.polygons, d.preparedPolygons);
		bh.consume(count);
	}
}
