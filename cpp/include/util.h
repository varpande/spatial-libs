#ifndef SPATIALLIBS_INCLUDE_UTIL_H_
#define SPATIALLIBS_INCLUDE_UTIL_H_

#include <chrono>
#include <utility>
#include <algorithm>
#include <thread>
#include <functional>
#include <vector>
#include <mutex>
#include <unordered_map>
#include <unistd.h>

#define MAXPATHLEN 2048

/* get working directory */
std::string get_working_path() {
  char temp[MAXPATHLEN];
  return (getcwd(temp, sizeof(temp)) ? std::string(temp) : std::string(""));
}

std::string get_project_directory() {
  std::string path = get_working_path();
  std::size_t found = path.find_last_of("cpp/");
  return path.substr(0, found);
}

#endif  // SPATIALLIBS_INCLUDE_UTIL_H_
