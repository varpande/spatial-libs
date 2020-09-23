#ifndef SPATIALLIBS_INCLUDE_GENERIC_POINT_H_
#define SPATIALLIBS_INCLUDE_GENERIC_POINT_H_

struct Point {
  double x;
  double y;

  friend Point operator+(const Point &lhs, const Point &rhs) { return Point{lhs.x + rhs.x, lhs.y + rhs.y}; }

  friend Point operator-(const Point &lhs, const Point &rhs) { return Point{lhs.x - rhs.x, lhs.y - rhs.y}; }

  friend Point operator*(const Point &lhs, float rhs) { return Point{lhs.x * rhs, lhs.y * rhs}; }

  bool operator==(const Point &o) const { return x == o.x && y == o.y; }

  bool operator!=(const Point &o) const { return x != o.x || y != o.y; }

  float Length() const { return Distance(Point{0, 0}); }

  float Distance(const Point &other) const {
    return (x - other.x) * (x - other.x) + (y - other.y) * (y - other.y);
  }

  const Point &ScaleToLength(float intended) {
    float current = Length();
    if (current > 0.0f) {
      float scale_factor = intended / current;
      x *= scale_factor;
      y *= scale_factor;
    }
    return *this;
  }
};

#endif  //  SPATIALLIBS_INCLUDE_GENERIC_POINT_H_
