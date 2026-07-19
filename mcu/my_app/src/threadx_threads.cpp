#include <stdint.h>
//#include "threadx_threads.h" 
#include "tx_port.h"

extern "C" {
  #include "tx_api.h"
  #include "main.h"
  #include <stm32n657xx.h>

    // Prototypes for the C++ functions we want to expose to the C kernel
    void my_thread_entry(ULONG thread_input);
}


// These must have C linkage so the kernel can call them

extern "C" void my_thread_entry(ULONG thread_input)
{
    while(1)
    {   
        HAL_GPIO_TogglePin(GPIOG, GPIO_PIN_8);
        tx_thread_sleep(1000);
    }
}

