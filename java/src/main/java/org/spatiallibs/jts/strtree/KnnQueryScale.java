package org.spatiallibs.jts.strtree;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import static org.openjdk.jmh.annotations.Scope.Benchmark;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.strtree.STRtree;

import org.spatiallibs.jts.index.BuildIndex;
import org.spatiallibs.jts.util.Utilities;
import org.spatiallibs.jts.queries.Queries;

@State(Scope.Benchmark)
public class KnnQueryScale {

	@State(value = Benchmark)
	public static class Data {
		// numPoints in millions
		@Param({"100K_tweets", "1M_tweets", "10M_tweets", "50M_tweets", "83M_tweets",
			"100K_rides", "1M_rides", "10M_rides", "100M_rides", "300M_rides"})
		private String dataset;

		// selectivity of a query
		@Param({"10"})
		private int k;

		// type of query
		@Param({"uniform", "point_aware"})
		private String queryType;

		private List<Geometry> points;
		private List<Geometry> queryPoints;
		private STRtree strtree;
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

			strtree = BuildIndex.buildRtreePoints(points);
			strtree.size();
			idx = 0;
		}

		@TearDown
		public void teardown() {
			points.clear();
			queryPoints.clear();
			strtree = null;
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
		Geometry point = d.queryPoints.get(d.idx);
		Object[] result = Queries.knnQuery(d.strtree, point, d.k);
		bh.consume(result);
		d.idx = (d.idx + 1) % d.queryPoints.size();
	}
}
