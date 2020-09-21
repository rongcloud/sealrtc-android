package cn.rongcloud.rtc;

import cn.rongcloud.rtc.media.http.HttpClient;
import cn.rongcloud.rtc.media.http.Request;
import cn.rongcloud.rtc.media.http.RequestMethod;
import cn.rongcloud.rtc.utils.FinLog;
import org.json.JSONObject;

/** http://10.12.8.82:8888/ */
public class LiveDataOperator {
    private static final LiveDataOperator instance = new LiveDataOperator();
    private final String address = "https://imqa.rongcloud.net/seallive-online-app-server/";
    private final String QUERY = "query";
    private final String PUBLISH = "publish";
    private final String UNPUBLISH = "unpublish";
    private static final String TAG = "LiveDataOperator";
    public static final String LIVE_URL = "mcuUrl";
    public static final String ROOM_ID = "roomId";
    public static final String ROOM_NAME = "roomName";
    public static final String PUB_ID = "pubUserId";

    public static LiveDataOperator getInstance() {
        return instance;
    }

    public void query(final OnResultCallBack onResultCallBack) {
        Request request =
                new Request.Builder()
                        .url(address + QUERY)
                        .method(RequestMethod.POST)
                        .body(new JSONObject().toString())
                        .build();
        HttpClient.getDefault()
                .request(
                        request,
                        new HttpClient.ResultCallback() {
                            @Override
                            public void onResponse(String result) {
                                FinLog.d(TAG, "query result:: " + result);
                                if (onResultCallBack != null) onResultCallBack.onSuccess(result);
                            }

                            @Override
                            public void onFailure(int errorCode) {
                                FinLog.e(TAG, "query errorCode:: " + errorCode);
                                if (onResultCallBack != null)
                                    onResultCallBack.onFailed(String.valueOf(errorCode));
                            }
                        });
    }

    public void publish(String data, final OnResultCallBack onResultCallBack) {
        Request request =
                new Request.Builder()
                        .url(address + PUBLISH)
                        .method(RequestMethod.POST)
                        .body(data)
                        .build();
        HttpClient.getDefault()
                .request(
                        request,
                        new HttpClient.ResultCallback() {
                            @Override
                            public void onResponse(String result) {
                                if (onResultCallBack != null) onResultCallBack.onSuccess(result);
                                FinLog.d(TAG, "publish result:: " + result);
                            }

                            @Override
                            public void onFailure(int errorCode) {
                                FinLog.e(TAG, "publish errorCode:: " + errorCode);
                                if (onResultCallBack != null)
                                    onResultCallBack.onFailed(String.valueOf(errorCode));
                            }
                        });
    }

    public void unpublish(String data, final OnResultCallBack onResultCallBack) {
        Request request =
                new Request.Builder()
                        .url(address + UNPUBLISH)
                        .method(RequestMethod.POST)
                        .body(data)
                        .build();
        HttpClient.getDefault()
                .request(
                        request,
                        new HttpClient.ResultCallback() {
                            @Override
                            public void onResponse(String result) {
                                FinLog.d(TAG, "unpublish result:: " + result);
                                if (onResultCallBack != null) onResultCallBack.onSuccess(result);
                            }

                            @Override
                            public void onFailure(int errorCode) {
                                FinLog.e(TAG, "unpublish errorCode:: " + errorCode);
                                if (onResultCallBack != null)
                                    onResultCallBack.onFailed(String.valueOf(errorCode));
                            }
                        });
    }

    public interface OnResultCallBack {
        void onSuccess(String result);

        void onFailed(String error);
    }
}
