package cn.rongcloud.rtc;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import cn.rongcloud.rtc.base.RongRTCBaseActivity;
import cn.rongcloud.rtc.callback.JoinRoomUICallBack;
import cn.rongcloud.rtc.callback.RongRTCResultUICallBack;
import cn.rongcloud.rtc.media.http.HttpClient;
import cn.rongcloud.rtc.media.http.Request;
import cn.rongcloud.rtc.media.http.RequestMethod;
import cn.rongcloud.rtc.room.RongRTCRoom;
import cn.rongcloud.rtc.util.ButtentSolp;
import cn.rongcloud.rtc.util.DownTimer;
import cn.rongcloud.rtc.util.DownTimerListener;
import cn.rongcloud.rtc.util.SessionManager;
import cn.rongcloud.rtc.util.UserUtils;
import cn.rongcloud.rtc.util.Utils;
import cn.rongcloud.rtc.utils.FinLog;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.common.DeviceUtils;

import static cn.rongcloud.rtc.util.UserUtils.*;

public class VerifyActivity extends RongRTCBaseActivity implements DownTimerListener {

    public static final String TAG = "VerifyActivity";
    private static final String _S = "s";
    private static DownTimer downTimer = new DownTimer();
    private TextView versionCodeView, tv_tips;
    private EditText edit_phone, edit_verificationCode;
    private Button btn_login, reg_getcode;
    private boolean isBright = true;
    private StringBuffer stringBuffer = new StringBuffer();
    private String mPhone = "";

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_login:
                    if (ButtentSolp.check(v.getId(), 500)) {
                        return;
                    }
                    mPhone = edit_phone.getText().toString().trim();
                    verifyCode();
                    break;
                case R.id.reg_getcode:
                    if (ButtentSolp.check(v.getId(), 500)) {
                        return;
                    }
                    if (null == downTimer) {
                        downTimer = new DownTimer();
                    }
                    downTimer.setListener(VerifyActivity.this);
                    downTimer.startDown(60 * 1000);
                    sendCode();
                    edit_verificationCode.setText("");
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);

        initView();
        addEditTextListener();
    }

    private void addEditTextListener() {
        edit_phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 11 && isBright) {
                    reg_getcode.setClickable(true);
                    reg_getcode.setBackgroundDrawable(getResources().getDrawable(R.drawable.rs_select_btn_blue));
                } else {
                    reg_getcode.setClickable(false);
                    reg_getcode.setBackgroundDrawable(getResources().getDrawable(R.drawable.rs_select_btn_gray));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        edit_verificationCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    btn_login.setClickable(true);
                    btn_login.setBackgroundDrawable(getResources().getDrawable(R.drawable.rs_select_btn_blue));
                } else {
                    btn_login.setClickable(false);
                    btn_login.setBackgroundDrawable(getResources().getDrawable(R.drawable.rs_select_btn_gray));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void initView() {
        btn_login = (Button) findViewById(R.id.btn_login);
        reg_getcode = (Button) findViewById(R.id.reg_getcode);
        btn_login.setOnClickListener(onClickListener);
        btn_login.setClickable(false);

        reg_getcode.setOnClickListener(onClickListener);
        edit_phone = (EditText) findViewById(R.id.edit_phone);
        String phone = SessionManager.getInstance(Utils.getContext()).getString(UserUtils.PHONE);
        if (!TextUtils.isEmpty(phone)) {
            edit_phone.setText(phone);
            reg_getcode.setClickable(true);
            reg_getcode.setBackgroundDrawable(getResources().getDrawable(R.drawable.rs_select_btn_blue));
        }
        tv_tips = (TextView) findViewById(R.id.tv_tips);
        edit_verificationCode = (EditText) findViewById(R.id.edit_verificationCode);
        versionCodeView = (TextView) findViewById(R.id.main_page_version_code);
        versionCodeView.setText(getResources().getString(R.string.blink_description_version) + BuildConfig.VERSION_NAME + (BuildConfig.DEBUG ? "_Debug" : ""));
        versionCodeView.setTextColor(getResources().getColor(R.color.blink_text_green));
    }

    @Override
    public void onTick(long millisUntilFinished) {
        stringBuffer.setLength(0);
        stringBuffer.append((millisUntilFinished / 1000));
        stringBuffer.append(_S);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                reg_getcode.setText(stringBuffer.toString());
                reg_getcode.setClickable(false);
                reg_getcode.setBackgroundDrawable(getResources().getDrawable(R.drawable.rs_select_btn_gray));
            }
        });
        isBright = false;
    }

    @Override
    public void onFinish() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                reg_getcode.setText(R.string.get_code);
                reg_getcode.setClickable(true);
                reg_getcode.setBackgroundDrawable(getResources().getDrawable(R.drawable.rs_select_btn_blue));
                isBright = true;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LoadDialog.dismiss(this);
        stopDown();
    }

    private void stopDown() {
        if (null != downTimer) {
            downTimer.stopDown();
            downTimer.finish();
        }
    }

    /**
     * 发送手机验证码
     */
    private void sendCode() {
        String json = "";
        try {
            mPhone = edit_phone.getText().toString().trim();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(UserUtils.PHONE, mPhone);
            jsonObject.put(REGION, "86");
            json = jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(json)) {
            FinLog.e(TAG, "send code error .json null !");
            return;
        }
        Request.Builder request = new Request.Builder();
        request.url(UserUtils.BASE_URL+UserUtils.URL_SEND_CODE);
        request.method(RequestMethod.POST);
        request.body(json);
        HttpClient.getDefault().request(request.build(), new HttpClient.ResultCallback() {
            @Override
            public void onResponse(String result) {
                FinLog.i(TAG, "send codo result result:" + result);
                try {
                    String code = "";
                    JSONObject jsonObject = new JSONObject(result);
                    if (jsonObject.has(CODE)) {
                        code = String.valueOf(jsonObject.get(CODE));
                    }
                    if (!TextUtils.isEmpty(code) && code.equals(RESPONSE_OK)) {
                        toast("验证码发送成功，请注意查收！");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int errorCode) {
                FinLog.i(TAG, "send code error errorCode:" + errorCode);
                toast("验证码获取失败，请重试！");
                stopDown();
            }

            @Override
            public void onError(IOException exception) {
                FinLog.i(TAG, "send code error :" + exception.getMessage());
                toast("验证码获取失败，请将检查网络连接！");
                stopDown();
            }
        });
    }

    /**
     * 验证手机验证码是否有效
     */
    private void verifyCode() {
        String json = "";
        LoadDialog.show(this);
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(PHONE, mPhone);
            jsonObject.put(REGION, "86");
            jsonObject.put(CODE, edit_verificationCode.getText().toString().trim());
            jsonObject.put(KEY, mPhone + DeviceUtils.getDeviceId(Utils.getContext()));
            json = jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(json)) {
            FinLog.e(TAG, "verify code error. json null !");
            LoadDialog.dismiss(this);
            return;
        }
        Request.Builder request = new Request.Builder();
        request.url(UserUtils.BASE_URL+UserUtils.URL_VERIFY_CODE);
        request.method(RequestMethod.POST);
        request.body(json);
        HttpClient.getDefault().request(request.build(), new HttpClient.ResultCallback() {
            /**
             *          code = 200;
             result =     {
             token = "LVMVhKnIp2t8z83RYJujGsXZO74SCBcJ+lQ6rhLFlLuZ10eb7WRL7yYjdc741NMZl/y5hHuAH2G1GCGzMo7N6Bk9PuRTt4Il";
             };
             * @param result
             */
            @Override
            public void onResponse(String result) {
                getToken(result);
            }

            @Override
            public void onFailure(int errorCode) {
                FinLog.e(TAG, "verify code failure. errorcode :" + errorCode);
                toast("验证失败：" + errorCode);
                LoadDialog.dismiss(VerifyActivity.this);
                showTips();
            }

            @Override
            public void onError(IOException exception) {
                FinLog.e(TAG, "verify code error .message:" + exception.getMessage());
                toast("验证失败，请将检查网络连接！");
                LoadDialog.dismiss(VerifyActivity.this);
                stopDown();
            }
        });
    }

    private void getToken(String result) {
        FinLog.i(TAG, "verify result result:" + result);
        try {
            String code = "";
            JSONObject jsonObject = new JSONObject(result);
            if (jsonObject.has(CODE)) {
                code = String.valueOf(jsonObject.get(CODE));
            }
            if (!TextUtils.isEmpty(code) && code.equals("200")) {
                if (jsonObject.has(RESULT)) {
                    JSONObject jsonObjectResult = jsonObject.getJSONObject(RESULT);
                    if (jsonObjectResult.has(TOKEN)) {
                        String token = String.valueOf(jsonObjectResult.get(TOKEN));
                        SessionManager.getInstance(Utils.getContext()).put(UserUtils.PHONE,mPhone);
                        SessionManager.getInstance(Utils.getContext()).put(mPhone, token);
                        LoadDialog.dismiss(VerifyActivity.this);
//                                connectRongIM(token);
                        toast("验证成功，请开始会议！");
                        finish();
                    }
                }
            } else {
                if (jsonObject.has("message")) {
                    toast(String.valueOf(jsonObject.get("message")));
                }
                LoadDialog.dismiss(VerifyActivity.this);
                showTips();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            LoadDialog.dismiss(VerifyActivity.this);
        }
    }

    /**
     * 连接 im
     *
     * @param token
     */
    private void connectRongIM(String token) {
        FinLog.i(TAG, "connectRongIM token ：" + token);
        RongIMClient.connect(token, new RongIMClient.ConnectCallback() {
            @Override
            public void onTokenIncorrect() {
                FinLog.e(TAG, "connectRongIM onTokenIncorrect");
                Toast.makeText(VerifyActivity.this, "验证失败，请重新获取！", Toast.LENGTH_SHORT).show();
                LoadDialog.dismiss(VerifyActivity.this);
            }

            @Override
            public void onSuccess(String s) {
                FinLog.e(TAG, "connectRongIM onSuccess s:" + s);
                connectToRoom();
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                FinLog.e(TAG, "connectRongIM errorCode:" + errorCode);
                LoadDialog.dismiss(VerifyActivity.this);
            }
        });
    }

    private void toast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(VerifyActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showTips() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_tips.setText(getResources().getString(R.string.VerificationCodeError));
                tv_tips.setVisibility(View.VISIBLE);
            }
        });
    }

    private void connectToRoom() {
        if (RongIMClient.getInstance().getCurrentConnectionStatus() == RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED) {
            final String roomId = SessionManager.getInstance(Utils.getContext()).getString(UserUtils.ROOMID_KEY);
            RongRTCEngine.getInstance().joinRoom(roomId, new JoinRoomUICallBack() {
                @Override
                protected void onUiSuccess(RongRTCRoom rongRTCRoom) {
                    LoadDialog.dismiss(VerifyActivity.this);
                    Toast.makeText(VerifyActivity.this, "加入房间成功", Toast.LENGTH_SHORT).show();
                    int userCount = rongRTCRoom.getRemoteUsers().size();

                    if (userCount >= OBSERVER_MUST) {
                        AlertDialog dialog = new AlertDialog.Builder(VerifyActivity.this)
                                .setMessage(getResources().getString(R.string.join_room_observer_prompt))
                                .setNegativeButton(getResources().getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        quitRoom(roomId);
                                        dialog.dismiss();
                                    }
                                })
                                .setPositiveButton(getResources().getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        startCallActivity(true, true, false, roomId);
                                    }
                                })
                                .create();
                        dialog.show();
                    } else if (userCount >= VIDEOMUTE_MUST) {
                        AlertDialog dialog = new AlertDialog.Builder(VerifyActivity.this)
                                .setMessage(getResources().getString(R.string.join_room_audio_only_prompt))
                                .setNegativeButton(getResources().getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        quitRoom(roomId);
                                        dialog.dismiss();
                                    }
                                })
                                .setPositiveButton(getResources().getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        startCallActivity(true, false, true, roomId);

                                    }
                                })
                                .create();
                        dialog.show();
                    } else {
                        boolean isVideoMute = SessionManager.getInstance(Utils.getContext()).getBoolean(UserUtils.isVideoMute_key);
                        boolean isObserver = SessionManager.getInstance(Utils.getContext()).getBoolean(UserUtils.isObserver_key);
                        startCallActivity(isVideoMute, isObserver, false, roomId);
                    }
                }

                @Override
                protected void onUiFailed(RTCErrorCode errorCode) {
                    LoadDialog.dismiss(VerifyActivity.this);
                    Toast.makeText(VerifyActivity.this, "加入房间失败", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            LoadDialog.dismiss(VerifyActivity.this);
            Toast.makeText(VerifyActivity.this, "IM 还未建立连接", Toast.LENGTH_SHORT).show();
        }
    }

    private void startCallActivity(boolean muteVideo, boolean observer, boolean canOnlyPublishAudio, String roomId) {
        Intent intent = new Intent(this, CallActivity.class);
        //加入房间之前 置为默认状态
        SessionManager.getInstance(Utils.getContext()).put("VideoModeKey", "smooth");
        //
        intent.putExtra(CallActivity.EXTRA_ROOMID, roomId);
        intent.putExtra(CallActivity.EXTRA_CAMERA, muteVideo);
        intent.putExtra(CallActivity.EXTRA_OBSERVER, observer);
        intent.putExtra(CallActivity.EXTRA_ONLY_PUBLISH_AUDIO, canOnlyPublishAudio);
        startActivity(intent);
        finish();
    }

    private void quitRoom(String roomId) {
        RongRTCEngine.getInstance().quitRoom(roomId, new RongRTCResultUICallBack() {

                    @Override
                    public void onUiSuccess() {

                    }

                    @Override
                    public void onUiFailed(RTCErrorCode errorCode) {

                    }
                }
        );
    }
}
