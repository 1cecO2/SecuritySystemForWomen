#ifndef _USART_H_
#define _USART_H_

#include "main.h"

void Usart3_Init(unsigned int baud);

void Usart_SendString(USART_TypeDef *USARTx, unsigned char *str, unsigned short len);

void UsartPrintf(USART_TypeDef *USARTx, char *fmt,...);
void Usart1_Init(unsigned int baud);

#endif
