#include "testApp.h"
string status = "initial";
string laststatus;
bool changestart = 1;
bool changefinish = 0;
int changecount;

float degree = 0;

int frameIndex = 0;		//360show
int framecount = 0;
string speedof360;
int expand = 0;
int showx = 0, showy = 0;
ofSerial serial;

/**定义发给串口的控制命令*/
unsigned char buffGO[7] = {'G', ',', '+', '2', '5', '5', ';'}; /**控制电控膜变为透明*/
unsigned char buffGC[7] = {'G', ',', '+', '0', '0', '0', ';'}; /**控制电控膜变为非透明*/
unsigned char buffMO[7] = {'M', ',', '+', '2', '5', '5', ';'}; /**控制背光板亮起*/
unsigned char buffMC[7] = {'M', ',', '+', '0', '0', '0', ';'}; /**控制背光板熄灭*/
unsigned char buffSL[7] = {'S', ',', '+', '0', '0', '1', ';'}; /**控制电机正转1度*/
unsigned char buffSR[7] = {'S', ',', '-', '0', '0', '1', ';'}; /**控制电机反转1度*/
unsigned char buffAllOpen[21] = {'M', ',', '+', '2', '5', '5', ';', 'G', ',', '+', '0', '0',
                '0', ';', 'S', ',', '+', '0', '0', '0', ';'}; /**控制所有设备为启动状态*/
unsigned char buffAllClose[21] = {'M', ',', '+', '0','0', '0', ';', 'G', ',', '+', '2', '5',
                '5', ';', 'S', ',', '-', '0', '0', '0', ';'}; /**控制所有设备恢复默认状态*/

//--------------------------------------------------------------
void testApp::setup(){
	ofSetFullscreen(1);
	ofHideCursor();
	ofBackground(255, 255, 255);
	ofSetFrameRate(60);
	start0.loadImage("start0.jpg");
	start1.loadImage("start1.jpg");
	start2.loadImage("start2.jpg");
	start3.loadImage("start3.jpg");
	start4.loadImage("start4.jpg");
	start5.loadImage("start5.jpg");
	show360main.loadImage("360main.jpg");

	ofDirectory dir;		//360show
	int nFiles = dir.listDir("360");
	if (nFiles) {

		for (int i = 0; i<dir.numFiles(); i++) {

			// add the image to the vector
			string filePath = dir.getPath(i);
			images.push_back(ofImage());
			images.back().loadImage(filePath);

		}

	}
	else printf("Could not find 360 folder\n");

	// this toggle will tell the sequence
	// be be indepent of the app fps
	bFrameIndependent = true;

	// this will set the speed to play
	// the animation back we set the
	// default to 24fps
	sequenceFPS = 24;

	/* 设置串口 */
	int baud = 9600;
	//serial.setup(0, baud); //open the first device
	serial.setup("COM3", baud); // windows example
	//serial.setup("/dev/tty.usbserial-A4001JEC", baud); // mac osx example
	//serial.setup("/dev/ttyUSB0", baud); //linux example
    serial.writeBytes(buffAllOpen, sizeof(buffAllOpen)/sizeof(unsigned char));
}

//--------------------------------------------------------------
void testApp::update(){
	if (changestart){
		if (changecount < 245){
			changecount += 10;
		}
		else{
			changecount = 255;
			changestart = 0;
			changefinish = 1;
		}
	}

	if (changefinish){
		if (changecount > 10){
			changecount -= 10;
		}
		else{
			changecount = 0;
			changefinish = 0;
			changestart = 0;
		}
	}

	if ((status == "360show" && !changestart) || (changestart&&laststatus == "360show")){
		switch ((int)sequenceFPS){
		case -4:
			framecount++;
			if (framecount == 4){
				framecount = 0;
				frameIndex--;
				if (frameIndex == -1)frameIndex = 46;
			}
			break;
		case -2:
			framecount++;
			if (framecount == 2){
				framecount = 0;
				frameIndex--;
				if (frameIndex == -1)frameIndex = 46;
			}
			break;
		case 0:
			break;
		case 2:
			framecount++;
			if (framecount == 2){
				framecount = 0;
				frameIndex++;
				if (frameIndex == 47)frameIndex = 0;
			}
			break;
		case 4:
			framecount++;
			if (framecount == 4){
				framecount = 0;
				frameIndex++;
				if (frameIndex == 47)frameIndex = 0;
			}
			break;
		}
	}
}

//--------------------------------------------------------------
void testApp::draw(){
//	cout << changecount;
	if ((status == "initial" && !changestart) || (changestart&&laststatus == "initial")){
		ofSetColor(255);
		start0.draw(0, 0, ofGetWindowWidth(), ofGetWindowHeight());
		frameIndex = 0;
		sequenceFPS = 0; framecount = 0; speedof360 = "pause";
	}

	if ((status == "show" && !changestart) || (changestart&&laststatus == "show")){
		sequenceFPS = 0; framecount = 0; speedof360 = "pause";
		ofSetColor(255);
		changePicFromDegree();
		ofSetColor(50);
		//ofRect(0, 0, 350, 30);
		ofSetColor(200);
		string info;
		int tempdeg = 360 * (degree / 6.28);
		info += "turned ";
		info += ofToString(tempdeg);
		info += " degree \nshow more info according to the angle";
		//ofDrawBitmapString(info, 0, 13);
	}

	if ((status == "360show" && !changestart) || (changestart&&laststatus == "360show")){
		ofSetColor(255);
		show360main.draw(0, 0, ofGetWindowWidth(), ofGetWindowHeight());
		ofEnableAlphaBlending();
		ofSetColor(255, min(255, 15 * expand));
		ofRect(0, 0, ofGetWindowWidth(), ofGetWindowHeight());
		ofDisableAlphaBlending();
		// we need some images if not return
		if ((int)images.size() <= 0) {
			ofSetColor(255);
			//ofDrawBitmapString("No Car Images...", 150, ofGetHeight() / 2);
			return;
		}

		// draw the image sequence at the new frame count
		ofSetColor(255);
		images[frameIndex].draw(ofGetWindowWidth() / 2 - 224 + showx - 2 * expand, ofGetWindowHeight() / 2 - 114 + showy - expand, 448 + 4 * expand, 228 + 2 * expand);



		// how fast is the app running and some other info
		ofSetColor(50);
		//ofRect(0, 0, 140, 70);
		ofSetColor(200);
		string info;
		info += "360 show ";
//		info += speedof360;
		info += "\nh j to turn \n-+ for size \narrows to move";

		//ofDrawBitmapString(info, 15, 20);
	}

	if (changecount>0){
		ofEnableAlphaBlending();
		ofSetColor(255, 255, 255, changecount);
		ofRect(0, 0, ofGetWindowWidth(), ofGetWindowHeight());
		ofDisableAlphaBlending();
	}
}

//--------------------------------------------------------------
void testApp::keyPressed(int key){
	switch (key){
	case '-':
		expand -= 1;
		expand = max(0, expand);
		return;
	case '=':
		expand += 1;
		return;
	case OF_KEY_RIGHT:
		showx += 1;
		return;
	case OF_KEY_LEFT:
		showx -= 1;
		return;
	case OF_KEY_UP:
		showy -= 1;
		return;
	case OF_KEY_DOWN:
		showy += 1;
		return;
    case 'h':
		if (status == "show"){
			//degree += 0.00157079632;
			degree += 1;
			serial.writeBytes(buffSR, sizeof(buffSR)/sizeof(unsigned char));
			return;
		} else if (status == "360show"){
		    sequenceFPS = 0; framecount = 0; speedof360 = "pause";
			frameIndex++;
			if (frameIndex == 47)frameIndex = 0;
			return;
		}
	case 'j':
		if (status == "show"){
			//degree -= 0.00157079632;
			degree -= 1;
			serial.writeBytes(buffSL, sizeof(buffSL)/sizeof(unsigned char));
			return;
		} else if (status == "360show"){
            sequenceFPS = 0; framecount = 0; speedof360 = "pause";
			frameIndex--;
			if (frameIndex == -1)frameIndex = 46;
			return;
		}
	}
}

//--------------------------------------------------------------
void testApp::keyReleased(int key){
	switch (key){
	case 'a':
		if (status != "initial"){
			laststatus = status;
			status = "initial";
			changestart = 1;
			changefinish = 0;
		}

        if(degree >= 0){
            buffAllOpen[16] = '+';
        }else{
            degree = -degree;
            buffAllOpen[16] = '-';
        }
        n1 = (int)degree/100;
        n2 = (int)(degree-n1*100)/10;
        n3 = (int)degree - n1*100 - n2*10;
        buffAllOpen[17] = ofToChar(ofToString(n1));
        buffAllOpen[18] = ofToChar(ofToString(n2));
        buffAllOpen[19] = ofToChar(ofToString(n3));
        degree = 0;
        serial.writeBytes(buffAllOpen, sizeof(buffAllOpen)/sizeof(unsigned char));
		return;
	case 's':
		if (status != "show"){
			laststatus = status;
			status = "show";
			changestart = 1;
			changefinish = 0;
			serial.writeBytes(buffGO, sizeof(buffGO)/sizeof(unsigned char));
		}
		return;
	case 'd':
		if (status != "360show"){
			laststatus = status;
			status = "360show";
			changestart = 1;
			changefinish = 0;
			expand = 0;
			showx = 0;
			showy = 0;
			serial.writeBytes(buffGC, sizeof(buffGC)/sizeof(unsigned char));
		}
		return;
    case 'f':
        if (status != "initial"){
			laststatus = status;
			status = "initial";
			changestart = 1;
			changefinish = 0;
		}

        if(degree >= 0){
            buffAllClose[16] = '+';
        }else{
            degree = -degree;
            buffAllClose[16] = '-';
        }
        n1 = (int)degree/100;
        n2 = (int)(degree-n1*100)/10;
        n3 = (int)degree - n1*100 - n2*10;
        buffAllClose[17] = ofToChar(ofToString(n1));
        buffAllClose[18] = ofToChar(ofToString(n2));
        buffAllClose[19] = ofToChar(ofToString(n3));
        degree = 0;
        serial.writeBytes(buffAllClose, sizeof(buffAllClose)/sizeof(unsigned char));
        return;
	}

	if (key == 'q'){
		sequenceFPS = -2; framecount = 0; speedof360 = "speed left"; return;
	}
	if (key == 'w'){
		sequenceFPS = -4; framecount = 0; speedof360 = "left"; return;
	}
	if (key == 'e'){
		sequenceFPS = 0; framecount = 0; speedof360 = "pause"; return;
	}
	if (key == 'r'){
		sequenceFPS = 4; framecount = 0; speedof360 = "right"; return;
	}
	if (key == 't'){
		sequenceFPS = 2; framecount = 0; speedof360 = "speed right"; return;
	}
}

//--------------------------------------------------------------
void testApp::mouseMoved(int x, int y){

}

//--------------------------------------------------------------
void testApp::mouseDragged(int x, int y, int button){

}

//--------------------------------------------------------------
void testApp::mousePressed(int x, int y, int button){

}

//--------------------------------------------------------------
void testApp::mouseReleased(int x, int y, int button){

}

//--------------------------------------------------------------
void testApp::windowResized(int w, int h){

}

//--------------------------------------------------------------
void testApp::gotMessage(ofMessage msg){

}

//--------------------------------------------------------------
void testApp::dragEvent(ofDragInfo dragInfo){

}

void testApp::changePicFromDegree(){
    printf("%f\n", degree);
    if(degree > 21 ){
        if(degree <= 60)
            start2.draw(0, 0, ofGetWindowWidth(), ofGetWindowHeight());
        else if(degree <= 100)
            start3.draw(0, 0, ofGetWindowWidth(), ofGetWindowHeight());
        else if(degree <= 140)
            start4.draw(0, 0, ofGetWindowWidth(), ofGetWindowHeight());
        else if(degree <= 180)
            start5.draw(0, 0, ofGetWindowWidth(), ofGetWindowHeight());
        else if(degree == 181){
            degree = -21;
        }
    }else if (degree < -21){
        if(degree >= -60)
            start5.draw(0, 0, ofGetWindowWidth(), ofGetWindowHeight());
        else if(degree >= -100)
            start4.draw(0, 0, ofGetWindowWidth(), ofGetWindowHeight());
        else if(degree >= -140)
            start3.draw(0, 0, ofGetWindowWidth(), ofGetWindowHeight());
        else if(degree >= -180)
            start2.draw(0, 0, ofGetWindowWidth(), ofGetWindowHeight());
        else if(degree == -181){
            degree = 21;
        }
    }else
        start1.draw(0, 0, ofGetWindowWidth(), ofGetWindowHeight());
}
