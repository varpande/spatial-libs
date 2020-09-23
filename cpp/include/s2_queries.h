#ifndef SPATIALLIBS_INCLUDE_S2_QUERIES_H_
#define SPATIALLIBS_INCLUDE_S2_QUERIES_H_

// Standard library headers
#include <vector>
#include <mutex>
#include <memory>
#include <fstream>
#include <unordered_map>

// S2 headers
#include "s2/mutable_s2shape_index.h"
#include "s2/s2contains_point_query.h"
#include "s2/s2earth.h"
#include "s2/s2point.h"
#include "s2/s2cell_id.h"
#include "s2/s2closest_point_query_base.h"
//include "s2/s2latlngrect.h"

// Schema and utility functions
#include "s2_schema.h"
#include "util.h"

static uint64_t latlng_range_query(S2LatLngRect &rectangle, S2Points &points) {
  std::vector <S2LatLng> result;
  for (int i = 0; i < points.s2points.size(); i++) {
    if (rectangle.Contains(points.s2latlng[i]))
      result.push_back(points.s2latlng[i]);
  }
  return result.size();
}

static uint64_t
pointindex_range_query(std::unique_ptr <S2PointIndex<int>> &index, S2LatLngRect &rectangle, S2Points &points) {
  S2ClosestPointQuery<int> query(index.get());

  // set query to only include points in the rectangle
  query.mutable_options()->set_region(&rectangle);

  S2Point target_point(rectangle.GetCenter());
  S2ClosestPointQueryPointTarget target(target_point);
  const auto result = query.FindClosestPoints(&target);

  return result.size();
}

static std::vector<unsigned>
point_in_polygon_query(std::unique_ptr <MutableS2ShapeIndex> &index, S2Polygons &polygons, S2Points &points) {
  std::vector<unsigned> counts(polygons.s2loops.size(), 0);
  auto query = MakeS2ContainsPointQuery(index.get());

  for (int i = 0; i < points.s2points.size(); i++) {
    std::vector < S2Shape * > candidates = query.GetContainingShapes(points.s2points[i]);
    for (auto &candidate : candidates) {
      size_t id = candidate->id();
      counts[id] = counts[id] + 1;
    }
  }

  return counts;
}

static uint64_t distance_query(std::unique_ptr <S2PointIndex<int>> &index, S2Point &target_point, double distance) {
  S2ClosestPointQuery<int> query(index.get());
  query.mutable_options()->set_max_distance(S2Earth::ToAngle(util::units::Meters(distance)));

  S2ClosestPointQueryPointTarget target(target_point);
  const auto result = query.FindClosestPoints(&target);

  return result.size();
}

static uint64_t knn_query(std::unique_ptr <S2PointIndex<int>> &index, S2Point &target_point, int k) {
  S2ClosestPointQuery<int> query(index.get());
  query.mutable_options()->set_max_results(k);

  S2ClosestPointQueryPointTarget target(target_point);
  const auto result = query.FindClosestPoints(&target);

  return result.size();
}

#endif //  SPATIALLIBS_INCLUDE_S2_QUERIES_H_
