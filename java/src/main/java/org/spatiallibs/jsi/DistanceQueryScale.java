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
public class DistanceQueryScale {

	@State(value = Benchmark)
	public static class Data {
		@Param({"100K_tweets", "1M_tweets", "10M_tweets", "50M_tweets", "83M_tweets",
			"100K_rides", "1M_rides", "10M_rides", "100M_rides", "300M_rides"})
		private String dataset;

		// selectivity of a query
		@Param({"0.1"})
		private String selectivity;

		// type of query
		@Param({"uniform", "point_aware"})
		private String queryType;

		private List<Point> points;
		private List<Point> queryPoints;
		private List<Float> distances;
		private SpatialIndex rtree;
		private int idx;

		@Setup
		public void setup() {
			if(dataset.contains("tweets")) {
				points = Utilities.readPointsBinary(System.getProperty("user.dir") + "/resources/datasets/projected/binary/java/" + dataset + ".bin");
				queryPoints = Utilities.getQueryPoints(System.getProperty("user.dir") + "/resources/query_datasets/projected/" + queryType + "/tweets_distance_" + selectivity + ".csv");
		                distances = Utilities.getDistances(System.getProperty("user.dir") + "/resources/query_datasets/projected/" + queryType + "/tweets_distance_" + selectivity + ".csv");
			}
			if(dataset.contains("rides")) {
				points = Utilities.readPointsBinary(System.getProperty("user.dir") + "/resources/datasets/projected/binary/java/" + dataset + ".bin");
				queryPoints = Utilities.getQueryPoints(System.getProperty("user.dir") + "/resources/query_datasets/projected/" + queryType + "/taxi_distance_" + selectivity + ".csv");
				distances = Utilities.getDistances(System.getProperty("user.dir") + "/resources/query_datasets/projected/" + queryType + "/taxi_distance_" + selectivity + ".csv");
			}

			rtree = BuildIndex.buildRtree(points);
			idx = 0;
		}

		@TearDown
		public void teardown() {
			points.clear();
			queryPoints.clear();
			distances.clear();
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
			Point point = d.queryPoints.get(d.idx);
			float distance = d.distances.get(d.idx);
			List<Integer> result = Queries.distanceQuery(d.rtree, d.points.size(), d.points, point, distance);
			bh.consume(result);
			d.idx = (d.idx + 1) % d.queryPoints.size();
	}

}
