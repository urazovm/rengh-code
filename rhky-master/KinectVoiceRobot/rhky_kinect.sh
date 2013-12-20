#!/bin/sh

#等到图形界面启动后运行一下命令

#恢复ALSA音频配置文件，防止无法录音

#死循环
while true
do
#检查是否有机器人程序运行，下面命令有结果则"$?"结果为0,不是0就运行机器人控制程序
ps aux |grep "./robot"| grep -v "grep">/dev/null
if [ "$?" = "0" ];then
        echo "robot is runing..."
else
	cp /root/KinectVoiceRobot/Alsa-Sound/asound.state /var/lib/alsa/;alsactl restore -f /var/lib/alsa/asound.state;cd /root/KinectVoiceRobot/MainController/;./robot
fi
done
