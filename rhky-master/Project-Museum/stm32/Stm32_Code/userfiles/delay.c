/**
  *****************************************************************************
  * @title   Delay.c
  * @author  sunw@flamingoeda.com
  * @date    31 Oct 2013
  * @brief   This function is delay function useing SysTick clock
  *
  *
  *******************************************************************************
  */

#include <stdio.h>
#include <stm32f10x.h>

u32 Sys_time = 0; 						// define Sys_time  1us ++


/*******************************************************************************
* Function Name  : SysTick_INIT
* Description    : Configures the used IRQ Channels and sets their priority.
* Input          : None
* Output         : None
* Return         : None
*******************************************************************************/


void SysTick_INIT (void)
{

    /* Setup SysTick Timer for 1 us interrupts  */
    if (SysTick_Config(SystemCoreClock / 1000000))
    {
        /* Capture error */
        while (1);
    }

    /* Configure the SysTick handler priority */
    NVIC_SetPriority(SysTick_IRQn, 0x0);

}

/*******************************************************************************
* Function Name  : delay_us
* Description    : delay function   unit  us
* Input          : u32
* Output         : None
* Return         : None
*******************************************************************************/

void delay_us (u32 delay_us)
{
    Sys_time = 0;
    do
    {

    }
    while (Sys_time < delay_us);
}

/*******************************************************************************
* Function Name  : delay_ms
* Description    : delay function   unit  ms
* Input          : u32
* Output         : None
* Return         : None
*******************************************************************************/


void delay (u32 delay_ms)
{
    u32 i;
    for (i = 0; i < delay_ms; i++)
    {
        delay_us(1000);
    }



}

/*******************************************************************************
* Function Name  : SysTick_Handler
* Description    : SysTick Clock interrupt   per interrupt Sys_time +1
* Input          : None
* Output         : None
* Return         : None
*******************************************************************************/

void SysTick_Handler (void)
{
    Sys_time++;
}
