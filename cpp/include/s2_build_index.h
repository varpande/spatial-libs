#ifndef SPATIALLIBS_INCLUDE_S2_BUILD_INDEX_H_
#define SPATIALLIBS_INCLUDE_S2_BUILD_INDEX_H_

// S2 headers
#include "s2/mutable_s2shape_index.h"
#include "s2/s2point_index.h"

// Schema and utility functions
#include "s2_schema.h"

static void build_point_index(std::unique_ptr <S2PointIndex<int>> *index, S2Points &points) {
  for (size_t i = 0; i < points.s2points.size(); i++)
    (*index)->Add(points.s2points[i], i);
}

static void build_shape_index(std::unique_ptr <MutableS2ShapeIndex> *index, S2Polygons &polygons) {
  for (size_t i = 0; i < polygons.s2loops.size(); i++)
    (*index)->Add(std::make_unique<S2Loop::Shape>(polygons.s2loops[i].get()));
  (*index)->ForceBuild();
}

#endif //  SPATIALLIBS_INCLUDE_S2_BUILD_INDEX_H_
