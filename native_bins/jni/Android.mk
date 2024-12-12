LOCAL_PATH := $(call my-dir)

# Build memread binary
include $(CLEAR_VARS)
LOCAL_MODULE := memread
LOCAL_SRC_FILES := memread.c
include $(BUILD_EXECUTABLE)

# Build nDegreeRead binary
include $(CLEAR_VARS)
LOCAL_MODULE := nDegreeRead
LOCAL_SRC_FILES := nDegreeRead.c
include $(BUILD_EXECUTABLE)

# Build memwrite binary
include $(CLEAR_VARS)
LOCAL_MODULE := memwrite
LOCAL_SRC_FILES := memwrite.c
include $(BUILD_EXECUTABLE)


post_build:
    $(shell cp $(LOCAL_PATH)/../libs/arm64-v8a/memread ../../app/src/main/res/raw/memread_arm64_v8a)
    $(shell cp $(LOCAL_PATH)/../libs/armeabi-v7a/memread ../../app/src/main/res/raw/memread_armeabi_v7a)
    $(shell cp $(LOCAL_PATH)/../libs/riscv64/memread ../../app/src/main/res/raw/memread_riscv64)
    $(shell cp $(LOCAL_PATH)/../libs/x86/memread ../../app/src/main/res/raw/memread_x86)
    $(shell cp $(LOCAL_PATH)/../libs/x86_64/memread ../../app/src/main/res/raw/memread_x86_64)
    $(shell cp $(LOCAL_PATH)/../libs/arm64-v8a/nDegreeRead ../../app/src/main/res/raw/ndegreeread_arm64_v8a)
    $(shell cp $(LOCAL_PATH)/../libs/armeabi-v7a/nDegreeRead ../../app/src/main/res/raw/ndegreeread_armeabi_v7a)
    $(shell cp $(LOCAL_PATH)/../libs/riscv64/nDegreeRead ../../app/src/main/res/raw/ndegreeread_riscv64)
    $(shell cp $(LOCAL_PATH)/../libs/x86/nDegreeRead ../../app/src/main/res/raw/ndegreeread_x86)
    $(shell cp $(LOCAL_PATH)/../libs/x86_64/nDegreeRead ../../app/src/main/res/raw/ndegreeread_x86_64)
    $(shell cp $(LOCAL_PATH)/../libs/arm64-v8a/memwrite ../../app/src/main/res/raw/memwrite_arm64_v8a)
    $(shell cp $(LOCAL_PATH)/../libs/armeabi-v7a/memwrite ../../app/src/main/res/raw/memwrite_armeabi_v7a)
    $(shell cp $(LOCAL_PATH)/../libs/riscv64/memwrite ../../app/src/main/res/raw/memwrite_riscv64)
    $(shell cp $(LOCAL_PATH)/../libs/x86/memwrite ../../app/src/main/res/raw/memwrite_x86)
    $(shell cp $(LOCAL_PATH)/../libs/x86_64/memwrite ../../app/src/main/res/raw/memwrite_x86_64)
 
# Register the post-build steps
$(POST_BUILD): post_build
