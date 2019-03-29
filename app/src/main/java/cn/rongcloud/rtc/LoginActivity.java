package cn.rongcloud.rtc;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.rtc.media.http.HttpClient;
import cn.rongcloud.rtc.media.http.Request;
import cn.rongcloud.rtc.media.http.RequestMethod;
import cn.rongcloud.rtc.util.AMUtils;
import cn.rongcloud.rtc.util.NToast;
import io.rong.imlib.RongIMClient;

/**
 * Created by AMing on 16/1/15.
 * Company RongCloud
 */
public class LoginActivity extends Activity implements View.OnClickListener {

    private final static String TAG = "LoginActivity";
    private static final String BASE_URL = "http://apiqa.rongcloud.net/";
    private static final String URL_LOGIN = "user/login";
    private static final String URL_GET_TOKEN = "user/get_token";
    private static final String URL_GET_TOKEN_NEW = "user/get_token_new";

    private ImageView mImg_Background;
    private ClearWriteEditText mPhoneEdit, mPasswordEdit, mRoomEdit;
    private String phoneString;
    private String passwordString;
    private String tokenIndex;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private Context mContext = null;
    List<String> unGrantedPermissions;
    private static final String[] MANDATORY_PERMISSIONS = {
            "android.permission.MODIFY_AUDIO_SETTINGS",
            "android.permission.RECORD_AUDIO",
            "android.permission.INTERNET",
            "android.permission.CAMERA",
            "android.permission.READ_PHONE_STATE",
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            "android.permission.BLUETOOTH_ADMIN",
            "android.permission.BLUETOOTH"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
        mContext = this;
        setContentView(R.layout.activity_login);
        sp = getSharedPreferences("config", MODE_PRIVATE);
        editor = sp.edit();
        initView();
        checkPermissions();
    }

    private void initView() {
        mPhoneEdit = (ClearWriteEditText) findViewById(R.id.de_login_phone);
        mPasswordEdit = (ClearWriteEditText) findViewById(R.id.de_login_password);
        mRoomEdit = (ClearWriteEditText) findViewById(R.id.de_login_room);
        Button mConfirm = (Button) findViewById(R.id.de_login_sign);
        mConfirm.setOnClickListener(this);
        mImg_Background = (ImageView) findViewById(R.id.de_img_backgroud);
        mPhoneEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 11) {
                    AMUtils.onInactive(mContext, mPhoneEdit);
                }
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("token", "");
                editor.commit();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        String oldPhone = sp.getString("loginphone", "");
        String oldPassword = sp.getString("loginpassword", "");
        String oldRoom = sp.getString("roomId", "");

        if (!TextUtils.isEmpty(oldPhone) ) {
            mPhoneEdit.setText(oldPhone);
            //mPasswordEdit.setText(oldPassword);
            mRoomEdit.setText(oldRoom);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.de_login_sign:
                phoneString = mPhoneEdit.getText().toString().trim();
                //passwordString = mPasswordEdit.getText().toString().trim();

                if (TextUtils.isEmpty(phoneString)) {
                    NToast.shortToast(mContext, "用户名不能为空");
                    mPhoneEdit.setShakeAnimation();
                    return;
                } else if (phoneString.matches("[^0-9|a-z|A-Z]*")) {
                    NToast.shortToast(mContext, "用户名只能为字母和数字组合");
                    mPhoneEdit.setShakeAnimation();
                    return;
                }

               /* if (TextUtils.isEmpty(passwordString)) {
                    NToast.shortToast(mContext, "密码不能为空");
                    mPasswordEdit.setShakeAnimation();
                    return;
                }
                if (passwordString.contains(" ")) {
                    NToast.shortToast(mContext, "密码不能包含空格");
                    mPasswordEdit.setShakeAnimation();
                    return;
                }*/
                LoadDialog.show(mContext);
                editor.putBoolean("exit", false);
                editor.commit();
                connect();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void connect() {
        String token = sp.getString("token", "");
        if (!TextUtils.isEmpty(token)) {
            RongIMClient.connect(token, new RongIMClient.ConnectCallback() {
                @Override
                public void onTokenIncorrect() {
                    login();
                }

                @Override
                public void onSuccess(String s) {
                    LoadDialog.dismiss(mContext);
                    final String roomId = mRoomEdit.getText().toString().trim();
                    loginSuccess(roomId);
                }

                @Override
                public void onError(RongIMClient.ErrorCode errorCode) {
                    login();
                }
            });
        } else {
            //login();
            getTokenNew();
        }
    }
    private void getTokenNew(){
        JSONObject loginInfo = new JSONObject();
        try {
            loginInfo.put("id", phoneString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Request request = new Request.Builder()
                .url(BASE_URL + URL_GET_TOKEN_NEW)
                .method(RequestMethod.POST)
                .body(loginInfo.toString())
                .build();
        HttpClient.getDefault().request(request, new HttpClient.ResultCallback() {
            @Override
            public void onResponse(String result) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    int code = jsonObject.getInt("code");
                    if (code == 200) {
                        final String roomId = mRoomEdit.getText().toString().trim();
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("loginphone", phoneString);
                        editor.putString("roomId", roomId);
                        editor.commit();
                        jsonObject = jsonObject.getJSONObject("result");
                        final String token = jsonObject.getString("token");
                        RongIMClient.connect(token, new RongIMClient.ConnectCallback() {
                            @Override
                            public void onTokenIncorrect() {

                            }

                            @Override
                            public void onSuccess(String s) {
                                LoadDialog.dismiss(mContext);
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putString("token", token);
                                editor.commit();
                                loginSuccess(roomId);
                            }

                            @Override
                            public void onError(RongIMClient.ErrorCode errorCode) {

                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int errorCode) {

            }

            @Override
            public void onError(IOException exception) {

            }
        });
    }

    private void login() {
        JSONObject loginInfo = new JSONObject();
        try {
            loginInfo.put("password", passwordString);
            loginInfo.put("phone", phoneString);
            loginInfo.put("region", "86");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Request request = new Request.Builder()
                .url(BASE_URL + URL_LOGIN)
                .method(RequestMethod.POST)
                .body(loginInfo.toString())
                .build();
        HttpClient.getDefault().request(request, new HttpClient.ResultCallback() {
            @Override
            public void onResponse(String result) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    int code = jsonObject.getInt("code");
                    if (code == 200) {
                        final String roomId = mRoomEdit.getText().toString().trim();
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("loginphone", phoneString);
                        editor.putString("loginpassword", passwordString);
                        editor.putString("roomId", roomId);
                        editor.commit();
                        jsonObject = jsonObject.getJSONObject("result");
                        final String token = jsonObject.getString("token");
                        RongIMClient.connect(token, new RongIMClient.ConnectCallback() {
                            @Override
                            public void onTokenIncorrect() {
                                reGetToken();
                            }

                            @Override
                            public void onSuccess(String s) {
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putString("token", token);
                                editor.commit();
                                LoadDialog.dismiss(mContext);
                                loginSuccess(roomId);
                            }

                            @Override
                            public void onError(RongIMClient.ErrorCode errorCode) {

                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int errorCode) {

            }

            @Override
            public void onError(IOException exception) {

            }
        });
    }

    private void loginSuccess(String roomId) {
        Intent intent = new Intent(LoginActivity.this, MainPageActivity.class);
        startActivity(intent);
    }

    private void reGetToken() {
        Request request = new Request.Builder()
                .url(BASE_URL + URL_GET_TOKEN)
                .method(RequestMethod.GET)
                .build();
        HttpClient.getDefault().request(request, new HttpClient.ResultCallback() {
            @Override
            public void onResponse(String result) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    int code = jsonObject.getInt("code");
                    if (code == 200) {
                        final String roomId = mRoomEdit.getText().toString().trim();
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("loginphone", phoneString);
                        editor.putString("loginpassword", passwordString);
                        editor.putString("roomId", roomId);
                        editor.commit();
                        jsonObject = jsonObject.getJSONObject("result");
                        final String token = jsonObject.getString("token");
                        RongIMClient.connect(token, new RongIMClient.ConnectCallback() {
                            @Override
                            public void onTokenIncorrect() {

                            }

                            @Override
                            public void onSuccess(String s) {
                                LoadDialog.dismiss(mContext);
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putString("token", token);
                                editor.commit();
                                loginSuccess(roomId);
                            }

                            @Override
                            public void onError(RongIMClient.ErrorCode errorCode) {

                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int errorCode) {

            }

            @Override
            public void onError(IOException exception) {

            }
        });
    }

    private void checkPermissions() {
        unGrantedPermissions = new ArrayList();
        for (String permission : MANDATORY_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                unGrantedPermissions.add(permission);
            }
        }
        if (unGrantedPermissions.size() == 0) {//已经获得了所有权限，开始加入聊天室
            initSDK();
        } else {//部分权限未获得，重新请求获取权限
            String[] array = new String[unGrantedPermissions.size()];
            ActivityCompat.requestPermissions(this, unGrantedPermissions.toArray(array), 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        unGrantedPermissions.clear();
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED)
                unGrantedPermissions.add(permissions[i]);
        }
        if (unGrantedPermissions.size() > 0) {
            for (String permission : unGrantedPermissions) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    Toast.makeText(this, getString(R.string.PermissionStr) + permission + getString(R.string.plsopenit), Toast.LENGTH_SHORT).show();
                    finish();
                } else ActivityCompat.requestPermissions(this, new String[]{permission}, 0);
            }
        } else {
            initSDK();
        }

    }

    private void initSDK() {
        RongIMClient.setServerInfo("http://navqa.cn.ronghub.com", "http://navxq.rongcloud.net");
        RongIMClient.init(getApplication(), "c9kqb3rdkbb8j", false);
    }
}
