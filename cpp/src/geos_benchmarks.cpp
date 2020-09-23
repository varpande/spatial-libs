#include <vector>

#include "geos_queries.h"
#include "geos_benchmarks.h"

#include "benchmark/benchmark.h"

int main(int argc, char **argv) {
  static std::vector <std::string> selectivity = {"0.0001", "0.001", "0.01", "0.1", "1"};
  static std::vector <std::string> datasets = {"83M_tweets", "300M_rides", "200M_osm"};
  static std::vector <std::string> dataset_scale = {"100K_tweets", "1M_tweets", "10M_tweets", "50M_tweets",
                                                    "83M_tweets",
                                                    "100K_rides", "1M_rides", "10M_rides", "100M_rides", "300M_rides",
                                                    "100K_osm", "1M_osm", "10M_osm", "100M_osm", "200M_osm"
  };
  static std::vector <std::string> query_type = {"point_aware", "uniform"};


  ////////////////////////////////////////////////// Range Query ////////////////////////////////////////////////
  //
  ///////////////////// STRtree /////////////////////
  //
  // Scale

  for (int i = 0; i < query_type.size(); i++) {
    for (int j = 0; j < dataset_scale.size(); j++) {
      std::string benchmark_name =
        "STRtreeRangeQueryScale/" + query_type[i] + "/Data-" + dataset_scale[j] + "/Selectivity-" + selectivity[3] +
        "%";
      benchmark::RegisterBenchmark(benchmark_name.c_str(), strtree_range, query_type[i], dataset_scale[j],
                                   selectivity[3])->Threads(1);
    }
  }

  // Selectivity benchmarks
  for (int i = 0; i < query_type.size(); i++) {
    for (int j = 0; j < datasets.size(); j++) {
      for (int k = 0; k < selectivity.size(); k++) {
        std::string benchmark_name =
          "STRtreeRangeQuerySelectivity/" + query_type[i] + "/Data-" + datasets[j] + "/Selectivity-" + selectivity[k] +
          "%";
        benchmark::RegisterBenchmark(benchmark_name.c_str(), strtree_range, query_type[i], datasets[j],
                                     selectivity[k])->Threads(1);
      }
    }
  }

  ///////////////////// Quadtree /////////////////////
  //
  // Scale

  for (int i = 0; i < query_type.size(); i++) {
    for (int j = 0; j < dataset_scale.size(); j++) {
      std::string benchmark_name =
        "QuadtreeRangeQueryScale/" + query_type[i] + "/Data-" + dataset_scale[j] + "/Selectivity-" + selectivity[3] +
        "%";
      benchmark::RegisterBenchmark(benchmark_name.c_str(), quadtree_range, query_type[i], dataset_scale[j],
                                   selectivity[3])->Threads(1);
    }
  }

  // Selectivity benchmarks
  for (int i = 0; i < query_type.size(); i++) {
    for (int j = 0; j < datasets.size(); j++) {
      for (int k = 0; k < selectivity.size(); k++) {
        std::string benchmark_name =
          "QuadtreeRangeQuerySelectivity/" + query_type[i] + "/Data-" + datasets[j] + "/Selectivity-" + selectivity[k] +
          "%";
        benchmark::RegisterBenchmark(benchmark_name.c_str(), quadtree_range, query_type[i], datasets[j],
                                     selectivity[k])->Threads(1);
      }
    }
  }


  ////////////////////////////////////////////////// Distance Query ////////////////////////////////////////////////
  //
  ///////////////////// STRtree In Built Within /////////////////////
  //
  // Scale
  for (int i = 0; i < query_type.size(); i++) {
    for (int j = 0; j < dataset_scale.size(); j++) {
      std::string benchmark_name =
        "STRtreeDistanceQueryScale/" + query_type[i] + "/Data-" + dataset_scale[j] + "/Selectivity-" + selectivity[3] +
        "%";
      benchmark::RegisterBenchmark(benchmark_name.c_str(), strtree_distance, query_type[i], dataset_scale[j],
                                   selectivity[3])->Threads(1);
    }
  }

  // Selectivity benchmarks
  for (int i = 0; i < query_type.size(); i++) {
    for (int j = 0; j < datasets.size(); j++) {
      for (int k = 0; k < selectivity.size(); k++) {
        std::string benchmark_name =
          "STRtreeDistanceQuerySelectivity/" + query_type[i] + "/Data-" + datasets[j] + "/Selectivity-" +
          selectivity[k] + "%";
        benchmark::RegisterBenchmark(benchmark_name.c_str(), strtree_distance, query_type[i], datasets[j],
                                     selectivity[k])->Threads(1);
      }
    }
  }


  ///////////////////// Quadtree In Built Within /////////////////////
  //
  // Scale
  for (int i = 0; i < query_type.size(); i++) {
    for (int j = 0; j < dataset_scale.size(); j++) {
      std::string benchmark_name =
        "QuadtreeDistanceQueryScale/" + query_type[i] + "/Data-" + dataset_scale[j] + "/Selectivity-" + selectivity[3] +
        "%";
      benchmark::RegisterBenchmark(benchmark_name.c_str(), quadtree_distance, query_type[i], dataset_scale[j],
                                   selectivity[3])->Threads(1);
    }
  }

  // Selectivity benchmarks
  for (int i = 0; i < query_type.size(); i++) {
    for (int j = 0; j < datasets.size(); j++) {
      for (int k = 0; k < selectivity.size(); k++) {
        std::string benchmark_name =
          "QuadtreeDistanceQuerySelectivity/" + query_type[i] + "/Data-" + datasets[j] + "/Selectivity-" +
          selectivity[k] + "%";
        benchmark::RegisterBenchmark(benchmark_name.c_str(), quadtree_distance, query_type[i], datasets[j],
                                     selectivity[k])->Threads(1);
      }
    }
  }

  for (int j = 0; j < datasets.size() - 1; j++) {
    std::string benchmark_name = "STRtreeJoinQuery/Data-" + datasets[j];
    benchmark::RegisterBenchmark(benchmark_name.c_str(), strtree_on_points, datasets[j])->Threads(1);
  }

  for (int j = 0; j < datasets.size() - 1; j++) {
    std::string benchmark_name = "QuadtreeJoinQuery/Data-" + datasets[j];
    benchmark::RegisterBenchmark(benchmark_name.c_str(), quadtree_on_points, datasets[j])->Threads(1);
  }
/*
	////////////////////////////////////////////////// Join Query ////////////////////////////////////////////////
	//
	/////////////////////// STRTree on Points ////////////////////////
	//
	// Scale benchmarks
	for (int j = 10; j <= 50; j += 5) {
		std::string benchmark_name = "STRtreeOnPointsJoinScale/Data-" + std::to_string(j) + "M/Selectivity-289Polygons";
		benchmark::RegisterBenchmark(benchmark_name.c_str(), strtree_on_points, j, std::to_string(289))->Threads(1);
	}

	// Selectivity benchmarks
	for (int j = 1; j <= 289; j++) {
		if(!(j%50) || j == 289) {
			std::string benchmark_name = "STRtreeOnPointsJoin/Data-50M/Selectivity-" + std::to_string(j) + "Polygons";
			benchmark::RegisterBenchmark(benchmark_name.c_str(), strtree_on_points, 50, std::to_string(j))->Threads(1);
		}
	}

	/////////////////////// Quadtree on Points ////////////////////////
	//
	// Scale benchmarks
	for (int j = 10; j <= 50; j += 5) {
		std::string benchmark_name = "QuadtreeOnPointsJoin/Data-" + std::to_string(j) + "M/Selectivity-289Polygons";
		benchmark::RegisterBenchmark(benchmark_name.c_str(), quadtree_on_points, j, std::to_string(289))->Threads(1);
	}

	// Selectivity benchmarks
	for (int j = 1; j <= 289; j++) {
		if(!(j%50) || j == 289) {
			std::string benchmark_name = "QuadtreeOnPointsJoin/Data-50M/Selectivity-" + std::to_string(j) + "Polygons";
			benchmark::RegisterBenchmark(benchmark_name.c_str(), quadtree_on_points, 50, std::to_string(j))->Threads(1);
		}
	}
*/

  benchmark::Initialize(&argc, argv);
  benchmark::RunSpecifiedBenchmarks();
}
