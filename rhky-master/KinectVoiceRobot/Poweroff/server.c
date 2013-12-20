#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <errno.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <sys/socket.h>
#include <netinet/in.h>
#define MAXLINE 4096

void initAtten(void); //发送立正姿势的命令
int runnet(void);  //启动继电器，启动网络监听，随时收取poweroff命令后关闭继电器并关机
void initconfig(void);  //初始化语音程序的配置文件check.txt防止意外终止后的语音程序无法使用
void poweroff(void);  //关闭继电器
int main(void)
{
	//检查配置文件，关闭系统
	int fd;
	ssize_t rd, wd;
	off_t sk;
	char ch, ch1 = '1', ch0 = '0';

	fd = open("config.txt", O_RDWR);
	if(fd < 0){
		printf("Open config.txt error.\n");
		exit(1);
	}

	rd = read(fd, &ch, 1);
	if(rd < 0){
		printf("Read config.txt error.\n");
		exit(1);
	}
	//配置文件内容为1时继续运行网络监听程序
	if (ch == ch1){
		initconfig();
		sk = lseek(fd, 0L, SEEK_SET);
		if(sk == -1){
			printf("Lseek error.\n");
			close(fd);
			exit(1);
		}
		wd = write(fd, &ch0, 1);
		if(wd < 0){
			printf("Write error.\n");
			close(fd);
			exit(1);
		}
		close(fd);
		runnet();
		return 0;
	}
	//配置文件内容为0时，关闭继电器，执行关机命令
	else if (ch == ch0){
		poweroff();
		sk = lseek(fd, 0L, SEEK_SET);
		if(sk == -1){
			printf("Lseek error.\n");
			close(fd);
			exit(1);
		}
		wd = write(fd, &ch1, 1);
	        if(wd < 0){
        		printf("Write error.\n");
			close(fd);
        		exit(1);
        	}
		if(system("poweroff") < 0){
			printf("Exec error.\n");
			close(fd);
			exit(1);
		}
		close(fd);
		return 0;
	}

	return 0;
}

//关闭继电器
void poweroff(void)
{
	int fd, wd;

	fd = open("/dev/ttyUSB0", O_WRONLY);
	if(fd < 0){
		printf("open USB0 error\n");
		exit(1);
	}
	wd = write(fd, "L", 1);
	if(wd != 1){
		printf("write 0 error\n");
		exit(1);
	}
	close(fd);
	return;
}

//初始化语音程序配置文件，确保其表示的程序运行状态不出错
void initconfig(void)
{
    int fd, wd;
    fd = open("/root/KinectVoiceRobot/Sound/check.txt", O_WRONLY | O_CREAT);
    if(fd < 0){
        printf("sound start: start error.\n");
        exit(1);
    }
    wd = write(fd, "0", 1);
    if(wd != 1){
        printf("sound start: write error.\n");
        exit(1);
    }
    close(fd);
}

//打开继电器和网络监听服务，随时接收局域网内发送来的字符串，并进行重启系统
int runnet(void)
{
	int    listenfd, connfd;
	struct sockaddr_in servaddr;
	char    buff[MAXLINE];
	int     n;
	time_t time1 = 0, time2 = 0;
	int fd, wd, rd;
	fd = open ("/dev/ttyUSB0", O_WRONLY);
	if(fd < 0){
		printf("error:open.\n");
		close(fd);
		exit(1);
	}
	sleep(2);
	wd = write (fd, "H", 1);//给插座上电
	if(wd <= 0){
		printf("error:write.\n");
		close(fd);
		exit(1);
	}
	sleep(3);  //等待舵机上电初始化动作完成
	initAtten();  //发送使机器人立正的命令

	if( (listenfd = socket(AF_INET, SOCK_STREAM, 0)) == -1 ){
		printf("create socket error: %s(errno: %d)\n",strerror(errno),errno);
		exit(0);
	}
	memset(&servaddr, 0, sizeof(servaddr));
	servaddr.sin_family = AF_INET;
	servaddr.sin_addr.s_addr = htonl(INADDR_ANY);
	servaddr.sin_port = htons(6666);
	if( bind(listenfd, (struct sockaddr*)&servaddr, sizeof(servaddr)) == -1){
		printf("bind socket error: %s(errno: %d)\n",strerror(errno),errno);
		exit(0);
	}
		if( listen(listenfd, 10) == -1){
		printf("listen socket error: %s(errno: %d)\n",strerror(errno),errno);
		exit(0);
	}

	printf("======waiting for client's request======\n");
	while(1){ 
		if( (connfd = accept(listenfd, (struct sockaddr*)NULL, NULL)) == -1){
			printf("accept socket error: %s(errno: %d)",strerror(errno),errno);
			continue;
		}
		n = recv(connfd, buff, MAXLINE, 0);
		buff[n] = '\0';
		close(connfd);
		if(!strcmp("poweroff", buff)){
			wd = write (fd, "L", 1);
			if(wd <= 0){
				printf("error:write.\n");
				close(fd);
				exit(1);
			}
			sleep(2);
			close(listenfd);
			close(fd);
			system("reboot");
		} else if(!strcmp("record", buff)){
			system("cd ../Sound;aoss ./main;");
			printf("record\n");
		}
	}
	close(listenfd);
	close(fd);

	return 0;
}

//发送立正命令
void initAtten(void)
{
	int fd, wd;
	char arr[1024] = {0};

	fd = open("/dev/ttyUSB1", O_WRONLY);
	if(fd == -1){
		printf("Attention: Open serial error.\n");
		close(fd);
		return;
	}
	sprintf(arr, "#0P1240#2P1600#4P1560#6P1980#8P1250#10P1600T3000\r\n");
	wd = write(fd, arr, strlen(arr));
	if(wd == -1){
		printf("Attention: Write error.\n");
		close(fd);
		return;
	}
	printf("%s\n", arr);
	close(fd);

	return;
}

