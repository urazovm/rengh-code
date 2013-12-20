#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/ioctl.h>
#include <linux/soundcard.h>
#include "qtts.h"
#include "qisr.h"

#define LENGTH 2	//录音时间长度
#define RATE 16000
#define SIZE 16
#define CHANNELS 1

void start(void);  //语音开始，写入程序状态
void end(void);  //语音结束，写入程序状态
void sound(void);	//录音
void sound_play(char* file); //播放音频
void sound_no(char* file);  //无对应回答时播放
void iat(void);		//语音识别
void tts(char* str, char* file);		//文字转语音
void commoninit(char* result);    //分析命令

int main(int argc, char **argv)
{

	start();
	sound();
	iat();
	end();

/*
	char* str = "举起右手。";
	char* file = "rhup";
	printf("%s\n%s\n", str, file);
	tts(str, file);
	sound_play(file);
*/
	return 0;
}

void start(void)
{
	int fd, wd;

	fd = open("/root/KinectVoiceRobot/Sound/check.txt", O_WRONLY | O_CREAT);
	if(fd < 0){
		printf("sound start: start error.\n");
		exit(1);
	}
	wd = write(fd, "1", 1);
	if(wd != 1){
		printf("sound start: write error.\n");
		exit(1);
	}
	close(fd);
}

void end(void)
{
	int fd, wd;

	fd = open("/root/KinectVoiceRobot/Sound/check.txt", O_WRONLY | O_CREAT);
	if(fd < 0){
		printf("sound end: start error.\n");
		exit(1);
	}
	wd = write(fd, "0", 1);
	if(wd != 1){
		printf("sound end: write error.\n");
		exit(1);
	}
	close(fd);
}

//录音程序
void sound(void)
{
	int fd, fd_pcm, fd_start;
	int arg;
	int status;
	char buff[LENGTH*RATE*SIZE*CHANNELS/8];

	fd = open("/dev/dsp", O_RDWR);
	if (fd < 0) {
		perror("Open /dev/dsp device failed\n");
		exit(1);
	}

	fd_pcm = open("/root/KinectVoiceRobot/Sound/file/test.pcm", O_RDWR|O_CREAT);
	if (fd < 0) {
		perror("Open /root/KinectVoiceRobot/MainController/sound/file/test.pcm device failed\n");
		exit(1);
	}

	fd_start = open("/root/KinectVoiceRobot/Sound/file/start.pcm", O_RDWR|O_CREAT);
	if (fd < 0) {
		perror("Open /root/KinectVoiceRobot/MainController/sound/file/start.pcm device failed\n");
		exit(1);
	}

	arg = SIZE;
	status = ioctl(fd, SOUND_PCM_WRITE_BITS, &arg);
	if (status == -1)
		perror("SOUND_PCM_WRITE_BITS ioctl failed\n");
	if (arg != SIZE)
		perror("Unable to set sample size\n");
	arg = CHANNELS;
	status = ioctl(fd, SOUND_PCM_WRITE_CHANNELS, &arg);
	if (status == -1)
		perror("SOUND_PCM_WRITE_CHANNELS ioctl failed\n");
	if (arg != CHANNELS)
		perror("Unable to set number of channels\n");
	arg = RATE;
	status = ioctl(fd, SOUND_PCM_WRITE_RATE, &arg);
	if (status == -1)
		perror("SOUND_PCM_WRITE_WRITE ioctl failed\n");

	status = read(fd_start, buff, sizeof(buff));
	if (status != sizeof(buff))
		perror("Read wrong number of bytes\n");

    status = write(fd, buff, sizeof(buff));
    if (status != sizeof(buff))
         perror("Write failed\n");

    status = ioctl(fd, SOUND_PCM_SYNC, 0);
    if (status == -1)
        perror("SOUND_PCM_SYNC ioctl failed");

	status = read(fd, buff, sizeof(buff));
	if (status != sizeof(buff))
		perror("Read wrong number of bytes\n");
/*
	status = write(fd, buff, sizeof(buff));
	if (status != sizeof(buff))
		perror("Write failed\n");
*/
	status = write(fd_pcm, buff, sizeof(buff));
	if (status != sizeof(buff))
		perror("Write failed\n");

	status = ioctl(fd, SOUND_PCM_SYNC, 0);
	if (status == -1)
		perror("SOUND_PCM_SYNC ioctl failed");

	close(fd_pcm);
	close(fd);
}

void sound_play(char* file)
{
	int fd, fd_pcm;
	int arg;
	int i = -1, n = -1;
	int status;
	int LEN;	//播放时间长度
	char* fn[] = {"welcom", "myname", "hello", "byebye", "joke_1", "joke_2", "eat", "where", "sha", "ben", "hao", "wuliao", "bank"};

	for(i = 0; i < sizeof(fn) / 4; i++){
		if(!strcmp(fn[i], file)){
			n = i;
			break;
		}
	}

	switch(n){
	case 0:
		LEN = 7;
		break;
	case 1:
		LEN = 4;
		break;
	case 2:
		LEN = 7;
		break;
	case 3:
		LEN = 4;
		break;
	case 4:
		LEN = 40;
		break;
	case 5:
		LEN = 33;
		break;
	case 6:
		LEN = 3;
		break;
	case 7:
		LEN = 5;
		break;
	case 8:
		LEN = 2;
		break;
	case 9:
		LEN = 2;
		break;
	case 10:
		LEN = 3;
		break;
	case 11:
		LEN = 4;
		break;
	case 12:
		LEN = 5;
		break;
	default:
		break;
	}
	char fileName[100] = {0};
	if(strcmp(fn[n], file)){
		LEN = 6;
		sprintf(fileName, "/root/KinectVoiceRobot/Sound/file/%s.pcm", file);
	}
	else{
		sprintf(fileName, "/root/KinectVoiceRobot/Sound/file/%s.pcm", fn[n]);
	}
	
	char buff[LEN*RATE*SIZE*CHANNELS/8];

	fd = open("/dev/dsp", O_RDWR);
	if (fd < 0) {
		perror("Open /dev/dsp device failed\n");
		exit(1);
	}

	fd_pcm = open(fileName, O_RDONLY);
	if (fd < 0) {
		printf("Open %s device failed\n", fileName);
		exit(1);
	}

	arg = SIZE;
	status = ioctl(fd, SOUND_PCM_WRITE_BITS, &arg);
	if (status == -1)
		perror("SOUND_PCM_WRITE_BITS ioctl failed\n");
	if (arg != SIZE)
		perror("Unable to set sample size\n");
	arg = CHANNELS;
	status = ioctl(fd, SOUND_PCM_WRITE_CHANNELS, &arg);
	if (status == -1)
		perror("SOUND_PCM_WRITE_CHANNELS ioctl failed\n");
	if (arg != CHANNELS)
		perror("Unable to set number of channels\n");
	arg = RATE;
	status = ioctl(fd, SOUND_PCM_WRITE_RATE, &arg);
	if (status == -1)
		perror("SOUND_PCM_WRITE_WRITE ioctl failed\n");

	status = read(fd_pcm, buff, sizeof(buff));

	status = write(fd, buff, sizeof(buff));
	if (status != sizeof(buff))
		perror("Write failed\n");

	status = ioctl(fd, SOUND_PCM_SYNC, 0);
	if (status == -1)
		perror("SOUND_PCM_SYNC ioctl failed");

	close(fd_pcm);
	close(fd);
}
//语音转文字
void iat(void)
{
	int ret = 0;

	ret = QISRInit("appid=50c2d5d1,vad_enable=0");
	if(ret != 0)
	{
		printf("QISRInit err %d", ret);
		return;
	}

	const char* sess_id = QISRSessionBegin("", "ssm=1,sub=iat,auf=audio/L16;rate=16000,aue=raw,ent=sms16k,rst=plain,rse=utf8", &ret);
	
	if ( ret != 0 )
	{
		printf("QISRSessionBegin err %d\n", ret);		
		return;
	}

	FILE* fp = fopen("/root/KinectVoiceRobot/Sound/file/test.pcm", "rb");
	FILE* fout = fopen("/root/KinectVoiceRobot/Sound/iat_result.txt", "wb+");
	if ( fp == NULL || fout == NULL )
	{
		printf ("open input sound_file/test.pcm failed.\n");
		return;
	}
	char buff[1024 * 4], strVol[16];
	int len;
	unsigned int volLen;
	int status = 2, ep_status = 0, rec_status = 0, rslt_status = 0;
	printf("writing audio...\n");
	while ( !feof(fp) )
	{
		len = fread(buff, 1, 1024 * 4, fp);

		ret = QISRAudioWrite(sess_id, buff, len, status, &ep_status, &rec_status);
		if ( ret != 0 )
		{
			printf("QISRAudioWrite err %d\n", ret);
			break;
		}

		printf(".");
		usleep(200000);
	}
	printf("\n");
	fclose(fp); 

	status = 4;
	ret = QISRAudioWrite(sess_id, buff, 1, status, &ep_status, &rec_status);
	if ( ret != 0 )
	{
		printf("QISRAudioWrite write last audio err %d\n", ret);
		return;
	}

	int j = 0;
	char* result = NULL;
	do 
	{
		result = QISRGetResult(sess_id, &rslt_status, 0, &ret);
		if ( ret != 0 )
		{
			printf("QISRGetResult err %d\n", ret);
			break;
		}

		if( rslt_status == 1 )
			printf("get result nomatch\n");
		else
		{
			if ( result != NULL )
			{
				fwrite(result, 1, strlen(result), fout);
				printf("get a result: %s\n", result);
				commoninit(result);
				return;
			}
		}
		usleep(500000);
		j++;
	} while (rslt_status != 5 && j<30);

	fclose(fout);

	ret = QISRSessionEnd(sess_id, NULL);
	if ( ret != 0 )
	{
		printf("QISRSessionEnd err %d\n", ret);
		//return;
	}

	ret = QISRFini();
	return;
}

//文字转语音
void tts(char* str, char* file)
{
        int ret = 0;

        ret = QTTSInit("appid=50c2d5d1");
        if(ret != 0){
                printf("QTTSInit err %d",ret);
        }

        const char* sess_id = QTTSSessionBegin("ssm=1,ent=vivi21,auf=audio/L16;rate=16000,vcn=vinn,tte=UTF8",&ret);
        if(ret != 0){
                printf("QTTSSessionBegin err %d",ret);
        }

        ret = QTTSTextPut(sess_id, str, strlen(str), NULL);
        if(ret != 0){
                printf("QTTSTextPut err %d",ret);
        }

        const void* audio_data=NULL;
        unsigned int audio_len = 0;
        int synth_status = 0;
        const char* audio_info = NULL;
        FILE* fp;
	char fileName[100] = {0};

	sprintf(fileName, "file/%s.pcm", file);
	
        if((fp=fopen(fileName,"wb"))==NULL){
                printf("open file:%s error!", fileName);
                ret = QTTSSessionEnd(sess_id,NULL);
                return;
        }

        while(ret==0 && synth_status != 2){
                audio_data = QTTSAudioGet(sess_id,&audio_len,&synth_status,&ret);
                if(ret != 0){
                        printf("QTTSAudioGet err %d",ret);
                }
                fwrite(audio_data,audio_len,1,fp);
        }

        fclose(fp);
        ret = QTTSSessionEnd(sess_id,NULL);
        if(ret != 0){
                printf("QTTSSessionEnd err %d",ret);
        }
        ret = QTTSFini();
        if(ret != 0){
                printf("QTTSFini err %d",ret);
        }
}


//分析命令
void commoninit(char* result)
{
	char* src[] = {"招呼", "叫", "你好", "再见", "笑话", "吃", "怎么走", "傻", "笨", "漂亮", "有", "近"};
	char* file[] = {"welcom", "myname", "hello", "byebye", "joke_1", "joke_2", "eat", "where", "sha", "ben", "hao", "wuliao", "bank"};
	char* test = NULL;
	char* no = "nofile";
	int i, n, m;

	for(i = 0; i < sizeof(src)/sizeof(no); i++){
		test = strstr(result, src[i]);
		if(test != NULL){
			n = i;
			test = src[i];
			break;
		}
	}

	if(test == NULL){
		sound_no(no);
		return;
	}

	if(n == 4){
		srand(time(NULL));
		m = rand() % 2;
		if(m == 1)
			n = 4;
		else
			n = 5;
	}
	if(n > 4)
		n += 1;

	printf("\t%s\n\t%s\n", test, file[n]);
	sound_play(file[n]);
}

void sound_no(char* file)
{
	int fd, fd_pcm;
	int arg;
	int status;
	int LEN = 6;	//播放时间长度
	char buff[LEN*RATE*SIZE*CHANNELS/8];
	char fileName[100] = {0};

	sprintf(fileName, "/root/KinectVoiceRobot/Sound/file/%s.pcm", file);

	fd = open("/dev/dsp", O_RDWR);
	if (fd < 0) {
		perror("Open /dev/dsp device failed\n");
		exit(1);
	}

	fd_pcm = open(fileName, O_RDONLY);
	if (fd < 0) {
		printf("Open %s device failed\n", fileName);
		exit(1);
	}

	arg = SIZE;
	status = ioctl(fd, SOUND_PCM_WRITE_BITS, &arg);
	if (status == -1)
		perror("SOUND_PCM_WRITE_BITS ioctl failed\n");
	if (arg != SIZE)
		perror("Unable to set sample size\n");
	arg = CHANNELS;
	status = ioctl(fd, SOUND_PCM_WRITE_CHANNELS, &arg);
	if (status == -1)
		perror("SOUND_PCM_WRITE_CHANNELS ioctl failed\n");
	if (arg != CHANNELS)
		perror("Unable to set number of channels\n");
	arg = RATE;
	status = ioctl(fd, SOUND_PCM_WRITE_RATE, &arg);
	if (status == -1)
		perror("SOUND_PCM_WRITE_WRITE ioctl failed\n");

	status = read(fd_pcm, buff, sizeof(buff));

	status = write(fd, buff, sizeof(buff));
	if (status != sizeof(buff))
		perror("Write failed\n");

	status = ioctl(fd, SOUND_PCM_SYNC, 0);
	if (status == -1)
		perror("SOUND_PCM_SYNC ioctl failed");

	close(fd_pcm);
	close(fd);
}
