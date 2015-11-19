LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

OPENCV_INSTALL_MODULES:=on
OPENCV_CAMERA_MODULES:=off
OPENCV_LIB_TYPE:=STATIC
include f:\OpenCV-android-sdk\sdk\native\jni\OpenCV.mk
LOCAL_MODULE    := Computations
LOCAL_SRC_FILES := Computations.cpp
#LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog -lGLESv2
LOCAL_LDLIBS    += -llog -lz

include $(BUILD_SHARED_LIBRARY)
