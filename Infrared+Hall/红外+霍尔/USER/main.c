#include "main.h"
#include "DHT11.h"
#include "GUA_Hall_Sensor.h"
#include "BODY_HW.h"
#include "esp8266.h"

extern struct TT2 tt2;
uint8_t temp,humi;
uint8_t dr_state;
 


// 通过WiFi模块发送实时数据
void send_info(const char* message) {
    Usart_SendString(USART1, (unsigned char *)message, strlen(message));
    Usart_SendString(USART1, (unsigned char *)"\r\n", 2); // 换行符，方便电脑端接收区分信息
	delay_ms (100);
}
 

uint8_t Read_Hall_Sensor_State(void)
{
    GUA_U8 nGUA_Ret = 0;
    uint8_t HallState = 0;

    // 初始化霍尔传感器
    GUA_Hall_Sensor_Init();

    // 检测霍尔传感器状态
    nGUA_Ret = GUA_Hall_Sensor_Check_Pin();

    // 解析返回值
    if (nGUA_Ret == GUA_HALL_SENSOR_STATUS_TRIGGER)
    {
        HallState = 1; // 触发状态，有磁铁接近
    }
    else if (nGUA_Ret == GUA_HALL_SENSOR_STATUS_IDLE)
    {
        HallState = 0; // 空闲状态，无磁铁接近
    }
    // 如果返回消抖中状态，可以根据需求决定返回值或继续检测
    // 例如继续检测直到得到确定状态
	

    return HallState;
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



		//外设初始化

		Adc_Init();		//模拟信号采集（烟雾、ch4、火焰、霍尔）初始化
		Beef_Init();

		ESP8266_Init();		// 进入透传发送状态
		while(ESP8266_SendCmd("AT+CIPSEND\r\n", ">"))
        OLED_ShowString(1,1,"T") ;       
		

    while (1)
    {
		
		dr_state=Read_Hall_Sensor_State();
        OLED_ShowNum(1, 1, dr_state, 1);
		// 构造发送数据
        char data[20];
        sprintf(data, "dr_state:%d\r\n", dr_state);
        // 发送数据到服务器
        send_info(data);
        // 检测人体红外传感器状态
        BODY_HW_TEST();



    }	
}
