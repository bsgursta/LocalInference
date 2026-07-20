#ifndef GPIO_H
#define GPIO_H

#include <array>
#include <cstdint>
#include <stm32n657xx.h>

class GpioPin {

private:

    /* Keep track of used Pins: A to H, N to Q */
    static std::array<uint16_t,12> used_port_map;

    const GPIO_TypeDef* _port;
    const uint8_t _port_idx;
    const uint8_t _pin_idx;

public:

    GpioPin(GPIO_TypeDef* port, uint8_t port_idx, uint8_t pin_idx);
    ~GpioPin();

};

#endif