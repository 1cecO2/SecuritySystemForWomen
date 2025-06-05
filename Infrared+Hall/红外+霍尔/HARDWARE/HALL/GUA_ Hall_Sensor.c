//******************************************************************************              
//name:             GUA_Hall_Sensor.c             
//introduce:        霍尔传感器驱动      
                  
//******************************************************************************
#include "stm32f10x.h" 
#include "GUA_Hall_Sensor.h"
#include "oled.h"
 
/*********************宏定义************************/
//霍尔传感器引脚
#define GUA_HALL_SENSOR_PORT               GPIOA
#define GUA_HALL_SENSOR_PIN                GPIO_Pin_11
 
//消抖总次数
#define GUA_HALL_SENSOR_DISAPPERAS_SHAKS_COUNT     50
/*********************内部变量************************/
static GUA_U32 sGUA_Hall_Sensor_DisapperasShakes_IdleCount = 0;			//消抖时的空闲状态计数值
static GUA_U32 sGUA_Hall_Sensor_DisapperasShakes_TriggerCount = 0;	//消抖时的触发状态计数值
 
/*********************内部函数************************/ 
static void GUA_Hall_Sensor_IO_Init(void);
 
//******************************************************************************            
//name:             GUA_Hall_Sensor_IO_Init           
//introduce:        霍尔传感器的IO初始化         
//parameter:        none                 
//return:           none         
//author:           甜甜的大香瓜                 
//email:            897503845@qq.com     
//QQ group          香瓜单片机之STM8/STM32(164311667)                  
//changetime:       2017.03.06                     
//******************************************************************************
static void GUA_Hall_Sensor_IO_Init(void)
{	
	//IO结构体
	GPIO_InitTypeDef GPIO_InitStructure;
		
	//时钟使能
	RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOA, ENABLE); 	
	
	//霍尔IO配置
	GPIO_InitStructure.GPIO_Pin = GUA_HALL_SENSOR_PIN;  		
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IPU; 
	GPIO_Init(GUA_HALL_SENSOR_PORT, &GPIO_InitStructure);	
}
 
//******************************************************************************        
//name:             GUA_Hall_Sensor_Check_Pin        
//introduce:        霍尔传感器检测触发状态     
//parameter:        none       
//return:           GUA_HALL_SENSOR_STATUS_IDLE or GUA_HALL_SENSOR_STATUS_TRIGGER     
//author:           甜甜的大香瓜             
//email:            897503845@qq.com     
//QQ group          香瓜单片机之STM8/STM32(164311667)                  
//changetime:       2017.03.06                     
//******************************************************************************  

GUA_U8 GUA_Hall_Sensor_Check_Pin(void)    
{    
  // 没触发
  if(GPIO_ReadInputDataBit(GUA_HALL_SENSOR_PORT, GUA_HALL_SENSOR_PIN) == SET) 
  {
    // 计数
    sGUA_Hall_Sensor_DisapperasShakes_IdleCount++;
    sGUA_Hall_Sensor_DisapperasShakes_TriggerCount = 0;
    
    // 判断计数是否足够
    if(sGUA_Hall_Sensor_DisapperasShakes_IdleCount >= GUA_HALL_SENSOR_DISAPPERAS_SHAKS_COUNT)
    {
      OLED_ShowNum(3, 1, sGUA_Hall_Sensor_DisapperasShakes_IdleCount, 6);  // 显示空闲计数
      OLED_ShowNum(4, 1, sGUA_Hall_Sensor_DisapperasShakes_TriggerCount, 6);  // 显示触发计数

      return GUA_HALL_SENSOR_STATUS_IDLE;    
    }
  }
  // 触发
  else
  {
    // 计数
    sGUA_Hall_Sensor_DisapperasShakes_IdleCount = 0;
    sGUA_Hall_Sensor_DisapperasShakes_TriggerCount++;
    
    // 判断计数是否足够
    if(sGUA_Hall_Sensor_DisapperasShakes_TriggerCount >= GUA_HALL_SENSOR_DISAPPERAS_SHAKS_COUNT)
    {
      OLED_ShowNum(3, 1, sGUA_Hall_Sensor_DisapperasShakes_IdleCount, 6);  // 显示空闲计数
      OLED_ShowNum(4, 1, sGUA_Hall_Sensor_DisapperasShakes_TriggerCount, 6);  // 显示触发计数
;
      return GUA_HALL_SENSOR_STATUS_TRIGGER;    
    }    
  }  
  
  // 显示消抖中状态
  OLED_ShowNum(3, 1, sGUA_Hall_Sensor_DisapperasShakes_IdleCount, 6);  // 显示空闲计数
  OLED_ShowNum(4, 1, sGUA_Hall_Sensor_DisapperasShakes_TriggerCount, 6);  // 显示触发计数

  
  return GUA_HALL_SENSOR_STATUS_DISAPPERAS_SHAKS;  
}
 
//******************************************************************************        
//name:             GUA_Limit_Switch_Init        
//introduce:        霍尔传感器初始化     
//parameter:        none       
//return:           none      `
//author:           甜甜的大香瓜             
//email:            897503845@qq.com     
//QQ group          香瓜单片机之STM8/STM32(164311667)                  
//changetime:       2017.03.06                     
//****************************************************************************** 
void GUA_Hall_Sensor_Init(void)
{
  //初始化IO
  GUA_Hall_Sensor_IO_Init();	  
}
