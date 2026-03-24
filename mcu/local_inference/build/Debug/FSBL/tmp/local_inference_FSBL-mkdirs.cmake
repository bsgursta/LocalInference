# Distributed under the OSI-approved BSD 3-Clause License.  See accompanying
# file Copyright.txt or https://cmake.org/licensing for details.

cmake_minimum_required(VERSION 3.5)

file(MAKE_DIRECTORY
  "/home/bsgursta/Programming_Projects/LocalInference/mcu/local_inference/FSBL"
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
