#ifndef __TIME_H
#define __TIME_H

#include "stm32f10x.h"
#include "stdio.h"
#include "stdint.h"

struct TT2{
	_Bool interrupt_10s;
	_Bool	interrupt_100ms;
	int32_t counttime;
};

void TIM3_Int_Init(u16 arr,u16 psc);

void TIM2_Int_Init(u16 arr,u16 psc);

#endif


