#common makefile header

BASEDIR = .
include configure.mk

MODULE_EXEC	=  main
MODULE_TARGET	= $(MODULE_EXEC)
LOCAL_INCLUDE	= -Iinclude
LOCAL_FLAGS	= $(LOCAL_INCLUDE) $(PIC_FLAG)
LIBS	:= -Lbin -lmsc -ldl -lpthread -Wl,-rpath=./bin -g

MODULE_OBJ   = \
	$(MODULE_OUTPUT)/main.o

#common makefile foot
include rules.mk
