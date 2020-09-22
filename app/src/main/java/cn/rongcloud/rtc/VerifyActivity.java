package cn.rongcloud.rtc;

import static cn.rongcloud.rtc.device.privatecloud.ServerUtils.getAppServer;
import static cn.rongcloud.rtc.util.UserUtils.CODE;
import static cn.rongcloud.rtc.util.UserUtils.KEY;
import static cn.rongcloud.rtc.util.UserUtils.PHONE;
import static cn.rongcloud.rtc.util.UserUtils.REGION;
import static cn.rongcloud.rtc.util.UserUtils.RESPONSE_OK;
import static cn.rongcloud.rtc.util.UserUtils.RESULT;
import static cn.rongcloud.rtc.util.UserUtils.TOKEN;
import static cn.rongcloud.rtc.util.UserUtils.USER_ID;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cn.rongcloud.rtc.base.RongRTCBaseActivity;
import cn.rongcloud.rtc.device.privatecloud.ServerUtils;
import cn.rongcloud.rtc.entity.CountryInfo;
import cn.rongcloud.rtc.util.http.HttpClient;
import cn.rongcloud.rtc.util.http.Request;
import cn.rongcloud.rtc.util.http.RequestMethod;
import cn.rongcloud.rtc.util.ButtentSolp;
import cn.rongcloud.rtc.util.DownTimer;
import cn.rongcloud.rtc.util.DownTimerListener;
import cn.rongcloud.rtc.util.SealErrorCode;
import cn.rongcloud.rtc.util.SessionManager;
import cn.rongcloud.rtc.util.UserUtils;
import cn.rongcloud.rtc.util.Utils;
import cn.rongcloud.rtc.utils.FinLog;
import com.google.gson.Gson;
import io.rong.imlib.common.DeviceUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class VerifyActivity extends RongRTCBaseActivity implements DownTimerListener {
    private static final int REQUEST_CODE_SELECT_COUNTRY = 1200;
    public static final String TAG = "VerifyActivity";
    private static final String _S = "s";
    private static DownTimer downTimer = new DownTimer();
    private TextView versionCodeView, tv_tips;
    private EditText edit_phone, edit_verificationCode;
    private Button btn_login, reg_getcode;
    private boolean isBright = true;
    private StringBuffer stringBuffer = new StringBuffer();
    private String mPhone = "";
    private TextView mTvCountry;

    private TextView mTvRegion;
    private CountryInfo mCountryInfo;
    private ImageView img_logo;
    private String userId = "";

    private View.OnClickListener onClickListener =
        new View.OnClickListener() {
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
                        if (mCountryInfo == null) {
                            showToast(R.string.select_country);
                            return;
                        }
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
                    case R.id.tv_country:
                        Intent intent =
                            new Intent(VerifyActivity.this, CountryListActivity.class);
                        startActivityForResult(intent, REQUEST_CODE_SELECT_COUNTRY);
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
        edit_phone.addTextChangedListener(
            new TextWatcher() {
                @Override
                public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() >= 1 && isBright) {
                        reg_getcode.setClickable(true);
                        reg_getcode.setBackgroundDrawable(
                            getResources().getDrawable(R.drawable.rs_select_btn_blue));
                    } else {
                        reg_getcode.setClickable(false);
                        reg_getcode.setBackgroundDrawable(
                            getResources().getDrawable(R.drawable.rs_select_btn_gray));
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        edit_verificationCode.addTextChangedListener(
            new TextWatcher() {
                @Override
                public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() > 0) {
                        btn_login.setClickable(true);
                        btn_login.setBackgroundDrawable(
                            getResources().getDrawable(R.drawable.rs_select_btn_blue));
                    } else {
                        btn_login.setClickable(false);
                        btn_login.setBackgroundDrawable(
                            getResources().getDrawable(R.drawable.rs_select_btn_gray));
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
    }

    private void initView() {
        btn_login = (Button) findViewById(R.id.btn_login);
        reg_getcode = (Button) findViewById(R.id.reg_getcode);
        btn_login.setOnClickListener(onClickListener);
        btn_login.setClickable(false);

        reg_getcode.setOnClickListener(onClickListener);
        edit_phone = (EditText) findViewById(R.id.edit_phone);
        String phone = SessionManager.getInstance().getString(UserUtils.PHONE);
        if (!TextUtils.isEmpty(phone)) {
            edit_phone.setText(phone);
            reg_getcode.setClickable(true);
            reg_getcode.setBackgroundDrawable(
                getResources().getDrawable(R.drawable.rs_select_btn_blue));
        }
        tv_tips = (TextView) findViewById(R.id.tv_tips);
        edit_verificationCode = (EditText) findViewById(R.id.edit_verificationCode);
        versionCodeView = (TextView) findViewById(R.id.main_page_version_code);
        versionCodeView.setText(
            getResources().getString(R.string.blink_description_version)
                + BuildConfig.VERSION_NAME
                + (BuildConfig.DEBUG ? "_Debug" : ""));
        versionCodeView.setTextColor(getResources().getColor(R.color.blink_text_green));
        mTvRegion = (TextView) findViewById(R.id.tv_region);
        mTvCountry = (TextView) findViewById(R.id.tv_country);
        mTvCountry.setOnClickListener(onClickListener);
        updateCountry();
        img_logo = (ImageView) findViewById(R.id.img_logo);
        if (img_logo != null) {
            if (ServerUtils.usePrivateCloud()) {
                img_logo.setImageResource(R.drawable.ic_launcher_privatecloud);
            } else {
                img_logo.setImageResource(R.drawable.ic_launcher);
            }
        }
    }

    @Override
    public void onTick(long millisUntilFinished) {
        stringBuffer.setLength(0);
        stringBuffer.append((millisUntilFinished / 1000));
        stringBuffer.append(_S);
        runOnUiThread(
            new Runnable() {
                @Override
                public void run() {
                    reg_getcode.setText(stringBuffer.toString());
                    reg_getcode.setClickable(false);
                    reg_getcode.setBackgroundDrawable(
                        getResources().getDrawable(R.drawable.rs_select_btn_gray));
                }
            });
        isBright = false;
    }

    @Override
    public void onFinish() {
        runOnUiThread(
            new Runnable() {
                @Override
                public void run() {
                    reg_getcode.setText(R.string.get_code);
                    reg_getcode.setClickable(true);
                    reg_getcode.setBackgroundDrawable(
                        getResources().getDrawable(R.drawable.rs_select_btn_blue));
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

    /** 发送手机验证码 */
    private void sendCode() {
        String json = "";
        try {
            mPhone = edit_phone.getText().toString().trim();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(UserUtils.PHONE, mPhone);
            jsonObject.put(REGION, mCountryInfo != null ? mCountryInfo.region : "86");
            json = jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(json)) {
            FinLog.e(TAG, "send code error .json null !");
            return;
        }
        Request.Builder request = new Request.Builder();
        request.url(UserUtils.getUrl(getAppServer(), UserUtils.URL_SEND_CODE));
        request.method(RequestMethod.POST);
        request.body(json);
        HttpClient.getDefault()
            .request(
                request.build(),
                new HttpClient.ResultCallback() {
                    @Override
                    public void onResponse(String result) {
                        FinLog.v(TAG, "send codo result result:" + result);
                        try {
                            String code = "";
                            JSONObject jsonObject = new JSONObject(result);
                            if (jsonObject.has(CODE)) {
                                code = String.valueOf(jsonObject.get(CODE));
                            }
                            if (!TextUtils.isEmpty(code) && code.equals(RESPONSE_OK)) {
                                toast(
                                    Utils.getContext()
                                        .getString(
                                            R.string.verify_code_sent_prompt));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int errorCode) {
                        FinLog.v(TAG, "send code error errorCode:" + errorCode);
                        toast(
                            Utils.getContext()
                                .getString(
                                    R.string.verify_code_sent_prompt_failed));
                        stopDown();
                    }
                });
    }

    /** 验证手机验证码是否有效 */
    private void verifyCode() {
        String json = "";
        LoadDialog.show(this);
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(PHONE, mPhone);
            jsonObject.put(REGION, mCountryInfo != null ? mCountryInfo.region : "86");
            jsonObject.put(CODE, edit_verificationCode.getText().toString().trim());
            jsonObject.put(KEY, getUserId());
            jsonObject.put("appkey",UserUtils.APP_KEY);
            json = jsonObject.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(json)) {
            FinLog.e(TAG, "verify code error. json null !");
            LoadDialog.dismiss(this);
            return;
        }
        Request.Builder request = new Request.Builder();
        request.url(UserUtils.getUrl(getAppServer(), UserUtils.URL_VERIFY_CODE));
        request.method(RequestMethod.POST);
        request.body(json);
        HttpClient.getDefault()
            .request(
                request.build(),
                new HttpClient.ResultCallback() {
                    /**
                     * code = 200; result = { token =
                     * "LVMVhKnIp2t8z83RYJujGsXZO74SCBcJ+lQ6rhLFlLuZ10eb7WRL7yYjdc741NMZl/y5hHuAH2G1GCGzMo7N6Bk9PuRTt4Il";
                     * };
                     *
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
                });
    }

    private String getUserId() {
        String deviceId = DeviceUtils.getDeviceId(Utils.getContext());
        int idLength = deviceId.length();
        userId =
            mPhone
                + "_"
                + (idLength > 4
                ? deviceId.substring(idLength - 4, idLength)
                : DeviceUtils.getDeviceId(Utils.getContext()))
                + "_and";
        return userId;
    }

    private void getToken(String result) {
        FinLog.v(TAG, "verify result result:" + result);
        try {
            int code = 0;
            JSONObject jsonObject = new JSONObject(result);
            if (jsonObject.has(CODE)) {
                code = jsonObject.getInt(CODE);
            }
            if (code == 200) {
                if (jsonObject.has(RESULT)) {
                    JSONObject jsonObjectResult = jsonObject.getJSONObject(RESULT);
                    if (jsonObjectResult.has(TOKEN)) {
                        String token = String.valueOf(jsonObjectResult.get(TOKEN));
                        SessionManager.getInstance().put(UserUtils.PHONE, mPhone);
                        SessionManager.getInstance().put(mPhone, token);
                        SessionManager.getInstance().put(USER_ID, userId);
                        String navi = jsonObjectResult.optString("navi");
                        // Server端已将 AppKey 与 IM Navi导航绑定
                        SessionManager.getInstance().put(UserUtils.APP_KEY, TextUtils.isEmpty(navi) ? UserUtils.NAV_SERVER : navi);
                        LoadDialog.dismiss(VerifyActivity.this);
                        toast(Utils.getContext().getString(R.string.verify_code_success));
                        finish();
                    }
                }
            } else {
                String promptMsg = Utils.getContext().getString(R.string.VerificationCodeError);
                if (code == SealErrorCode.INVALID_VERIFICATION_CODE.getValue()) {
                    promptMsg = Utils.getContext().getString(R.string.verify_code_invalid);
                } else if (code == SealErrorCode.VERIFICATION_CODE_EXPIRED.getValue()) {
                    promptMsg = Utils.getContext().getString(R.string.verify_code_expired);
                }
                toast(promptMsg);
                LoadDialog.dismiss(VerifyActivity.this);
                showTips();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            LoadDialog.dismiss(VerifyActivity.this);
        }
    }

    private void toast(final String msg) {
        runOnUiThread(
            new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(VerifyActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void showTips() {
        runOnUiThread(
            new Runnable() {
                @Override
                public void run() {
                    tv_tips.setText(getResources().getString(R.string.VerificationCodeError));
                    tv_tips.setVisibility(View.VISIBLE);
                }
            });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SELECT_COUNTRY:
                updateCountry();
                break;
        }
    }

    private void updateCountry() {
        String json = SessionManager.getInstance().getString(UserUtils.COUNTRY);
        if (TextUtils.isEmpty(json)) {
            mCountryInfo = CountryInfo.createDefault();
        } else {
            try {
                mCountryInfo = new Gson().fromJson(json, CountryInfo.class);
            } catch (Exception e) {
                mCountryInfo = CountryInfo.createDefault();
            }
        }
        mTvCountry.setText(
            getString(R.string.select_country_hint)
                + " "
                + (Utils.isZhLanguage() ? mCountryInfo.zh : mCountryInfo.en));
        mTvRegion.setText("+" + mCountryInfo.region);
    }
}
