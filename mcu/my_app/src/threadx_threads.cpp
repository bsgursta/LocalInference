#include <stdint.h>

extern "C" {
    #include "tx_api.h"
    #include "main.h"
#include <stm32n657xx.h>

    // Prototypes for the C++ functions we want to expose to the C kernel
    void tx_application_define(void *first_unused_memory);
    void my_thread_entry(ULONG thread_input);
}

TX_THREAD my_thread;

// These must have C linkage so the kernel can call them
// Create all functions to call
extern "C" void tx_application_define(void *first_unused_memory)
{
    tx_thread_create(&my_thread, (char*)"My Thread",
        my_thread_entry, 0x1234, first_unused_memory, 1024,
        3, 3, TX_NO_TIME_SLICE, TX_AUTO_START);
}

extern "C" void my_thread_entry(ULONG thread_input)
{
    while(1)
    {   
        HAL_GPIO_TogglePin(GPIOG,GPIO_PIN_8);
        tx_thread_sleep(1000);
    }
}