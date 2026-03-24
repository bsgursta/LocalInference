#include "tx_api.h"
#include "main.h"


TX_THREAD my_thread;

/* Use to create the threads needed*/
    // First unused is the heap first unused address
void tx_application_define(void *first_unused_memory)
{
    tx_thread_create(&my_thread, "My Thread",
    my_thread_entry, 0x1234, first_unused_memory, 1024,
    3, 3, TX_NO_TIME_SLICE, TX_AUTO_START);
}
void my_thread_entry(ULONG thread_input)
{
/* Enter into a forever loop. */
while(1)
{   
    
    /* Sleep for 1 tick. */
    tx_thread_sleep(1);
}
}