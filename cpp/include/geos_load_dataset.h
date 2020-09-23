#ifndef SPATIALLIBS_INCLUDE_GEOS_LOAD_DATASET_H_
#define SPATIALLIBS_INCLUDE_GEOS_LOAD_DATASET_H_

#include <string>
#include <iostream>
#include <experimental/filesystem>

#include "geos/geom/Geometry.h"
#include "geos/io/WKTReader.h"
#include "geos/geom/Coordinate.h"
#include "geos/geom/Point.h"
#include "geos/geom/Envelope.h"

#include "io.h"
#include "geos_schema.h"
#include "util.h"

// Helper function that reads a WKT and return a GEOS Geometry
static geos::geom::Geometry *wkt_to_geos_geometry(const std::string wkt, geos::geom::GeometryFactory::Ptr *factory) {
  geos::io::WKTReader reader(factory->get());
  geos::geom::Geometry *geom = nullptr;

  try {
    geom = reader.read(wkt);
  }
  catch (const std::exception &ex) {
    std::cout << "Failed to read Geometry: " << std::endl;
  }
  return geom;
}

// Load geos neighborhoods to GEOSPolygons from a file with format (id, neighborhood_name, WKT)
// TODO: add support for csvs maybe tobe able to load id, polygon_name, wkt?
static void load_geos_neighborhoods(GEOSGeometry &polygons, const char *fileName) {
  ReadFile file;
  file.read_whole_file(fileName);

  const char *fbegin = file.get_begin();
  const char *fend = file.get_end();

  while (fbegin != fend) {
    auto iter = fbegin;
/*
		while (*iter != ',') iter++;
		fbegin = ++iter;
		while (*iter != ',') iter++;
		fbegin = ++iter;
*/
    while (*iter != '\n') iter++;
    polygons.geometry.push_back(wkt_to_geos_geometry(std::string(fbegin, iter - fbegin), &(polygons.factory)));
    fbegin = iter + 1;
  }
}

// Load geos points dataset from a file in format (latitude,longitude)
static void load_geos_points(GEOSGeometry &points, const char *fileName) {
  ReadFile file;
  file.read_whole_file(fileName);

  const char *fbegin = file.get_begin();
  const char *fend = file.get_end();
  while (fbegin != fend) {
    auto iter = fbegin;
    while (*iter != ',') iter++;
    double x = std::stod(std::string(fbegin, iter - fbegin), nullptr);
    fbegin = ++iter;
    while (*iter != '\n') iter++;
    double y = std::stod(std::string(fbegin, iter - fbegin), nullptr);
    geos::geom::Geometry *geom = points.factory->createPoint(geos::geom::Coordinate(x, y));
    points.geometry.push_back(geom);
    fbegin = iter + 1;
  }
}

// Load geos points dataset from a file in format (latitude,longitude)
static void load_geos_points_binary(GEOSGeometry &points, const std::string &fileName) {
  std::vector <Point> binary_points = LoadBinaryPoints(fileName);
  double x, y;
  for (int i = 0; i < binary_points.size(); i++) {
    x = binary_points[i].x;
    y = binary_points[i].y;
    geos::geom::Geometry *geom = points.factory->createPoint(geos::geom::Coordinate(x, y));
    points.geometry.push_back(geom);
    int *index = new int;
    *index = i;
    points.id.push_back(index);
  }
}

static void load_ranges(GEOSGeometry &target_ranges, const char *fileName) {
  ReadFile file;
  file.read_whole_file(fileName);

  const char *fbegin = file.get_begin();
  const char *fend = file.get_end();

  while (fbegin != fend) {
    auto iter = fbegin;
    while (*iter != ',') iter++;
    double x1 = std::stod(std::string(fbegin, iter - fbegin), nullptr);
    fbegin = ++iter;
    while (*iter != ',') iter++;
    double y1 = std::stod(std::string(fbegin, iter - fbegin), nullptr);
    fbegin = ++iter;
    while (*iter != ',') iter++;
    double x2 = std::stod(std::string(fbegin, iter - fbegin), nullptr);
    fbegin = ++iter;
    while (*iter != '\n') iter++;
    double y2 = std::stod(std::string(fbegin, iter - fbegin), nullptr);
    fbegin = ++iter;

    geos::geom::Envelope rectangle(geos::geom::Coordinate(x1, y1), geos::geom::Coordinate(x2, y2));
    target_ranges.geometry.push_back(target_ranges.factory->toGeometry(&rectangle));
  }
}

static void load_distances(GEOSGeometry &target_points, GEOSGeometry &target_ranges, std::vector<double> &distances,
                           const char *fileName) {
  ReadFile file;
  file.read_whole_file(fileName);

  const char *fbegin = file.get_begin();
  const char *fend = file.get_end();

  while (fbegin != fend) {
    auto iter = fbegin;
    while (*iter != ',') iter++;
    double x = std::stod(std::string(fbegin, iter - fbegin), nullptr);
    fbegin = ++iter;
    while (*iter != ',') iter++;
    double y = std::stod(std::string(fbegin, iter - fbegin), nullptr);
    fbegin = ++iter;
    while (*iter != '\n') iter++;
    double distance = std::stod(std::string(fbegin, iter - fbegin), nullptr);
    fbegin = ++iter;

    geos::geom::Envelope rectangle(geos::geom::Coordinate(x - distance, y - distance),
                                   geos::geom::Coordinate(x + distance, y + distance));
    geos::geom::Coordinate point(x, y);

    target_points.geometry.push_back(target_ranges.factory->createPoint(point));
    target_ranges.geometry.push_back(target_ranges.factory->toGeometry(&rectangle));
    distances.push_back(distance);
  }
}

static void
get_ranges(GEOSGeometry &target_ranges, std::string query_type, std::string dataset, std::string selectivity) {
  if (dataset == "rides") dataset = "taxi";
  std::string path = "/resources/query_datasets/projected/";
  if (dataset == "osm") path = "/resources/query_datasets/raw/";

  std::string input_range_file =
    get_project_directory() + path + query_type + "/" + dataset + "_range_" + selectivity + ".csv";
  load_ranges(target_ranges, input_range_file.c_str());
}

static void get_distances(GEOSGeometry &target_points, GEOSGeometry &target_ranges, std::vector<double> &distances,
                          std::string query_type, std::string dataset, std::string selectivity) {
  if (dataset == "rides") dataset = "taxi";
  std::string path = "/resources/query_datasets/projected/";
  if (dataset == "osm") path = "/resources/query_datasets/raw/";

  std::string input_distance_file =
    get_project_directory() + path + query_type + "/" + dataset + "_distance_" + selectivity + ".csv";

  load_distances(target_points, target_ranges, distances, input_distance_file.c_str());
}

/*
static void get_distances(GEOSGeometry& target_points, GEOSGeometry& target_ranges, std::vector<double>& distances, std::string selectivity) {
	std::string input_distance_file = get_project_directory() + "/resources/query_datasets/projected/taxi_distance_" + selectivity + ".csv";
	load_distances(target_points, target_ranges, distances, input_distance_file.c_str());
}
*/

static void get_points(GEOSGeometry &points, std::string dataset) {
  std::string path = "/resources/datasets/projected/binary/cpp/";
  if (dataset.substr(dataset.find("_") + 1) == "osm") path = "/resources/datasets/raw/binary/cpp/";

  std::string input_points_file = get_project_directory() + path + dataset + ".bin";
  load_geos_points_binary(points, input_points_file);
}

static void get_polygons(GEOSGeometry &polygons, std::string dataset) {
  std::string input_polygons_file =
    get_project_directory() + "/resources/datasets/neighborhoods/projected/polygons_289.csv";
  if (dataset == "osm") {
    input_polygons_file = get_project_directory() + "/resources/datasets/neighborhoods/projected/countries.csv";
  }
  load_geos_neighborhoods(polygons, input_polygons_file.c_str());
}

static void get_prepared_polygons(GEOSGeometry &polygons, GEOSPreparedGeometry &prepared) {
  for (int i = 0; i < polygons.geometry.size(); i++) {
    prepared.prepared_geometry.push_back(prepared.pfactory.create(polygons.geometry[i]));
    int *index = new int;
    *index = i;
    prepared.id.push_back(index);
  }
}

#endif  // SPATIALLIBS_INCLUDE_GEOS_LOAD_DATASET_H_
