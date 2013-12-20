/**
  *****************************************************************************
  * @title   main.c
  * @author  sunw@flamingoeda.com
  * @date    18 Nov 2013
  * @brief   STM32 control StepDirver LED CARD.   use Uart send command
  * @CMD data fomat
  * |object,+value;|
  * object  G  galss dirver pwm out put.
  * 		M  MOSFET contrl
  * 		S  Stepdirver
  * +       use Contrl stepdirver DIR.
  * value   for value=0 to 255;
  *
  *******************************************************************************  */


#include <stdio.h>
#include <stm32f10x.h>
#include "stm32f10x_gpio.h"
#include "stm32f10x_rcc.h"




/******************************************************************************
*function declaration
********************************************************************************/

//System time clock function  Use for dealy function

void SysTick_INIT (void);


//step function
void StepDirverSetup(void);
void rotateDeg(float);

//MOSFET out put setup
void MOS_Out (u8, uint16_t);
void delay (u32 delay_ms);

//card out put
void CARD_Out (u8 num, uint16_t CCR_X);

// PWM Configuration
void RCC_Configuration(void);
void TIM_PWMConfig(void);

//USART function
void usart_init(void);


/******************************************************************************
END function declaration
********************************************************************************/



/******************************************************************************
 * External variable declarations
 * USART_CMDFLAG  USART CMD flag      receive a CMD from USART3  FLAG=1   else FLAG=0
 * CMD_DATABUFF  CMD BUFF  This array save CMD data.
******************************************************************************/

extern u8 USART_CMDFLAG;

extern char CMD_DATABUFF[11];

/******************************************************************************
 * End external variable declarations
********************************************************************************/

int main(void)
{


    /*System time clock configuartion*/
    SysTick_INIT ();
    /*Configuration StepDirver GPIO*/
    StepDirverSetup();
    /*PWM out put Configuartion for GPIO A  and GPIO C  use time2 time3*/
    RCC_Configuration();
    TIM_PWMConfig();
    /*USART Configuartion   After initial success output " Hello Word !\r\n" if */
    usart_init();



    int CMD_VAL;    // save CMD numeric part
    CMD_VAL=255;



//test LED config

    MOS_Out(1, CMD_VAL);
  	MOS_Out(2, CMD_VAL);
    MOS_Out(3, CMD_VAL);


    while(1)
    {
        if (USART_CMDFLAG == 1)      // wait for FLAG =1
        {
            USART_CMDFLAG = 0;       // Reset FLAG
            CMD_VAL = (CMD_DATABUFF[3] - 48) * 100 + (CMD_DATABUFF[4] - 48) * 10 + (CMD_DATABUFF[5] - 48);
            if (CMD_DATABUFF[0] == 'S')
                if (CMD_DATABUFF[1] == ',')
                {
                    if (CMD_DATABUFF[2] == '+')
                    {
                        rotateDeg(CMD_VAL);
                    }
                    if (CMD_DATABUFF[2] == '-')
                    {
                        rotateDeg(0 - CMD_VAL);
                    }
                }

            if (CMD_DATABUFF[0] == 'M'){					// if OBJ = M  set LED brightness
            	MOS_Out(1, CMD_VAL);
            	MOS_Out(2, CMD_VAL);
            	MOS_Out(3, CMD_VAL);

            }

            if (CMD_DATABUFF[0] == 'G')						// if OBJ = G  set Glass Transparency
                CARD_Out(1, CMD_VAL);
        }
    }
}
