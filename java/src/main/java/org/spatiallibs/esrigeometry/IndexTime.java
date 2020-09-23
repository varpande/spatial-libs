package org.spatiallibs.esrigeometry;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import static org.openjdk.jmh.annotations.Scope.Benchmark;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.esri.core.geometry.Envelope2D;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.QuadTree;

import org.spatiallibs.esrigeometry.index.BuildIndex;
import org.spatiallibs.esrigeometry.util.Utilities;
import org.spatiallibs.esrigeometry.queries.Queries;

@State(Scope.Benchmark)
public class IndexTime {

	@State(Scope.Benchmark)
	public static class Data {
		@Param({"83M_tweets", "300M_rides", "200M_osm"})
		private String dataset;

		private List<Geometry> points;
		private QuadTree quadtree;
		boolean gps = false;
		int height = 9;

		@Setup
		public void setup() {
			gps = false;
			if(dataset.contains("tweets")) {
				points = Utilities.readPointsBinary(System.getProperty("user.dir") + "/resources/datasets/projected/binary/java/" + dataset + ".bin");
				height = 32;
			}
			if(dataset.contains("rides")) {
				points = Utilities.readPointsBinary(System.getProperty("user.dir") + "/resources/datasets/projected/binary/java/" + dataset + ".bin");
				height = 18;
			}
			if(dataset.contains("osm")) {
				points = Utilities.readPointsBinary(System.getProperty("user.dir") + "/resources/datasets/raw/binary/java/" + dataset + ".bin");
				height = 15;
				gps = true;
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
		d.quadtree = BuildIndex.buildQuadtree(d.points, d.height, d.gps);
		int count = d.quadtree.getElementCount();
		bh.consume(count);
		bh.consume(d.quadtree);
	}
}
