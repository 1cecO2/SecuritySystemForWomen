#include "main.h"
#include "DHT11.h"


float smog=0,ch4=0,flame=0;
extern struct TT2 tt2;
uint8_t temp,humi;


// ͨ��WiFiģ�鷢�ͱ�����Ϣ
void send_alarm_info(const char* message) {
    Usart_SendString(USART1, (unsigned char *)message, strlen(message));
    Usart_SendString(USART1, (unsigned char *)"\r\n", 2); // ���з���������Զ˽���������Ϣ
	delay_ms (10);
}

// ͨ��WiFiģ�鷢��ʵʱ����
void send_info(const char* message) {
    Usart_SendString(USART1, (unsigned char *)message, strlen(message));
    Usart_SendString(USART1, (unsigned char *)"\r\n", 2); // ���з���������Զ˽���������Ϣ
	delay_ms (10);
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


	
		OLED_ShowString(4,1,"Start");
		//�����ʼ��

		Adc_Init();		//ģ���źŲɼ�������ch4�����桢��������ʼ��
		Beef_Init();
		ESP8266_Init();

//		// ����͸������״̬
		while(ESP8266_SendCmd("AT+CIPSEND\r\n", ">"))
		OLED_ShowString(4,6,"Start");
    while (1)
    {
		

		DHT11_Read_Data(&temp,&humi);
        OLED_ShowNum(1, 6, temp, 2);            // ��ʾ�¶�
        OLED_ShowNum(2, 6, humi, 2);            // ��ʾʪ��
		//��ʾ�¶�����
		OLED_ShowNum(1,6,temp,2);
		//��ʾʪ������
		OLED_ShowNum(2,6,humi,2);
		
		float smog = MQ2_GetData_PPM(); // ��ȡŨ��ֵ
        // ��ӡ��������ڻ� OLED��
//        UsartPrintf(USART3, "MQ2 Concentration: %.2f ppm\r\n", smog);
       	float ch4 = MQ4_GetPPM(); // ��ȡŨ��ֵ
        // ��ӡ��������ڻ� OLED��
//        UsartPrintf(USART3, "MQ4 Concentration: %.2f ch4\r\n", ch4);
        float flame = KY026_GetValue(); // 
		OLED_ShowNum(3,6,flame,4);
        // ��ӡ��������ڻ� OLED��
 //       UsartPrintf(USART3, "KY026 Concentration: %.2f flame\r\n", flame);

//		
		char data[300];
		sprintf(data, "smog:%.2f\r\nch4:%.2f\r\nflame:%d\r\ntemp:%d\r\nhumi:%d\r\n",
				smog, ch4, (int)flame, (int)temp, (int)humi);
		// ͨ��ESP8266�������ݣ�͸��ģʽ��
        send_info(data);
		        // 4. OLED��ʾ����ѡ����Ӱ��ʵʱ�ԣ�����ʾ�������Ż���

        OLED_ShowNum(3, 6, (int)flame, 4);      // ��ʾ����ֵ��ת��Ϊint���⸡����ʾ���⣩
		delay_ms(5000);
    }	
}
