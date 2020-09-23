#ifndef SPATIALLIBS_INCLUDE_S2_SCHEMA_H_
#define SPATIALLIBS_INCLUDE_S2_SCHEMA_H_

#include "s2/s2loop.h"
#include "s2/s2point.h"

//#define std::vector<S2Point> S2_POINTS

struct S2Polygons {
  std::vector <uint64_t> ids;
  std::vector <std::string> names;
  std::vector <std::unique_ptr<S2Loop>> s2loops;
};

struct S2Points {
  std::vector<double> latitude;
  std::vector<double> longitude;
  std::vector <S2LatLng> s2latlng;
  std::vector <S2Point> s2points;
};

struct cell_id_range {
  uint64_t min;
  uint64_t max;
};

#endif  // SPATIALLIBS_INCLUDE_S2_SCHEMA_H_
