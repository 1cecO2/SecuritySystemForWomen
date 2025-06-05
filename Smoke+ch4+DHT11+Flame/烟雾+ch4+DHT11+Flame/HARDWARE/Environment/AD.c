#include "main.h"


#define CAL_PPM 20  // 校准环境中PPM值
#define RL			5		// RL阻值
#define MQ2_READ_TIMES	10  //MQ-2传感器ADC循环读取次数

//static float MQ4_R0;

uint16_t AD_Value[3];					//定义用于存放AD转换结果的全局数组
extern uint16_t times;
 
void Adc_Init(void)//初始化函数
{ 
 RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOA|RCC_APB2Periph_ADC1,ENABLE);//使能时钟
 RCC_AHBPeriphClockCmd(RCC_AHBPeriph_DMA1, ENABLE);		//开启DMA1的时钟
	
 RCC_ADCCLKConfig(RCC_PCLK2_Div6);//保证不超过14M
	
 GPIO_InitTypeDef GPIO_Initstructre;   
 GPIO_Initstructre.GPIO_Mode=GPIO_Mode_AIN; 
 GPIO_Initstructre.GPIO_Pin=GPIO_Pin_0|GPIO_Pin_1|GPIO_Pin_2;
 GPIO_Initstructre.GPIO_Speed=GPIO_Speed_50MHz;
 GPIO_Init(GPIOA,&GPIO_Initstructre);

	/*规则组通道配置*/
	ADC_RegularChannelConfig(ADC1, ADC_Channel_0, 1, ADC_SampleTime_55Cycles5);	//规则组序列1的位置，配置为通道0
	ADC_RegularChannelConfig(ADC1, ADC_Channel_1, 2, ADC_SampleTime_55Cycles5);	//规则组序列2的位置，配置为通道1
	ADC_RegularChannelConfig(ADC1, ADC_Channel_2, 3, ADC_SampleTime_55Cycles5);	//规则组序列3的位置，配置为通道2

	/*ADC初始化*/
	ADC_InitTypeDef ADC_InitStructure;											//定义结构体变量
	ADC_InitStructure.ADC_Mode = ADC_Mode_Independent;							//模式，选择独立模式，即单独使用ADC1
	ADC_InitStructure.ADC_DataAlign = ADC_DataAlign_Right;						//数据对齐，选择右对齐
	ADC_InitStructure.ADC_ExternalTrigConv = ADC_ExternalTrigConv_None;			//外部触发，使用软件触发，不需要外部触发
	ADC_InitStructure.ADC_ContinuousConvMode = ENABLE;							//连续转换，使能，每转换一次规则组序列后立刻开始下一次转换
	ADC_InitStructure.ADC_ScanConvMode = ENABLE;								//扫描模式，使能，扫描规则组的序列，扫描数量由ADC_NbrOfChannel确定
	ADC_InitStructure.ADC_NbrOfChannel = 3;										//通道数，扫描规则组的前通道
	ADC_Init(ADC1, &ADC_InitStructure);											//将结构体变量交给ADC_Init，配置ADC1
		
	/*DMA初始化*/
	DMA_InitTypeDef DMA_InitStructure;											//定义结构体变量
	DMA_InitStructure.DMA_PeripheralBaseAddr = (uint32_t)&ADC1->DR;				//外设基地址，给定形参AddrA
	DMA_InitStructure.DMA_PeripheralDataSize = DMA_PeripheralDataSize_HalfWord;	//外设数据宽度，选择半字，对应16为的ADC数据寄存器
	DMA_InitStructure.DMA_PeripheralInc = DMA_PeripheralInc_Disable;			//外设地址自增，选择失能，始终以ADC数据寄存器为源
	DMA_InitStructure.DMA_MemoryBaseAddr = (uint32_t)AD_Value;					//存储器基地址，给定存放AD转换结果的全局数组AD_Value
	DMA_InitStructure.DMA_MemoryDataSize = DMA_MemoryDataSize_HalfWord;			//存储器数据宽度，选择半字，与源数据宽度对应
	DMA_InitStructure.DMA_MemoryInc = DMA_MemoryInc_Enable;						//存储器地址自增，选择使能，每次转运后，数组移到下一个位置
	DMA_InitStructure.DMA_DIR = DMA_DIR_PeripheralSRC;							//数据传输方向，选择由外设到存储器，ADC数据寄存器转到数组
	DMA_InitStructure.DMA_BufferSize = 3;										//转运的数据大小（转运次数），与ADC通道数一致
	DMA_InitStructure.DMA_Mode = DMA_Mode_Circular;								//模式，选择循环模式，与ADC的连续转换一致
	DMA_InitStructure.DMA_M2M = DMA_M2M_Disable;								//存储器到存储器，选择失能，数据由ADC外设触发转运到存储器
	DMA_InitStructure.DMA_Priority = DMA_Priority_Medium;						//优先级，选择中等
	DMA_Init(DMA1_Channel1, &DMA_InitStructure);								//将结构体变量交给DMA_Init，配置DMA1的通道1
		
	/*DMA和ADC使能*/
	DMA_Cmd(DMA1_Channel1, ENABLE);							//DMA1的通道1使能
	ADC_DMACmd(ADC1, ENABLE);								//ADC1触发DMA1的信号使能
	ADC_Cmd(ADC1, ENABLE);									//ADC1使能
	
	/*ADC校准*/
	ADC_ResetCalibration(ADC1);								//固定流程，内部有电路会自动执行校准
	while (ADC_GetResetCalibrationStatus(ADC1) == SET);
	ADC_StartCalibration(ADC1);
	while (ADC_GetCalibrationStatus(ADC1) == SET);
	
	/*ADC触发*/
	ADC_SoftwareStartConvCmd(ADC1, ENABLE);	//软件触发ADC开始工作，由于ADC处于连续转换模式，故触发一次后ADC就可以一直连续不断地工作
}

float Get_V(u8 ch)
{
	return AD_Value[ch]/4096.0*3.3;
}

/********************************************
 * 1.651428	          200               *
 * 1.437143	          300               *
 * 1.257143	          400               *
 * 1.137143	          500               *
 * 1		          		600               *
 * 0.928704	          700               *
 * 0.871296	          800               *
 * 0.816667	          900               *
 * 0.785714	          1000              *
 * 0.574393	          2000              *
 * 0.466047	          3000              *
 * 0.415581	          4000              *
 * 0.370478	          5000              *
 * 0.337031	          6000              *
 * 0.305119	          7000              *
 * 0.288169	          8000              *
 * 0.272727	          9000              *
 * 0.254795	          10000             *
 *                                      *
 * ppm = 613.9f * pow(RS/RL, -2.074f)   *
 ***************************************/

float MQ2_GetData_PPM(void)
{
	u16 tempData ;
	
	tempData=Get_Adc_Average(0,60);
	
	float Vol = (4.2f*tempData/4096.f);
	float RS = (4.2f-Vol)/(Vol*0.5f);
	float R0=6.64f;
	
	float ppm = pow(11.5428f*R0/RS, 0.6549f);
	
	return ppm;

}

//float MQ2_GetPPM(void)
//{   
//	  u16 adcx;
//	  adcx=Get_Adc_Average(0,30);//ADC1,取30次的平均值
//	
//    float Vrl = 3.3f * adcx / 4096.f;//3.3v的参考电压，4096份
//	  Vrl = ( (float)( (int)( (Vrl+0.005)*100 ) ) )/100;
//    float RS = (3.3f - Vrl) / Vrl * RL;
//	  
//    if(times<6) // 获取系统执行时间，3s前进行校准，用到了定时器
//    {
//		  MQ2_R0 = RS / pow(CAL_PPM / 613.9f, 1 / -2.074f);//校准R0
//    } 
//	  float ppm = 613.9f * pow(RS/MQ2_R0, -2.074f);

//    return  ppm;
//}

float MQ4_GetPPM(void)
{
	  u16 adcx;
	  adcx=Get_Adc_Average(1,60);//ADC1,取30次的平均值	
      float Vrl = 3.3f * adcx / 4096.f;//3.3v的参考电压，4096份
	//无天然气的环境下，实测AOUT端的电压为0.5V，当检测到天然气时，电压每升高0.1V,实际被测气体浓度增加200ppm
			float ppm = (3.3F - Vrl) / Vrl * RL;
			return ppm;
}

float KY026_GetValue(void)
{
	  u16 adcx;
	  adcx=Get_Adc_Average(2,60);//ADC1,取30次的平均值	
    float Vrl = 3.3f * adcx / 4096.f;//3.3v的参考电压，4096份

	  float Value = (Vrl - 0.5) / 0.1 * 200;
	if(Value<0) Value=0;
		return Value;
}


u16 Get_Adc_Average(u8 ch,u8 times)//两个入口参数，通道，取平均值的次数
{
 u32 temp_val=0;
 u8 t;
 for(t=0;t<times;t++)
 {
   temp_val+=AD_Value[ch];
   delay_ms(5);
 }
 return temp_val/times;
 
}
