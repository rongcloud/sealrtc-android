package cn.rongcloud.rtc.whiteboard;

import android.text.TextUtils;
import cn.rongcloud.rtc.base.RTCErrorCode;
import cn.rongcloud.rtc.media.http.HttpClient;
import cn.rongcloud.rtc.media.http.Request;
import cn.rongcloud.rtc.media.http.RequestMethod;
import io.rong.common.RLog;
import org.json.JSONException;
import org.json.JSONObject;

public class WhiteBoardApi {
    private static final String TAG = "WhiteBoardApi";
    private static final String HERE_WHITE_URL = "https://cloudcapiv4.herewhite.com/";
    private static final String CREATE_ROOM = "room?token=%s";
    private static final String JOIN_ROOM = "room/join?uuid=%s&token=%s";
    private static final String DELETE_ROOM = "room/close?token=%s";
    // 参考服务提供商文档，token写在客户端是不安全的，你“可以”将 Mini Token
    // 写入业务服务器的代码中，或服务端的配置文件中。https://developer.herewhite.com/#/zh-CN/v2/concept
    private static final String MINI_TOKEN =
            "WHITEcGFydG5lcl9pZD02dFBKT1lzMG52MHFoQzN2Z1BRUXVmN0t0RnVOVGl0bzBhRFAmc2lnPTMyZTRiNTMwNjkyN2RhN2I3NzI4MjMwOTJlZTNmNDJhNWI3MGMyMjU6YWRtaW5JZD0yMTEmcm9sZT1taW5pJmV4cGlyZV90aW1lPTE1ODkzNzY1MjEmYWs9NnRQSk9ZczBudjBxaEMzdmdQUVF1ZjdLdEZ1TlRpdG8wYURQJmNyZWF0ZV90aW1lPTE1NTc4MTk1Njkmbm9uY2U9MTU1NzgxOTU2OTQyNTAw";
    public static final String WHITE_BOARD_KEY = "rongRTCWhite";
    public static final String WHITE_BOARD_SCENE_PATH = "/rtc";
    public static final String WHITE_BOARD_INIT_SCENE_PATH = "/init";

    public static void createRoom(String name, int limit, HttpClient.ResultCallback callback) {
        String json = "";
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", name);
            jsonObject.put("limit", limit);
            jsonObject.put("mode", "persistent");
            json = jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(json)) {
            RLog.e(TAG, "here white create room error .json null !");
            if (callback != null) {
                callback.onFailure(RTCErrorCode.JsonParseError.getValue());
            }
            return;
        }
        Request.Builder request = new Request.Builder();
        String token = String.format(CREATE_ROOM, MINI_TOKEN);
        request.url(HERE_WHITE_URL + token);
        request.method(RequestMethod.POST);
        request.body(json);
        HttpClient.getDefault().request(request.build(), callback);
    }

    // 通过uuid去white server获取roomToken，我们在rtc room里setRoomAttributeValue,目前此方法不需要调用
    public static void joinRoom(String uuid, HttpClient.ResultCallback callback) {
        String json = "";
        try {
            JSONObject jsonObject = new JSONObject("");
            json = jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(json)) {
            RLog.e(TAG, "here white join room error .json null !");
            if (callback != null) {
                callback.onFailure(RTCErrorCode.JsonParseError.getValue());
            }
            return;
        }
        Request.Builder request = new Request.Builder();
        String joinRoomInfo = String.format(JOIN_ROOM, uuid, MINI_TOKEN);
        request.url(HERE_WHITE_URL + joinRoomInfo);
        request.method(RequestMethod.POST);
        request.body(json);
        HttpClient.getDefault().request(request.build(), callback);
    }

    // 退出rtc room时我们会删除白板房间
    public static void deleteRoom(String uuid, HttpClient.ResultCallback callback) {
        String json = "";
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("uuid", uuid);
            json = jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(json)) {
            RLog.e(TAG, "here white join room error .json null !");
            if (callback != null) {
                callback.onFailure(RTCErrorCode.JsonParseError.getValue());
            }
            return;
        }
        Request.Builder request = new Request.Builder();
        String joinRoomInfo = String.format(DELETE_ROOM, MINI_TOKEN);
        request.url(HERE_WHITE_URL + joinRoomInfo);
        request.method(RequestMethod.POST);
        request.body(json);
        HttpClient.getDefault().request(request.build(), callback);
    }
}
