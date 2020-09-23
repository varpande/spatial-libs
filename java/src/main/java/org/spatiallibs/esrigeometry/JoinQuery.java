package org.spatiallibs.esrigeometry;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import static org.openjdk.jmh.annotations.Scope.Benchmark;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.esri.core.geometry.Envelope2D;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.QuadTree;

import org.spatiallibs.common.ResourceLoader;
import org.spatiallibs.esrigeometry.index.BuildIndex;
import org.spatiallibs.esrigeometry.util.Utilities;
import org.spatiallibs.esrigeometry.queries.Queries;

public class JoinQuery {

	@State(value = Benchmark)
	public static class Data {
		@Param({"83M_tweets", "300M_rides"})
		private String dataset;

		private List<Geometry> points;
		private List<Geometry> polygons;
		private QuadTree quadtree;

		@Setup
		public void setup() {
			boolean gps = false;
			if(dataset.contains("tweets")) {
				points = Utilities.readPointsBinary(System.getProperty("user.dir") + "/resources/datasets/projected/binary/java/" + dataset + ".bin");
				polygons = Utilities.readPolygons(ResourceLoader.readFileInList(System.getProperty("user.dir") + "/resources/datasets/neighborhoods/projected/polygons_289.csv"));
				quadtree = BuildIndex.buildQuadtree(points, 9, gps);
			}
			if(dataset.contains("rides")) {
				points = Utilities.readPointsBinary(System.getProperty("user.dir") + "/resources/datasets/projected/binary/java/" + dataset + ".bin");
				polygons = Utilities.readPolygons(ResourceLoader.readFileInList(System.getProperty("user.dir") + "/resources/datasets/neighborhoods/projected/polygons_289.csv"));
				quadtree = BuildIndex.buildQuadtree(points, 18, gps);
			}
			if(dataset.contains("osm")) {
				points = Utilities.readPointsBinary(System.getProperty("user.dir") + "/resources/datasets/raw/binary/java/" + dataset + ".bin");
				polygons = Utilities.readPolygons(ResourceLoader.readFileInList(System.getProperty("user.dir") + "/resources/datasets/neighborhoods/raw/countries.csv"));
				gps = true;
				quadtree = BuildIndex.buildQuadtree(points, 15, gps);
			}

			quadtree.getElementCount();
		}

		@TearDown
		public void teardown() {
			points.clear();
			polygons.clear();
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
		int[] count = Queries.join(d.quadtree, d.polygons, d.points);
		bh.consume(count);
	}
}
