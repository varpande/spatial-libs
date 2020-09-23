#include <vector>

#include "geos/index/strtree/STRtree.h"
#include "geos/index/quadtree/Quadtree.h"

//#include "geos_queries.h"
#include "geos_index.h"
#include "geos_schema.h"
#include "geos_load_dataset.h"
#include "benchmark/benchmark.h"

static void compute_strtree(std::string dataset) {
  GEOSGeometry points;
  std::unique_ptr <geos::index::strtree::STRtree> strtree = std::make_unique<geos::index::strtree::STRtree>();

  get_points(points, dataset);

  build_strtree(&strtree, points.geometry);
}

static void compute_quadtree(std::string dataset) {
  GEOSGeometry points;
  std::unique_ptr <geos::index::quadtree::Quadtree> quadtree = std::make_unique<geos::index::quadtree::Quadtree>();

  get_points(points, dataset);

//	HeapProfilerStart(“quadtree”)
  build_quadtree(&quadtree, points.geometry);
//	HeapProfilerStop()
}

int main(int argc, char **argv) {

  std::string dataset = argv[1];
  std::string tree = argv[2];

  if (tree == "quadtree") {
    compute_quadtree(dataset);
  } else if (tree == "strtree") {
    compute_strtree(dataset);
  }
}
