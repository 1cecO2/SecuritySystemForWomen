#include "main.h"

uint16_t times = 0;

struct TT2 tt2={0};


void TIM2_Int_Init(u16 arr,u16 psc)
{
	 TIM_TimeBaseInitTypeDef TIM_TimeBaseInitSture;
	 NVIC_InitTypeDef NVIC_InitStructure;
 
	 
   RCC_APB1PeriphClockCmd(RCC_APB1Periph_TIM2,ENABLE);
	
	 TIM_TimeBaseInitSture.TIM_Period = arr-1;//自动装载值
	 TIM_TimeBaseInitSture.TIM_Prescaler = psc-1;//预分频系数
	 TIM_TimeBaseInitSture.TIM_CounterMode = TIM_CounterMode_Up;//向上计数
	 TIM_TimeBaseInitSture.TIM_ClockDivision = TIM_CKD_DIV1;
	
	 TIM_TimeBaseInit(TIM2,&TIM_TimeBaseInitSture);
 
   TIM_ITConfig(TIM2,TIM_IT_Update,ENABLE);
 
	 NVIC_InitStructure.NVIC_IRQChannel = TIM2_IRQn;
	 NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0;  //先占优先级0级
	 NVIC_InitStructure.NVIC_IRQChannelSubPriority = 3;  //从优先级3级
	 NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE; //IRQ通道被使能
	 NVIC_Init(&NVIC_InitStructure);  //根据NVIC_InitStruct中指定的参数初始化外设NVIC寄存器

   TIM_Cmd(TIM2,ENABLE);
}

void TIM2_IRQHandler(void)//中断函数，0.1秒中断一次
{
	if(TIM_GetITStatus(TIM2,TIM_IT_Update) != RESET)//判断状态
	 { 
		 tt2.counttime++;
		 tt2.interrupt_100ms=1;
		 TIM_ClearITPendingBit(TIM2,TIM_IT_Update);//清除中断待处理位
		 if(tt2.counttime>100) {tt2.interrupt_10s=1;tt2.counttime=0;}
	 }
}


void TIM3_Int_Init(u16 arr,u16 psc)
{
	 TIM_TimeBaseInitTypeDef TIM_TimeBaseInitSture;
	 NVIC_InitTypeDef NVIC_InitStructure;
 
	 
   RCC_APB1PeriphClockCmd(RCC_APB1Periph_TIM3,ENABLE);//定时器3，时钟
	
	 TIM_TimeBaseInitSture.TIM_Period = arr-1;//自动装载值
	 TIM_TimeBaseInitSture.TIM_Prescaler = psc-1;//预分频系数
	 TIM_TimeBaseInitSture.TIM_CounterMode = TIM_CounterMode_Up;//向上计数
	 TIM_TimeBaseInitSture.TIM_ClockDivision = TIM_CKD_DIV1;
	
	 TIM_TimeBaseInit(TIM3,&TIM_TimeBaseInitSture);//定时器3
 
   TIM_ITConfig(TIM3,TIM_IT_Update,ENABLE);//定时器3，更新中断，使能
 
	 NVIC_InitStructure.NVIC_IRQChannel = TIM3_IRQn;  //TIM3中断
	 NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0;  //先占优先级0级
	 NVIC_InitStructure.NVIC_IRQChannelSubPriority = 3;  //从优先级3级
	 NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE; //IRQ通道被使能
	 NVIC_Init(&NVIC_InitStructure);  //根据NVIC_InitStruct中指定的参数初始化外设NVIC寄存器

   TIM_Cmd(TIM3,ENABLE);
}
 
void TIM3_IRQHandler(void)//中断函数，0.5秒中断一次
{
	if(TIM_GetITStatus(TIM3,TIM_IT_Update) != RESET)//判断状态
	 { 
		 TIM_ClearITPendingBit(TIM3,TIM_IT_Update);//清除中断待处理位
		 times++;
		 if (times > 10) times = 10; // 限制最大值（避免溢出）
	 }
}

