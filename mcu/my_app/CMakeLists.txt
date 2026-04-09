add_library(my_app_lib STATIC
    src/threadx_threads.cpp
)

# Add the chip definition so the HAL headers know what device this is
target_compile_definitions(my_app_lib PRIVATE STM32N657xx)

target_include_directories(my_app_lib PUBLIC inc)
