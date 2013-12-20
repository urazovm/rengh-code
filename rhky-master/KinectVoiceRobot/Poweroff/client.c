#include<stdio.h>
#include<stdlib.h>
#include<string.h>
#include<errno.h>
#include<sys/types.h>
#include<sys/socket.h>
#include<netinet/in.h>
#define MAXLINE 4096

int main(int argc, char** argv)
{
	int sockfd, n;
	char* sendline = "record";
	struct sockaddr_in	servaddr;

	if( (sockfd = socket(AF_INET, SOCK_STREAM, 0)) < 0)
	{
		printf("create socket error: %s(errno: %d)\n", strerror(errno),errno);
		exit(0);
	}
	memset(&servaddr, 0, sizeof(servaddr));
	servaddr.sin_family = AF_INET;
	servaddr.sin_port = htons(6666);
	if( inet_pton(AF_INET, "192.168.1.100", &servaddr.sin_addr) <= 0){
		printf("inet_pton error for %s\n", "192.168.1.100");
		exit(0);
	}
	if( connect(sockfd, (struct sockaddr*)&servaddr, sizeof(servaddr)) < 0)
	{
		printf("connect error: %s(errno: %d)\n",strerror(errno),errno);
		exit(0);
	}
	if( send(sockfd, sendline, strlen(sendline), 0) < 0){
		printf("send msg error: %s(errno: %d)\n", strerror(errno), errno);
	}
	close(sockfd);
	return 0;
}
