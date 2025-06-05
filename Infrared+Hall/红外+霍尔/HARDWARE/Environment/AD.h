#ifndef __AD_H
#define __AD_H


#include "stm32f10x.h"
#include "stdio.h"
#include "stdint.h"

extern uint16_t AD_Value[];	

void Adc_Init(void);

float MQ2_GetPPM(void);
u16 Get_Adc_Average(u8 ch,u8 times);

float MQ4_GetPPM(void);

float KY026_GetValue(void);

float KY024_GetValue(void);

#endif
