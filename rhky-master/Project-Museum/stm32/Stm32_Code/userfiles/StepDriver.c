/**
  *****************************************************************************
  * @title   StepDriver.c
  * @author  sunw@flamingoeda.com
  * @date    18 Nov 2013
  * @brief   This file use GPIO to control StepDirver.
  *
  *******************************************************************************
  */
/**set StepDirver GPIO **/
#define RCC_StepDirver                              RCC_APB2Periph_GPIOD
#define GPIO_StepDirver_PORT                      	GPIOD
#define DIR_PIN 									GPIO_Pin_4
#define STEP_PIN									GPIO_Pin_8
#define EN_PIN										GPIO_Pin_0
#define RSET_PIN									GPIO_Pin_12
#define GPIO_StepDirver_ALL                        	DIR_PIN |STEP_PIN |EN_PIN |RSET_PIN


/********************************************************************************
 * 	set StepDirver speed
 * 	speed should not exceed .6 in half step and .25 in full step
 *******************************************************************************/

#define StepSpeed  									0.2


#include "stm32f10x.h"
#include "stdio.h"
#include "stm32f10x_gpio.h"
#include "stm32f10x_rcc.h"
/******************************************************************************
 * External variable declarations
 * Sys_time     system Clock time
 ******************************************************************************/

void delay (u32);
void delay_us(u32);



/*******************************************************************************
* Function Name  : StepDirverSetup
* Description    : configuration Stepdirver GPIO
* Input          : None
* Output         : None
* Return         : None
*******************************************************************************/

void StepDirverSetup(void)
{
    /* Initialize StepDirver PIN */
    GPIO_InitTypeDef  GPIO_InitStructure;
    /* Initialize LED which connected to StepDirver , Enable the Clock*/
    RCC_APB2PeriphClockCmd(RCC_StepDirver, ENABLE);
    /* Configure the StepDirver pin */
    GPIO_InitStructure.GPIO_Pin = GPIO_StepDirver_ALL;
    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_Out_PP;
    GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
    GPIO_Init(GPIO_StepDirver_PORT, &GPIO_InitStructure);

    /* Set the StepDirver REST EN  */
    GPIO_StepDirver_PORT->ODR |= RSET_PIN;
    GPIO_StepDirver_PORT->ODR &= ~EN_PIN;

}

/*******************************************************************************
* Function Name  : rotateDeg
* Description    : configuration Stepdirver rotate deg  useing Parameters Deg
* Input          : float deg
* Output         : None
* Return         : None
*******************************************************************************/

void rotateDeg(float deg)
{
    int i = 0, steps;

    /*Set the DIR pin*/
    if (deg >= 0) GPIO_StepDirver_PORT->ODR |= DIR_PIN;
    else
    {
        GPIO_StepDirver_PORT->ODR &= ~DIR_PIN;
        //if deg<0 get abs deg
        deg = -deg;
    }


    steps = (deg) * (1 / 0.25);
    float Speed_delay = (1.0 / StepSpeed) * 70;

    for (i = 0; i < steps ; i++)
    {

        GPIO_StepDirver_PORT->ODR |= STEP_PIN;
        delay_us(Speed_delay);

        GPIO_StepDirver_PORT->ODR &= ~STEP_PIN;
        delay_us(Speed_delay);
    }



}
