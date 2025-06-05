#include "body_hw.h"
#include "stm32f10x.h"
#include "usart.h"
#include "delay.h"
#include "oled.h"
#include "BODY_HW.h"
#include "adcx.h"
#include "Serial.h"
#include "esp8266.h"
#include <stdio.h>
#include <string.h>

/*****************��Ƭ�����******************
											STM32
 * �ļ�			:	������⴫����c�ļ�                   
 * �汾			: V1.0
 * ����			: 2024.8.26
 * MCU			:	STM32F103C8T6
 * �ӿ�			:	������							


**********************BEGIN***********************/
u16 pre_value = 0;
u8 pass_count = 0;  // ��������ͳ��
u32 stay_time = 0;  // ͣ��ʱ���ʱ


void BODY_HW_Init(void)
{
	GPIO_InitTypeDef GPIO_InitStructure;
		
	RCC_APB2PeriphClockCmd (BODY_HW_GPIO_CLK, ENABLE );	// ������ ������DO�ĵ�Ƭ�����Ŷ˿�ʱ��
	GPIO_InitStructure.GPIO_Pin = BODY_HW_GPIO_PIN;			// �������� ������DO�ĵ�Ƭ������ģʽ
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IPD;			// ����Ϊ��������
		
	GPIO_Init(BODY_HW_GPIO_PORT, &GPIO_InitStructure);				// ��ʼ�� 
	
}



uint16_t BODY_HW_GetData(void)
{
	uint16_t tempData;
	tempData = GPIO_ReadInputDataBit(BODY_HW_GPIO_PORT, BODY_HW_GPIO_PIN);
	return tempData;
}

// ��������
void alarm() {
    GPIO_ResetBits(GPIOB ,GPIO_Pin_13);
    delay_ms(5000);
    GPIO_SetBits(GPIOB ,GPIO_Pin_13);
    pass_count = 0;  // ���������þ�������
    stay_time = 0;   // ����������ͣ��ʱ��
}
// ͨ��WiFiģ�鷢�ͱ�����Ϣ
void send_alarm_info(const char* message) {
    Usart_SendString(USART1, (unsigned char *)message, strlen(message));
    Usart_SendString(USART1, (unsigned char *)"\r\n", 2); // ���з���������Զ˽���������Ϣ
	delay_ms (500);
}

void BODY_HW_TEST(void )
{


        // ��ʾ��������
//        OLED_ShowString(3,1,"Pass count:");
        OLED_ShowNum(1,4,pass_count,1);

        // ��ʾͣ��ʱ��
//        OLED_ShowString(5,1,"Stay time:");
        OLED_ShowNum(2,4,stay_time / 1000, 1); // ת��Ϊ��

        if (BODY_HW_GetData() != pre_value)
        {
            if (BODY_HW_GetData() == 1)
            {
                // �����˵����ˣ����������� 1
                pass_count++;
                stay_time = 0;  // ���¿�ʼ��ʱ
  //              OLED_ShowString(2,1,"yes");
            }
            else
            {
                // �����˵����ˣ�ֹͣ��ʱ
                stay_time = 0;
 //               OLED_ShowString(2,1,"NO ");
            }
        }

        if (BODY_HW_GetData() == 1)
        {
            // ����ʱ��ͣ��ʱ���ۼ�
            stay_time += 100;  // ÿ��ѭ����ʱ 100ms�������ۼ� 100
            if (stay_time >= 10000)  // ͣ��ʱ�䳬�� 10 ��
            {
                alarm();
                send_alarm_info("Stay time PASS 10s");
            }
        }

        if (pass_count >= 4)  // ������������ 4 ��
        {
            alarm();
            send_alarm_info("Pass count PASS 4");
        }

        pre_value = BODY_HW_GetData();
}

