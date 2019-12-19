//
// Created by qinxue on 2019/5/22.
//


#include "cn_rongcloud_rtc_resampler_AudioResampler.h"

AudioResampler *resampler = NULL;

AVSampleFormat getAVSampleFormat(jint type);

JNIEXPORT AVSampleFormat getAVSampleFormat(jint type) {
    if (type == 0) {
        return AV_SAMPLE_FMT_U8;
    } else if (type == 1) {
        return AV_SAMPLE_FMT_S16;
    };
}

void JNICALL Java_cn_rongcloud_rtc_resampler_AudioResampler_n_1initAudioResamper
        (JNIEnv *, jobject o, jint int_type, jint in_rate, jint in_ch_num, jint out_type,
         jint out_rate, jint out_ch_num) {
    av_register_all();
    AVSampleFormat input_fmt = getAVSampleFormat(int_type);
    AVSampleFormat output_fmt = getAVSampleFormat(out_type);
    resampler = new AudioResampler(input_fmt, in_rate, in_ch_num, output_fmt, out_rate, out_ch_num);
}

JNIEXPORT jbyteArray JNICALL Java_cn_rongcloud_rtc_resampler_AudioResampler_n_1resample
        (JNIEnv *env, jobject o, jbyteArray bytes,jint size) {
    jbyte *pbyte = env->GetByteArrayElements(bytes, 0);
    uint8_t *out_temp = new uint8_t[size*4];

    int out_size = 0;
    if (resampler != NULL) {
        out_size = resampler->Resample(reinterpret_cast<void **>(&pbyte), size,
                                       reinterpret_cast<void **>(&out_temp));
    }
    LOGI("n_1resample %d:",out_size);
    jbyteArray jbarray = env->NewByteArray(out_size);
    env->SetByteArrayRegion(jbarray, 0, out_size, reinterpret_cast<const jbyte *>(out_temp));
    env->ReleaseByteArrayElements(bytes, pbyte, 0);
    return jbarray;
}

extern "C"
JNIEXPORT void JNICALL
Java_cn_rongcloud_rtc_resampler_AudioResampler_release(JNIEnv *env, jobject instance) {
    resampler = NULL;
}