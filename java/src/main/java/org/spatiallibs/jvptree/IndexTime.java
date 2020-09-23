package org.spatiallibs.jvptree;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import static org.openjdk.jmh.annotations.Scope.Benchmark;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.eatthepath.jvptree.VPTree;

import org.spatiallibs.jvptree.util.Utilities;
import org.spatiallibs.jvptree.queries.Queries;
import org.spatiallibs.jvptree.index.BuildIndex;
import org.spatiallibs.jvptree.cartesianpoint.CartesianPoint;
import org.spatiallibs.jvptree.cartesianpoint.SimpleCartesianPoint;

@State(Scope.Benchmark)
public class IndexTime {

	@State(value = Benchmark)
	public static class Data {
		// dataset in millions
		@Param({"83M_tweets", "300M_rides"})
		private String dataset;

		private List<SimpleCartesianPoint> points;
		private VPTree<CartesianPoint, SimpleCartesianPoint> vptree;

		@Setup
		public void setup() {
			if(dataset.contains("tweets")) {
				points = Utilities.readPointsBinary(System.getProperty("user.dir") + "/resources/datasets/projected/binary/java/" + dataset + ".bin");
			}
			if(dataset.contains("rides")) {
				points = Utilities.readPointsBinary(System.getProperty("user.dir") + "/resources/datasets/projected/binary/java/" + dataset + ".bin");
			}
			if(dataset.contains("osm")) {
				points = Utilities.readPointsBinary(System.getProperty("user.dir") + "/resources/datasets/raw/binary/java/" + dataset + ".bin");
			}

			vptree = BuildIndex.buildVpTree(points);
		}

		@TearDown
		public void teardown() {
			points.clear();
			vptree = null;
		}
	}

	@Benchmark
	@BenchmarkMode(Mode.SingleShotTime)
	@Warmup(iterations = 0)
	@OutputTimeUnit(TimeUnit.SECONDS)
	@Timeout(time = 8, timeUnit = TimeUnit.HOURS)
	@Fork(value = 1)
	public void benchmark(Data d, Blackhole bh) {
		d.vptree = BuildIndex.buildVpTree(d.points);
		bh.consume(d.vptree);
	}
}
