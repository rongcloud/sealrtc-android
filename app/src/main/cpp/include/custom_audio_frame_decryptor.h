#ifndef ANDROID_WORKSPACE_CRYPTO_CUSTOM_AUDIO_FRAME_DECRYPTOR_H
#define ANDROID_WORKSPACE_CRYPTO_CUSTOM_AUDIO_FRAME_DECRYPTOR_H

#include <jni.h>
#include <android/log.h>
#include <stdlib.h>
#include <string.h>
#include <limits.h>
#include <assert.h>
#include <stddef.h>
#include <stdint.h>

#include "custom_frame_decryptor_interface.h"

/**
   自定义音频解密类，CustomAudioFrameDecryptor 类名可以修改，但必须继承自CustomFrameDecryptorInterface
   若修改类名，需要同时修改 frame_crypto_jni.cpp 和 CMakeLists.txt  文件内对应名字
   **/
class CustomAudioFrameDecryptor : public webrtc::CustomFrameDecryptorInterface {
public:
    CustomAudioFrameDecryptor();

  /**
        开发者定义解密方法
        @param  encrypted_frame 解密前的数据起始地址
        @param  encrypted_frame_size 解密前的数据大小
        @param  frame 解密后的数据起始地址，融云SDK已申请内存，开发者无需重新申请
        @param  bytes_written 解密后数据的大小
        @return  0: 成功,非0: 失败。
       **/
    virtual int Decrypt(const uint8_t* encrypted_frame, size_t encrypted_frame_size,
                        uint8_t *frame, size_t* bytes_written);

  /**
     *计算解密后数据的长度
     @param frame_size　密文大小
     @return size_t 明文长度
     **/
    virtual size_t GetMaxPlaintextByteSize(size_t frame_size);

    virtual ~CustomAudioFrameDecryptor() {}
};

#endif //ANDROID_WORKSPACE_CRYPTO_CUSTOM_AUDIO_FRAME_DECRYPTOR_H
