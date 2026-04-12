# Distributed under the OSI-approved BSD 3-Clause License.  See accompanying
# file LICENSE.rst or https://cmake.org/licensing for details.

cmake_minimum_required(VERSION ${CMAKE_VERSION}) # this file comes with cmake

# If CMAKE_DISABLE_SOURCE_CHANGES is set to true and the source directory is an
# existing directory in our source tree, calling file(MAKE_DIRECTORY) on it
# would cause a fatal error, even though it would be a no-op.
if(NOT EXISTS "/home/bsgursta/Programming_Projects/LocalInference/mcu/local_inference/FSBL")
  file(MAKE_DIRECTORY "/home/bsgursta/Programming_Projects/LocalInference/mcu/local_inference/FSBL")
endif()
file(MAKE_DIRECTORY
  "/home/bsgursta/Programming_Projects/LocalInference/mcu/local_inference/FSBL/build"
  "/home/bsgursta/Programming_Projects/LocalInference/mcu/local_inference/build/Debug/FSBL"
  "/home/bsgursta/Programming_Projects/LocalInference/mcu/local_inference/build/Debug/FSBL/tmp"
  "/home/bsgursta/Programming_Projects/LocalInference/mcu/local_inference/build/Debug/FSBL/src/local_inference_FSBL-stamp"
  "/home/bsgursta/Programming_Projects/LocalInference/mcu/local_inference/build/Debug/FSBL/src"
  "/home/bsgursta/Programming_Projects/LocalInference/mcu/local_inference/build/Debug/FSBL/src/local_inference_FSBL-stamp"
)

set(configSubDirs )
foreach(subDir IN LISTS configSubDirs)
    file(MAKE_DIRECTORY "/home/bsgursta/Programming_Projects/LocalInference/mcu/local_inference/build/Debug/FSBL/src/local_inference_FSBL-stamp/${subDir}")
endforeach()
if(cfgdir)
  file(MAKE_DIRECTORY "/home/bsgursta/Programming_Projects/LocalInference/mcu/local_inference/build/Debug/FSBL/src/local_inference_FSBL-stamp${cfgdir}") # cfgdir has leading slash
endif()
