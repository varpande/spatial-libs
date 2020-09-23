#include <iostream>
#include <string>
#include <fstream>
#include <sstream>
#include <vector>

using namespace std;

struct Point {
  double x, y;
};

vector <Point> ReadPointsCsv() {
  vector <Point> points;
  points.reserve(1e9);
  string buffer;

  while (cin.good()) {
    Point p;
    char comma;
    cin >> p.x >> comma >> p.y;
    if (cin.good()) {
      points.push_back(p);
    }
  }

  return points;
}

void WritePointsBin(const string &output_file, const vector <Point> &points) {
  ofstream out(output_file);
  out.write((char *) points.data(), points.size() * sizeof(Point));
  if (!out.good()) {
    cout << "Error when writing" << endl;
    throw;
  }
}

// g++ points_to_binary.cpp -std=c++14 -g0 -O3 && cat input.csv | ./a.out output.bin
int main(int argc, char **argv) {
  cout.precision(17); // To get doubls printed in detail

  if (argc != 2) {
    cout << "usage: " << argv[0] << " <output>" << endl;
    return -1;
  }

  const string output_file = argv[1];

  vector <Point> points = ReadPointsCsv();
  WritePointsBin(output_file, points);

  return 0;
}
