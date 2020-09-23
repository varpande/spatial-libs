#include <vector>

#include "geos/index/strtree/STRtree.h"
#include "geos/index/quadtree/Quadtree.h"

//#include "geos_queries.h"
#include "geos_index.h"
#include "geos_schema.h"
#include "geos_load_dataset.h"
#include "benchmark/benchmark.h"

auto strtree_time = [](benchmark::State &state, std::string dataset) {
  GEOSGeometry points;
  std::unique_ptr <geos::index::strtree::STRtree> strtree = std::make_unique<geos::index::strtree::STRtree>();

  get_points(points, dataset);

  while (state.KeepRunning()) {
    build_strtree(&strtree, points.geometry);
  }

};

auto quadtree_time = [](benchmark::State &state, std::string dataset) {
  GEOSGeometry points;
  std::unique_ptr <geos::index::quadtree::Quadtree> quadtree = std::make_unique<geos::index::quadtree::Quadtree>();

  get_points(points, dataset);

  while (state.KeepRunning()) {
    build_quadtree(&quadtree, points.geometry);
  }

};

int main(int argc, char **argv) {

  static std::vector <std::string> datasets = {"83M_tweets", "300M_rides", "200M_osm"};

  for (int i = 0; i < datasets.size(); i++) {
    std::string benchmark_name = "STRtreeIndexTime/Data-" + datasets[i];
    benchmark::RegisterBenchmark(benchmark_name.c_str(), strtree_time, datasets[i])->Threads(1);
  }

  for (int i = 0; i < datasets.size(); i++) {
    std::string benchmark_name = "QuadtreeIndexTime/Data-" + datasets[i];
    benchmark::RegisterBenchmark(benchmark_name.c_str(), quadtree_time, datasets[i])->Threads(1);
  }

  benchmark::Initialize(&argc, argv);
  benchmark::RunSpecifiedBenchmarks();
}
