/*********************************************
 * 程序名：attention.c
 * 功能：使机器人摆出立正姿势
 * 日期：2013.01.20
 * 作者：任广辉
 *********************************************/
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <math.h>

int get_time(const char *s);

int main(void)
{
	int x, n, fd_serial;
	//x：发送字符串的元素下标；n：发送字符串的次数；fd_serial：USB转串口设备的文件描述符

	const char *arr_attention[] = {"#0P1220#2P1500#1P1550T3000\r\n", "#5P1580#4P1300#3P2000T3000\r\n"};
	//舵机命令字符串，2200PWM脉冲，用时1000ms

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

	//一直运行，直到选择退出
	while(1){
		//打印选择菜单
		printf("\nMenu:\n\t1. Attention\n\n\t0. Exit\nChosse:");
		//选择
		scanf("%d", &n);
		//判断选择的菜单
		switch(n){
		case 0:	//退出程序
			close(fd_serial);	//关闭设备文件
			exit(0);		//退出
			break;
		case 1:	//发送Attention指令
			for(x = 0; x < sizeof(arr_attention) / sizeof(char*); x++){	//每次发送一条指令
				if((write(fd_serial, arr_attention[x], strlen(arr_attention[x]))) == -1){
					printf("Write arr_attention[%d]:\"%s\" error.\n", x, arr_attention[x]);
					close(fd_serial);
					exit(1);
				}
				usleep(get_time(arr_attention[x]));	//延时时间为该条指令完成所需时间
			}
			printf("OK.\n");
			break;
		default:	//选择不正确，跳出switch，重新选择
			printf("Choose error.\n");
			break;
		}

	}
	close(fd_serial);	//关闭文件设备
//	close(fd_relay);

	return 0;
}

//计算每条指令中设置的时间，返回时间，单位为us
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
                                m++;
                        }
                }
        }

	//将各个位的数字转换为一个整数，并将时间单位ms转换为us
        p = m;
        for (i = 0; i < m; i++){
                us_time += (set_time[p - 1]) * pow(10.0, n + 3);
                n++;
                p--;
        }
	printf("%dms\n", (int)us_time / 1000);

        return (int)us_time;
}
