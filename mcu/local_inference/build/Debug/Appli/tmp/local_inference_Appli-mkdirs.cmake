# Distributed under the OSI-approved BSD 3-Clause License.  See accompanying
# file Copyright.txt or https://cmake.org/licensing for details.

cmake_minimum_required(VERSION 3.5)

file(MAKE_DIRECTORY
  "/home/bsgursta/Programming_Projects/LocalInference/mcu/local_inference/Appli"
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
