cmake_minimum_required(VERSION 3.22)

project(${CMAKE_PROJECT_NAME} C CXX ASM)

include("mx-generated.cmake")

set(MY_APP_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../my_app")

target_include_directories(${CMAKE_PROJECT_NAME} PRIVATE
    ${MY_APP_DIR}/inc
    Middlewares/ST/threadx/common/inc
    Middlewares/ST/threadx/ports/cortex_m55/gnu/inc
)

file(GLOB_RECURSE USER_SOURCES "${MY_APP_DIR}/src/*.cpp" "${MY_APP_DIR}/src/*.c")

target_sources(${CMAKE_PROJECT_NAME} PRIVATE
    ${USER_SOURCES}
)

target_compile_definitions(${CMAKE_PROJECT_NAME} PRIVATE
    USE_EXTERNAL_APP
)