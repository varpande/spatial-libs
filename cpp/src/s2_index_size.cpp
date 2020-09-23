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

int main(int argc, char **argv) {

  std::string dataset = argv[1];

  S2Points points;
  auto point_index = std::make_unique < S2PointIndex < int >> ();
  get_points(points, dataset);

  build_point_index(&point_index, points);
}
