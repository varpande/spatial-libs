#ifndef SPATIALLIBS_INCLUDE_GEOS_INDEX_H_
#define SPATIALLIBS_INCLUDE_GEOS_INDEX_H_

#include <vector>

#include "geos/geom/Geometry.h"
#include "geos/index/strtree/STRtree.h"
#include "geos/index/quadtree/Quadtree.h"

// Index Constructors

// A strtree that stores geometry identifier as payload
void build_strtree(std::unique_ptr <geos::index::strtree::STRtree> *geos_strtree,
                   std::vector<geos::geom::Geometry *> &geometry, std::vector<int *> geom_id) {
  for (unsigned int i = 0; i < geometry.size(); i++) {
    (*geos_strtree)->insert(geometry[i]->getEnvelopeInternal(), reinterpret_cast<void *>(geom_id[i]));
  }
  (*geos_strtree)->build();
}

// STRtree that stores geometry as payload in the index.
// We need the tree with geometry as payload for kNN queries.
// The kNN traversal algorithm uses the "item" in the tree to
// compute distance to nodes in the tree.
void build_strtree(std::unique_ptr <geos::index::strtree::STRtree> *geos_strtree,
                   std::vector<geos::geom::Geometry *> &geometry) {
  for (unsigned int i = 0; i < geometry.size(); i++)
    (*geos_strtree)->insert(geometry[i]->getEnvelopeInternal(), reinterpret_cast<void *>(geometry[i]));
  (*geos_strtree)->build();
}

// A quadtree that stores the geometry identifier as payload
void build_quadtree(std::unique_ptr <geos::index::quadtree::Quadtree> *geos_quadtree,
                    std::vector<geos::geom::Geometry *> &geometry, std::vector<int *> geom_id) {
  for (unsigned int i = 0; i < geometry.size(); i++) {
    (*geos_quadtree)->insert(geometry[i]->getEnvelopeInternal(), reinterpret_cast<void *>(geom_id[i]));
  }
}

// A quadtree that stores the geometry itself as payload
void build_quadtree(std::unique_ptr <geos::index::quadtree::Quadtree> *geos_quadtree,
                    std::vector<geos::geom::Geometry *> &geometry) {
  for (unsigned int i = 0; i < geometry.size(); i++) {
    (*geos_quadtree)->insert(geometry[i]->getEnvelopeInternal(), reinterpret_cast<void *>(geometry[i]));
  }
}

// Empty all indices
// May not be required but keep for now
void empty_strtree(std::unique_ptr <geos::index::strtree::STRtree> *geos_strtree,
                   std::vector<geos::geom::Geometry *> &geometry, std::vector<int *> pg) {
  for (unsigned int i = 0; i < geometry.size(); i++) {
    (*geos_strtree)->remove(geometry[i]->getEnvelopeInternal(), reinterpret_cast<void *>(pg[i]));
  }
}

void empty_strtree(std::unique_ptr <geos::index::strtree::STRtree> *geos_strtree,
                   std::vector<geos::geom::Geometry *> &geometry) {
  for (unsigned int i = 0; i < geometry.size(); i++)
    (*geos_strtree)->remove(geometry[i]->getEnvelopeInternal(), reinterpret_cast<void *>(geometry[i]));
}

void empty_quadtree(std::unique_ptr <geos::index::quadtree::Quadtree> *geos_quadtree,
                    std::vector<geos::geom::Geometry *> &geometry, std::vector<int *> pg) {
  for (unsigned int i = 0; i < geometry.size(); i++) {
    (*geos_quadtree)->remove(geometry[i]->getEnvelopeInternal(), reinterpret_cast<void *>(pg[i]));
  }
}

void empty_quadtree(std::unique_ptr <geos::index::quadtree::Quadtree> *geos_quadtree,
                    std::vector<geos::geom::Geometry *> &geometry) {
  for (unsigned int i = 0; i < geometry.size(); i++) {
    (*geos_quadtree)->remove(geometry[i]->getEnvelopeInternal(), reinterpret_cast<void *>(geometry[i]));
  }
}

#endif  // SPATIALLIBS_INCLUDE_GEOS_INDEX_H_
