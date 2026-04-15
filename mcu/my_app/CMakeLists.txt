add_library(my_app_lib STATIC
    src/threadx_threads.cpp
)

target_include_directories(my_app_lib PUBLIC 
    "${PROJECT_SOURCE_DIR}/Core/Inc"
)

target_link_libraries(my_app_lib PUBLIC 
    stm32cubemx
)


