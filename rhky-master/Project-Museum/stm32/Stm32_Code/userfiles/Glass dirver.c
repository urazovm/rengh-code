#include "stm32f10x.h"
#include "stm32f10x_tim.h"
#include "stm32f10x_rcc.h"
#include "stm32f10x_gpio.h"

/* Private typedef -----------------------------------------------------------*/
/* Private define ------------------------------------------------------------*/
/* Private macro -------------------------------------------------------------*/
/* Private variables ---------------------------------------------------------*/
u16 i;
TIM_TimeBaseInitTypeDef  TIM_TimeBaseStructure;
TIM_OCInitTypeDef  TIM_OCInitStructure;
u16 CCR_Val[8];




uint16_t PrescalerValue = 0;

/* Private function prototypes -----------------------------------------------*/
void RCC_Configuration(void);
void PWMGPIO_Configuration(void);
void MOS_Out (u8 num, uint16_t CCR_X);
void CARD_Out (u8 num, uint16_t CCR_X);

/* Private functions ---------------------------------------------------------*/

/**
  * @brief   TIM_PWMOutput, config the Channel1-Channel4 of TIM3 to generate 4 PWM
  * signals with 4 different duty cycles.
  */



/*******************************************************************************
* Function Name  : TIM_PWMConfig
* Description    : config the Channel1-Channel4 of TIM3 and TIM2 to generate 4 PWM
* 					24000000/1200=2K PWM
* Input          : None
* Output         : None
* Return         : None
*******************************************************************************/
void TIM_PWMConfig(void)
{

    /* System Clocks Configuration */
    RCC_Configuration();

    /* PWM GPIO_Configuration */
    PWMGPIO_Configuration();

    /* Compute the prescaler value */
    PrescalerValue = (uint16_t) (72000000 / 24000000) - 1;

    /* Time base configuration */
    TIM_TimeBaseStructure.TIM_Period = 1199;
    TIM_TimeBaseStructure.TIM_Prescaler = PrescalerValue;
    TIM_TimeBaseStructure.TIM_ClockDivision = 0;
    TIM_TimeBaseStructure.TIM_CounterMode = TIM_CounterMode_Up;

    TIM_TimeBaseInit(TIM3, &TIM_TimeBaseStructure);   //configuration TIM3


    /* Time base configuration */
    TIM_TimeBaseStructure.TIM_Period = 1199;
    TIM_TimeBaseStructure.TIM_Prescaler = PrescalerValue;
    TIM_TimeBaseStructure.TIM_ClockDivision = 0;
    TIM_TimeBaseStructure.TIM_CounterMode = TIM_CounterMode_Up;
    TIM_TimeBaseInit(TIM2, &TIM_TimeBaseStructure);       //configuration TIM2

}

/*******************************************************************************
* Function Name  : TIM3_PWMOutput
* Description    : TIM3 OC1 to OC4 Channels output PWM
* Input          : None
* Output         : None
* Return         : None
*******************************************************************************/

void TIM3_PWMOutput (void)
{
    /* PWM1 Mode configuration: Channel1 */
    TIM_OCInitStructure.TIM_OCMode = TIM_OCMode_PWM1;
    TIM_OCInitStructure.TIM_OutputState = TIM_OutputState_Enable;
    TIM_OCInitStructure.TIM_Pulse = CCR_Val[0];
    TIM_OCInitStructure.TIM_OCPolarity = TIM_OCPolarity_High;

    TIM_OC1Init(TIM3, &TIM_OCInitStructure);

    TIM_OC1PreloadConfig(TIM3, TIM_OCPreload_Enable);

    /* PWM1 Mode configuration: Channel2 */
    TIM_OCInitStructure.TIM_OutputState = TIM_OutputState_Enable;
    TIM_OCInitStructure.TIM_Pulse = CCR_Val[1];

    TIM_OC2Init(TIM3, &TIM_OCInitStructure);

    TIM_OC2PreloadConfig(TIM3, TIM_OCPreload_Enable);

    /* PWM1 Mode configuration: Channel3 */
    TIM_OCInitStructure.TIM_OutputState = TIM_OutputState_Enable;
    TIM_OCInitStructure.TIM_Pulse = CCR_Val[2];

    TIM_OC3Init(TIM3, &TIM_OCInitStructure);

    TIM_OC3PreloadConfig(TIM3, TIM_OCPreload_Enable);

    /* PWM1 Mode configuration: Channel4 */
    TIM_OCInitStructure.TIM_OutputState = TIM_OutputState_Enable;
    TIM_OCInitStructure.TIM_Pulse = CCR_Val[3];

    TIM_OC4Init(TIM3, &TIM_OCInitStructure);

    TIM_OC4PreloadConfig(TIM3, TIM_OCPreload_Enable);

    TIM_ARRPreloadConfig(TIM3, ENABLE);

    /* TIM3 enable counter */
    TIM_Cmd(TIM3, ENABLE);
}



/*******************************************************************************
* Function Name  : TIM2_PWMOutput
* Description    : TIM2 OC1 to OC4 Channels out put PWM
* Input          : None
* Output         : None
* Return         : None
*******************************************************************************/


void TIM2_PWMOutput (void)
{
    /* PWM1 Mode configuration: Channel1 */
    TIM_OCInitStructure.TIM_OCMode = TIM_OCMode_PWM1;
    TIM_OCInitStructure.TIM_OutputState = TIM_OutputState_Enable;
    TIM_OCInitStructure.TIM_Pulse = CCR_Val[4];
    TIM_OCInitStructure.TIM_OCPolarity = TIM_OCPolarity_High;

    TIM_OC1Init(TIM2, &TIM_OCInitStructure);

    TIM_OC1PreloadConfig(TIM2, TIM_OCPreload_Enable);

    /* PWM1 Mode configuration: Channel2 */
    TIM_OCInitStructure.TIM_OutputState = TIM_OutputState_Enable;
    TIM_OCInitStructure.TIM_Pulse = CCR_Val[5];

    TIM_OC2Init(TIM2, &TIM_OCInitStructure);

    TIM_OC2PreloadConfig(TIM2, TIM_OCPreload_Enable);

    /* PWM1 Mode configuration: Channel3 */
    TIM_OCInitStructure.TIM_OutputState = TIM_OutputState_Enable;
    TIM_OCInitStructure.TIM_Pulse = CCR_Val[6];

    TIM_OC3Init(TIM2, &TIM_OCInitStructure);

    TIM_OC3PreloadConfig(TIM2, TIM_OCPreload_Enable);

    /* PWM1 Mode configuration: Channel4 */
    TIM_OCInitStructure.TIM_OutputState = TIM_OutputState_Enable;
    TIM_OCInitStructure.TIM_Pulse = CCR_Val[7];

    TIM_OC4Init(TIM2, &TIM_OCInitStructure);

    TIM_OC4PreloadConfig(TIM2, TIM_OCPreload_Enable);

    TIM_ARRPreloadConfig(TIM2, ENABLE);

    /* TIM2 enable counter */
    TIM_Cmd(TIM2, ENABLE);
}






/*******************************************************************************
* Function Name  : RCC_Configuration
* Description    : Configures the system clocks.
* Input          : None
* Output         : None
* Return         : None
*******************************************************************************/


void RCC_Configuration(void)
{
    /* TIM3 clock enable TIM2 clock*/
    RCC_APB1PeriphClockCmd(	RCC_APB1Periph_TIM2 | RCC_APB1Periph_TIM3 |
    						RCC_APB1Periph_TIM4, ENABLE);

    /* GPIOA and GPIOB clock enable */
    RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOA | RCC_APB2Periph_GPIOB |
                           RCC_APB2Periph_GPIOC | RCC_APB2Periph_AFIO, ENABLE);
}


/*******************************************************************************
* Function Name  : MOS_Out
* Description    : MOSFET out put.
* Input          : None
* Output         : None
* Return         : None
*******************************************************************************/


void MOS_Out (u8 num, uint16_t CCR_X)
{
    if (num < 4 && num >= 0) CCR_Val[num - 1] = ((u16)((float)(CCR_X)) * 4.69);
    if (CCR_Val[num - 1] >= 1200) CCR_Val[num - 1] = 1200;
    if (CCR_Val[num - 1] < 1) CCR_Val[num - 1] = 0;
    TIM3_PWMOutput ();
}

/*******************************************************************************
* Function Name  : CARD_Out
* Description    : Glass out put.
* Input          : None
* Output         : None
* Return         : None
*******************************************************************************/


void CARD_Out (u8 num, uint16_t CCR_X)
{
    if (num < 4 && num >= 0) CCR_Val[num + 3] = ((u16)((float)(CCR_X)) * 4.69);
    if (CCR_Val[num + 3] >= 1200) CCR_Val[num + 3] = 1200;
    if (CCR_Val[num + 3] < 1) CCR_Val[num + 3] = 0;
    TIM2_PWMOutput ();
}






void PWMGPIO_Configuration(void)
{
    GPIO_InitTypeDef GPIO_InitStructure;

#ifdef STM32F10X_CL
    /*GPIOC Configuration: TIM3 channel1, 2, 3 and 4*/
    GPIO_InitStructure.GPIO_Pin =  GPIO_Pin_6 | GPIO_Pin_7 | GPIO_Pin_8 | GPIO_Pin_9;
    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF_PP;
    GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;

    GPIO_Init(GPIOC, &GPIO_InitStructure);    //GPIOC Configuration: Time3 channel 1,2,3,4

    /**define PWM out put pin GPIOA**/
    GPIO_InitStructure.GPIO_Pin =  GPIO_Pin_0 | GPIO_Pin_1 | GPIO_Pin_2 | GPIO_Pin_3;
    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF_PP;
    GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;

    GPIO_Init(GPIOA, &GPIO_InitStructure);   //GPIOA Configuration: Time2 channel 1,2,3,4


    GPIO_PinRemapConfig(GPIO_FullRemap_TIM3, ENABLE);   // TIME3 use Fullremap mode.  remap to PC6 PC7 PC8 PC9

#else
    /* GPIOA Configuration:TIM3 Channel1, 2, 3 and 4 as alternate function push-pull */
    GPIO_InitStructure.GPIO_Pin = GPIO_Pin_6 | GPIO_Pin_7;
    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF_PP;
    GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;

    GPIO_Init(GPIOA, &GPIO_InitStructure);

    GPIO_InitStructure.GPIO_Pin = GPIO_Pin_0 | GPIO_Pin_1;
    GPIO_Init(GPIOB, &GPIO_InitStructure);
#endif
}

#ifdef  USE_FULL_ASSERT

/**
  * @brief  Reports the name of the source file and the source line number
  *         where the assert_param error has occurred.
  * @param  file: pointer to the source file name
  * @param  line: assert_param error line source number
  * @retval None
  */
void assert_failed(uint8_t *file, uint32_t line)
{
    /* User can add his own implementation to report the file name and line number,
       ex: printf("Wrong parameters value: file %s on line %d\r\n", file, line) */

    while (1)
    {

    }
}
#endif
