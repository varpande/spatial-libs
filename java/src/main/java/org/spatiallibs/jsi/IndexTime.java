package org.spatiallibs.jsi;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import static org.openjdk.jmh.annotations.Scope.Benchmark;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.infomatiq.jsi.Point;
import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.SpatialIndex;

import org.spatiallibs.jsi.index.BuildIndex;
import org.spatiallibs.jsi.util.Utilities;
import org.spatiallibs.jsi.queries.Queries;

@State(Scope.Benchmark)
public class IndexTime {

	@State(value = Benchmark)
	public static class Data {
		// dataset in millions
		@Param({"83M_tweets", "300M_rides", "200M_osm"})
		private String dataset;

		private List<Point> points;
		private SpatialIndex rtree;

		@Setup
		public void setup() {
			if(dataset.equals("83M_tweets")) {
				points = Utilities.readPointsBinary(System.getProperty("user.dir") + "/resources/datasets/projected/binary/java/" + dataset + ".bin");
			}
			if(dataset.equals("300M_rides")) {
				points = Utilities.readPointsBinary(System.getProperty("user.dir") + "/resources/datasets/projected/binary/java/" + dataset + ".bin");
			}
		}

		@TearDown
		public void teardown() {
			points.clear();
			rtree = null;
		}
	}

	@Benchmark
	@BenchmarkMode(Mode.SingleShotTime)
	@Warmup(iterations = 0)
	@OutputTimeUnit(TimeUnit.SECONDS)
	@Timeout(time = 8, timeUnit = TimeUnit.HOURS)
	@Fork(value = 1)
	public void benchmark(Data d, Blackhole bh) {
		d.rtree = BuildIndex.buildRtree(d.points);
		bh.consume(d.rtree);
	}
}
