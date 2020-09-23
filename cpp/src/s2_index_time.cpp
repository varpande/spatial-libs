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

/*
auto point_index_size = [](benchmark::State& state, int dataset_size) {
	S2Points points;
	auto point_index = std::make_unique<S2PointIndex<int>>();

	get_points(points, dataset_size);

	while(state.KeepRunning ()) {
		build_point_index(&point_index, points);
	}
};
*/

auto point_index_time = [](benchmark::State &state, std::string dataset) {

  S2Points points;
  auto point_index = std::make_unique < S2PointIndex < int >> ();

  get_points(points, dataset);

  while (state.KeepRunning()) {
    build_point_index(&point_index, points);
  }
};


int main(int argc, char **argv) {

  static std::vector <std::string> datasets = {"83M_tweets", "300M_rides", "200M_osm"};

  for (int j = 0; j < datasets.size(); j++) {
    std::string benchmark_name = "PointIndexingTime/Data-" + datasets[j];
    benchmark::RegisterBenchmark(benchmark_name.c_str(), point_index_time, datasets[j])->Threads(1);
  }
/*
	// Scale point dataset from 10 to 50 million points
	for (int i = 10; i <= 50; i += 5) {
		std::string benchmark_name = "PointIndexingTime/Data-" + std::to_string(i) + "M";
		benchmark::RegisterBenchmark(benchmark_name.c_str(), point_index_time, i)->Threads(1);
	}
*/
  benchmark::Initialize(&argc, argv);
  benchmark::RunSpecifiedBenchmarks();
}
