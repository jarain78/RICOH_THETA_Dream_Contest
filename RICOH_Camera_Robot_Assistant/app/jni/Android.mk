LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

OPENCV_INSTALL_MODULES:=on
OPENCV_LIB_TYPE:=SHARED
include E:\Dropbox\Hacksterio_Projects\RICOH_THETA_Dream_Contest\RICOH_Camera_Robot_Assistant\OpenCV-android-sdk\sdk\native\jni\OpenCV.mk
LOCAL_MODULE := robotassistant
LOCAL_SRC_FILES := sample.cpp
include $(BUILD_SHARED_LIBRARY)