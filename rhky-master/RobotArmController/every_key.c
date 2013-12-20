/*****************************************************
 * 程序名：every_key.c
 * 功能：测试S1接口的舵机转动，每按一次键转动10个脉冲
 * 日期：2013.01.19
 * 作者：任广辉
 *****************************************************/
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <math.h>

int get_time(const char *s);
int get_pwm(const char*s);

int main(void)
{
	int x, n, fd_serial;
	//x：发送字符串的元素下标；fd_serial：USB转串口设备的文件描述符

	const char *arr_s1_up[] = {"#0P2080T1000\r\n", "#0P2060T1000\r\n",  "#0P2040T1000\r\n", "#0P2020T1000\r\n", "#0P2000T1000\r\n", "#0P1980T1000\r\n", "#0P1960T1000\r\n", "#0P1940T1000\r\n", "#0P1920T1000\r\n", "#0P1900T1000\r\n", "#0P1880T1000\r\n", "#0P1860T1000\r\n", "#0P1840T1000\r\n", "#0P1820T1000\r\n", "#0P1800T1000\r\n", "#0P1780T1000\r\n", "#0P1760T1000\r\n", "#0P1740T1000\r\n", "#0P1720T1000\r\n", "#0P1700T1000\r\n", "#0P1680T1000\r\n", "#0P1660T1000\r\n", "#0P1640T1000\r\n", "#0P1620T1000\r\n", "#0P1600T1000\r\n", "#0P1580T1000\r\n", "#0P1560T1000\r\n", "#0P1540T1000\r\n", "#0P1520T1000\r\n", "#0P1500T1000\r\n", "#0P1480T1000\r\n", "#0P1460T1000\r\n", "#0P1440T1000\r\n", "#0P1420T1000\r\n", "#0P1400T1000\r\n", "#0P1380T1000\r\n", "#0P1360T1000\r\n", "#0P1340T1000\r\n", "#0P1320T1000\r\n", "#0P1300T1000\r\n", "#0P1280T1000\r\n", "#0P1260T1000\r\n", "#0P1240T1000\r\n", "#0P1220T1000\r\n", "#0P1200T1000\r\n", "#0P1180T1000\r\n", "#0P1160T1000\r\n", "#0P1140T1000\r\n", "#0P1120T1000\r\n", "#0P1100T1000\r\n", "#0P1080T1000\r\n", "#0P1060T1000\r\n", "#0P1040T1000\r\n", "#0P1020T1000\r\n", "#0P1000T1000\r\n", "#0P980T1000\r\n", "#0P960T1000\r\n", "#0P940T1000\r\n", "#0P920T1000\r\n"};
	//舵机命令字符串，PWM脉冲，用时1000ms

	printf ("\n");
	printf ("****************************************************\n");
	printf ("*           The  Robot  Test  Program              *\n");
	printf ("****************************************************\n");

	//打开USB转串口设备文件
	fd_serial = open ("/dev/ttyUSB0", O_WRONLY);
	if (-1 == fd_serial){
		printf ("Open ttyUSB0 error\n");
		exit (1);
	}

	//打开继电器控制板设备文件
/*	fd_relay = open ("/dev/ttyUSB1", O_WRONLY);
	if(-1 == fd_serial){
		printf("Open ttyUSB1 error\n");
		close(fd_serial);
		exit(1);
	}
*/
	printf("Wait 2 seconds...\n");
	//等待串口信号稳定
	sleep(2);
	for(x = 0; x < sizeof(arr_s1_up) / sizeof(char*); x++){	//每次发送一条指令，每条指令脉冲递减20
		printf("\nStart:\n\t1. Send Common\n\n\t2. Exit\nChoose: ");
		scanf("%d", &n);
		if(n == 1){	//输入数字1，执行写入，然后跳入下一次循环
			if((write(fd_serial, arr_s1_up[x], strlen(arr_s1_up[x]))) == -1){
				printf("Write arr_s1_up[%d]:\"%s\" error.\n", x, arr_s1_up[x]);
				close(fd_serial);
				exit(1);
			}
			usleep(get_time(arr_s1_up[x]));	//延时时间为该条指令完成所需时间
			get_pwm(arr_s1_up[x]);		//打印目前转到的脉冲位置
		}
		else if(n == 2){
			close(fd_serial);
			return 1;
		}
	}
	close(fd_serial);	//关闭文件设备
//	close(fd_relay);

	return 0;
}

//计算每条指令中设置的时间，打印时间，单位为ms，返回时间，单位为us
int get_time(const char *s)
{
        int i, j, p, m = 0;
        double n = 0.0, us_time = 0.0;
        int set_time[5] = {-1, -1, -1, -1, -1};

        for (i = 0; i < (strlen(s)); i++){
                if (s[i] == 'T'){	//查找字符‘T’
			//计算‘T’后的数字，减48代表将字符类型的时间值转换为整型的时间值
                        for (j = 0; (((int)s[i + j + 1] - 48) >= 0) && (((int)s[i + j + 1] - 48) <= 9); j++){
                                set_time[j] = ((int)s[i + j + 1] - 48);
                                printf ("%d", set_time[j]);	//打印每一位的整数
                                m++;
                        }
                printf ("ms\n");
                }
        }

	//将多个个位数整合为一个整数
        p = m;
        for (i = 0; i < m; i++){
                us_time += (set_time[p - 1]) * pow(10.0, n + 3);
                n++;
                p--;
        }

        return (int)us_time;
}

//得出每条指令字符串中的PWM值并打印出来
int get_pwm(const char *s)
{
        int i, j, p, m = 0;
        double n = 0.0, us_time = 0.0;
        int set_time[5] = {-1, -1, -1, -1, -1};

        for (i = 0; i < (strlen(s)); i++){
                if (s[i] == 'P'){	//查找字符‘P’
			//计算‘P’后的数字，减48代表将字符类型值转换为整型值
                        for (j = 0; (((int)s[i + j + 1] - 48) >= 0) && (((int)s[i + j + 1] - 48) <= 9); j++){
                                set_time[j] = ((int)s[i + j + 1] - 48);
                                printf ("%d", set_time[j]);	//打印每一位的整数
                                m++;
                        }
                printf ("pwm\n");	//打印计算出的PWM
                }
        }

	//将多个个位数整合为一个整数
        p = m;
        for (i = 0; i < m; i++){
                us_time += (set_time[p - 1]) * pow(10.0, n);
                n++;
                p--;
        }

	return (int)us_time;
}
