/*********************************************
 * 程序名：for_ten.c
 * 功能：测试S1接口的舵机转动，运行10个来回
 * 日期：2013.01.19
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

	const char *arr_s1_up[] = {"#0P2000T1000\r\n"};
	//舵机命令字符串，2200PWM脉冲，用时1000ms
	const char *arr_s2_down[] = {"#0P500T1000\r\n"};
	//舵机命令，500脉冲，用时1000ms

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
	//延时2S，等待串口信号稳定
	sleep(2);
	//循环发送n次命令，使用时注释掉while死循环和switch语句，必须使用延时程序为下一条发送的命令延时
	for(n = 0; n < 10; n++){
		for(x = 0; x < sizeof(arr_s1_up) / sizeof(char*); x++){	//每次发送一条指令
			if((write(fd_serial, arr_s1_up[x], strlen(arr_s1_up[x]))) == -1){
				printf("Write arr_s1_up[%d]:\"%s\" error.\n", x, arr_s2_down[x]);
				close(fd_serial);
				exit(1);
			}
			usleep(get_time(arr_s1_up[x]));	//延时时间为该条指令完成所需时间
		}
		for(x = 0; x < sizeof(arr_s2_down) / sizeof(char*); x++){	//每次发送一条指令
			if((write(fd_serial, arr_s2_down[x], strlen(arr_s2_down[x]))) == -1){
				printf("Write arr_s2_down[%d]:\"%s\" error.\n", x, arr_s2_down[x]);
				close(fd_serial);
				exit(1);
			}
			usleep(get_time(arr_s2_down[x]));	//延时时间为该条指令完成所需时间
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

	//将时间单位ms转换为us
        p = m;
        for (i = 0; i < m; i++){
                us_time += (set_time[p - 1]) * pow(10.0, n + 3);
                n++;
                p--;
        }
	printf("%dms\n", (int)us_time / 1000);

        return (int)us_time;
}
