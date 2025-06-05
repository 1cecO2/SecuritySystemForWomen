#include "main.h"
#include "DHT11.h"
#include "GUA_Hall_Sensor.h"
#include "BODY_HW.h"
#include "esp8266.h"

extern struct TT2 tt2;
uint8_t temp,humi;
uint8_t dr_state;
 


// ͨ��WiFiģ�鷢��ʵʱ����
void send_info(const char* message) {
    Usart_SendString(USART1, (unsigned char *)message, strlen(message));
    Usart_SendString(USART1, (unsigned char *)"\r\n", 2); // ���з���������Զ˽���������Ϣ
	delay_ms (100);
}
 

uint8_t Read_Hall_Sensor_State(void)
{
    GUA_U8 nGUA_Ret = 0;
    uint8_t HallState = 0;

    // ��ʼ������������
    GUA_Hall_Sensor_Init();

    // ������������״̬
    nGUA_Ret = GUA_Hall_Sensor_Check_Pin();

    // ��������ֵ
    if (nGUA_Ret == GUA_HALL_SENSOR_STATUS_TRIGGER)
    {
        HallState = 1; // ����״̬���д����ӽ�
    }
    else if (nGUA_Ret == GUA_HALL_SENSOR_STATUS_IDLE)
    {
        HallState = 0; // ����״̬���޴����ӽ�
    }
    // �������������״̬�����Ը��������������ֵ��������
    // ����������ֱ���õ�ȷ��״̬
	

    return HallState;
}
 



int main(void)
{ 
	    

		SystemInit();//����ϵͳʱ��Ϊ72M    
		delay_init(72);

		NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2);//�趨��ѡ��
		
		Usart1_Init(115200);
		Usart3_Init(115200);
        
		OLED_Init();					//oled��ʼ��
		OLED_Clear();
	   
		DHT11_Init();		



		//�����ʼ��

		Adc_Init();		//ģ���źŲɼ�������ch4�����桢��������ʼ��
		Beef_Init();

		ESP8266_Init();		// ����͸������״̬
		while(ESP8266_SendCmd("AT+CIPSEND\r\n", ">"))
        OLED_ShowString(1,1,"T") ;       
		

    while (1)
    {
		
		dr_state=Read_Hall_Sensor_State();
        OLED_ShowNum(1, 1, dr_state, 1);
		// ���췢������
        char data[20];
        sprintf(data, "dr_state:%d\r\n", dr_state);
        // �������ݵ�������
        send_info(data);
        // ���������⴫����״̬
        BODY_HW_TEST();



    }	
}
