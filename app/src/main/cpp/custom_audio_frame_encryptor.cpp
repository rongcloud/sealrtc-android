#include <jni.h>
#include <android/log.h>
#include <stdlib.h>
#include <string.h>
#include <limits.h>
#include <assert.h>
#include <iostream>

#include "custom_audio_frame_encryptor.h"
#include "frame_crypto_jni.h"

CustomAudioFrameEncryptor::CustomAudioFrameEncryptor() {

}

/**
      自定义音频加密方法，开发者在这个方法里实现音频自定义加密
      @param  payload_data 加密前的数据起始地址
      @param  payload_size 加密前的数据大小
      @param  encrypted_frame 加密后的数据起始地址，融云SDK已申请内存，开发者无需重新申请
      @param  bytes_written 加密后数据的大小
      @return  0: 成功,非0: 失败。
      **/
int CustomAudioFrameEncryptor::Encrypt(const uint8_t *payload_data, size_t payload_size,
                                       uint8_t *encrypted_frame, size_t *bytes_written) {
    uint8_t fake_key_ = 0x88;

    for (size_t i = 0; i < payload_size; i++) {
        encrypted_frame[i] = payload_data[i] ^ fake_key_;
    }
    *bytes_written = payload_size;
    return 0;
}

/**
    计算音频加密后数据的长度
      @param frame_size　明文大小
      @return size_t 密文长度
      **/
size_t CustomAudioFrameEncryptor::GetMaxCiphertextByteSize(size_t frame_size) {
  return frame_size;
}