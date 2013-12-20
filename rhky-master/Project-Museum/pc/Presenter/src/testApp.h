#pragma once

#include "ofMain.h"

class testApp : public ofBaseApp{
	public:
		void setup();
		void update();
		void draw();

		void keyPressed(int key);
		void keyReleased(int key);
		void mouseMoved(int x, int y);
		void mouseDragged(int x, int y, int button);
		void mousePressed(int x, int y, int button);
		void mouseReleased(int x, int y, int button);
		void windowResized(int w, int h);
		void dragEvent(ofDragInfo dragInfo);
		void gotMessage(ofMessage msg);
		void changePicFromDegree();

		ofImage start0;			//����������ĵ�һ����ʾ��ͼƬ��

		ofImage start1;			//��ʼ��ʽ������ͼƬ��
        ofImage start2;
        ofImage start3;
        ofImage start4;
        ofImage start5;

		float sequenceFPS;		//360show
		bool  bFrameIndependent;
		vector <ofImage> images;
		ofImage show360main;

        int n1, n2, n3;
};
