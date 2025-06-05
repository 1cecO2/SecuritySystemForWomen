#include "main.h"
#include "DHT11.h"


float smog=0,ch4=0,flame=0;
extern struct TT2 tt2;
uint8_t temp,humi;


// 通过WiFi模块发送报警信息
void send_alarm_info(const char* message) {
    Usart_SendString(USART1, (unsigned char *)message, strlen(message));
    Usart_SendString(USART1, (unsigned char *)"\r\n", 2); // 换行符，方便电脑端接收区分信息
	delay_ms (10);
}

// 通过WiFi模块发送实时数据
void send_info(const char* message) {
    Usart_SendString(USART1, (unsigned char *)message, strlen(message));
    Usart_SendString(USART1, (unsigned char *)"\r\n", 2); // 换行符，方便电脑端接收区分信息
	delay_ms (10);
}

int main(void)
{ 
	    

		SystemInit();//配置系统时钟为72M    
		delay_init(72);

		NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2);//设定优选组
		
		Usart1_Init(115200);
		Usart3_Init(115200);
        
		OLED_Init();					//oled初始化
		OLED_Clear();
	   
		DHT11_Init();		


	
		OLED_ShowString(4,1,"Start");
		//外设初始化

		Adc_Init();		//模拟信号采集（烟雾、ch4、火焰、霍尔）初始化
		Beef_Init();
		ESP8266_Init();

//		// 进入透传发送状态
		while(ESP8266_SendCmd("AT+CIPSEND\r\n", ">"))
		OLED_ShowString(4,6,"Start");
    while (1)
    {
		

		DHT11_Read_Data(&temp,&humi);
        OLED_ShowNum(1, 6, temp, 2);            // 显示温度
        OLED_ShowNum(2, 6, humi, 2);            // 显示湿度
		//显示温度数据
		OLED_ShowNum(1,6,temp,2);
		//显示湿度数据
		OLED_ShowNum(2,6,humi,2);
		
		float smog = MQ2_GetData_PPM(); // 获取浓度值
        // 打印结果（串口或 OLED）
//        UsartPrintf(USART3, "MQ2 Concentration: %.2f ppm\r\n", smog);
       	float ch4 = MQ4_GetPPM(); // 获取浓度值
        // 打印结果（串口或 OLED）
//        UsartPrintf(USART3, "MQ4 Concentration: %.2f ch4\r\n", ch4);
        float flame = KY026_GetValue(); // 
		OLED_ShowNum(3,6,flame,4);
        // 打印结果（串口或 OLED）
 //       UsartPrintf(USART3, "KY026 Concentration: %.2f flame\r\n", flame);

//		
		char data[300];
		sprintf(data, "smog:%.2f\r\nch4:%.2f\r\nflame:%d\r\ntemp:%d\r\nhumi:%d\r\n",
				smog, ch4, (int)flame, (int)temp, (int)humi);
		// 通过ESP8266发送数据（透传模式）
        send_info(data);
		        // 4. OLED显示（可选，不影响实时性，若显示阻塞需优化）

        OLED_ShowNum(3, 6, (int)flame, 4);      // 显示火焰值（转换为int避免浮点显示问题）
		delay_ms(5000);
    }	
}
