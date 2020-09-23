package org.spatiallibs.jts.quadtree;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import static org.openjdk.jmh.annotations.Scope.Benchmark;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.quadtree.Quadtree;

import org.spatiallibs.jts.index.BuildIndex;
import org.spatiallibs.jts.util.Utilities;
import org.spatiallibs.jts.queries.Queries;

@State(Scope.Benchmark)
public class RangeQuerySelectivity {

	@State(value = Benchmark)
	public static class Data {
		// numPoints in millions
		@Param({"83M_tweets", "300M_rides"})
		private String numPoints;

		// selectivity of a query
		@Param({"0.0001", "0.001", "0.01", "0.1", "1"})
		private String selectivity;

		// type of query
		@Param({"uniform", "point_aware"})
		private String queryType;

		private List<Geometry> points;
		private List<Envelope> envs;
		private Quadtree quadtree;
		private int idx;

		@Setup
		public void setup() {
			if(numPoints.equals("83M_tweets")) {
				points = Utilities.readPointsBinary(System.getProperty("user.dir") + "/resources/datasets/projected/binary/java/" + numPoints + ".bin");
				envs = Utilities.getEnvelopes(System.getProperty("user.dir") + "/resources/query_datasets/projected/" + queryType + "/tweets_range_" + selectivity + ".csv");
			}
			if(numPoints.equals("300M_rides")) {
				points = Utilities.readPointsBinary(System.getProperty("user.dir") + "/resources/datasets/projected/binary/java/" + numPoints + ".bin");
				envs = Utilities.getEnvelopes(System.getProperty("user.dir") + "/resources/query_datasets/projected/" + queryType + "/taxi_range_" + selectivity + ".csv");
			}
			if(numPoints.equals("200M_osm")) {
				points = Utilities.readPointsBinary(System.getProperty("user.dir") + "/resources/datasets/raw/binary/java/" + numPoints + ".bin");
				envs = Utilities.getEnvelopes(System.getProperty("user.dir") + "/resources/query_datasets/raw/" + queryType + "/osm_range_" + selectivity + ".csv");
			}
			quadtree = BuildIndex.buildQuadtree(points);
			quadtree.size();
			idx = 0;
		}

		@TearDown
		public void teardown() {
			points.clear();
			envs.clear();
			quadtree = null;
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
		List<Object> candidates = d.quadtree.query(d.envs.get(d.idx));
		Iterator<Object> ls_itr = candidates.iterator();
		List<Object> result = new ArrayList<Object>();
		while(ls_itr.hasNext()) {
			int idx = (int) (ls_itr.next());
			if(d.envs.get(d.idx).contains(d.points.get(idx).getEnvelopeInternal()))
				result.add(d.points.get(idx));
		}
		bh.consume(result);
		d.idx = (d.idx + 1) % d.envs.size();
	}
}
