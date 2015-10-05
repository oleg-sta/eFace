LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := Computations
LOCAL_SRC_FILES := Computations.cpp

include $(BUILD_SHARED_LIBRARY)
