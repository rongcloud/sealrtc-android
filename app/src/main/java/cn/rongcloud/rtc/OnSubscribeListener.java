package cn.rongcloud.rtc;

import cn.rongcloud.rtc.api.stream.RCRTCInputStream;

public interface OnSubscribeListener {

    void onSubscribe(String userId, RCRTCInputStream inputStream);

    void onUnsubscribe(String userId, RCRTCInputStream inputStream);
}
