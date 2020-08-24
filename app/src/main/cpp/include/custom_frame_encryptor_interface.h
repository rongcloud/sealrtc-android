#ifndef API_CRYPTO_CUSTOM_FRAME_ENCRYPTOR_INTERFACE_H_
#define API_CRYPTO_CUSTOM_FRAME_ENCRYPTOR_INTERFACE_H_

#include <stddef.h>
#include <stdint.h>

namespace webrtc {
    class CustomFrameEncryptorInterface {
    public:
        virtual ~CustomFrameEncryptorInterface() {}

      /**
       自定义加密方法
       @param  payload_data 加密前的数据起始地址
       @param  payload_size 加密前的数据大小
       @param  encrypted_frame 加密后的数据起始地址，融云SDK已申请内存，开发者无需重新申请
       @param  bytes_written 加密后数据的大小
       @return  0: 成功,非0: 失败。
       **/
        virtual int Encrypt(const uint8_t *payload_data, size_t payload_size,
                            uint8_t *encrypted_frame, size_t *bytes_written) = 0;

      /**
       *计算加密后数据的长度
       @param frame_size　明文大小
       @return size_t 密文长度
       **/
        virtual size_t GetMaxCiphertextByteSize(size_t frame_size) = 0;
    };
}

#endif
