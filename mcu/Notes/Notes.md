- N6 has no internal flash. uses offboard SRAM
- I swapped the boot pins to get the debugger to work.
- For some reason if something is in "free" instead of the appli I can't use it in the code.
- I had to move alot of the threadx stuff into the HAL code because it wasn't working.

Link: https://github.com/STMicroelectronics/x-cube-azrtos-h7/blob/main/Projects/NUCLEO-H723ZG/Applications/NetXDuo/Nx_TCP_Echo_Client/NetXDuo/App/app_netxduo.c