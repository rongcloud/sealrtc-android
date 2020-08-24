#include <jni.h>
#include <android/log.h>
#include <stdlib.h>
#include <string.h>
#include <limits.h>
#include <assert.h>
#include <iostream>
#include <vector>

#include "custom_video_frame_decryptor.h"
#include "frame_crypto_jni.h"

CustomVideoFrameDecryptor::CustomVideoFrameDecryptor() {

}

/**
    *计算解密后数据的长度
    @param frame_size　密文大小
    @return size_t 明文长度
    **/
size_t CustomVideoFrameDecryptor::GetMaxPlaintextByteSize(size_t frameSize) {
    return frameSize;
}

/**
        开发者自定义解密方法，开发者在这个方法里实现视频自定义解密
        @param  encrypted_frame 解密前的数据起始地址
        @param  encrypted_frame_size 解密前的数据大小
        @param  frame 解密后的数据起始地址，融云SDK已申请内存，开发者无需重新申请
        @param  bytes_written 解密后数据的大小
        @return  0: 成功,非0: 失败。
       **/
int CustomVideoFrameDecryptor::Decrypt(const uint8_t *encryptedFrame, size_t encryptedFrameSize,
                                       uint8_t *frame, size_t *bytesWritten) {
    uint8_t fake_key_ = 0x88;
    //LOGI("custom_crypto enter %s, %d ", __func__, __LINE__);
    for (size_t i = 0; i < encryptedFrameSize; i++) {
        if (i % 2 == 0)
            frame[i] = encryptedFrame[i] ^ fake_key_;
        else
            frame[i] = encryptedFrame[i];
    }

    *bytesWritten = encryptedFrameSize;
    //LOGI("custom_crypto exit %s, %d ", __func__, __LINE__);
    return 0;
}
