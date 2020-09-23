/* standard library headers */
#include <vector>

/* s2 header */
#include "s2/s2closest_point_query.h"

/* schema and helper functions headers */
#include "s2_schema.h"
#include "s2_load_dataset.h"
#include "util.h"
#include "s2_queries.h"
#include "s2_build_index.h"

/* For testing */
//#include <fstream>
#include "benchmark/benchmark.h"

auto point_index_range = [](benchmark::State &state, std::string query_type, std::string dataset,
                            std::string selectivity) {
  S2Points points;
  std::vector <S2LatLngRect> rects;
  auto point_index = std::make_unique < S2PointIndex < int >> ();
  int itr = 0;

  get_points(points, dataset);
  get_rectangles(rects, query_type, dataset.substr(dataset.find("_") + 1), selectivity);

  build_point_index(&point_index, points);

  while (state.KeepRunning()) {
    uint64_t res_size = pointindex_range_query(point_index, rects[itr], points);
    benchmark::DoNotOptimize(res_size);
    itr = (itr + 1) % rects.size();
  }
};

/*
auto latlng_range = [](benchmark::State& state, int dataset_size, std::string selectivity) {
	S2Points points;
	std::vector<S2LatLngRect> rects;

	get_points(points, dataset_size);
	get_rectangles(rects, selectivity);

	int itr = 0;

	while(state.KeepRunning ()) {
		uint64_t ret_size = latlng_range_query(rects[0], points);
		benchmark::DoNotOptimize(ret_size);
		itr = (itr + 1) % rects.size();
	}
};
*/

auto point_index_distance = [](benchmark::State &state, std::string query_type, std::string dataset,
                               std::string selectivity) {
  S2Points points;
  std::vector <S2Point> query_points;
  std::vector<double> distances;
  auto point_index = std::make_unique < S2PointIndex < int >> ();
  int itr = 0;

  get_points(points, dataset);
  get_distances(query_points, distances, query_type, dataset.substr(dataset.find("_") + 1), selectivity);

  build_point_index(&point_index, points);

  while (state.KeepRunning()) {
    uint64_t res_size = distance_query(point_index, query_points[itr], distances[itr]);
    benchmark::DoNotOptimize(res_size);
    itr = (itr + 1) % query_points.size();
  }
};

auto point_index_knn = [](benchmark::State &state, std::string query_type, std::string dataset, int k) {
  S2Points points, query_points;
  auto point_index = std::make_unique < S2PointIndex < int >> ();
  int itr = 0;

  get_points(points, dataset);
  get_knn_points(query_points, query_type, dataset.substr(dataset.find("_") + 1));

  build_point_index(&point_index, points);

  while (state.KeepRunning()) {
    uint64_t res_size = knn_query(point_index, query_points.s2points[itr], k);
    benchmark::DoNotOptimize(res_size);
    itr = (itr + 1) % query_points.s2points.size();
  }
};

auto point_in_polygon_join = [](benchmark::State &state, std::string dataset) {
  S2Points points;
  S2Polygons polygons;

  MutableS2ShapeIndex::Options options;
  options.set_max_edges_per_cell(1);
  auto polygon_index = std::make_unique<MutableS2ShapeIndex>(options);

  get_points(points, dataset);
  get_polygons(polygons, dataset.substr(dataset.find("_") + 1));

  build_shape_index(&polygon_index, polygons);

  while (state.KeepRunning()) {
    std::vector<unsigned> count = point_in_polygon_query(polygon_index, polygons, points);
    benchmark::DoNotOptimize(count);
  }
};

int main(int argc, char **argv) {

  static std::vector <std::string> selectivity = {"0.0001", "0.001", "0.01", "0.1", "1"};
  static std::vector<int> knn_selectivity = {1, 2, 5, 10, 50, 100, 500, 1000, 5000, 10000};
  static std::vector <std::string> datasets = {"83M_tweets", "300M_rides", "200M_osm"};
  static std::vector <std::string> dataset_scale = {"100K_tweets", "1M_tweets", "10M_tweets", "50M_tweets",
                                                    "83M_tweets",
                                                    "100K_rides", "1M_rides", "10M_rides", "100M_rides", "300M_rides",
                                                    "100K_osm", "1M_osm", "10M_osm", "100M_osm", "200M_osm"
  };
  static std::vector <std::string> knn_dataset_scale = {"100K_tweets", "1M_tweets", "10M_tweets", "50M_tweets",
                                                        "83M_tweets",
                                                        "100K_rides", "1M_rides", "10M_rides", "100M_rides",
                                                        "300M_rides"
  };
  static std::vector <std::string> knn_datasets = {"83M_tweets", "300M_rides"};
  static std::vector <std::string> query_type = {"point_aware", "uniform"};

  // Register All Benchmarks

  // Range Query

  // Point Index Range Query
  // Scale point dataset from 10 to 45 million points
  for (int i = 0; i < query_type.size(); i++) {
    for (int j = 0; j < dataset_scale.size(); j++) {
      std::string benchmark_name =
        "RangeQueryScale/" + query_type[i] + "/Data-" + dataset_scale[j] + "/Selectivity-" + selectivity[3] + "%";
      benchmark::RegisterBenchmark(benchmark_name.c_str(), point_index_range, query_type[i], dataset_scale[j],
                                   selectivity[3])->Threads(1);
    }
  }

  // Selectivity bechnmarks
  for (int i = 0; i < query_type.size(); i++) {
    for (int j = 0; j < datasets.size(); j++) {
      for (int k = 0; k < selectivity.size(); k++) {
        std::string benchmark_name =
          "RangeQuerySelectivity" + query_type[i] + "/Data-" + datasets[j] + "/Selectivity-" + selectivity[k] + "%";
        benchmark::RegisterBenchmark(benchmark_name.c_str(), point_index_range, query_type[i], datasets[j],
                                     selectivity[k])->Threads(1);
      }
    }
  }

  // Distance Query Benchmarks
  // Scale
  for (int i = 0; i < query_type.size(); i++) {
    for (int j = 0; j < dataset_scale.size(); j++) {
      std::string benchmark_name =
        "DistanceQueryScale/" + query_type[i] + "/Data-" + dataset_scale[j] + "/Selectivity-" + selectivity[3] + "%";
      benchmark::RegisterBenchmark(benchmark_name.c_str(), point_index_distance, query_type[i], dataset_scale[j],
                                   selectivity[3])->Threads(1);
    }
  }

  // Selectivity bechnmarks
  for (int i = 0; i < query_type.size(); i++) {
    for (int j = 0; j < datasets.size(); j++) {
      for (int k = 0; k < selectivity.size(); k++) {
        std::string benchmark_name =
          "DistanceQuerySelectivity" + query_type[i] + "/Data-" + datasets[j] + "/Selectivity-" + selectivity[k] + "%";
        benchmark::RegisterBenchmark(benchmark_name.c_str(), point_index_distance, query_type[i], datasets[j],
                                     selectivity[k])->Threads(1);
      }
    }
  }

  for (int j = 0; j < datasets.size() - 1; j++) {
    std::string benchmark_name = "JoinQuery/Data-" + datasets[j];
    benchmark::RegisterBenchmark(benchmark_name.c_str(), point_in_polygon_join, datasets[j])->Threads(1);
  }

  // Knn Query Benchmarks
  // Scale
  for (int i = 0; i < query_type.size(); i++) {
    for (int j = 0; j < knn_dataset_scale.size(); j++) {
      std::string benchmark_name =
        "KnnQueryScale/" + query_type[i] + "/Data-" + knn_dataset_scale[j] + "/k-" + std::to_string(knn_selectivity[3]);
      benchmark::RegisterBenchmark(benchmark_name.c_str(), point_index_knn, query_type[i], knn_dataset_scale[j],
                                   knn_selectivity[3])->Threads(1);
    }
  }

  // Selectivity bechnmarks
  for (int i = 0; i < query_type.size(); i++) {
    for (int j = 0; j < knn_datasets.size(); j++) {
      for (int k = 0; k < knn_selectivity.size(); k++) {
        std::string benchmark_name = "KnnQuerySelectivity" + query_type[i] + "/Data-" + knn_datasets[j] + "/k-" +
                                     std::to_string(knn_selectivity[k]);
        benchmark::RegisterBenchmark(benchmark_name.c_str(), point_index_knn, query_type[i], knn_datasets[j],
                                     knn_selectivity[k])->Threads(1);
      }
    }
  }
/*
	// Distance Query benchmarks
	// Scale point dataset
	for (int i = 10; i <= 45; i += 5) {
		std::string benchmark_name = "DistanceQueryScale/Data-" + std::to_string(i) + "M/Selectivity-" + selectivity[3] + "%";
		benchmark::RegisterBenchmark(benchmark_name.c_str(), point_index_distance, i, selectivity[3])->Threads(1);
	}

	// Selectivity benchmarks
	for (int j = 0; j < selectivity.size(); j++) {
		std::string benchmark_name = "DistanceQuerySelectivity/Data-" + std::to_string(50) + "M/Selectivity-" + selectivity[j] + "%";
		benchmark::RegisterBenchmark(benchmark_name.c_str(), point_index_distance, 50, selectivity[j])->Threads(1);
	}

	// Point in Polygon Join Query Benchmarks
	// Scale point dataset
	for (int i = 10; i <= 50; i += 5) {
		std::string benchmark_name = "JoinQueryScale/Data-" + std::to_string(i) + "M/Selectivity-289Polygons";
		benchmark::RegisterBenchmark(benchmark_name.c_str(), point_in_polygon_join, i, std::to_string(289))->Threads(1);
	}

	// Scale polygons
	for (int i = 1; i <= 289; i += 1) {
		if(!(i % 50) || i == 289) {
			std::string benchmark_name = "JoinQuerySelectivity/Data-" + std::to_string(50) + "M/Selectivity-" + std::to_string(i) + "Polygons";
			benchmark::RegisterBenchmark(benchmark_name.c_str(), point_in_polygon_join, 50, std::to_string(i))->Threads(1);
		}
	}
*/
  benchmark::Initialize(&argc, argv);
  benchmark::RunSpecifiedBenchmarks();
}
