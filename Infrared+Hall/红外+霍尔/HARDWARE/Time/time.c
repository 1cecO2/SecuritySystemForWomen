#include "main.h"

uint16_t times = 0;

struct TT2 tt2={0};


void TIM2_Int_Init(u16 arr,u16 psc)
{
	 TIM_TimeBaseInitTypeDef TIM_TimeBaseInitSture;
	 NVIC_InitTypeDef NVIC_InitStructure;
 
	 
   RCC_APB1PeriphClockCmd(RCC_APB1Periph_TIM2,ENABLE);
	
	 TIM_TimeBaseInitSture.TIM_Period = arr-1;//�Զ�װ��ֵ
	 TIM_TimeBaseInitSture.TIM_Prescaler = psc-1;//Ԥ��Ƶϵ��
	 TIM_TimeBaseInitSture.TIM_CounterMode = TIM_CounterMode_Up;//���ϼ���
	 TIM_TimeBaseInitSture.TIM_ClockDivision = TIM_CKD_DIV1;
	
	 TIM_TimeBaseInit(TIM2,&TIM_TimeBaseInitSture);
 
   TIM_ITConfig(TIM2,TIM_IT_Update,ENABLE);
 
	 NVIC_InitStructure.NVIC_IRQChannel = TIM2_IRQn;
	 NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0;  //��ռ���ȼ�0��
	 NVIC_InitStructure.NVIC_IRQChannelSubPriority = 3;  //�����ȼ�3��
	 NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE; //IRQͨ����ʹ��
	 NVIC_Init(&NVIC_InitStructure);  //����NVIC_InitStruct��ָ���Ĳ�����ʼ������NVIC�Ĵ���

   TIM_Cmd(TIM2,ENABLE);
}

void TIM2_IRQHandler(void)//�жϺ�����0.1���ж�һ��
{
	if(TIM_GetITStatus(TIM2,TIM_IT_Update) != RESET)//�ж�״̬
	 { 
		 tt2.counttime++;
		 tt2.interrupt_100ms=1;
		 TIM_ClearITPendingBit(TIM2,TIM_IT_Update);//����жϴ�����λ
		 if(tt2.counttime>100) {tt2.interrupt_10s=1;tt2.counttime=0;}
	 }
}


void TIM3_Int_Init(u16 arr,u16 psc)
{
	 TIM_TimeBaseInitTypeDef TIM_TimeBaseInitSture;
	 NVIC_InitTypeDef NVIC_InitStructure;
 
	 
   RCC_APB1PeriphClockCmd(RCC_APB1Periph_TIM3,ENABLE);//��ʱ��3��ʱ��
	
	 TIM_TimeBaseInitSture.TIM_Period = arr-1;//�Զ�װ��ֵ
	 TIM_TimeBaseInitSture.TIM_Prescaler = psc-1;//Ԥ��Ƶϵ��
	 TIM_TimeBaseInitSture.TIM_CounterMode = TIM_CounterMode_Up;//���ϼ���
	 TIM_TimeBaseInitSture.TIM_ClockDivision = TIM_CKD_DIV1;
	
	 TIM_TimeBaseInit(TIM3,&TIM_TimeBaseInitSture);//��ʱ��3
 
   TIM_ITConfig(TIM3,TIM_IT_Update,ENABLE);//��ʱ��3�������жϣ�ʹ��
 
	 NVIC_InitStructure.NVIC_IRQChannel = TIM3_IRQn;  //TIM3�ж�
	 NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0;  //��ռ���ȼ�0��
	 NVIC_InitStructure.NVIC_IRQChannelSubPriority = 3;  //�����ȼ�3��
	 NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE; //IRQͨ����ʹ��
	 NVIC_Init(&NVIC_InitStructure);  //����NVIC_InitStruct��ָ���Ĳ�����ʼ������NVIC�Ĵ���

   TIM_Cmd(TIM3,ENABLE);
}
 
void TIM3_IRQHandler(void)//�жϺ�����0.5���ж�һ��
{
	if(TIM_GetITStatus(TIM3,TIM_IT_Update) != RESET)//�ж�״̬
	 { 
		 TIM_ClearITPendingBit(TIM3,TIM_IT_Update);//����жϴ�����λ
		 times++;
		 if (times > 10) times = 10; // �������ֵ�����������
	 }
}

