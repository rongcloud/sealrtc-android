package cn.rongcloud.rtc;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import cn.rongcloud.rtc.api.RCRTCEngine;
import cn.rongcloud.rtc.api.RCRTCRemoteUser;
import cn.rongcloud.rtc.api.callback.IRCRTCResultCallback;
import cn.rongcloud.rtc.api.report.StatusBean;
import cn.rongcloud.rtc.api.report.StatusReport;
import cn.rongcloud.rtc.api.stream.RCRTCInputStream;
import cn.rongcloud.rtc.api.stream.RCRTCVideoInputStream;
import cn.rongcloud.rtc.base.RCRTCMediaType;
import cn.rongcloud.rtc.base.RCRTCStream;
import cn.rongcloud.rtc.base.RCRTCStreamType;
import cn.rongcloud.rtc.base.RTCErrorCode;
import cn.rongcloud.rtc.utils.FinLog;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class TestActivityAdapter extends BaseAdapter {

    private final String TAG = TestActivityAdapter.class.getSimpleName();
    private final String resourceVideo = "视频";
    private final String resourceAudio = "音频";
    private final String resourceCustom = "自定义";
    private final String subscribeString = "订阅";
    private final String unsubscribeString = "取消订阅";
    private final String subscribeNormalString = "切大流";
    private final String subscribeTinyString = "切小流";
    private ConcurrentHashMap<String, View> viewMap = new ConcurrentHashMap<String, View>();
    private List<RCRTCRemoteUser> remoteUsers;
    private Context context;
    private OnSubscribeListener onSubscribeListener;
    private int[] subscribe_ids = {R.id.test_item_resource_subscribe_audio, R.id.test_item_resource_subscribe_video, R.id.test_item_resource_subscribe_custom};
    private StatusReport statusReport;

    public TestActivityAdapter(Context context, List<RCRTCRemoteUser> remoteUsers, OnSubscribeListener onSubscribeListener) {
        this.context = context;
        this.remoteUsers = remoteUsers;
        this.onSubscribeListener = onSubscribeListener;
    }

    public void updateData(List<RCRTCRemoteUser> remoteUsers) {
        this.remoteUsers = remoteUsers;
    }

    public void updateStatusReport(StatusReport report) {
        try {
            this.statusReport = report;
            Object[] statusVideoRcvs = statusReport.statusVideoRcvs.values().toArray();
            for (int i = 0; i < statusVideoRcvs.length; i++) {
                final StatusBean statusBean = (StatusBean) statusVideoRcvs[i];
                if (!viewMap.containsKey(statusBean.uid)) {
                    break;
                }
                RCRTCRemoteUser remoteUser = null;
                for (RCRTCRemoteUser remoteU : remoteUsers) {
                    if (remoteU.getUserId().equals(statusBean.uid)) {
                        remoteUser = remoteU;
                        break;
                    }
                }
                if (remoteUser.getStreams() == null || remoteUser.getStreams().size() == 0) {
                    break;
                }
                String tag = "";
                for (RCRTCInputStream inputStream : remoteUser.getStreams()) {
                    if (statusBean.id.equals(inputStream.getStreamId()) && inputStream.getMediaType().getDescription().equals(statusBean.mediaType)) {
                        tag = inputStream.getTag();
                        break;
                    }
                }

                final String finalTag = tag;
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (finalTag.equals(RCRTCStream.RONG_TAG)) {
                            // 默认视频
                            ((TextView) viewMap.get(statusBean.uid).findViewById(R.id.test_item_resource_bite_video)).setText(
                                String.valueOf(statusBean.bitRate) + "   " + statusBean.frameHeight + "x" + statusBean.frameWidth);
                        } else {
                            // 自定义视频
                            ((TextView) viewMap.get(statusBean.uid).findViewById(R.id.test_item_resource_bite_custom)).setText(
                                String.valueOf(statusBean.bitRate) + "   " + statusBean.frameHeight + "x" + statusBean.frameWidth);
                        }
                    }
                };
                runOnUiThread(runnable);
            }
            Object[] statusAudioRcvs = statusReport.statusAudioRcvs.values().toArray();
            for (int i = 0; i < statusAudioRcvs.length; i++) {
                final StatusBean statusBean = (StatusBean) statusAudioRcvs[i];
                if (!viewMap.containsKey(statusBean.uid)) {
                    break;
                }
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        ((TextView) viewMap.get(statusBean.uid).findViewById(R.id.test_item_resource_bite_audio)).setText(String.valueOf(statusBean.bitRate));
                    }
                };
                runOnUiThread(runnable);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void runOnUiThread(Runnable runnable) {
        if (context != null) {
            ((Activity) context).runOnUiThread(runnable);
        }
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return remoteUsers == null || remoteUsers.size() == 0 ? 0 : remoteUsers.size();
    }

    @Override
    public Object getItem(int i) {
        return remoteUsers == null ? null : remoteUsers.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (remoteUsers == null) {
            return null;
        }
        final RCRTCRemoteUser remoteUser = (RCRTCRemoteUser) getItem(i);
        final View layoutView = LayoutInflater.from(context).inflate(R.layout.activity_test_item, null);
        viewMap.put(remoteUser.getUserId(), layoutView);
        ((TextView) layoutView.findViewById(R.id.test_item_id)).setText(remoteUser.getUserId());
        List<RCRTCInputStream> streamList = remoteUser.getStreams();
        for (final RCRTCInputStream inputStream : streamList) {
            final int subscribeIdIndex;
            // 设置资源类型
            if (inputStream.getMediaType() == RCRTCMediaType.VIDEO) {
                if (inputStream.getTag().equals(RCRTCStream.RONG_TAG)) {
                    ((TextView) layoutView.findViewById(R.id.test_item_resource_video)).setText(resourceVideo);
                    layoutView.findViewById(R.id.test_item_resource_video_container).setVisibility(View.VISIBLE);
                    subscribeIdIndex = 1;
                    //切换小流
                    layoutView.findViewById(R.id.test_item_resource_subscribe_video_tiny).setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(final View view) {
                            if (!((TextView) layoutView.findViewById(R.id.test_item_resource_subscribe_video)).getText().toString().equals(unsubscribeString)) {
                                FinLog.e(TAG, "还未订阅视频前，不允许切换大小流");
                                return;
                            }
                            if (((TextView) layoutView.findViewById(R.id.test_item_resource_subscribe_video_tiny)).getText().toString().equals(subscribeNormalString)) {
                                remoteUser.switchToNormalStream(new IRCRTCResultCallback() {
                                    @Override
                                    public void onSuccess() {
                                        ((TextView) layoutView.findViewById(R.id.test_item_resource_subscribe_video_tiny)).setText(subscribeTinyString);
                                    }

                                    @Override
                                    public void onFailed(RTCErrorCode errorCode) {

                                    }
                                });
                            } else {
                                remoteUser.switchToTinyStream(new IRCRTCResultCallback() {
                                    @Override
                                    public void onSuccess() {
                                        ((TextView) layoutView.findViewById(R.id.test_item_resource_subscribe_video_tiny)).setText(subscribeNormalString);
                                    }

                                    @Override
                                    public void onFailed(RTCErrorCode errorCode) {

                                    }
                                });
                            }
                        }
                    });
                } else {
                    ((TextView) layoutView.findViewById(R.id.test_item_resource_custom)).setText(resourceCustom);
                    layoutView.findViewById(R.id.test_item_resource_custom_container).setVisibility(View.VISIBLE);
                    subscribeIdIndex = 2;
                }
            } else {
                layoutView.findViewById(R.id.test_item_resource_audio_container).setVisibility(View.VISIBLE);
                ((TextView) layoutView.findViewById(R.id.test_item_resource_audio)).setText(resourceAudio);
                subscribeIdIndex = 0;
            }
            final String subscribeDes = subscribeString;
            // 设置订阅状态
            ((TextView) layoutView.findViewById(subscribe_ids[subscribeIdIndex])).setText(subscribeDes);
            ((TextView) layoutView.findViewById(subscribe_ids[subscribeIdIndex])).setTag(subscribeDes);
            ((TextView) layoutView.findViewById(subscribe_ids[subscribeIdIndex])).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View view) {
                    if (view.getTag().equals(subscribeString)) {
                        final String userId = remoteUser.getUserId();
                        RCRTCEngine.getInstance().getRoom().getLocalUser().subscribeStream(inputStream, new IRCRTCResultCallback() {
                            @Override
                            public void onSuccess() {
                                ((TextView) view).setText(unsubscribeString);
                                view.setTag(unsubscribeString);
                                onSubscribeListener.onSubscribe(userId, inputStream);
                            }

                            @Override
                            public void onFailed(RTCErrorCode errorCode) {
                            }
                        });
                    } else if (view.getTag().equals(unsubscribeString)) {
                        final String userId = remoteUser.getUserId();
                        RCRTCEngine.getInstance().getRoom().getLocalUser().unsubscribeStream(inputStream, new IRCRTCResultCallback() {
                            @Override
                            public void onSuccess() {
                                ((TextView) view).setText(subscribeString);
                                view.setTag(subscribeString);
                                onSubscribeListener.onUnsubscribe(userId, inputStream);
                                if (subscribeIdIndex == 1) {
                                    ((TextView) layoutView.findViewById(R.id.test_item_resource_subscribe_video_tiny)).setText(subscribeNormalString);
                                }
                            }

                            @Override
                            public void onFailed(RTCErrorCode errorCode) {
                            }
                        });
                    }
                }
            });
        }
        return layoutView;
    }


}
