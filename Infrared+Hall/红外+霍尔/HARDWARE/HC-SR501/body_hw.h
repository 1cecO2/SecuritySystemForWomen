#ifndef __BODY_HW_H
#define	__BODY_HW_H
#include "stm32f10x.h"
#include "adcx.h"
#include "delay.h"
#include "math.h"

/*****************单片机设计******************
											STM32
 * 文件			:	HC-SR501人体红外传感器h文件                   
 * 版本			: V1.0
 * 日期			: 2024.8.26
 * MCU			:	STM32F103C8T6
 * 接口			:	见代码							


**********************BEGIN***********************/


/***************根据自己需求更改****************/
// BODY_HW GPIO宏定义

#define		BODY_HW_GPIO_CLK								RCC_APB2Periph_GPIOA
#define 	BODY_HW_GPIO_PORT								GPIOA
#define 	BODY_HW_GPIO_PIN								GPIO_Pin_4			

/*********************END**********************/


void BODY_HW_Init(void);
uint16_t BODY_HW_GetData(void);
void BODY_HW_TEST(void );

#endif /* __ADC_H */

