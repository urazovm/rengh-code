include ./Build/Common/CommonDefs.mak

#BIN_DIR = ../Bin
BIN_DIR = .

INC_DIRS = ../../Include =/usr/include/ni

SRC_FILES = ./*.cpp

EXE_NAME = robot 

ifeq "$(GLUT_SUPPORTED)" "1"
	ifeq ("$(OSTYPE)","Darwin")
		LDFLAGS += -framework OpenGL -framework GLUT
	else
		USED_LIBS += glut GL
	endif
else
	ifeq "$(GLES_SUPPORTED)" "1"
		DEFINES += USE_GLES
		USED_LIBS += GLES_CM IMGegl srv_um
	else

	endif
endif

USED_LIBS += OpenNI

LIB_DIRS += ../../Lib
include ./Build/Common/CommonCppMakefile

