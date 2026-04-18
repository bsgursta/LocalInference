#include <stdint.h>

extern "C" {
    #include "tx_api.h"
    #include "nx_api.h"
    #include "main.h"
    #include <stm32n657xx.h>
    #include "app_netxduo.h"

    // Prototypes for the C++ functions we want to expose to the C kernel
    void my_thread_entry(ULONG thread_input);
    void my_thread_tcp(ULONG thread_input);
}


// These must have C linkage so the kernel can call them

extern "C" void my_thread_entry(ULONG thread_input)
{
    while(1)
    {   
        HAL_GPIO_TogglePin(GPIOC, GPIO_PIN_2);
        tx_thread_sleep(1000);
    }
}


extern "C" void my_thread_tcp(ULONG thread_input)
{
  UINT ret;
  UINT count = 0;

  ULONG bytes_read;
  UCHAR data_buffer[512];

  ULONG source_ip_address;
  UINT source_port;

  NX_PACKET *server_packet;
  NX_PACKET *data_packet;

  NX_TCP_SOCKET TCPSocket;

  /* create the TCP socket */
  ret = nx_tcp_socket_create(&NetXDuoEthIpInstance, &TCPSocket, "TCP Server Socket", NX_IP_NORMAL, NX_FRAGMENT_OKAY,
                             NX_IP_TIME_TO_LIVE, WINDOW_SIZE, NX_NULL, NX_NULL);
  if (ret != NX_SUCCESS)
  {
    Error_Handler();
  }

  /* bind the client socket for the DEFAULT_PORT */
  ret =  nx_tcp_client_socket_bind(&TCPSocket, DEFAULT_PORT, NX_WAIT_FOREVER);

  if (ret != NX_SUCCESS)
  {
    Error_Handler();
  }

  /* connect to the remote server on the specified port */
  ret = nx_tcp_client_socket_connect(&TCPSocket, TCP_SERVER_ADDRESS, TCP_SERVER_PORT, NX_WAIT_FOREVER);

  if (ret != NX_SUCCESS)
  {
    Error_Handler();
  }
  
}
