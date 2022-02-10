# Copyright 2007-2008 The Android Open Source Project

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)


LOCAL_MODULE_TAGS := optional

LOCAL_PACKAGE_NAME := preload
LOCAL_CERTIFICATE := platform

# Builds against the public SDK
#LOCAL_SDK_VERSION := current

LOCAL_JAVA_LIBRARIES += telephony-common 
LOCAL_JAVA_LIBRARIES += android.test.runner
LOCAL_STATIC_JAVA_LIBRARIES += android-support-v4
LOCAL_STATIC_JAVA_LIBRARIES += android-common jsr305
LOCAL_REQUIRED_MODULES := privapp_whitelist_com.sprd.preload


LOCAL_SRC_FILES := $(call all-java-files-under, app/src/main/java)
LOCAL_RESOURCE_DIR := $(addprefix $(LOCAL_PATH)/, app/src/main/res)

LOCAL_AAPT_FLAGS += --extra-packages com.android.ex.chips

LOCAL_PRIVILEGED_MODULE := true

LOCAL_PRIVATE_PLATFORM_APIS := true

LOCAL_PRODUCT_MODULE := true

include $(BUILD_PACKAGE)

# This finds and builds the test apk as well, so a single make does both.
include $(call all-makefiles-under,$(LOCAL_PATH))
