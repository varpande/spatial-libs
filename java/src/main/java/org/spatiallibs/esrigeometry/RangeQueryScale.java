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

		private List<Geometry> points;
		private List<Envelope2D> envs;
		private QuadTree quadtree;
		private int idx;

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
		}

		@TearDown
		public void teardown() {
			points.clear();
			envs.clear();
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
			List<Geometry> result = Queries.rangeQuery(d.quadtree, d.envs.get(d.idx), d.points);
			bh.consume(result);
			d.idx = (d.idx + 1) % d.envs.size();
	}
}
