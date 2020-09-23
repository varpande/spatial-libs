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
public class RangeQuerySelectivity {

	@State(Scope.Benchmark)
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
		private List<Envelope2D> envs;
		private QuadTree quadtree;
		private int idx;
		private long queries;
		private long result;

		@Setup
		public void setup() {
			boolean gps = false;
			if(dataset.contains("tweets")) {
				points = Utilities.readPointsBinary(System.getProperty("user.dir") + "/resources/datasets/projected/binary/java/" + dataset + ".bin");
				envs = Utilities.getEnvelopes(System.getProperty("user.dir") + "/resources/query_datasets/projected/" + queryType + "/tweets_range_" + selectivity + ".csv");
			}
			if(dataset.contains("rides")) {
				points = Utilities.readPointsBinary(System.getProperty("user.dir") + "/resources/datasets/projected/binary/java/" + dataset + ".bin");
				envs = Utilities.getEnvelopes(System.getProperty("user.dir") + "/resources/query_datasets/projected/" + queryType + "/taxi_range_" + selectivity + ".csv");
			}
			if(dataset.contains("osm")) {
				points = Utilities.readPointsBinary(System.getProperty("user.dir") + "/resources/datasets/raw/binary/java/" + dataset + ".bin");
				envs = Utilities.getEnvelopes(System.getProperty("user.dir") + "/resources/query_datasets/raw/" + queryType + "/osm_range_" + selectivity + ".csv");
				gps = true;
			}

			quadtree = BuildIndex.buildQuadtree(points, 18, gps);
			quadtree.getElementCount();
			idx = 0;
			queries = 0;
			result = 0;
		}

		@TearDown
		public void teardown() {
			double avg_result = ((double) result) / (double) (queries);
			points.clear();
			envs.clear();
			quadtree = null;
		}
	}

	@Benchmark
	@BenchmarkMode(Mode.Throughput)
	@Warmup(iterations = 3, time = 3, timeUnit = java.util.concurrent.TimeUnit.SECONDS)
	@OutputTimeUnit(TimeUnit.SECONDS)
	@Timeout(time = 8, timeUnit = TimeUnit.HOURS)
	@Measurement(iterations = 5, time = 30, timeUnit = java.util.concurrent.TimeUnit.SECONDS)
	@Fork(value = 1)
	public void benchmark(Data d, Blackhole bh) {
			List<Geometry> result = Queries.rangeQuery(d.quadtree, d.envs.get(d.idx), d.points);
			d.result = (d.result) + result.size();
			d.queries = (d.queries) + 1;
			bh.consume(result);
			d.idx = (d.idx + 1) % d.envs.size();
	}
}
