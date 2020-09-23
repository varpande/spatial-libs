### Directory layout

    .
    ├── build                           # Compiled files
    ├── src                             # Source files
    │   ├── geos_benchmarks.cpp         # Various GEOS benchmarks registered with Google Benchmark
    │   ├── s2_benchmarks.cpp           # Various S2 benchmarks registered with Google Benchmark
    │   └── ...                         # 
    ├── include                         # Header files
    │   ├── ...
    │   ├── geos_benchmarks.h           # Lambda expressions for GEOS queries
    │   ├── geos_index.h                # GEOS index constructors and destructors
    │   ├── geos_queries.h              # Implementation of various queries in GEOS
    │   ├── s2_benchmarks.h             # Lambda expressions for S2 queries
    │   ├── s2_index.h                  # S2 index constructors and destructors
    │   ├── s2_queries.h                # Implementation of various queries in S2
    │   └── ...                         # etc.
    ├── CMakeLists.txt
    └── README.md

### General Workflow
The benchmarks are registered with Google benchmark in `src/<lib_name>_benchmarks.cpp` using [RegisterBenchmark](https://github.com/google/benchmark#using-register-benchmark). Using RegisterBenchmark(name, fn, args...) allows us to register the scaling dataset benchmarks as well as the varying selectivities benchmarks as independent benchmarks by virtue of varying the function parameters that registers these benchmarks. The lambda expressions for these benchmarks are defined in `include/<lib_name>_benchmarks.h`. These benchmarks consists of three phases. First is the setup phase, where the dataset and the query dataset are loaded into memory (defined in `include/<lib_name>_load_dataset.h`), and approriate indexes (defined in `include/<lib_name>_index.h`) are built. Next is the benchmark execution phase, which execute the concerned query (defined in `include/<lib_name>_queries.h`). The last phase is the tear-down phase, which destructs various allocated objects if needed.
