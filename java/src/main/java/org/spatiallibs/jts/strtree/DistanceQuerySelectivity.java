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
public class DistanceQuerySelectivity {

	@State(value = Benchmark)
	public static class Data {

		@Param({"83M_tweets", "300M_rides"})
		private String dataset;

		// selectivity of a query
		@Param({"0.0001", "0.001", "0.01", "0.1", "1"})
		private String selectivity;

		// type of query
		@Param({"uniform", "point_aware"})
		private String queryType;

		private List<Geometry> points;
		private List<Geometry> circles;
		private List<Double> distances;
		private Object[] obj;
		private STRtree strtree;
		private int idx;

		@Setup
		public void setup() {
			if(dataset.contains("tweets")) {
				points = Utilities.readPointsBinary(System.getProperty("user.dir") + "/resources/datasets/projected/binary/java/" + dataset + ".bin");
				obj = Utilities.getQueryPointsAndDistances(System.getProperty("user.dir") + "/resources/query_datasets/projected/" + queryType + "/tweets_distance_" + selectivity + ".csv");
				circles = (List<Geometry>) obj[0];
				distances = (List<Double>) obj[1];
			}
			if(dataset.contains("rides")) {
				points = Utilities.readPointsBinary(System.getProperty("user.dir") + "/resources/datasets/projected/binary/java/" + dataset + ".bin");
				obj = Utilities.getQueryPointsAndDistances(System.getProperty("user.dir") + "/resources/query_datasets/projected/" + queryType + "/taxi_distance_" + selectivity + ".csv");
				circles = (List<Geometry>) obj[0];
				distances = (List<Double>) obj[1];
			}
			strtree = BuildIndex.buildRtree(points);
			strtree.size();
			idx = 0;
		}

		@TearDown
		public void teardown() {
			points.clear();
			circles.clear();
			distances.clear();
			strtree = null;
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
		Geometry circle = d.circles.get(d.idx);
        double distance = d.distances.get(d.idx);
        List<Object> result = Queries.distanceQueryRtree(d.strtree, circle, d.points, distance);
		bh.consume(result);
		d.idx = (d.idx + 1) % d.circles.size();
	}
}
