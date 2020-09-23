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
public class RangeQueryScale {

	@State(value = Benchmark)
	public static class Data {
		@Param({"100K_tweets", "1M_tweets", "10M_tweets", "50M_tweets", "83M_tweets",
			"100K_rides", "1M_rides", "10M_rides", "100M_rides", "300M_rides",
			"100K_osm", "1M_osm", "10M_osm", "100M_osm", "200M_osm"})
		private String dataset;

		// selectivity of a query
		@Param({"0.1"})
		private String selectivity;

		// type of query
		@Param({"uniform", "point_aware"})
		private String queryType;

		private List<Point> points;
		private List<Rectangle> rects;
		private SpatialIndex rtree;
		private int idx;

		@Setup
		public void setup() {
			if(dataset.contains("tweets")) {
				points = Utilities.readPointsBinary(System.getProperty("user.dir") + "/resources/datasets/projected/binary/java/" + dataset + ".bin");
				rects = Utilities.getEnvelopes(System.getProperty("user.dir") + "/resources/query_datasets/projected/" + queryType + "/tweets_range_" + selectivity + ".csv");
			}
			if(dataset.contains("rides")) {
				points = Utilities.readPointsBinary(System.getProperty("user.dir") + "/resources/datasets/projected/binary/java/" + dataset + ".bin");
				rects = Utilities.getEnvelopes(System.getProperty("user.dir") + "/resources/query_datasets/projected/" + queryType + "/taxi_range_" + selectivity + ".csv");
			}
			if(dataset.contains("osm")) {
				points = Utilities.readPointsBinary(System.getProperty("user.dir") + "/resources/datasets/raw/binary/java/" + dataset + ".bin");
				rects = Utilities.getEnvelopes(System.getProperty("user.dir") + "/resources/query_datasets/raw/" + queryType + "/osm_range_" + selectivity + ".csv");
			}

			rtree = BuildIndex.buildRtree(points);
			idx = 0;
		}

		@TearDown
		public void teardown() {
			points.clear();
			rects.clear();
			rtree = null;
		}
	}

	@Benchmark
	@BenchmarkMode(Mode.Throughput)
	@Warmup(iterations = 3, time = 5, timeUnit = java.util.concurrent.TimeUnit.SECONDS)
	@OutputTimeUnit(TimeUnit.SECONDS)
	@Timeout(time = 8, timeUnit = TimeUnit.HOURS)
	@Measurement(iterations = 5, time = 30, timeUnit = java.util.concurrent.TimeUnit.SECONDS)
	@Fork(value = 1)
	public void benchmark(Data d, Blackhole bh) {
			List<Integer> result = Queries.rangeQuery(d.rtree, d.rects.get(d.idx), d.points.size(), d.points);
			bh.consume(result);
			d.idx = (d.idx + 1) % d.rects.size();
	}

}
