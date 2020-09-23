#ifndef SPATIALLIBS_INCLUDE_GEOS_BENCHMARKS_H_
#define SPATIALLIBS_INCLUDE_GEOS_BENCHMARKS_H_

#include <vector>

#include "geos/index/strtree/STRtree.h"
#include "geos/index/quadtree/Quadtree.h"

#include "geos_index.h"
#include "geos_schema.h"
#include "geos_load_dataset.h"
#include "benchmark/benchmark.h"

#include <iomanip>

auto strtree_range = [](benchmark::State &state, std::string query_type, std::string dataset, std::string selectivity) {
  GEOSGeometry points;
  GEOSGeometry target_ranges;
  std::unique_ptr <geos::index::strtree::STRtree> strtree = std::make_unique<geos::index::strtree::STRtree>();
  int itr;

  // setup
  if (state.thread_index == 0) {
    get_points(points, dataset);
    get_ranges(target_ranges, query_type, dataset.substr(dataset.find("_") + 1), selectivity);
    build_strtree(&strtree, points.geometry, points.id);
    itr = 0;
  }

  while (state.KeepRunning()) {
    uint64_t res_size = strtree_range_query(*(strtree.get()), target_ranges.geometry[itr]);
    benchmark::DoNotOptimize(res_size);
    itr = (itr + 1) % target_ranges.geometry.size();
  }
};

auto quadtree_range = [](benchmark::State &state, std::string query_type, std::string dataset,
                         std::string selectivity) {
  GEOSGeometry points;
  GEOSGeometry target_ranges;
  std::unique_ptr <geos::index::quadtree::Quadtree> quadtree = std::make_unique<geos::index::quadtree::Quadtree>();
  int itr;

  // setup
  if (state.thread_index == 0) {
    get_points(points, dataset);
    get_ranges(target_ranges, query_type, dataset.substr(dataset.find("_") + 1), selectivity);
    build_quadtree(&quadtree, points.geometry);
    itr = 0;
  }

  while (state.KeepRunning()) {
    uint64_t res_size = quadtree_range_query(*(quadtree.get()), target_ranges.geometry[itr]);
    benchmark::DoNotOptimize(res_size);
    itr = (itr + 1) % target_ranges.geometry.size();
  }

};

auto strtree_distance = [](benchmark::State &state, std::string query_type, std::string dataset,
                           std::string selectivity) {
  GEOSGeometry points, target_ranges, target_points;
  std::vector<double> distances;
  std::unique_ptr <geos::index::strtree::STRtree> strtree = std::make_unique<geos::index::strtree::STRtree>();
  int itr;

  // setup
  if (state.thread_index == 0) {
    get_points(points, dataset);
    get_distances(target_points, target_ranges, distances, query_type, dataset.substr(dataset.find("_") + 1),
                  selectivity);
    build_strtree(&strtree, points.geometry, points.id);
    itr = 0;
  }

  while (state.KeepRunning()) {
    uint64_t res_size = strtree_distance_query(*(strtree.get()), target_points.geometry[itr],
                                               target_ranges.geometry[itr], points, distances[itr]);
    benchmark::DoNotOptimize(res_size);
    itr = (itr + 1) % target_ranges.geometry.size();
  }

  // tear-down
  if (state.thread_index == 0) {
    distances.clear();
  }
};

auto quadtree_distance = [](benchmark::State &state, std::string query_type, std::string dataset,
                            std::string selectivity) {
  GEOSGeometry points, target_ranges, target_points;
  std::vector<double> distances;
  std::unique_ptr <geos::index::quadtree::Quadtree> quadtree = std::make_unique<geos::index::quadtree::Quadtree>();
  int itr;

  // setup
  if (state.thread_index == 0) {
    get_points(points, dataset);
    get_distances(target_points, target_ranges, distances, query_type, dataset.substr(dataset.find("_") + 1),
                  selectivity);
    build_quadtree(&quadtree, points.geometry);
    itr = 0;
  }

  while (state.KeepRunning()) {
    uint64_t res_size = quadtree_distance_query(*(quadtree.get()), target_points.geometry[itr],
                                                target_ranges.geometry[itr], distances[itr]);
    benchmark::DoNotOptimize(res_size);
    itr = (itr + 1) % target_ranges.geometry.size();
  }

  // tear-down
  if (state.thread_index == 0) {
    distances.clear();
  }
};

auto strtree_distance_own = [](benchmark::State &state, std::string query_type, std::string dataset,
                               std::string selectivity) {
  GEOSGeometry points, target_ranges, target_points;
  std::vector<double> distances;
  std::unique_ptr <geos::index::strtree::STRtree> strtree = std::make_unique<geos::index::strtree::STRtree>();
  int itr;

  // setup
  if (state.thread_index == 0) {
    get_points(points, dataset);
    get_distances(target_points, target_ranges, distances, query_type, dataset.substr(dataset.find("_") + 1),
                  selectivity);
    build_strtree(&strtree, points.geometry);
    itr = 0;
  }

  while (state.KeepRunning()) {
    uint64_t res_size = strtree_distance_query_own(*(strtree.get()), target_points.geometry[itr],
                                                   target_ranges.geometry[itr], distances[itr]);
    benchmark::DoNotOptimize(res_size);
    itr = (itr + 1) % target_ranges.geometry.size();
  }

  // tear-down
  if (state.thread_index == 0) {
    distances.clear();
  }
};

auto quadtree_distance_own = [](benchmark::State &state, std::string query_type, std::string dataset,
                                std::string selectivity) {
  GEOSGeometry points, target_ranges, target_points;
  std::vector<double> distances;
  std::unique_ptr <geos::index::quadtree::Quadtree> quadtree = std::make_unique<geos::index::quadtree::Quadtree>();
  int itr;

  // setup
  if (state.thread_index == 0) {
    get_points(points, dataset);
    get_distances(target_points, target_ranges, distances, query_type, dataset.substr(dataset.find("_") + 1),
                  selectivity);
    build_quadtree(&quadtree, points.geometry);
    itr = 0;
  }

  while (state.KeepRunning()) {
    uint64_t res_size = quadtree_distance_query_own(*(quadtree.get()), target_points.geometry[itr],
                                                    target_ranges.geometry[itr], distances[itr]);
    benchmark::DoNotOptimize(res_size);
    itr = (itr + 1) % target_ranges.geometry.size();
  }

  // tear-down
  if (state.thread_index == 0) {
    distances.clear();
  }
};

auto strtree_on_points = [](benchmark::State &state, std::string dataset) {
  GEOSGeometry points, polygons;
  GEOSPreparedGeometry prepared_polygons;
  std::unique_ptr <geos::index::strtree::STRtree> strtree = std::make_unique<geos::index::strtree::STRtree>();

  // setup
  if (state.thread_index == 0) {
    get_points(points, dataset);
    get_polygons(polygons, dataset);
    get_prepared_polygons(polygons, prepared_polygons);
    build_strtree(&strtree, points.geometry);
  }

  while (state.KeepRunning()) {
    std::vector<unsigned> counts = strtree_on_points_join(*(strtree.get()), polygons, prepared_polygons);
    benchmark::DoNotOptimize(counts);
  }
};

auto quadtree_on_points = [](benchmark::State &state, std::string dataset) {
  GEOSGeometry points, polygons;
  GEOSPreparedGeometry prepared_polygons;
  std::unique_ptr <geos::index::quadtree::Quadtree> quadtree = std::make_unique<geos::index::quadtree::Quadtree>();

  // setup
  if (state.thread_index == 0) {
    get_points(points, dataset);
    get_polygons(polygons, dataset);
    get_prepared_polygons(polygons, prepared_polygons);
    build_quadtree(&quadtree, points.geometry);
  }

  while (state.KeepRunning()) {
    std::vector<unsigned> counts = quadtree_on_points_join(*(quadtree.get()), polygons, prepared_polygons);
    benchmark::DoNotOptimize(counts);
  }
};


auto strtree_on_polygons = [](benchmark::State &state, std::string dataset) {
  GEOSGeometry points, polygons;
  GEOSPreparedGeometry prepared_polygons;
  std::unique_ptr <geos::index::strtree::STRtree> strtree = std::make_unique<geos::index::strtree::STRtree>();

  // setup
  if (state.thread_index == 0) {
    get_points(points, dataset);
    get_polygons(polygons, dataset);
    get_prepared_polygons(polygons, prepared_polygons);
    build_strtree(&strtree, polygons.geometry, prepared_polygons.id);
  }

  while (state.KeepRunning()) {
    std::vector<unsigned> counts = strtree_on_polygons_join(*(strtree.get()), points, prepared_polygons);
    benchmark::DoNotOptimize(counts);
  }
};

auto quadtree_on_polygons = [](benchmark::State &state, std::string dataset) {
  GEOSGeometry points, polygons;
  GEOSPreparedGeometry prepared_polygons;
  std::unique_ptr <geos::index::quadtree::Quadtree> quadtree = std::make_unique<geos::index::quadtree::Quadtree>();

  // setup
  if (state.thread_index == 0) {
    get_points(points, dataset);
    get_polygons(polygons, dataset);
    get_prepared_polygons(polygons, prepared_polygons);
    build_quadtree(&quadtree, polygons.geometry, prepared_polygons.id);
  }

  while (state.KeepRunning()) {
    std::vector<unsigned> counts = quadtree_on_polygons_join(*(quadtree.get()), points, prepared_polygons);
    benchmark::DoNotOptimize(counts);
  }
};

#endif  // SPATIALLIBS_INCLUDE_GEOS_BENCHMARKS_H_
