## Introduction
This repository provides a performance evaluation of five spatial libraries (two in C++, and three in Java):

- [Google S2](https://github.com/google/s2geometry)
- [Geometry Engine, Open Source](https://github.com/libgeos/geos)
- [ESRI geometry-api-java](https://github.com/Esri/geometry-api-java)
- [JTS Topology Suite](https://github.com/locationtech/jts)
- [Java Spatial Index (JSI)](https://github.com/aled/jsi)

We also compare the libraries with an open source implementation of Vantage Point Tree, called [jvptree](https://github.com/jchambers/jvptree). The evaluation was done on the basis of four queries: range query, distance query, kNN query, and a point-in-polygon join query. There are two benchmarks for each query: scale the dataset with query selectivity fixed to 0.1%, and fix the dataset to 50 million locations (800MB raw data) and increase the selectivity of the query.

## Getting Started

### Requirements

The following tools are required to be installed on the system:
1. __Java SDK__: For optimal performance Oracle Java should be prefered ([guide for setup on ubuntu](http://ubuntuhandbook.org/index.php/2019/03/install-oracle-java-12-ubuntu-18-04-16-04/)).
2. __Maven__: `sudo apt install maven`
3. __gtest__: `sudo apt install libgtest-dev`

### Download Datasets

This benchmark uses several dataset and query files. To download them, please go to setup folder and run the setup script:

``cd setup && ./setup.sh``

The setup script downloads all the datasets (aproximately 19GB), creates all the symlinks required by the various benchmarks, and also converts the location datasets into a binary format for faster loading during the benchmark runs. Depending on your internet connection, the setup can take anywhere between 25-60 minutes. To run the benchmark, a minimum of 32GB of main memory is required, as some libraries have a high memory usage for some queries.

### Java Benchmarks
We use [Java Microbenchmark Harness (JMH)](https://openjdk.java.net/projects/code-tools/jmh/) for running the benchmarks of the libraries written in Java (automatically downloaded via Maven). To run all of the Java-based benchmarks, go to the _java_ folder, and:

1. `cd java`
2. `mvn clean package`
3. `java -jar target/benchmarks.jar`

If you would like to run only a subset of the benchmarks, you can do so as:
* `java -jar target/benchmarks.jar jsi.RangeQueryScale` or
* `java -jar target/benchmarks.jar jsi.RangeQuerySelectivity` or
* `java -jar target/benchmarks.jar jts.kdtree.RangeQueryScale`

Specific benchmarks can be selected using `<lib_name>.<query>`.

- `<lib_name>` can be one of the following, `esrigeometry`, `jsi`, `jts`, and `jvptree`.

- `<queries>` can be one of the following, `RangeQuery`, `DistanceQuery`, `KnnQuery`, and `JoinQuery`. All of the queries have two flavors: `Scale` and `Selectivity` (e.g. `RangeQueryScale` or `RangeQuerySelectivity`) Please note that not all libraries support all queries.
For `jts` there is an extra redirection in the form of `<lib_name>.<index>.<query>`, and the queries and their code is thus modularized based on the indexes. `<index>` can be one of `kdtree`, `quadtree`, and `strtree`.

There are approximately >450 benchmark combinations for all the libraries in Java and it can take around 2 days to execute them all. Every benchmark runs for at least 5 minutes (to get stable numbers), some may run longer if the queries are more expensive. In addition, every benchmark needs to load and index the dataset.

### C++ Benchmarks
We use [Google Benchmark](https://github.com/google/benchmark) for running the benchmarks of libraries written in C++. To execute the C++ based benchmarks, please run the following commands:

1. `cd cpp`
2. `mkdir build`
3. `cd build`
4. `cmake -DCMAKE_BUILD_TYPE=Release ..`
5. `make -j geos_benchmarks s2_benchmarks`
6. `bin/geos_benchmarks` or `bin/s2_benchmarks`

All the tools/libraries that the benchmarks are dependent on, are automatically downloaded, compiled and linked with the benchmark binaries during the build process. You may (or may not) have to set some environment variables (library dependent) after running cmake, please look at the cmake output to be sure. Library dependencies are not also download automatically currently, and if some dependency is not found, it will be reported in cmake output. Simple `make -j` builds all targets.

If you would like to run only a subset of the benchmarks, you may use `bin/s2_benchmarks --benchmark_list_tests=true` for example to retrieve a list of all benchmarks for the binary. You can then use `--benchmark_filter=<benchmark_name>` with the benchmark binary to run that particular benchmark. For example:

* `bin/s2_benchmarks --benchmark_filter=RangeQueryScale` or
* `bin/s2_benchmarks --benchmark_filter=RangeQuerySelectivity`

etc.

There are approximately >100 benchmarks for all the libraries in C++ and it can take upto 1 day to execute them all. The runtime of each benchmark is dynamically determined by Google Benchmark. Again, some benchmarks may take longer to stabilize while some may not.

## Disclaimer
The benchmarks were run on Ubuntu 18.04 and use Linux specific commands at various places for I/O etc. Operations on non-Linux operating systems may not work.
