//******************************************************************************              
//name:             GUA_Hall_Sensor.h             
//introduce:        霍尔传感器驱动的头文件      
                
//****************************************************************************** 
#ifndef _GUA_HALL_SENSOR_H_
#define _GUA_HALL_SENSOR_H_
 
/*********************宏定义************************/  
//类型宏
#ifndef GUA_U8        
typedef unsigned char GUA_U8;        
#endif    
 
#ifndef GUA_8        
typedef signed char GUA_8;        
#endif      
      
#ifndef GUA_U16        
typedef unsigned short GUA_U16;        
#endif 
 
#ifndef GUA_16        
typedef signed short GUA_16;        
#endif         
      
#ifndef GUA_U32        
typedef unsigned long GUA_U32;        
#endif 
 
#ifndef GUA_32        
typedef signed long GUA_32;       
#endif
 
#ifndef GUA_U64    
typedef unsigned long long GUA_U64;  
#endif
 
#ifndef GUA_64    
typedef signed long long GUA_64;  
#endif
 
//霍尔传感器的触发状态
#define GUA_HALL_SENSOR_STATUS_TRIGGER                      0		//霍尔传感器触发
#define GUA_HALL_SENSOR_STATUS_IDLE                         1		//霍尔传感器没触发
#define GUA_HALL_SENSOR_STATUS_DISAPPERAS_SHAKS             2		//霍尔传感器消抖中
 
/*********************外部函数声明************************/ 
GUA_U8 GUA_Hall_Sensor_Check_Pin(void);  
void GUA_Hall_Sensor_Init(void);
 
#endif
