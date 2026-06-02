# Set the minimum version of CMake that can be used
cmake_minimum_required(VERSION 3.20)

# Enable compile command to ease indexing with e.g. clangd
set(CMAKE_EXPORT_COMPILE_COMMANDS TRUE)

# Project definition
project(Nx_TCP_Echo_Client)

# Enable CMake support for ASM and C/CPP languages
enable_language(C CXX ASM)

set(BUILD_UNIT_0_NAME Nx_TCP_Echo_Client)

# Create an executable object type
add_executable(${BUILD_UNIT_0_NAME})

# STM32CubeIDE for Visual Studio Code managed CMake file
include(cmake/vscode_generated.cmake)

# Compiler options
target_compile_options(${BUILD_UNIT_0_NAME} PRIVATE
  # User defined compiler options
)

# Linker options
target_link_options(${BUILD_UNIT_0_NAME} PRIVATE 
  # User defined linker options
)

# Add sources to executable/library
target_sources(${BUILD_UNIT_0_NAME} PRIVATE
  # User defined sources
)

# Add include directories
target_include_directories(${BUILD_UNIT_0_NAME} PRIVATE
  # User defined include directories
  Drivers/STM32N6xx_HAL_Driver/Inc
)

# CUSTOM LOGIC

add_subdirectory("../my_app" "${CMAKE_CURRENT_BINARY_DIR}/my_app_build")

target_link_libraries(${CMAKE_PROJECT_NAME}
    # Add user defined libraries
    PRIVATE
    my_app_lib
)

# CUSTOM LOGIC
