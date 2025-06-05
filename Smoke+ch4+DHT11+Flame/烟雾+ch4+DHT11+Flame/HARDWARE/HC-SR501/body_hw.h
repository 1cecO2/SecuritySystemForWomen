#ifndef __BODY_HW_H
#define	__BODY_HW_H
#include "stm32f10x.h"
#include "adcx.h"
#include "delay.h"
#include "math.h"

// BODY_HW GPIO�궨��

#define		BODY_HW_GPIO_CLK								RCC_APB2Periph_GPIOA
#define 	BODY_HW_GPIO_PORT								GPIOA
#define 	BODY_HW_GPIO_PIN								GPIO_Pin_4		


void BODY_HW_Init(void);
uint16_t BODY_HW_GetData(void);

#endif /* __ADC_H */

