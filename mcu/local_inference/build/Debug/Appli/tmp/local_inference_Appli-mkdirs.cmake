# Distributed under the OSI-approved BSD 3-Clause License.  See accompanying
# file LICENSE.rst or https://cmake.org/licensing for details.

cmake_minimum_required(VERSION ${CMAKE_VERSION}) # this file comes with cmake

# If CMAKE_DISABLE_SOURCE_CHANGES is set to true and the source directory is an
# existing directory in our source tree, calling file(MAKE_DIRECTORY) on it
# would cause a fatal error, even though it would be a no-op.
if(NOT EXISTS "/home/bsgursta/Programming_Projects/LocalInference/mcu/local_inference/Appli")
  file(MAKE_DIRECTORY "/home/bsgursta/Programming_Projects/LocalInference/mcu/local_inference/Appli")
endif()
file(MAKE_DIRECTORY
  "/home/bsgursta/Programming_Projects/LocalInference/mcu/local_inference/Appli/build"
  "/home/bsgursta/Programming_Projects/LocalInference/mcu/local_inference/build/Debug/Appli"
  "/home/bsgursta/Programming_Projects/LocalInference/mcu/local_inference/build/Debug/Appli/tmp"
  "/home/bsgursta/Programming_Projects/LocalInference/mcu/local_inference/build/Debug/Appli/src/local_inference_Appli-stamp"
  "/home/bsgursta/Programming_Projects/LocalInference/mcu/local_inference/build/Debug/Appli/src"
  "/home/bsgursta/Programming_Projects/LocalInference/mcu/local_inference/build/Debug/Appli/src/local_inference_Appli-stamp"
)

set(configSubDirs )
foreach(subDir IN LISTS configSubDirs)
    file(MAKE_DIRECTORY "/home/bsgursta/Programming_Projects/LocalInference/mcu/local_inference/build/Debug/Appli/src/local_inference_Appli-stamp/${subDir}")
endforeach()
if(cfgdir)
  file(MAKE_DIRECTORY "/home/bsgursta/Programming_Projects/LocalInference/mcu/local_inference/build/Debug/Appli/src/local_inference_Appli-stamp${cfgdir}") # cfgdir has leading slash
endif()
