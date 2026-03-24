# Additional clean files
cmake_minimum_required(VERSION 3.16)

if("${CONFIG}" STREQUAL "" OR "${CONFIG}" STREQUAL "Debug")
  file(REMOVE_RECURSE
  "/home/bsgursta/Programming_Projects/LocalInference/mcu/local_inference/Appli/build"
  "/home/bsgursta/Programming_Projects/LocalInference/mcu/local_inference/FSBL/build"
  )
endif()
