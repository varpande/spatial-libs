#ifndef SPATIALLIBS_INCLUDE_IO_H_
#define SPATIALLIBS_INCLUDE_IO_H_

#include <fcntl.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <unistd.h>
#include <cstdlib>
#include <fstream>
#include <iostream>
#include <string>

#include "generic_point.h"

// Fastest way to read an entire file at once in to a buffer
// http://insanecoding.blogspot.com/2011/11/reading-in-entire-file-at-once-in-c.html
// File slurp courtesy: 
// https://gist.github.com/rayhamel/1823976595c08d26e6576a36e4688e87

class ReadFile {
private:
  void *begin{nullptr};
  void *end;
public:
  ReadFile() : begin(nullptr), end(nullptr) {}

  ~ReadFile();

  void read_whole_file(const char *fileName);

  const char *get_begin() { return static_cast<char *>(begin); }

  const char *get_end() { return static_cast<char *>(end); }

};

ReadFile::~ReadFile() {
  free(begin);
}

void ReadFile::read_whole_file(const char *fileName) {
  int fd = open(fileName, O_RDONLY);

  if (fd == -1) {
    std::cout << "Error opening file: " << fileName << std::endl;
    throw;
  }

  /* Advice OS for Sequential Read */
  auto r = posix_fadvise(fd, 0, 0, POSIX_FADV_SEQUENTIAL);

  /* Get File Stats */
  struct stat file_statistics;
  fstat(fd, &file_statistics);

  const size_t blocksize{static_cast<size_t>(file_statistics.st_blksize)};
  const size_t bufsize{static_cast<size_t>(file_statistics.st_size) + sizeof(char32_t)};

  auto p = posix_memalign(&begin, blocksize, bufsize);
  end = static_cast<char *>(begin) + file_statistics.st_size;
  madvise(begin, bufsize, POSIX_MADV_SEQUENTIAL);

  if (read(fd, begin, file_statistics.st_size) == -1) {
    std::cout << "Read Failed" << std::endl;
    throw;
  }
  *static_cast<char32_t *>(end) = U'\0';

}

uint64_t GetFileLength(const std::string &file_name) {
  int fileFD = open(file_name.c_str(), O_RDWR);
  if (fileFD < 0) {
    std::cout << "Unable to open file: " << file_name << std::endl; // You can check errno to see what happend
    throw;
  }
  if (fcntl(fileFD, F_GETFL) == -1) {
    std::cout << "Unable to call fcntl on file: " << file_name << std::endl; // You can check errno to see what happend
    throw;
  }
  struct stat st;
  fstat(fileFD, &st);
  close(fileFD);
  return st.st_size;
}

std::vector <Point> ReadPointsCsv(const std::string &input_file) {
  std::ifstream in(input_file);

  std::vector <Point> points;
  points.reserve(1e9);
  std::string buffer;

  while (in.good()) {
    Point p;
    char comma;
    in >> p.x >> comma >> p.y;
    if (in.good()) {
      points.push_back(p);
    }
  }

  return points;
}

void WritePointsBinary(const std::string &output_file, const std::vector <Point> &points) {
  std::ofstream out(output_file);
  out.write((char *) points.data(), points.size() * sizeof(Point));
  if (!out.good()) {
    std::cout << "Error when writing" << std::endl;
    throw;
  }
}

std::vector <Point> LoadBinaryPoints(const std::string &file_name) {
  uint64_t length = GetFileLength(file_name);
  std::vector <Point> points(length / sizeof(Point));
  std::ifstream in(file_name);
  in.read((char *) &points[0], length);
  return points;
}

#endif  // SPATIALLIBS_INCLUDE_IO_H_
