cmake_minimum_required(VERSION 3.20)

add_library(my_app_lib STATIC
    src/threadx_threads.cpp
)

target_compile_options(my_app_lib PUBLIC
    -mcpu=cortex-m55
    -mthumb
    -mfloat-abi=hard
    -mfpu=fpv5-d16
)

target_compile_definitions(my_app_lib PUBLIC 
    STM32N657xx
)

target_include_directories(my_app_lib PUBLIC
    "inc" 
    "${PROJECT_SOURCE_DIR}/Core/Inc"
    "${PROJECT_SOURCE_DIR}/Middlewares/ST/threadx/common/inc"
    "${PROJECT_SOURCE_DIR}/Middlewares/ST/threadx/ports/cortex_m55/gnu/inc"
    
    #Library doesn't know where the Hal stuff is but main does
    "${PROJECT_SOURCE_DIR}/Drivers/STM32N6xx_HAL_Driver/Inc"
    "${PROJECT_SOURCE_DIR}/Drivers/CMSIS/Device/ST/STM32N6xx/Include"
    "${PROJECT_SOURCE_DIR}/Drivers/CMSIS/Include"
)

target_link_libraries(my_app_lib PUBLIC 
    #stm32cubemx
)


