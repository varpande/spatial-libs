#ifndef SPATIALLIBS_INCLUDE_GEOS_SCHEMA_H_
#define SPATIALLIBS_INCLUDE_GEOS_SCHEMA_H_

#include <vector>

#include "geos/geom/Geometry.h"
#include "geos/geom/prep/PreparedGeometry.h"
#include "geos/geom/GeometryFactory.h"
#include "geos/geom/prep/PreparedGeometryFactory.h"

// GeometryFactory keeps a reference of the geometry created.
// It should be kept to destroy the Geometry object on exit.
struct GEOSGeometry {
  std::vector<geos::geom::Geometry *> geometry;
  std::vector<int *> id;
  geos::geom::GeometryFactory::Ptr factory;

  GEOSGeometry() {
    factory = geos::geom::GeometryFactory::create();
  }

  ~GEOSGeometry() {
    for (auto g : geometry) {
      factory->destroyGeometry(g);
    }
    geometry.clear();
    for (auto i : id) {
      delete i;
    }
    id.clear();
  }
};

struct GEOSPreparedGeometry {
  std::vector<const geos::geom::prep::PreparedGeometry *> prepared_geometry;
  std::vector<int *> id;
  geos::geom::prep::PreparedGeometryFactory pfactory;

  ~GEOSPreparedGeometry() {
    for (auto pg : prepared_geometry) {
      pfactory.destroy(pg);
    }
    prepared_geometry.clear();
    for (auto i : id) {
      delete i;
    }
    id.clear();
  }
};

#endif  // SPATIALLIBS_INCLUDE_GEOS_SCHEMA_H_
