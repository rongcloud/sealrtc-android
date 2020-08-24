#ifndef API_CRYPTO_CUSTOM_FRAME_DECRYPTOR_INTERFACE_H_
#define API_CRYPTO_CUSTOM_FRAME_DECRYPTOR_INTERFACE_H_

#include <stddef.h>
#include <stdint.h>

namespace webrtc {
    class CustomFrameDecryptorInterface {
    public:
        virtual ~CustomFrameDecryptorInterface() {}

      /**
       开发者定义解密方法
       @param  encrypted_frame 解密前的数据起始地址
       @param  encrypted_frame_size 解密前的数据大小
       @param  frame 解密后的数据起始地址，融云SDK已申请内存，开发者无需重新申请
       @param  bytes_written 解密后数据的大小
       @return  0: 成功,非0: 失败。
      **/
        virtual int Decrypt(const uint8_t *encrypted_frame, size_t encrypted_frame_size,
                            uint8_t *frame, size_t *bytes_written) = 0;

      /**
       *计算解密后数据的长度
       @param frame_size　密文大小
       @return size_t 明文长度
       **/
      virtual size_t GetMaxPlaintextByteSize(size_t frame_size) = 0;

    };
}

#endif
