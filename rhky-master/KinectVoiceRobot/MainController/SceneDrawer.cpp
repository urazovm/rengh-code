/***********************************************************
 * 功  能：新增加ArmController, systemcall函数，传
 *         入手、肘、肩及中心点；判断手臂的姿势并发送相应命
 *         令控制机器人的手臂动作。
 * 时  间：2013.03.07，17：15
 * 修改人：任广辉
 ***********************************************************/
#include<errno.h>
#include <arpa/inet.h>
#include<sys/socket.h>
#include<netinet/in.h>
#define MAXLINE 4096

#include <math.h>
#include "SceneDrawer.h"

#ifndef USE_GLES
#if (XN_PLATFORM == XN_PLATFORM_MACOSX)
	#include <GLUT/glut.h>
#else
	#include <GL/glut.h>
#endif
#else
	#include "opengles.h"
#endif

extern xn::UserGenerator g_UserGenerator;
extern xn::DepthGenerator g_DepthGenerator;

extern XnBool g_bDrawBackground;
extern XnBool g_bDrawPixels;
extern XnBool g_bDrawSkeleton;
extern XnBool g_bPrintID;
extern XnBool g_bPrintState;

extern XnBool g_bPrintFrameID;
extern XnBool g_bMarkJoints;

#include <map>
std::map<XnUInt32, std::pair<XnCalibrationStatus, XnPoseDetectionStatus> > m_Errors;
void XN_CALLBACK_TYPE MyCalibrationInProgress(xn::SkeletonCapability& /*capability*/, XnUserID id, XnCalibrationStatus calibrationError, void* /*pCookie*/)
{
	m_Errors[id].first = calibrationError;
}
void XN_CALLBACK_TYPE MyPoseInProgress(xn::PoseDetectionCapability& /*capability*/, const XnChar* /*strPose*/, XnUserID id, XnPoseDetectionStatus poseError, void* /*pCookie*/)
{
	m_Errors[id].second = poseError;
}

unsigned int getClosestPowerOfTwo(unsigned int n)
{
	unsigned int m = 2;
	while(m < n) m<<=1;

	return m;
}
GLuint initTexture(void** buf, int& width, int& height)
{
	GLuint texID = 0;
	glGenTextures(1,&texID);

	width = getClosestPowerOfTwo(width);
	height = getClosestPowerOfTwo(height); 
	*buf = new unsigned char[width*height*4];
	glBindTexture(GL_TEXTURE_2D,texID);

	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

	return texID;
}

GLfloat texcoords[8];
void DrawRectangle(float topLeftX, float topLeftY, float bottomRightX, float bottomRightY)
{
	GLfloat verts[8] = {	topLeftX, topLeftY,
		topLeftX, bottomRightY,
		bottomRightX, bottomRightY,
		bottomRightX, topLeftY
	};
	glVertexPointer(2, GL_FLOAT, 0, verts);
	glDrawArrays(GL_TRIANGLE_FAN, 0, 4);

	//TODO: Maybe glFinish needed here instead - if there's some bad graphics crap
	glFlush();
}
void DrawTexture(float topLeftX, float topLeftY, float bottomRightX, float bottomRightY)
{
	glEnableClientState(GL_TEXTURE_COORD_ARRAY);
	glTexCoordPointer(2, GL_FLOAT, 0, texcoords);

	DrawRectangle(topLeftX, topLeftY, bottomRightX, bottomRightY);

	glDisableClientState(GL_TEXTURE_COORD_ARRAY);
}

XnFloat Colors[][3] =
{
	{0,1,1},
	{0,0,1},
	{0,1,0},
	{1,1,0},
	{1,0,0},
	{1,.5,0},
	{.5,1,0},
	{0,.5,1},
	{.5,0,1},
	{1,1,.5},
	{1,1,1}
};
XnUInt32 nColors = 10;
#ifndef USE_GLES
void glPrintString(void *font, char *str)
{
	int i,l = (int)strlen(str);

	for(i=0; i<l; i++)
	{
		glutBitmapCharacter(font,*str++);
	}
}
#endif

char readSound(void)
{
	int fd, rd;
	char buff[1] = {-1};

	fd = open("../Sound/check.txt", O_RDONLY);
	if(fd < 0){
		printf("read check.txt error.\n");
		exit(1);
	}
	rd = read(fd, buff, 1);
	if(rd != 1){
		printf("read num of bytes error.\n");
		exit(1);
	}
//	printf("%c\n", buff[0]);
	close(fd);

	return buff[0];
}

/*
//计算每条指令中设置的时间，返回时间，单位为us
int get_time(const char *s)
{
        int i, j, p, m = 0;
        double n = 0.0, us_time = 0.0;
        int set_time[5] = {-1, -1, -1, -1, -1};

        for (i = 0; i < (strlen(s)); i++){
                if (s[i] == 'T'){       //查找字符‘T’
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
*/

//调用语音程序
void record()
{
        int sockfd, n;
        char* sendline = "record";
        struct sockaddr_in      servaddr;

        if( (sockfd = socket(AF_INET, SOCK_STREAM, 0)) < 0)
        {
                printf("create socket error: %s(errno: %d)\n", strerror(errno),errno);
                return;
        }
        memset(&servaddr, 0, sizeof(servaddr));
        servaddr.sin_family = AF_INET;
        servaddr.sin_port = htons(6666);
        if( inet_pton(AF_INET, "192.168.3.130", &servaddr.sin_addr) <= 0){
                printf("inet_pton error for %s\n", "192.168.3.130");
                return;
        }
        if( connect(sockfd, (struct sockaddr*)&servaddr, sizeof(servaddr)) < 0)
        {
                printf("connect error: %s(errno: %d)\n",strerror(errno),errno);
                return;
        }
        if( send(sockfd, sendline, strlen(sendline), 0) < 0){
                printf("send msg error: %s(errno: %d)\n", strerror(errno), errno);
        }
        close(sockfd);
        return;
}

int n = 70;
time_t sav = 0, now;
void controllerRobot(int* p)
{
	int fd_serial, wrReturnNum;
	char arr[1024];
	//转换为机器人控制命令
	time(&now);
	if(sav == 0){
		sav = now;
		fd_serial = open("/dev/ttyUSB1", O_WRONLY);
		if(fd_serial == -1){
			printf("Error:open serial\n");
			return;
		}
		lseek(fd_serial, 0L, SEEK_END);
		sprintf(arr, "#0P%d#2P%d#4P%d#6P%d#8P%d#10P%dT1000\r\n", p[0], p[1], p[2], p[3], p[4], p[5]);
		wrReturnNum = write(fd_serial, arr, strlen(arr));
		if(wrReturnNum == -1){
			printf("Write error\n");
			return;
		}
	//	printf("%s\n", arr);
		close(fd_serial);
	}
	else if((sav + 0.5) < now){	//命令发送间隔最小2秒
		sav = now;
		fd_serial = open("/dev/ttyUSB1", O_WRONLY);
		if(fd_serial == -1){
			printf("Error:open serial\n");
			return;
		}
		lseek(fd_serial, 0L, SEEK_END);
		sprintf(arr, "#0P%d#2P%d#4P%d#6P%d#8P%d#10P%dT1000\r\n", p[0], p[1], p[2], p[3], p[4], p[5]);
		wrReturnNum = write(fd_serial, arr, strlen(arr));
		if(wrReturnNum == -1){
			printf("Write error\n");
			return;
		}
	//	printf("%s\n", arr);
		close(fd_serial);
	}
}

//ejoint1、2、3、4、5、6、7分别是右手、左手、右肘、左肘、右肩、左肩、中心center点
int jishu = 0;
void ArmController(XnUserID player, XnSkeletonJoint eJoint1, XnSkeletonJoint eJoint2, XnSkeletonJoint eJoint3, XnSkeletonJoint eJoint4, XnSkeletonJoint eJoint5, XnSkeletonJoint eJoint6, XnSkeletonJoint eJoint7)
{
	if (!g_UserGenerator.GetSkeletonCap().IsTracking(player)){
		printf("not tracked!\n");
		return;
	}
	if (!g_UserGenerator.GetSkeletonCap().IsJointActive(eJoint1) || !g_UserGenerator.GetSkeletonCap().IsJointActive(eJoint2) || !g_UserGenerator.GetSkeletonCap().IsJointActive(eJoint3) || !g_UserGenerator.GetSkeletonCap().IsJointActive(eJoint4) || !g_UserGenerator.GetSkeletonCap().IsJointActive(eJoint5) || !g_UserGenerator.GetSkeletonCap().IsJointActive(eJoint6) || !g_UserGenerator.GetSkeletonCap().IsJointActive(eJoint7)){
		return;
	}
	XnSkeletonJointPosition joint1;
	XnSkeletonJointPosition joint2;
	XnSkeletonJointPosition joint3;
	XnSkeletonJointPosition joint4;
	XnSkeletonJointPosition joint5;
	XnSkeletonJointPosition joint6;
	XnSkeletonJointPosition joint7;
	g_UserGenerator.GetSkeletonCap().GetSkeletonJointPosition(player, eJoint1, joint1);
	g_UserGenerator.GetSkeletonCap().GetSkeletonJointPosition(player, eJoint2, joint2);
	g_UserGenerator.GetSkeletonCap().GetSkeletonJointPosition(player, eJoint3, joint3);
	g_UserGenerator.GetSkeletonCap().GetSkeletonJointPosition(player, eJoint4, joint4);
	g_UserGenerator.GetSkeletonCap().GetSkeletonJointPosition(player, eJoint5, joint5);
	g_UserGenerator.GetSkeletonCap().GetSkeletonJointPosition(player, eJoint6, joint6);
	g_UserGenerator.GetSkeletonCap().GetSkeletonJointPosition(player, eJoint7, joint7);
	if (joint1.fConfidence < 0.5 || joint2.fConfidence < 0.5 || joint3.fConfidence < 0.5 || joint4.fConfidence < 0.5 || joint5.fConfidence < 0.5 || joint6.fConfidence < 0.5 || joint7.fConfidence < 0.5){
		return;
	}
	XnPoint3D pt[5];
	pt[0] = joint1.position;	//right_hand
	pt[1] = joint2.position;	//left_hand
	pt[2] = joint3.position;	//right_elbow
	pt[3] = joint4.position;	//left_elbow
	pt[4] = joint5.position;	//right_soulder
	pt[5] = joint6.position;	//left_soulder
	pt[6] = joint7.position;	//torso_center

	//printf("\t%lf\n", pt[6].Z);
	int p[6] = {0};		//舵机数组

	//计算中心偏离角度
	double i, j, k, Center;
	i = sqrt(pow(pt[6].X, 2) + pow(pt[6].Z - 100, 2));
	j = sqrt(pow(-100, 2));
	k = sqrt(pow(pt[6].X, 2) + pow(pt[6].Z, 2));
	Center = acos((k*k + j*j - i*i)/(2*k*j)) * 180 / 3.14;

	//计算肩膀与X轴的夹角，判断是否是转身。
	double sounda, soundb, soundc, soundx;
	sounda = sqrt(pow(-1000 - pt[5].X, 2) + pow(pt[4].Y - pt[5].Y, 2) + pow(pt[4].Z - pt[5].Z, 2));
	soundb = sqrt(pow(pt[4].X - pt[5].X, 2) + pow(pt[4].Y - pt[5].Y, 2) + pow(pt[4].Z - pt[5].Z, 2));
	soundc = sqrt(pow(pt[4].X - (-1000), 2));
	soundx = acos((soundc*soundc + soundb*soundb - sounda*sounda)/(2*soundb*soundc)) * 180 / 3.14;

	if(Center > 4.0 || soundx > 30.0 || pt[6].Z > 2500 || pt[6].Z < 2000){	//偏离中心角度大于12度或肩膀与kinect角度大于30度时不做任何动作
		p[0] = 1240;p[1] = 1600;p[2] = 1560;p[3] = 1980;p[4] = 1250;p[5] = 1600;
		controllerRobot(p);
		return;
	}

	//计算肘关节角度
	double a, b, c, rElbow;
	a = sqrt(pow(pt[4].X - pt[0].X, 2) + pow(pt[4].Y - pt[0].Y, 2) + pow(pt[4].Z - pt[0].Z, 2));
	b = sqrt(pow(pt[2].X - pt[0].X, 2) + pow(pt[2].Y - pt[0].Y, 2) + pow(pt[2].Z - pt[0].Z, 2));
	c = sqrt(pow(pt[4].X - pt[2].X, 2) + pow(pt[4].Y - pt[2].Y, 2) + pow(pt[4].Z - pt[2].Z, 2));
	rElbow = acos((c*c + b*b - a*a)/(2*b*c)) * 180 / 3.14;

	double d, e, f, lElbow;
	d = sqrt(pow(pt[5].X - pt[1].X, 2) + pow(pt[5].Y - pt[1].Y, 2) + pow(pt[5].Z - pt[1].Z, 2));
	e = sqrt(pow(pt[3].X - pt[1].X, 2) + pow(pt[3].Y - pt[1].Y, 2) + pow(pt[3].Z - pt[1].Z, 2));
	f = sqrt(pow(pt[5].X - pt[3].X, 2) + pow(pt[5].Y - pt[3].Y, 2) + pow(pt[5].Z - pt[3].Z, 2));
	lElbow = acos((f*f + e*e - d*d)/(2*e*f)) * 180 / 3.14;

	//计算肩膀与身体夹角，控制肩膀内的舵机
	double rm, rn, rp, rSoulder;
	rm = sqrt(pow(pt[2].X - pt[4].X, 2) + pow(pt[2].Y + 500, 2) + pow(pt[2].Z - pt[4].Z, 2));
	rn = sqrt(pow(pt[4].Y + 500, 2));
	rp = sqrt(pow(pt[2].X - pt[4].X, 2) + pow(pt[2].Y - pt[4].Y, 2) + pow(pt[2].Z - pt[4].Z, 2));
	rSoulder = acos((rp*rp + rn*rn - rm*rm)/(2*rp*rn)) * 180 / 3.14;

	double lm, ln, lp, lSoulder;
	lm = sqrt(pow(pt[3].X - pt[5].X, 2) + pow(pt[3].Y + 500, 2) + pow(pt[3].Z - pt[5].Z, 2));
	ln = sqrt(pow(pt[5].Y + 500, 2));
	lp = sqrt(pow(pt[3].X - pt[5].X, 2) + pow(pt[3].Y - pt[5].Y, 2) + pow(pt[3].Z - pt[5].Z, 2));
	lSoulder = acos((lp*lp + ln*ln - lm*lm)/(2*lp*ln)) * 180 / 3.14;

/*	伸开手臂高于肩膀时角度会剧烈变化，如瞬间从30度变为150度。
	//计算肩膀的投影与y轴夹角，控制肩膀内的舵机
	double rm, rn, rp, rSoulder;
	rm = sqrt(pow(pt[2].Y - (-500), 2) + pow(pt[2].Z - pt[4].Z, 2));
	rn = sqrt(pow(pt[2].Y - pt[4].Y, 2) + pow(pt[2].Z - pt[4].Z, 2));
	rp = sqrt(pow((-500) - pt[4].Y, 2));
	rSoulder = acos((rp*rp + rn*rn - rm*rm)/(2*rp*rn)) * 180 / 3.14;

	double lm, ln, lp, lSoulder;
	lm = sqrt(pow(pt[3].Y - (-500), 2) + pow(pt[3].Z - pt[5].Z, 2));
	ln = sqrt(pow(pt[3].Y - pt[5].Y, 2) + pow(pt[3].Z - pt[5].Z, 2));
	lp = sqrt(pow((-500) - pt[5].Y, 2));
	lSoulder = acos((lp*lp + ln*ln - lm*lm)/(2*lp*ln)) * 180 / 3.14;
*/

	//计算胳膊和zOy平面的夹角，控制肩外舵机
	double rsa, rsb, rsc, rSou;
	rsa = sqrt(pow(pt[2].X - pt[4].X, 2));
	rsb = sqrt(pow(pt[4].Y - pt[2].Y, 2) + pow(pt[4].Z - pt[2].Z, 2));
	rsc = sqrt(pow(pt[4].X - pt[2].X, 2) + pow(pt[4].Y - pt[2].Y, 2) + pow(pt[4].Z - pt[2].Z, 2));
	rSou = acos((rsb*rsb + rsc*rsc - rsa*rsa)/(2*rsb*rsc)) * 180 / 3.14;

	double lsa, lsb, lsc, lSou;
    lsa = sqrt(pow(pt[3].X - pt[5].X, 2));
    lsb = sqrt(pow(pt[5].Y - pt[3].Y, 2) + pow(pt[5].Z - pt[3].Z, 2));
    lsc = sqrt(pow(pt[5].X - pt[3].X, 2) + pow(pt[5].Y - pt[3].Y, 2) + pow(pt[5].Z - pt[3].Z, 2));
    lSou = acos((lsc*lsc + lsb*lsb - lsa*lsa)/(2*lsb*lsc)) * 180 / 3.14;

//	printf("\n\tCenter: %lf\n", Center);
//	printf("\n\trSoulder: %lf\n", rSoulder);
//	printf("\n\tlSoulder: %lf\n", lSoulder);
//	printf("\n\trElbow: %lf\n", rElbow);
//	printf("\n\tlElbow: %lf\n", lElbow);
//	printf("\n\trSou: %lf\n", rSou);
//	printf("\n\tlSou: %lf\n", lSou);

	//手臂在zOy平面的投影和Y所成的角度，判断肩内舵机的转向
	if(rSoulder > 150)
		rSoulder = 150;
	if(rSoulder < 20)
		rSoulder = 20;
	p[3] = (int)((-6.76) * rSoulder + 2114.6);
	if(p[3] < 1100)
		p[3] = 1100;
	if(p[3] > 1980)
		p[3] = 1980;

	if(lSoulder > 150)
		lSoulder = 150;
	if(lSoulder < 20)
		lSoulder = 20;
	p[0] = (int)(6.46 * lSoulder + 1110.8);
	if(p[0] < 1240)
		p[0] = 1240;
	if(p[0] > 2080)
		p[0] = 2080;

	//臂膀与其在zOy平面投影的夹角，控制肩膀外部舵机的转动
	if(rSou > 80)
		rSou = 80;
	if(rSou < 20)
		rSou = 20;
	p[4] = (int)(4.17 * rSou + 1166.6);
	if(p[4] > 1500)
		p[4] = 1500;
	if(p[4] < 1250)
		p[4] = 1250;

	if(lSou > 80)
		lSou = 80;
	if(lSou < 20)
		lSou = 20;
	p[1] = (int)(8 * lSou + 1440);
	if(p[1] > 2080)
		p[1] = 2080;
	if(p[1] < 1600)
		p[1] = 1600;

	//肘关节角度转舵机转动PWM值
	if(rElbow > 170 || (p[3] > 1600 && p[4] < 1350) || (p[3] < 1100 && p[4] < 1350) || (p[3] < 1600 && p[3] > 1100 && p[4] < 1350))
		rElbow = 170;
	if(rElbow < 100)
		rElbow = 100;
	p[5] = (int)((-6.86) * rElbow + 2766.2);
	if(p[5] > 2080)
		p[5] = 2080;
	if(p[5] < 1600)
		p[5] = 1600;

	if(lElbow > 170 || (p[0] > 1750 && p[1] < 1800) || (p[0] < 1550 && p[1] < 1800) || (p[0] < 1750 && p[0] > 1550 && p[1] < 1800))
		lElbow = 170;
	if(lElbow < 100)
		lElbow = 100;
	p[2] = (int)((-7.43) * lElbow + 2823);
	if(p[2] > 2080)
		p[2] = 2080;
	if(p[2] < 1560)
		p[2] = 1560;

//	printf("\n\tr1: %d\tr2: %d\tr3: %d\n", p4, p2, p0);
//	printf("\n\tl1: %d\tl2: %d\tl3: %d\n", p5, p3, p1);

	void* xfly;
	char buff[1] = {-1};
	//两手交叉，符合条件则创建线程，调用语音程序
	if((pt[1].X - pt[0].X) > 100)	//肩膀与kinect的角度达到30度则不启动
	{
		p[0] = 1240;p[1] = 1600;p[2] = 1560;p[3] = 1980;p[4] = 1250;p[5] = 1600;
		controllerRobot(p);

		buff[0] = readSound();
		if(buff[0] == '1'){
			//printf("it's busy...\n");
			return;
		}
		record();
	}
	buff[0] = readSound();
	if(buff[0] == '1'){
		//printf("it's busy...\n");
		return;
	}

	printf("%d\t%d\t%d\t%d\t%d\t%d\n", p[0], p[1], p[2], p[3], p[4], p[5]);
	controllerRobot(p);

	return;
}

bool DrawLimb(XnUserID player, XnSkeletonJoint eJoint1, XnSkeletonJoint eJoint2)
{

	if (!g_UserGenerator.GetSkeletonCap().IsTracking(player))
	{
		printf("not tracked!\n");
		return true;
	}

	if (!g_UserGenerator.GetSkeletonCap().IsJointActive(eJoint1) ||
		!g_UserGenerator.GetSkeletonCap().IsJointActive(eJoint2))
	{
		return false;
	}

	XnSkeletonJointPosition joint1, joint2;
	g_UserGenerator.GetSkeletonCap().GetSkeletonJointPosition(player, eJoint1, joint1);
	g_UserGenerator.GetSkeletonCap().GetSkeletonJointPosition(player, eJoint2, joint2);

	if (joint1.fConfidence < 0.5 || joint2.fConfidence < 0.5)
	{
		return true;
	}

	XnPoint3D pt[2];
	pt[0] = joint1.position;
	pt[1] = joint2.position;

	g_DepthGenerator.ConvertRealWorldToProjective(2, pt, pt);
#ifndef USE_GLES
	glVertex3i(pt[0].X, pt[0].Y, 0);
	glVertex3i(pt[1].X, pt[1].Y, 0);
#else
	GLfloat verts[4] = {pt[0].X, pt[0].Y, pt[1].X, pt[1].Y};
	glVertexPointer(2, GL_FLOAT, 0, verts);
	glDrawArrays(GL_LINES, 0, 2);
	glFlush();
#endif

	return true;
}

static const float DEG2RAD = 3.14159/180;
 
void drawCircle(float x, float y, float radius)
{
   glBegin(GL_TRIANGLE_FAN);
 
   for (int i=0; i < 360; i++)
   {
      float degInRad = i*DEG2RAD;
      glVertex2f(x + cos(degInRad)*radius, y + sin(degInRad)*radius);
   }
 
   glEnd();
}
void DrawJoint(XnUserID player, XnSkeletonJoint eJoint)
{
	if (!g_UserGenerator.GetSkeletonCap().IsTracking(player))
	{
		printf("not tracked!\n");
		return;
	}

	if (!g_UserGenerator.GetSkeletonCap().IsJointActive(eJoint))
	{
		return;
	}

	XnSkeletonJointPosition joint;
	g_UserGenerator.GetSkeletonCap().GetSkeletonJointPosition(player, eJoint, joint);

	if (joint.fConfidence < 0.5)
	{
		return;
	}

	XnPoint3D pt;
	pt = joint.position;

	g_DepthGenerator.ConvertRealWorldToProjective(1, &pt, &pt);

	drawCircle(pt.X, pt.Y, 2);
}

const XnChar* GetCalibrationErrorString(XnCalibrationStatus error)
{
	switch (error)
	{
	case XN_CALIBRATION_STATUS_OK:
		return "OK";
	case XN_CALIBRATION_STATUS_NO_USER:
		return "NoUser";
	case XN_CALIBRATION_STATUS_ARM:
		return "Arm";
	case XN_CALIBRATION_STATUS_LEG:
		return "Leg";
case XN_CALIBRATION_STATUS_HEAD:
		return "Head";
	case XN_CALIBRATION_STATUS_TORSO:
		return "Torso";
	case XN_CALIBRATION_STATUS_TOP_FOV:
		return "Top FOV";
	case XN_CALIBRATION_STATUS_SIDE_FOV:
		return "Side FOV";
	case XN_CALIBRATION_STATUS_POSE:
		return "Pose";
	default:
		return "Unknown";
	}
}
const XnChar* GetPoseErrorString(XnPoseDetectionStatus error)
{
	switch (error)
	{
	case XN_POSE_DETECTION_STATUS_OK:
		return "OK";
	case XN_POSE_DETECTION_STATUS_NO_USER:
		return "NoUser";
	case XN_POSE_DETECTION_STATUS_TOP_FOV:
		return "Top FOV";
	case XN_POSE_DETECTION_STATUS_SIDE_FOV:
		return "Side FOV";
	case XN_POSE_DETECTION_STATUS_ERROR:
		return "General error";
	default:
		return "Unknown";
	}
}

//time_t kill1, kill2;
void DrawDepthMap(const xn::DepthMetaData& dmd, const xn::SceneMetaData& smd)
{
/*每隔一段时间杀死进程，已作废
	while(!jishu){
		time(&kill2);
		jishu = jishu + 1;
	}
	time(&kill1);
	if(kill1 - kill2 > 600){
		int re;
		kill2 = kill1;
		re = system("ps aux|grep ./robot|grep -v grep|awk '{print $2}'|xargs kill -s 9");
		if(re == -1)
			exit(1);
	}
*/
	static bool bInitialized = false;	
	static GLuint depthTexID;
	static unsigned char* pDepthTexBuf;
	static int texWidth, texHeight;

	float topLeftX;
	float topLeftY;
	float bottomRightY;
	float bottomRightX;
	float texXpos;
	float texYpos;

	if(!bInitialized)
	{
		texWidth =  getClosestPowerOfTwo(dmd.XRes());
		texHeight = getClosestPowerOfTwo(dmd.YRes());

//		printf("Initializing depth texture: width = %d, height = %d\n", texWidth, texHeight);
		depthTexID = initTexture((void**)&pDepthTexBuf,texWidth, texHeight) ;

//		printf("Initialized depth texture: width = %d, height = %d\n", texWidth, texHeight);
		bInitialized = true;

		topLeftX = dmd.XRes();
		topLeftY = 0;
		bottomRightY = dmd.YRes();
		bottomRightX = 0;
		texXpos =(float)dmd.XRes()/texWidth;
		texYpos  =(float)dmd.YRes()/texHeight;

		memset(texcoords, 0, 8*sizeof(float));
		texcoords[0] = texXpos, texcoords[1] = texYpos, texcoords[2] = texXpos, texcoords[7] = texYpos;
	}

	unsigned int nValue = 0;
	unsigned int nHistValue = 0;
	unsigned int nIndex = 0;
	unsigned int nX = 0;
	unsigned int nY = 0;
	unsigned int nNumberOfPoints = 0;
	XnUInt16 g_nXRes = dmd.XRes();
	XnUInt16 g_nYRes = dmd.YRes();

	unsigned char* pDestImage = pDepthTexBuf;

	const XnDepthPixel* pDepth = dmd.Data();
	const XnLabel* pLabels = smd.Data();

	static unsigned int nZRes = dmd.ZRes();
	static float* pDepthHist = (float*)malloc(nZRes* sizeof(float));

	// Calculate the accumulative histogram
	memset(pDepthHist, 0, nZRes*sizeof(float));
	for (nY=0; nY<g_nYRes; nY++)
	{
		for (nX=0; nX<g_nXRes; nX++)
		{
			nValue = *pDepth;

			if (nValue != 0)
			{
				pDepthHist[nValue]++;
				nNumberOfPoints++;
			}

			pDepth++;
		}
	}

	for (nIndex=1; nIndex<nZRes; nIndex++)
	{
		pDepthHist[nIndex] += pDepthHist[nIndex-1];
	}
	if (nNumberOfPoints)
	{
		for (nIndex=1; nIndex<nZRes; nIndex++)
		{
			pDepthHist[nIndex] = (unsigned int)(256 * (1.0f - (pDepthHist[nIndex] / nNumberOfPoints)));
		}
	}

	pDepth = dmd.Data();
	if (g_bDrawPixels)
	{
		XnUInt32 nIndex = 0;
		// Prepare the texture map
		for (nY=0; nY<g_nYRes; nY++)
		{
			for (nX=0; nX < g_nXRes; nX++, nIndex++)
			{

				pDestImage[0] = 0;
				pDestImage[1] = 0;
				pDestImage[2] = 0;
				if (g_bDrawBackground || *pLabels != 0)
				{
					nValue = *pDepth;
					XnLabel label = *pLabels;
					XnUInt32 nColorID = label % nColors;
					if (label == 0)
					{
						nColorID = nColors;
					}

					if (nValue != 0)
					{
						nHistValue = pDepthHist[nValue];

						pDestImage[0] = nHistValue * Colors[nColorID][0]; 
						pDestImage[1] = nHistValue * Colors[nColorID][1];
						pDestImage[2] = nHistValue * Colors[nColorID][2];
					}
				}

				pDepth++;
				pLabels++;
				pDestImage+=3;
			}

			pDestImage += (texWidth - g_nXRes) *3;
		}
	}
	else
	{
		xnOSMemSet(pDepthTexBuf, 0, 3*2*g_nXRes*g_nYRes);
	}

	glBindTexture(GL_TEXTURE_2D, depthTexID);
	glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, texWidth, texHeight, 0, GL_RGB, GL_UNSIGNED_BYTE, pDepthTexBuf);

	// Display the OpenGL texture map
	glColor4f(0.75,0.75,0.75,1);

	glEnable(GL_TEXTURE_2D);
	DrawTexture(dmd.XRes(),dmd.YRes(),0,0);	
	glDisable(GL_TEXTURE_2D);

	char strLabel[50] = "";
	XnUserID aUsers[15];
	XnUInt16 nUsers = 15;
	g_UserGenerator.GetUsers(aUsers, nUsers);

	for (int i = 0; i < nUsers; ++i)
	{
#ifndef USE_GLES
		if (g_bPrintID)
		{
			XnPoint3D com;
			g_UserGenerator.GetCoM(aUsers[i], com);
			g_DepthGenerator.ConvertRealWorldToProjective(1, &com, &com);

			XnUInt32 nDummy = 0;

			xnOSMemSet(strLabel, 0, sizeof(strLabel));
			if (!g_bPrintState)
			{
				// Tracking
				xnOSStrFormat(strLabel, sizeof(strLabel), &nDummy, "%d", aUsers[i]);
			}
			else if (g_UserGenerator.GetSkeletonCap().IsTracking(aUsers[i]))
			{
				// Tracking
				xnOSStrFormat(strLabel, sizeof(strLabel), &nDummy, "%d - Tracking", aUsers[i]);
			}
			else if (g_UserGenerator.GetSkeletonCap().IsCalibrating(aUsers[i]))
			{
				// Calibrating
				xnOSStrFormat(strLabel, sizeof(strLabel), &nDummy, "%d - Calibrating [%s]", aUsers[i], GetCalibrationErrorString(m_Errors[aUsers[i]].first));
			}
			else
			{
				// Nothing
				xnOSStrFormat(strLabel, sizeof(strLabel), &nDummy, "%d - Looking for pose [%s]", aUsers[i], GetPoseErrorString(m_Errors[aUsers[i]].second));
			}


			glColor4f(1-Colors[i%nColors][0], 1-Colors[i%nColors][1], 1-Colors[i%nColors][2], 1);

			glRasterPos2i(com.X, com.Y);
			glPrintString(GLUT_BITMAP_HELVETICA_18, strLabel);
		}
#endif
		if (g_bDrawSkeleton && g_UserGenerator.GetSkeletonCap().IsTracking(aUsers[i]))
		{
			glColor4f(1-Colors[aUsers[i]%nColors][0], 1-Colors[aUsers[i]%nColors][1], 1-Colors[aUsers[i]%nColors][2], 1);

			// Draw Joints
			if (g_bMarkJoints)
			{
				// Try to draw all joints
				DrawJoint(aUsers[i], XN_SKEL_HEAD);
				DrawJoint(aUsers[i], XN_SKEL_NECK);
				DrawJoint(aUsers[i], XN_SKEL_TORSO);
				DrawJoint(aUsers[i], XN_SKEL_WAIST);

				DrawJoint(aUsers[i], XN_SKEL_LEFT_COLLAR);
				DrawJoint(aUsers[i], XN_SKEL_LEFT_SHOULDER);
				DrawJoint(aUsers[i], XN_SKEL_LEFT_ELBOW);
				DrawJoint(aUsers[i], XN_SKEL_LEFT_WRIST);
				DrawJoint(aUsers[i], XN_SKEL_LEFT_HAND);
				DrawJoint(aUsers[i], XN_SKEL_LEFT_FINGERTIP);

				DrawJoint(aUsers[i], XN_SKEL_RIGHT_COLLAR);
				DrawJoint(aUsers[i], XN_SKEL_RIGHT_SHOULDER);
				DrawJoint(aUsers[i], XN_SKEL_RIGHT_ELBOW);
				DrawJoint(aUsers[i], XN_SKEL_RIGHT_WRIST);
				DrawJoint(aUsers[i], XN_SKEL_RIGHT_HAND);
				DrawJoint(aUsers[i], XN_SKEL_RIGHT_FINGERTIP);

				DrawJoint(aUsers[i], XN_SKEL_LEFT_HIP);
				DrawJoint(aUsers[i], XN_SKEL_LEFT_KNEE);
				DrawJoint(aUsers[i], XN_SKEL_LEFT_ANKLE);
				DrawJoint(aUsers[i], XN_SKEL_LEFT_FOOT);

				DrawJoint(aUsers[i], XN_SKEL_RIGHT_HIP);
				DrawJoint(aUsers[i], XN_SKEL_RIGHT_KNEE);
				DrawJoint(aUsers[i], XN_SKEL_RIGHT_ANKLE);
				DrawJoint(aUsers[i], XN_SKEL_RIGHT_FOOT);
			}

#ifndef USE_GLES
			glBegin(GL_LINES);
#endif

			// Draw Limbs
			DrawLimb(aUsers[i], XN_SKEL_HEAD, XN_SKEL_NECK);
			DrawLimb(aUsers[i], XN_SKEL_NECK, XN_SKEL_LEFT_SHOULDER);
			DrawLimb(aUsers[i], XN_SKEL_LEFT_SHOULDER, XN_SKEL_LEFT_ELBOW);
			if (!DrawLimb(aUsers[i], XN_SKEL_LEFT_ELBOW, XN_SKEL_LEFT_WRIST))
			{
				DrawLimb(aUsers[i], XN_SKEL_LEFT_ELBOW, XN_SKEL_LEFT_HAND);
			}
			else
			{
				DrawLimb(aUsers[i], XN_SKEL_LEFT_WRIST, XN_SKEL_LEFT_HAND);
				DrawLimb(aUsers[i], XN_SKEL_LEFT_HAND, XN_SKEL_LEFT_FINGERTIP);
			}


			DrawLimb(aUsers[i], XN_SKEL_NECK, XN_SKEL_RIGHT_SHOULDER);
			DrawLimb(aUsers[i], XN_SKEL_RIGHT_SHOULDER, XN_SKEL_RIGHT_ELBOW);
			if (!DrawLimb(aUsers[i], XN_SKEL_RIGHT_ELBOW, XN_SKEL_RIGHT_WRIST))
			{
				DrawLimb(aUsers[i], XN_SKEL_RIGHT_ELBOW, XN_SKEL_RIGHT_HAND);
			}
			else
			{
				DrawLimb(aUsers[i], XN_SKEL_RIGHT_WRIST, XN_SKEL_RIGHT_HAND);
				DrawLimb(aUsers[i], XN_SKEL_RIGHT_HAND, XN_SKEL_RIGHT_FINGERTIP);
			}

			DrawLimb(aUsers[i], XN_SKEL_LEFT_SHOULDER, XN_SKEL_TORSO);
			DrawLimb(aUsers[i], XN_SKEL_RIGHT_SHOULDER, XN_SKEL_TORSO);
			DrawLimb(aUsers[i], XN_SKEL_TORSO, XN_SKEL_LEFT_HIP);
			DrawLimb(aUsers[i], XN_SKEL_LEFT_HIP, XN_SKEL_LEFT_KNEE);
			DrawLimb(aUsers[i], XN_SKEL_LEFT_KNEE, XN_SKEL_LEFT_FOOT);
			DrawLimb(aUsers[i], XN_SKEL_TORSO, XN_SKEL_RIGHT_HIP);
			DrawLimb(aUsers[i], XN_SKEL_RIGHT_HIP, XN_SKEL_RIGHT_KNEE);
			DrawLimb(aUsers[i], XN_SKEL_RIGHT_KNEE, XN_SKEL_RIGHT_FOOT);
			DrawLimb(aUsers[i], XN_SKEL_LEFT_HIP, XN_SKEL_RIGHT_HIP);
			time(&now);
			ArmController(aUsers[i], XN_SKEL_RIGHT_HAND, XN_SKEL_LEFT_HAND, XN_SKEL_RIGHT_ELBOW, XN_SKEL_LEFT_ELBOW, XN_SKEL_RIGHT_SHOULDER, XN_SKEL_LEFT_SHOULDER, XN_SKEL_TORSO);
			
#ifndef USE_GLES
			glEnd();
#endif
		}
	}

	if (g_bPrintFrameID)
	{
		static XnChar strFrameID[80];
		xnOSMemSet(strFrameID, 0, 80);
		XnUInt32 nDummy = 0;
		xnOSStrFormat(strFrameID, sizeof(strFrameID), &nDummy, "%d", dmd.FrameID());

		glColor4f(1, 0, 0, 1);

		glRasterPos2i(10, 10);

		glPrintString(GLUT_BITMAP_HELVETICA_18, strFrameID);
	}
}

