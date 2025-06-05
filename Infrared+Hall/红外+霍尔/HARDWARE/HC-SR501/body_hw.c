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

/*****************单片机设计******************
											STM32
 * 文件			:	人体红外传感器c文件                   
 * 版本			: V1.0
 * 日期			: 2024.8.26
 * MCU			:	STM32F103C8T6
 * 接口			:	见代码							


**********************BEGIN***********************/
u16 pre_value = 0;
u8 pass_count = 0;  // 经过次数统计
u32 stay_time = 0;  // 停留时间计时


void BODY_HW_Init(void)
{
	GPIO_InitTypeDef GPIO_InitStructure;
		
	RCC_APB2PeriphClockCmd (BODY_HW_GPIO_CLK, ENABLE );	// 打开连接 传感器DO的单片机引脚端口时钟
	GPIO_InitStructure.GPIO_Pin = BODY_HW_GPIO_PIN;			// 配置连接 传感器DO的单片机引脚模式
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IPD;			// 设置为下拉输入
		
	GPIO_Init(BODY_HW_GPIO_PORT, &GPIO_InitStructure);				// 初始化 
	
}



uint16_t BODY_HW_GetData(void)
{
	uint16_t tempData;
	tempData = GPIO_ReadInputDataBit(BODY_HW_GPIO_PORT, BODY_HW_GPIO_PIN);
	return tempData;
}

// 报警函数
void alarm() {
    GPIO_ResetBits(GPIOB ,GPIO_Pin_13);
    delay_ms(5000);
    GPIO_SetBits(GPIOB ,GPIO_Pin_13);
    pass_count = 0;  // 报警后重置经过次数
    stay_time = 0;   // 报警后重置停留时间
}
// 通过WiFi模块发送报警信息
void send_alarm_info(const char* message) {
    Usart_SendString(USART1, (unsigned char *)message, strlen(message));
    Usart_SendString(USART1, (unsigned char *)"\r\n", 2); // 换行符，方便电脑端接收区分信息
	delay_ms (500);
}

void BODY_HW_TEST(void )
{


        // 显示经过次数
//        OLED_ShowString(3,1,"Pass count:");
        OLED_ShowNum(1,4,pass_count,1);

        // 显示停留时间
//        OLED_ShowString(5,1,"Stay time:");
        OLED_ShowNum(2,4,stay_time / 1000, 1); // 转换为秒

        if (BODY_HW_GetData() != pre_value)
        {
            if (BODY_HW_GetData() == 1)
            {
                // 从无人到有人，经过次数加 1
                pass_count++;
                stay_time = 0;  // 重新开始计时
  //              OLED_ShowString(2,1,"yes");
            }
            else
            {
                // 从有人到无人，停止计时
                stay_time = 0;
 //               OLED_ShowString(2,1,"NO ");
            }
        }

        if (BODY_HW_GetData() == 1)
        {
            // 有人时，停留时间累加
            stay_time += 100;  // 每次循环延时 100ms，所以累加 100
            if (stay_time >= 10000)  // 停留时间超过 10 秒
            {
                alarm();
                send_alarm_info("Stay time PASS 10s");
            }
        }

        if (pass_count >= 4)  // 经过次数超过 4 次
        {
            alarm();
            send_alarm_info("Pass count PASS 4");
        }

        pre_value = BODY_HW_GetData();
}

