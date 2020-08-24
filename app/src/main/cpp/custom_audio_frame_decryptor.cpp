#include <jni.h>
#include <android/log.h>
#include <stdlib.h>
#include <string.h>
#include <limits.h>
#include <assert.h>
#include <iostream>

#include "custom_audio_frame_decryptor.h"
#include "frame_crypto_jni.h"

CustomAudioFrameDecryptor::CustomAudioFrameDecryptor() {

}

/**
        自定义解密方法，开发者在这个方法里实现音频自定义解密
        @param  encrypted_frame 解密前的数据起始地址
        @param  encrypted_frame_size 解密前的数据大小
        @param  frame 解密后的数据起始地址，融云SDK已申请内存，开发者无需重新申请
        @param  bytes_written 解密后数据的大小
        @return  0: 成功,非0: 失败。
       **/
int CustomAudioFrameDecryptor::Decrypt(const uint8_t *encrypted_frame, size_t encrypted_frame_size,
                                       uint8_t *frame, size_t *bytes_written) {
    uint8_t fake_key_ = 0x88;

    for (size_t i = 0; i < encrypted_frame_size; i++) {
        frame[i] = encrypted_frame[i] ^ fake_key_;
    }

    *bytes_written = encrypted_frame_size;
    return 0;
}

/**
    *计算解密后数据的长度
    @param frame_size　密文大小
    @return size_t 明文长度
    **/
size_t CustomAudioFrameDecryptor::GetMaxPlaintextByteSize(size_t frame_size) {
  return frame_size;
}
