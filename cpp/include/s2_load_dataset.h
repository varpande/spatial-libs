#ifndef SPATIALLIBS_INCLUDE_S2_LOAD_DATASET_H_
#define SPATIALLIBS_INCLUDE_S2_LOAD_DATASET_H_

#include <string.h>

#include "s2/s2loop.h"
#include "s2/s2point.h"

#include "io.h"
#include "s2_schema.h"
#include "util.h"

/* Helper function that reads a WKT and makes an S2Loop */
static void wkt_to_s2loop(const std::string wkt, S2Loop &loop) {
  // TODO remove magic number, parse POLYGON((
  const char *begin = wkt.c_str() + 9;
  char *next = nullptr;
  std::vector <S2Point> points;
  for (auto itr = begin; itr != begin + wkt.length(); itr++) {
    double longitude = strtod(itr, &next);
    itr = ++next;
    double latitude = strtod(itr, &next);
    points.push_back(S2LatLng::FromDegrees(latitude, longitude).Normalized().ToPoint());
    //std::cout << longitude << ", " << latitude << std::endl;
    if (*next == ')') break;
    else itr = next;
  }
  points.pop_back();
  std::reverse(points.begin(), points.end());
  loop.Init(points);
}

/* Load s2 neighborhoods from a file with format (id, neighborhood_name, WKT) */
static void load_s2_neighborhoods(S2Polygons &polygons, const char *fileName) {
  ReadFile file;
  file.read_whole_file(fileName);

  const char *fbegin = file.get_begin();
  const char *fend = file.get_end();

  while (fbegin != fend) {
    auto iter = fbegin;
    while (*iter != ',') iter++;
    polygons.ids.push_back(std::stoul(std::string(fbegin, iter - fbegin)));
    fbegin = ++iter;
    while (*iter != ',') iter++;
    polygons.names.push_back(std::string(fbegin, iter - fbegin));
    fbegin = ++iter;
    while (*iter != '\n') iter++;
    auto loop = std::make_unique<S2Loop>();
    wkt_to_s2loop(std::string(fbegin, iter - fbegin), *loop);
    polygons.s2loops.push_back(move(loop));
    fbegin = iter + 1;
  }
}

/* Load s2 points from a file with format (latitude, longitude) */
static void load_s2_points(S2Points &points, const char *fileName) {
  ReadFile file;
  file.read_whole_file(fileName);

  const char *fbegin = file.get_begin();
  const char *fend = file.get_end();

  while (fbegin != fend) {
    auto iter = fbegin;
    while (*iter != ',') iter++;
    double latitude = std::stod(std::string(fbegin, iter - fbegin), nullptr);
    fbegin = ++iter;
    while (*iter != '\n') iter++;
    double longitude = std::stod(std::string(fbegin, iter - fbegin), nullptr);
    S2LatLng latlng(S2LatLng::FromDegrees(latitude, longitude).Normalized());
    //std::cout << longitude << "," << latitude << std::endl;
    points.latitude.push_back(latitude);
    points.longitude.push_back(longitude);
    points.s2latlng.push_back(latlng);
    points.s2points.push_back(latlng.ToPoint());
    fbegin = iter + 1;
  }
}

/* Load s2 points from a file with format (latitude, longitude) */
static void load_rectangles(std::vector <S2LatLngRect> &rects, const char *fileName) {
  ReadFile file;
  file.read_whole_file(fileName);

  const char *fbegin = file.get_begin();
  const char *fend = file.get_end();

  while (fbegin != fend) {
    auto iter = fbegin;
    while (*iter != ',') iter++;
    double low_latitude = std::stod(std::string(fbegin, iter - fbegin), nullptr);
    fbegin = ++iter;
    while (*iter != ',') iter++;
    double low_longitude = std::stod(std::string(fbegin, iter - fbegin), nullptr);
    fbegin = ++iter;
    while (*iter != ',') iter++;
    double high_latitude = std::stod(std::string(fbegin, iter - fbegin), nullptr);
    fbegin = ++iter;
    while (*iter != '\n') iter++;
    double high_longitude = std::stod(std::string(fbegin, iter - fbegin), nullptr);

    S2LatLng lo(S2LatLng::FromDegrees(low_latitude, low_longitude).Normalized());
    S2LatLng hi(S2LatLng::FromDegrees(high_latitude, high_longitude).Normalized());
    S2LatLngRect rectangle(lo, hi);
    rects.push_back(rectangle);

    fbegin = iter + 1;
  }
}

static void
load_distance_query_points(std::vector <S2Point> &query_points, std::vector<double> &distances, const char *fileName) {
  ReadFile file;
  file.read_whole_file(fileName);

  const char *fbegin = file.get_begin();
  const char *fend = file.get_end();

  while (fbegin != fend) {
    auto iter = fbegin;
    while (*iter != ',') iter++;
    double latitude = std::stod(std::string(fbegin, iter - fbegin), nullptr);
    fbegin = ++iter;
    while (*iter != ',') iter++;
    double longitude = std::stod(std::string(fbegin, iter - fbegin), nullptr);
    fbegin = ++iter;
    while (*iter != '\n') iter++;
    double distance = std::stod(std::string(fbegin, iter - fbegin), nullptr);

    S2LatLng latlng(S2LatLng::FromDegrees(latitude, longitude).Normalized());
    query_points.push_back(latlng.ToPoint());
    distances.push_back(distance);

    fbegin = iter + 1;
  }
}

/* Load s2 points from a file with format (latitude, longitude) */
static void load_s2_points_binary(S2Points &points, const std::string &fileName) {
  std::vector <Point> binary_points = LoadBinaryPoints(fileName);
  double latitude, longitude;
  for (int i = 0; i < binary_points.size(); i++) {
    latitude = binary_points[i].x;
    longitude = binary_points[i].y;
    S2LatLng latlng(S2LatLng::FromDegrees(latitude, longitude).Normalized());
    points.latitude.push_back(latitude);
    points.longitude.push_back(longitude);
    points.s2latlng.push_back(latlng);
    points.s2points.push_back(latlng.ToPoint());
  }
}

static void get_points(S2Points &points, std::string dataset) {
  std::string input_points_file = get_project_directory() + "/resources/datasets/raw/binary/cpp/" + dataset + ".bin";
  load_s2_points_binary(points, input_points_file.c_str());
}

static void get_polygons(S2Polygons &polygons, std::string dataset) {
  std::string input_polygons_file = get_project_directory() + "/resources/datasets/neighborhoods/raw/polygons_289.csv";
  if (dataset == "osm") {
    input_polygons_file = get_project_directory() + "/resources/datasets/neighborhoods/raw/countries.csv";
  }
  load_s2_neighborhoods(polygons, input_polygons_file.c_str());
}

static void get_rectangles(std::vector <S2LatLngRect> &rects, std::string query_type, std::string dataset,
                           std::string selectivity) {
  if (dataset == "rides") dataset = "taxi";
  std::string input_rects_file =
    get_project_directory() + "/resources/query_datasets/raw/" + query_type + "/" + dataset + "_range_" + selectivity +
    ".csv";
  load_rectangles(rects, input_rects_file.c_str());
}

static void get_distances(std::vector <S2Point> &query_points, std::vector<double> &distances, std::string query_type,
                          std::string dataset, std::string selectivity) {
  if (dataset == "rides") dataset = "taxi";
  std::string input_distance_file =
    get_project_directory() + "/resources/query_datasets/raw/" + query_type + "/" + dataset + "_distance_" +
    selectivity + ".csv";
//	std::string input_distance_file = get_project_directory() + "/resources/query_datasets/raw/" + dataset + "_distance_" + selectivity + ".csv";
  load_distance_query_points(query_points, distances, input_distance_file.c_str());
}

static void get_knn_points(S2Points &points, std::string query_type, std::string dataset) {
  if (dataset == "rides") dataset = "taxi";
  std::string input_knn_file =
    get_project_directory() + "/resources/query_datasets/raw/" + query_type + "/" + dataset + "_knn.csv";
  load_s2_points(points, input_knn_file.c_str());
}

#endif  // SPATIALLIBS_INCLUDE_S2_LOAD_DATASET_H_
