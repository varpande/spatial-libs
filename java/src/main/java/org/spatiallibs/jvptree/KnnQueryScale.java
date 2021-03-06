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
public class KnnQueryScale {

	@State(value = Benchmark)
	public static class Data {
		@Param({"100K_tweets", "1M_tweets", "10M_tweets", "50M_tweets", "83M_tweets",
			"100K_rides", "1M_rides", "10M_rides", "100M_rides", "300M_rides"})
		private String dataset;

		// selectivity of a query
		@Param({"10"})
		private int k;

		// type of query
		@Param({"uniform", "point_aware"})
		private String queryType;

		private List<SimpleCartesianPoint> points;
		private List<SimpleCartesianPoint> queryPoints;
		private List<Double> distances;
		private VPTree<CartesianPoint, SimpleCartesianPoint> vptree;
		private int idx;

		@Setup
		public void setup() {
			if(dataset.contains("tweets")) {
				points = Utilities.readPointsBinary(System.getProperty("user.dir") + "/resources/datasets/projected/binary/java/" + dataset + ".bin");
				queryPoints = Utilities.getQueryPoints(System.getProperty("user.dir") + "/resources/query_datasets/projected/" + queryType + "/tweets_knn.csv");
			}
			if(dataset.contains("rides")) {
				points = Utilities.readPointsBinary(System.getProperty("user.dir") + "/resources/datasets/projected/binary/java/" + dataset + ".bin");
				queryPoints = Utilities.getQueryPoints(System.getProperty("user.dir") + "/resources/query_datasets/projected/" + queryType + "/taxi_knn.csv");
			}

			System.out.println("Num points: " + points.size());

			vptree = BuildIndex.buildVpTree(points);
			idx = 0;
		}

		@TearDown
		public void teardown() {
			points.clear();
			queryPoints.clear();
			vptree = null;
		}
	}

	@Benchmark
	@BenchmarkMode(Mode.Throughput)
	@Warmup(iterations = 3, time = 10, timeUnit = java.util.concurrent.TimeUnit.SECONDS)
	@OutputTimeUnit(TimeUnit.SECONDS)
	@Timeout(time = 8, timeUnit = TimeUnit.HOURS)
	@Measurement(iterations = 5, time = 60, timeUnit = java.util.concurrent.TimeUnit.SECONDS)
	@Fork(value = 1)
	public void benchmark(Data d, Blackhole bh) {
		SimpleCartesianPoint queryPoint = d.queryPoints.get(d.idx);
		List<SimpleCartesianPoint> result = Queries.knnQuery(d.vptree, queryPoint, d.k);
		bh.consume(result);
		d.idx = (d.idx + 1) % d.queryPoints.size();
	}
}
