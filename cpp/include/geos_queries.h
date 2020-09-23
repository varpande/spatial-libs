#ifndef SPATIALLIBS_INCLUDE_GEOS_QUERIES_H_
#define SPATIALLIBS_INCLUDE_GEOS_QUERIES_H_

#include <vector>

#include "geos/geom/Geometry.h"
#include "geos/index/strtree/STRtree.h"
#include "geos/index/quadtree/Quadtree.h"

#include "geos_schema.h"

static uint64_t strtree_range_query(geos::index::strtree::STRtree &strtree, geos::geom::Geometry *target_range) {
  std::vector<void *> candidates;
  strtree.query(target_range->getEnvelopeInternal(), candidates);
  return candidates.size();
}

static uint64_t quadtree_range_query(geos::index::quadtree::Quadtree &quadtree, geos::geom::Geometry *target_range) {
  std::vector < geos::geom::Geometry * > result;
  result.reserve(1e8);
  GEOSPreparedGeometry pg;
  pg.prepared_geometry.push_back(pg.pfactory.create(target_range));
  std::vector<void *> candidates;
  quadtree.query(target_range->getEnvelopeInternal(), candidates);

  for (auto &candidate : candidates) {
    const auto &point = static_cast<geos::geom::Geometry *>(candidate);
    if (pg.prepared_geometry[0]->contains(point))
      result.push_back(point);
  }

  uint64_t res_size = result.size();
  result.clear();
  return res_size;
}

static uint64_t strtree_distance_query(geos::index::strtree::STRtree &strtree, geos::geom::Geometry *target_point,
                                       geos::geom::Geometry *target_range, GEOSGeometry &points, double distance) {
  std::vector < geos::geom::Geometry * > result;
  result.reserve(1e8);
  std::vector<void *> candidates;
  strtree.query(target_range->getEnvelopeInternal(), candidates);

  for (auto &candidate : candidates) {
    int idx = *(static_cast<int *>(candidate));
    //		const auto& point = static_cast<geos::geom::Geometry*>(candidate);
    if (points.geometry[idx]->isWithinDistance(target_point, distance))
      result.push_back(points.geometry[idx]);
  }

  uint64_t res_size = result.size();
  result.clear();
  return res_size;
}

static uint64_t quadtree_distance_query(geos::index::quadtree::Quadtree &quadtree, geos::geom::Geometry *target_point,
                                        geos::geom::Geometry *target_range, double distance) {
  std::vector < geos::geom::Geometry * > result;
  result.reserve(1e8);
  std::vector<void *> candidates;
  quadtree.query(target_range->getEnvelopeInternal(), candidates);

  for (auto &candidate : candidates) {
    const auto &point = static_cast<geos::geom::Geometry *>(candidate);
    if (point->isWithinDistance(target_point, distance))
      result.push_back(point);
  }

  uint64_t res_size = result.size();
  result.clear();
  return res_size;
}

static double Distance(double x1, double x2, double y1, double y2) {
  return (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
}

static uint64_t strtree_distance_query_own(geos::index::strtree::STRtree &strtree, geos::geom::Geometry *target_point,
                                           geos::geom::Geometry *target_range, double distance) {
  std::vector < geos::geom::Geometry * > result;
  result.reserve(1e8);
  std::vector<void *> candidates;
  const geos::geom::Coordinate *c1 = target_point->getCoordinate();
  double d = distance * distance;

  strtree.query(target_range->getEnvelopeInternal(), candidates);

  for (auto &candidate : candidates) {
    const auto &point = static_cast<geos::geom::Geometry *>(candidate);
    const geos::geom::Coordinate *c2 = point->getCoordinate();
    if (Distance(c1->x, c1->y, c2->x, c2->y) <= d)
      result.push_back(point);
  }

  uint64_t res_size = result.size();
  result.clear();
  return res_size;
}

static uint64_t
quadtree_distance_query_own(geos::index::quadtree::Quadtree &quadtree, geos::geom::Geometry *target_point,
                            geos::geom::Geometry *target_range, double distance) {
  std::vector < geos::geom::Geometry * > result;
  result.reserve(1e8);
  std::vector<void *> candidates;
  const geos::geom::Coordinate *c1 = target_point->getCoordinate();
  double d = distance * distance;

  quadtree.query(target_range->getEnvelopeInternal(), candidates);

  for (auto &candidate : candidates) {
    const auto &point = static_cast<geos::geom::Geometry *>(candidate);
    const geos::geom::Coordinate *c2 = point->getCoordinate();
    if (Distance(c1->x, c1->y, c2->x, c2->y) <= d)
      result.push_back(point);
  }
  return result.size();
}

static std::vector<unsigned> strtree_on_points_join(geos::index::strtree::STRtree &strtree, GEOSGeometry &polygons,
                                                    GEOSPreparedGeometry &prepared_polygons) {
  std::vector<unsigned> counts(polygons.geometry.size(), 0);

  for (int i = 0; i < polygons.geometry.size(); i++) {
    std::vector<void *> candidates;
    strtree.query(polygons.geometry[i]->getEnvelopeInternal(), candidates);
    for (auto &candidate : candidates) {
      const auto &point = static_cast<geos::geom::Geometry *>(candidate);
      counts[i] += prepared_polygons.prepared_geometry[i]->contains(point);
    }
  }

  return counts;
}

static std::vector<unsigned> quadtree_on_points_join(geos::index::quadtree::Quadtree &quadtree, GEOSGeometry &polygons,
                                                     GEOSPreparedGeometry &prepared_polygons) {
  std::vector<unsigned> counts(polygons.geometry.size(), 0);

  for (int i = 0; i < polygons.geometry.size(); i++) {
    std::vector<void *> candidates;
    quadtree.query(polygons.geometry[i]->getEnvelopeInternal(), candidates);
    for (auto &candidate : candidates) {
      const auto &point = static_cast<geos::geom::Geometry *>(candidate);
      counts[i] += prepared_polygons.prepared_geometry[i]->contains(point);
    }
  }

  return counts;
}

static std::vector<unsigned> strtree_on_polygons_join(geos::index::strtree::STRtree &strtree, GEOSGeometry &points,
                                                      GEOSPreparedGeometry &prepared_polygons) {
  std::vector<unsigned> counts(prepared_polygons.prepared_geometry.size(), 0);

  for (int i = 0; i < points.geometry.size(); i++) {
    std::vector<void *> candidates;
    strtree.query(points.geometry[i]->getEnvelopeInternal(), candidates);
    for (auto &candidate : candidates) {
      int e = *(static_cast<int *>(candidate));
      counts[e] += prepared_polygons.prepared_geometry[e]->contains(points.geometry[i]);
    }
  }

  return counts;
}

static std::vector<unsigned> quadtree_on_polygons_join(geos::index::quadtree::Quadtree &quadtree, GEOSGeometry &points,
                                                       GEOSPreparedGeometry &prepared_polygons) {
  std::vector<unsigned> counts(prepared_polygons.prepared_geometry.size(), 0);

  for (int i = 0; i < points.geometry.size(); i++) {
    std::vector<void *> candidates;
    quadtree.query(points.geometry[i]->getEnvelopeInternal(), candidates);
    for (auto &candidate : candidates) {
      unsigned int e = *(static_cast<unsigned int *>(candidate));
      counts[e] += prepared_polygons.prepared_geometry[e]->contains(points.geometry[i]);
    }
  }

  return counts;
}

#endif  // SPATIALLIBS_INCLUDE_GEOS_QUERIES_H_
