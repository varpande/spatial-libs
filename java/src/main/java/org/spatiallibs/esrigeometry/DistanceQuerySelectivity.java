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
		private List<Geometry> queryPoints;
		private List<Double> distances;
		private List<Envelope2D> envs;
		private QuadTree quadtree;
		private int idx;

		@Setup
		public void setup() {
			boolean gps = false;
			if(dataset.contains("tweets")) {
				points = Utilities.readPointsBinary(System.getProperty("user.dir") + "/resources/datasets/projected/binary/java/" + dataset + ".bin");
				queryPoints = Utilities.getQueryPoints(System.getProperty("user.dir") + "/resources/query_datasets/projected/"+ queryType + "/tweets_distance_" + selectivity + ".csv");
				distances = Utilities.getDistances(System.getProperty("user.dir") + "/resources/query_datasets/projected/"+ queryType + "/tweets_distance_" + selectivity + ".csv");
				quadtree = BuildIndex.buildQuadtree(points, 9, gps);
			}
			if(dataset.contains("rides")) {
				points = Utilities.readPointsBinary(System.getProperty("user.dir") + "/resources/datasets/projected/binary/java/" + dataset + ".bin");
				queryPoints = Utilities.getQueryPoints(System.getProperty("user.dir") + "/resources/query_datasets/projected/"+ queryType + "/taxi_distance_" + selectivity + ".csv");
                                distances = Utilities.getDistances(System.getProperty("user.dir") + "/resources/query_datasets/projected/"+ queryType + "/taxi_distance_" + selectivity + ".csv");
				quadtree = BuildIndex.buildQuadtree(points, 18, gps);
			}

			envs = Utilities.getEnvelopes(queryPoints, distances);

			quadtree.getElementCount();
			idx = 0;
		}

		@TearDown
		public void teardown() {
			points.clear();
			queryPoints.clear();
			distances.clear();
			quadtree = null;
			idx = 0;
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
			Envelope2D env = d.envs.get(d.idx);
			Geometry geom = d.queryPoints.get(d.idx);
			double distance = d.distances.get(d.idx);
			List<Geometry> result = Queries.distanceQuery(d.quadtree, d.points, env, geom, distance);
			bh.consume(result);
			d.idx = (d.idx + 1) % d.envs.size();
	}
}
