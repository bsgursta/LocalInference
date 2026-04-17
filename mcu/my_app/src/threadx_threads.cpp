#include <stdint.h>

extern "C" {
    #include "tx_api.h"
    #include "nx_api.h"
    #include "main.h"
    #include <stm32n657xx.h>

    // Prototypes for the C++ functions we want to expose to the C kernel
    void my_thread_entry(ULONG thread_input);
    void my_thread_tcp(ULONG thread_input);
}

//TX_THREAD my_thread;
/*
// These must have C linkage so the kernel can call them
// Create all functions to call
extern "C" void my_app_threads(void *first_unused_memory)
{
    tx_thread_create(&my_thread, (char*)"My Thread",
        my_thread_entry, 0x1234, first_unused_memory, 1024,
        3, 3, TX_NO_TIME_SLICE, TX_AUTO_START);
}
*/
extern "C" void my_thread_entry(ULONG thread_input)
{
    while(1)
    {   
        HAL_GPIO_TogglePin(GPIOC, GPIO_PIN_2);
        tx_thread_sleep(1000);
    }
}

/* Externs for the resources created by the NetX Duo stack */
extern NX_IP          nx_ip_0;
extern NX_PACKET_POOL nx_pool_0;

extern "C" void my_thread_tcp(ULONG thread_input)
{
    UINT status;
    NX_TCP_SOCKET my_socket;
    NX_PACKET *packet_ptr;
    ULONG server_ip = IP_ADDRESS(192, 168, 1, 100); // Change to your PC's IP
    UINT server_port = 5000;                        // Change to your PC's port

    /* 1. Wait for the IP instance to be ready and have a valid link/IP address */
    /* This prevents the thread from crashing if the cable isn't plugged in yet */
    ULONG actual_status;
    nx_ip_status_check(&nx_ip_0, NX_IP_ADDRESS_RESOLVED, &actual_status, NX_WAIT_FOREVER);

    while(1)
    {   
        // Toggle LED to show the thread is active
        HAL_GPIO_TogglePin(GPIOC, GPIO_PIN_2);

        /* 2. Create the TCP Socket */
        status = nx_tcp_socket_create(&nx_ip_0, &my_socket, (CHAR*)"Client Socket", 
                                      NX_IP_NORMAL, NX_FRAGMENT_OKAY, NX_IP_TIME_TO_LIVE, 
                                      512, NX_NULL, NX_NULL);

        /* 3. Bind the socket to a local port (NX_ANY_PORT lets the stack choose) */
        status = nx_tcp_client_socket_bind(&my_socket, NX_ANY_PORT, NX_WAIT_FOREVER);

        /* 4. Attempt to connect to the computer */
        status = nx_tcp_client_socket_connect(&my_socket, server_ip, server_port, NX_IP_PERIODIC_RATE * 5);

        if (status == NX_SUCCESS)
        {
            /* 5. Connection Successful! Let's send a message */
            nx_packet_allocate(&nx_pool_0, &packet_ptr, NX_TCP_PACKET, NX_WAIT_FOREVER);
            nx_packet_data_append(packet_ptr, (VOID*)"Hello from STM32N6!", 19, &nx_pool_0, NX_WAIT_FOREVER);
            
            nx_tcp_socket_send(&my_socket, packet_ptr, NX_WAIT_FOREVER);

            /* 6. Disconnect gracefully */
            nx_tcp_socket_disconnect(&my_socket, NX_WAIT_FOREVER);
        }

        /* 7. Unbind and Delete to free up resources before the next loop iteration */
        nx_tcp_client_socket_unbind(&my_socket);
        nx_tcp_socket_delete(&my_socket);

        // Wait 5 seconds before trying again
        tx_thread_sleep(500);
    }
}