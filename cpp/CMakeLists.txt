cmake_minimum_required(VERSION 2.8.2)

project(compare_spatial_libraries)

include(CTest)

include(DownloadProject.cmake)

# Download GEOS v3.7.1
download_project(PROJ geos
        GIT_REPOSITORY https://github.com/libgeos/geos.git
        GIT_TAG 27a5e771c3a52e7b9103cdd69100a33cc1f67eb3
        )

# Download Google S2 v0.9.0
download_project(PROJ s2
        GIT_REPOSITORY https://github.com/google/s2geometry.git
        GIT_TAG v0.9.0
        )

# Download Google Benchmark v1.5.0
download_project(PROJ benchmark
        GIT_REPOSITORY https://github.com/google/benchmark.git
        GIT_TAG v1.5.0
        )

# Download TC Malloc
download_project(PROJ tcmalloc
        GIT_REPOSITORY https://github.com/gperftools/gperftools.git
        GIT_TAG gperftools-2.7.90
        )

option(BENCHMARK_ENABLE_TESTING OFF)
option(GEOS_ENABLE_TESTS OFF)
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin)

# enforce build type as Release
if(NOT CMAKE_BUILD_TYPE)
        set(CMAKE_BUILD_TYPE Release)
endif()

add_subdirectory(${geos_SOURCE_DIR} ${geos_BINARY_DIR})
add_subdirectory(${s2_SOURCE_DIR} ${s2_BINARY_DIR})
add_subdirectory(${benchmark_SOURCE_DIR} ${benchmark_BINARY_DIR})

#add_subdirectory(${tcmalloc_SOURCE_DIR} ${tcmalloc_BINARY_DIR})
#add_subdirectory(${tcmalloc_BINARY_DIR})

# geos_revision.cmake and geos_revision.h.in are generated
# in ${CMAKE_CURRENT_BINARY_DIR} by geos CMakeLists and we 
# need to move them to geos_BINARY_DIR.
file(COPY ${CMAKE_CURRENT_BINARY_DIR}/geos_revision.cmake ${CMAKE_CURRENT_BINARY_DIR}/geos_revision.h.in DESTINATION ${geos_BINARY_DIR})

# s2 exports its include directory automatically for CMAKE > 2.8.11
if (CMAKE_VERSION VERSION_LESS 2.8.11)
    include_directories("${s2_SOURCE_DIR}")
endif ()

# geos does not export it's include directories, so include them manually
#include_directories("${s2_SOURCE_DIR}")
include_directories("${geos_SOURCE_DIR}/include")
include_directories("${geos_BINARY_DIR}/include")
include_directories(include)

# add s2 executables, set source properties and link dependencies
# add s2 benchmarks
add_executable(s2_benchmarks src/s2_benchmarks.cpp)
set_source_files_properties(src/s2_benchmarks.cpp PROPERTIES COMPILE_FLAGS "-std=c++14 -DNDEBUG -O3 -mavx -Wno-attributes")
target_link_libraries(s2_benchmarks s2 pthread benchmark)

# add s2 index time benchmarks
add_executable(s2_index_time src/s2_index_time.cpp)
set_source_files_properties(src/s2_index_time.cpp PROPERTIES COMPILE_FLAGS "-std=c++14 -DNDEBUG -O3 -mavx -Wno-attributes")
target_link_libraries(s2_index_time s2 pthread benchmark)

# add geos executables, set source properties and link dependencies
#add geos benchmarks
add_executable(geos_benchmarks src/geos_benchmarks.cpp)
set_source_files_properties(src/geos_benchmarks.cpp PROPERTIES COMPILE_FLAGS "-std=c++14 -DNDEBUG -O3 -march=native -Wno-cpp")
target_link_libraries(geos_benchmarks geos pthread benchmark)

#add geos index time
add_executable(geos_index_time src/geos_index_time.cpp)
set_source_files_properties(src/geos_index_time.cpp PROPERTIES COMPILE_FLAGS "-std=c++14 -DNDEBUG -O3 -march=native -Wno-cpp")
target_link_libraries(geos_index_time geos pthread benchmark)

#add geos index size
add_executable(geos_index_size src/geos_index_size.cpp)
set_source_files_properties(src/geos_index_size.cpp PROPERTIES COMPILE_FLAGS "-std=c++14 -DNDEBUG -O3 -march=native -Wno-cpp -fno-builtin-malloc -fno-builtin-calloc -fno-builtin-realloc -fno-builtin-free")
target_link_libraries(geos_index_size geos pthread benchmark -L/tpch/pandey/dasfaa_journal/spatial-libs/cpp/build/tcmalloc-build/libtcmalloc.so)

#add s2 index size
add_executable(s2_index_size src/s2_index_size.cpp)
set_source_files_properties(src/s2_index_size.cpp PROPERTIES COMPILE_FLAGS "-std=c++14 -DNDEBUG -O3 -march=native -Wno-cpp -fno-builtin-malloc -fno-builtin-calloc -fno-builtin-realloc -fno-builtin-free")
target_link_libraries(s2_index_size s2 pthread benchmark -L/tpch/pandey/dasfaa_journal/spatial-libs/cpp/build/tcmalloc-build/libtcmalloc.so)
