package cn.rongcloud.rtc;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import cn.rongcloud.rtc.base.RongRTCBaseActivity;

import cn.rongcloud.rtc.media.http.HttpClient;
import cn.rongcloud.rtc.media.http.Request;
import cn.rongcloud.rtc.media.http.RequestMethod;
import cn.rongcloud.rtc.util.AppRTCUtils;
import cn.rongcloud.rtc.utils.FinLog;
import cn.rongcloud.rtc.entity.CMPAddress;
import cn.rongcloud.rtc.util.ButtentSolp;
import cn.rongcloud.rtc.util.SessionManager;
import cn.rongcloud.rtc.util.Utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by suancai on 2016/11/4.
 */

public class SettingActivity extends RongRTCBaseActivity {

    private LinearLayout testSettingOptionsContainer;

    private LinearLayout backButton;
    private static final String TAG = "SettingActivitytag";

    public static final String FPS = "FPS";
    public static final String CONNECTION_MODE = "CONNECTION_MODE";
    public static final String RESOLUTION = "RESOLUTION";
    public static final String CODECS = "CODECS";
    public static final String BIT_RATE_MIN = "BIT_RATE_MIN";
    public static final String BIT_RATE_MAX = "BIT_RATE_MAX";
    public static final String IS_OBSERVER = "IS_OBSERVER";
    public static final String IS_GPUIMAGEFILTER = "IS_GPUIMAGEFILTER";
    public static final String IS_SRTP = "IS_SRTP";
    public static final String IS_RONGRTC_CONNECTIONMODE = "IS_RONGRTC_CONNECTIONMODE";
    public static final String MEDIA_URL = "media_url";
    /**保存的bool值 true：大小流开启，false 关闭**/
    public static final String IS_STREAM_TINY="STREAMTINY";
    /**自动化测试*/
    public static final String IS_AUTO_TEST="AUTOTEST";
    /**水印*/
    public static final String IS_WATER="show_water_mark";


    public static final String RESOLUTION_LOW = "240x320";
    public static final String RESOLUTION_MEDIUM = "480x640";
    public static final String RESOLUTION_HIGH = "720x1280";
    public static final String RESOLUTION_SUPER = "1080x1920(仅部分手机支持)";
    private String[] list_resolution = new String[]{RESOLUTION_LOW, RESOLUTION_MEDIUM, RESOLUTION_HIGH};
    private String[] list_fps = new String[]{"15", "24", "30"};
    private String[] list_bitrate_max = new String[]{};
    private String[] list_bitrate_min = new String[]{};
    private String[] list_connectionMode = new String[]{"Relay", "P2P"};
    private String[] list_format = new String[]{"H264", "VP8", "VP9"};
    private String[] list_observer,list_gpuImageFilter,list_connectionType,list_streamTiny,list_autotest,list_water;

    private int defaultBitrateMinIndex = 0;
    private int defaultBitrateMaxIndex = 0;

    private static final int REQUEST_CODE_RESOLUTION = 12;
    private static final int REQUEST_CODE_FPS = 13;
    private static final int REQUEST_CODE_BITRATE_MAX = 14;
    private static final int REQUEST_CODE_MODE = 15;
    private static final int REQUEST_CODE_FORMAT = 16;
    private static final int REQUEST_CODE_BITRATE_MIN = 17;
    private static final int REQUEST_CODE_IS_OBSERVER = 18;
    private static final int REQUEST_CODE_IS_GPUIMAGEFILTER = 19;
    private static final int REQUEST_CODE_IS_SRTP = 20;
    private static final int REQUEST_CODE_IS_CONNECTIONTYPE = 21;
    private static final int REQUEST_CODE_IS_STREAM_TINY = 22;
    private static final int REQUEST_CODE_IS_AUTO_TEST = 23;
    private static final int REQUEST_CODE_IS_WATER = 24;

    private int tapStep = 0;
    private long lastClickTime = 0;
    private TextView settingOptionText1, settingOptionText2, settingOptionText3, settingOptionText4, settingOptionText5,
            settingOptionText6, settingOptionText7, settingOptionText8, settingOptionSRTP, settingOptionConnectionType,
            setting_option_streamTiny,setting_autotest,setting_water, settingOptionMediaUrl;
    private LinearLayout settings_Modify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        initViews();

        setupListeners();

    }

    private void initViews() {
        settings_Modify = (LinearLayout) findViewById(R.id.settings_Modify);

        list_observer = new String[]{getResources().getString(R.string.settings_text_observer_no), getResources().getString(R.string.settings_text_observer_yes)};
        list_gpuImageFilter = new String[]{getResources().getString(R.string.settings_text_gpufliter_no), getResources().getString(R.string.settings_text_gpufliter_yes)};
        list_connectionType = new String[]{getResources().getString(R.string.settings_text_connectionType_quic), getResources().getString(R.string.settings_text_connectionType_tcp)};

        list_streamTiny= new String[]{getResources().getString(R.string.settings_text_MediaStreamTiny_no), getResources().getString(R.string.settings_text_MediaStreamTiny_yes)};
        setting_option_streamTiny= (TextView) findViewById(R.id.setting_option_10_txt);
        //true：大小流开启，false 关闭(默认)
        boolean streamTiny=SessionManager.getInstance(this).getIsSupportTiny(IS_STREAM_TINY);
        setting_option_streamTiny.setText(streamTiny?list_streamTiny[1]:list_streamTiny[0]);

        //默认关闭自动化测试
        list_autotest= new String[]{getResources().getString(R.string.settings_text_MediaStreamTiny_no), getResources().getString(R.string.settings_text_MediaStreamTiny_yes)};
        setting_autotest = (TextView) findViewById(R.id.tv_setting_option_autotest);
        setting_autotest.setText(SessionManager.getInstance(this).getBoolean(IS_AUTO_TEST) ? list_streamTiny[1] : list_streamTiny[0]);

        list_water= new String[]{getResources().getString(R.string.settings_text_MediaStreamTiny_no), getResources().getString(R.string.settings_text_MediaStreamTiny_yes)};
        setting_water = (TextView) findViewById(R.id.tv_setting_option_water);
        setting_water.setText(SessionManager.getInstance(this).getBoolean(IS_WATER) ? list_water[1] : list_water[0]);

        settingOptionText1 = ((TextView) findViewById(R.id.setting_option_1_txt));
        String resolution = SessionManager.getInstance(this).getString(RESOLUTION);
        if (TextUtils.isEmpty(resolution))
            resolution = SessionManager.getInstance(this).put(RESOLUTION, list_resolution[1]);
        settingOptionText1.setText(resolution);
        reInitBitrates(settingOptionText1.getText().toString());

        settingOptionText2 = ((TextView) findViewById(R.id.setting_option_2_txt));
        String fps = SessionManager.getInstance(this).getString(FPS);
        if (TextUtils.isEmpty(fps))
            fps = SessionManager.getInstance(this).put(FPS, list_fps[0]);
        settingOptionText2.setText(fps);

        settingOptionText3 = ((TextView) findViewById(R.id.setting_option_3_txt));
        String biterate = SessionManager.getInstance(this).getString(BIT_RATE_MAX);
        if (TextUtils.isEmpty(biterate))
            biterate = SessionManager.getInstance(this).put(BIT_RATE_MAX, list_bitrate_max[defaultBitrateMaxIndex]);
        settingOptionText3.setText(biterate);

        settingOptionText4 = ((TextView) findViewById(R.id.setting_option_4_txt));
        String connectionMode = SessionManager.getInstance(this).getString(CONNECTION_MODE);
        if (TextUtils.isEmpty(connectionMode))
            connectionMode = SessionManager.getInstance(this).put(CONNECTION_MODE, list_connectionMode[0]);
        settingOptionText4.setText(connectionMode);

        settingOptionText5 = ((TextView) findViewById(R.id.setting_option_5_txt));
        String format = SessionManager.getInstance(this).getString(CODECS);
        if (TextUtils.isEmpty(format))
            format = SessionManager.getInstance(this).put(CODECS, list_format[0]);
        settingOptionText5.setText(format);

        settingOptionText6 = ((TextView) findViewById(R.id.setting_option_6_txt));
        String biterateMin = SessionManager.getInstance(this).getString(BIT_RATE_MIN);
        if (TextUtils.isEmpty(biterateMin))
            biterateMin = SessionManager.getInstance(this).put(BIT_RATE_MIN, list_bitrate_min[defaultBitrateMinIndex]);
        settingOptionText6.setText(biterateMin);

        settingOptionText7 = (TextView) findViewById(R.id.setting_option_7_txt);
        boolean isObserver = SessionManager.getInstance(this).getBoolean(IS_OBSERVER);
        String observerMode = list_observer[isObserver ? 1 : 0];
        settingOptionText7.setText(observerMode);

        settingOptionText8 = (TextView) findViewById(R.id.setting_option_8_txt);
        boolean isGpuImageFilter = SessionManager.getInstance(this).getBoolean(IS_GPUIMAGEFILTER);
        String gpuImageFiliter = list_gpuImageFilter[isGpuImageFilter ? 1 : 0];
        settingOptionText8.setText(gpuImageFiliter);

        settingOptionSRTP = (TextView) findViewById(R.id.setting_option_9_txt);
        boolean isSrtp = SessionManager.getInstance(this).getBoolean(IS_SRTP);
        String srtpOption = list_gpuImageFilter[isSrtp ? 1 : 0];
        settingOptionSRTP.setText(srtpOption);

        backButton = (LinearLayout) findViewById(R.id.settings_back);
        testSettingOptionsContainer = (LinearLayout) findViewById(R.id.setting_test_list);

        //connection type
        settingOptionConnectionType = (TextView) findViewById(R.id.setting_option_connectiontype_txt);
        boolean isQuic = SessionManager.getInstance(this).getBoolean(IS_RONGRTC_CONNECTIONMODE);
        settingOptionConnectionType.setText(list_connectionType[isQuic ? 0 : 1]);

        settingOptionMediaUrl = (TextView) findViewById(R.id.tv_setting_option_media_url);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String name = SessionManager.getInstance(this).getString("MediaName");
        if (!TextUtils.isEmpty(name)) {
            settingOptionMediaUrl.setText(name);
        }
    }

    private void setupListeners() {
        findViewById(R.id.setting_option_1).setOnClickListener(new OnOptionViewClickListener(R.string.settings_text_resolution, list_resolution, REQUEST_CODE_RESOLUTION));
        findViewById(R.id.setting_option_2).setOnClickListener(new OnOptionViewClickListener(R.string.settings_text_fps, list_fps, REQUEST_CODE_FPS));
        findViewById(R.id.setting_option_3).setOnClickListener(new OnOptionViewClickListener(R.string.settings_text_rate, list_bitrate_max, REQUEST_CODE_BITRATE_MAX));
        findViewById(R.id.setting_option_4).setOnClickListener(new OnOptionViewClickListener(R.string.settings_text_connection_mode, list_connectionMode, REQUEST_CODE_MODE));
        findViewById(R.id.setting_option_5).setOnClickListener(new OnOptionViewClickListener(R.string.settings_text_coding_mode, list_format, REQUEST_CODE_FORMAT));
        findViewById(R.id.setting_option_6).setOnClickListener(new OnOptionViewClickListener(R.string.settings_text_min_rate, list_bitrate_min, REQUEST_CODE_BITRATE_MIN));
        findViewById(R.id.setting_option_7).setOnClickListener(new OnOptionViewClickListener(R.string.settings_text_observer, list_observer, REQUEST_CODE_IS_OBSERVER));
        findViewById(R.id.setting_option_8).setOnClickListener(new OnOptionViewClickListener(R.string.settings_text_gpufliter, list_gpuImageFilter, REQUEST_CODE_IS_GPUIMAGEFILTER));
        findViewById(R.id.setting_option_9).setOnClickListener(new OnOptionViewClickListener(R.string.settings_text_srtp, list_gpuImageFilter, REQUEST_CODE_IS_SRTP));
        findViewById(R.id.setting_option_connectiontype).setOnClickListener(new OnOptionViewClickListener(R.string.settings_text_connection, list_connectionType, REQUEST_CODE_IS_CONNECTIONTYPE));
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.settings_title_layout).setOnClickListener(new OnTitleViewClickListener());
        findViewById(R.id.setting_option_streamTiny).setOnClickListener(new OnOptionViewClickListener(R.string.Opensizestream,list_streamTiny,REQUEST_CODE_IS_STREAM_TINY));
        findViewById(R.id.setting_option_autotest).setOnClickListener(new OnOptionViewClickListener(R.string.autotest,list_autotest,REQUEST_CODE_IS_AUTO_TEST));
        findViewById(R.id.setting_option_water).setOnClickListener(new OnOptionViewClickListener(R.string.watermark,list_water,REQUEST_CODE_IS_WATER));
        findViewById(R.id.setting_option_media_url_).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingActivity.this, MediaServerActivity.class));
            }
        });
    }

    private class OnOptionViewClickListener implements View.OnClickListener {
        String title;
        String[] datas;
        int requestCode;

        public OnOptionViewClickListener(int resId, String[] datas, int requestCode) {
            this.title = getResources().getString(resId);
            this.datas = datas;
            this.requestCode = requestCode;
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(SettingActivity.this, OptionsPickActivity.class);
            intent.putExtra(OptionsPickActivity.BUNDLE_KEY_DATAS, datas);
            intent.putExtra(OptionsPickActivity.BUNDLE_KEY_TITLE, title);
            startActivityForResult(intent, requestCode);
        }
    }

    /**
     * Click 5 times to show the hidden views
     */
    private class OnTitleViewClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            long timeDuration = System.currentTimeMillis() - lastClickTime;
            if (timeDuration > 500) {
                tapStep = 0;
                lastClickTime = 0;
            } else {
                tapStep++;
                if (tapStep == 4) {
                    if (BuildConfig.DEBUG) {
                        findViewById(R.id.setting_options_hidden).setVisibility(View.VISIBLE);
                    }
                    if (null != settings_Modify) {
                        //settings_Modify.setVisibility(View.VISIBLE);
                        settings_Modify.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (ButtentSolp.check(v.getId(), 1800)) {
                                    FinLog.v("SettingActivity", getString(R.string.btnsolpstr));
                                    return;
                                }
                                modifyServerAddress();
                            }
                        });
                    }
                }
            }

            lastClickTime = System.currentTimeMillis();
        }
    }

    private void reInitBitrates(String resolution) {
        String kbps = "Kbps";
        try {
            //数组长度 間隔  最小
            int length = 0, parameters = 10, min = 0;//
            if (!TextUtils.isEmpty(resolution)) {
                if (RESOLUTION_MEDIUM.equals(resolution)) {
                    //                min=200;
                    length = (1000 - min) / parameters;
                } else if (RESOLUTION_HIGH.equals(resolution)) {
                    //                min=500;
                    length = (3000 - min) / parameters;
                } else if (RESOLUTION_LOW.equals(resolution)) {
                    //                min=100;
                    length = (600 - min) / parameters;
                } else if (RESOLUTION_SUPER.equals(resolution)) {
                    //                min=1500;
                    length = (4600 - min) / parameters;
                }
            }
            list_bitrate_max = new String[length + 1];
            list_bitrate_min = new String[length + 1];
            for (int i = 0; i <= length; i++) {
                int bitrate = i * parameters + min;
                list_bitrate_max[i] = bitrate + kbps;
                list_bitrate_min[i] = bitrate + kbps;
            }
            //設置默認 步长：10
            if (!TextUtils.isEmpty(resolution) && RESOLUTION_MEDIUM.equals(resolution)) {
                defaultBitrateMinIndex = 35;//350
                defaultBitrateMaxIndex = 100;//1000
            } else if (!TextUtils.isEmpty(resolution) && RESOLUTION_HIGH.equals(resolution)) {
                defaultBitrateMinIndex = 75;//750
                defaultBitrateMaxIndex = 250;//2500
            } else if (!TextUtils.isEmpty(resolution) && RESOLUTION_LOW.equals(resolution)) {
                defaultBitrateMinIndex = 15;//150
                defaultBitrateMaxIndex = 50;//500
            } else if (!TextUtils.isEmpty(resolution) && RESOLUTION_SUPER.equals(resolution)) {
                defaultBitrateMinIndex = 150;//1500
                defaultBitrateMaxIndex = 450;//4500
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null)
            return;

        String result = data.getStringExtra(OptionsPickActivity.BUNDLE_KEY_RESULT);
        if (TextUtils.isEmpty(result))
            return;
        switch (requestCode) {
            case REQUEST_CODE_RESOLUTION:
                settingOptionText1.setText(result);
                SessionManager.getInstance(this).put(RESOLUTION, result);
                changeBitrateByResolution(result);
                break;
            case REQUEST_CODE_FPS:
                SessionManager.getInstance(this).put(FPS, result);
                settingOptionText2.setText(result);
                break;
            case REQUEST_CODE_BITRATE_MAX:
                settingOptionText3.setText(result);
                SessionManager.getInstance(this).put(BIT_RATE_MAX, result);
                break;
            case REQUEST_CODE_MODE:
                settingOptionText4.setText(result);
                SessionManager.getInstance(this).put(CONNECTION_MODE, result);
                break;
            case REQUEST_CODE_FORMAT:
                settingOptionText5.setText(result);
                SessionManager.getInstance(this).put(CODECS, result);
                break;
            case REQUEST_CODE_BITRATE_MIN:
                settingOptionText6.setText(result);
                SessionManager.getInstance(this).put(BIT_RATE_MIN, result);
                break;
            case REQUEST_CODE_IS_OBSERVER:
                settingOptionText7.setText(result);
                SessionManager.getInstance(this).put(IS_OBSERVER, result.equals(list_observer[1]));
                break;
            case REQUEST_CODE_IS_GPUIMAGEFILTER:
                settingOptionText8.setText(result);
                SessionManager.getInstance(this).put(IS_GPUIMAGEFILTER, result.equals(list_gpuImageFilter[1]));
                break;
            case REQUEST_CODE_IS_SRTP:
                settingOptionSRTP.setText(result);
                SessionManager.getInstance(this).put(IS_SRTP, result.equals(list_gpuImageFilter[1]));
                break;
            case REQUEST_CODE_IS_CONNECTIONTYPE:
                settingOptionConnectionType.setText(result);
                if (list_connectionType[0].equals(result)) {
//                    RongRTCEngine.getInstance().setRongRTCConnectionMode(true);
                } else if (list_connectionType[1].equals(result)) {
//                    RongRTCEngine.getInstance().setRongRTCConnectionMode(false);
                }
//                SessionManager.getInstance(this).put(IS_RONGRTC_CONNECTIONMODE, RongRTCContext.ConfigParameter.RongRTCConnectionMode == RongRTCEngine.RongRTCConnectionMode.QUIC ? true : false);
//                if (!SessionManager.getInstance(Utils.getContext()).contains(AppRTCUtils.CUSTOM_CMPKEY))
//                    EventBus.getDefault().postSticky("1");
                break;
            case REQUEST_CODE_IS_STREAM_TINY:
                setting_option_streamTiny.setText(result);
                SessionManager.getInstance(this).put(IS_STREAM_TINY,list_streamTiny[1].equals(result));
                break;
            case REQUEST_CODE_IS_AUTO_TEST:
                setting_autotest.setText(result);
                SessionManager.getInstance(this).put(IS_AUTO_TEST,list_autotest[1].equals(result));
                break;
            case REQUEST_CODE_IS_WATER:
                setting_water.setText(result);
                SessionManager.getInstance(this).put(IS_WATER,list_water[1].equals(result));
                break;
            default:
                break;
        }
    }

    private void changeBitrateByResolution(String resolution) {
        try {
            reInitBitrates(resolution);
            settingOptionText3.setText(list_bitrate_max[defaultBitrateMaxIndex]);
            settingOptionText6.setText(list_bitrate_min[defaultBitrateMinIndex]);
            SessionManager.getInstance(this).put(BIT_RATE_MIN, list_bitrate_min[defaultBitrateMinIndex]);
            SessionManager.getInstance(this).put(BIT_RATE_MAX, list_bitrate_max[defaultBitrateMaxIndex]);
            setupListeners();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private View dialogView = null;
    private Dialog dialog;
    private LayoutInflater inflater;
    private EditText edit_ServerURL, edit_cmpServer, edit_appid;
    private CMPAddress cmpAddress;
    private Button btn_ok, btn_cancele;

    /**
     * 修改服务器CMP地址
     */
    private void modifyServerAddress() {
        try {
            inflater = LayoutInflater.from(SettingActivity.this);
            dialogView = inflater.inflate(R.layout.activity_cmpaddress, null);
            RelativeLayout layout = (RelativeLayout) dialogView.findViewById(R.id.rel_data);
            edit_appid = (EditText) layout.findViewById(R.id.edit_appid);
            edit_ServerURL = (EditText) layout.findViewById(R.id.edit_ServerURL);
            edit_cmpServer = (EditText) layout.findViewById(R.id.edit_cmpServer);
            btn_ok = (Button) layout.findViewById(R.id.btn_ok);
            btn_ok.setOnClickListener(btnClickListener);
            btn_cancele = (Button) layout.findViewById(R.id.btn_cancel);
            btn_cancele.setOnClickListener(btnClickListener);
            String appid = AppRTCUtils.getAppID();
            if (!TextUtils.isEmpty(appid)) {
                edit_appid.setText("");
                edit_appid.setText(appid);
            }

            try {
                if (null != AppRTCUtils.getCMPAddress(AppRTCUtils.CUSTOM_CMPKEY)) {
                    cmpAddress = AppRTCUtils.getCMPAddress(AppRTCUtils.CUSTOM_CMPKEY);
                } else if (null != AppRTCUtils.getCMPAddress(AppRTCUtils.SELECT_KEY)) {
                    cmpAddress = AppRTCUtils.getCMPAddress(AppRTCUtils.SELECT_KEY);
                }
                if (!TextUtils.isEmpty(cmpAddress.getServerURL())) {
                    edit_ServerURL.setText("");
                    edit_ServerURL.setText(cmpAddress.getServerURL());
                }
                if (!TextUtils.isEmpty(cmpAddress.getCmpServer())) {
                    edit_cmpServer.setText("");
                    edit_cmpServer.setText(cmpAddress.getCmpServer());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            dialog = new Dialog(SettingActivity.this, R.style.loadingdata_dialog);
            dialog.setCancelable(false);//bu可以用“返回键”取消
            dialog.setContentView(layout, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            dialog.setContentView(layout);// 设置布局
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            if (!SettingActivity.this.isFinishing())
                dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "ERROR!", Toast.LENGTH_SHORT).show();
        }
    }

    private View.OnClickListener btnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_ok:
                    String cmp = "",tokenServer="";
                    try {
                        if (null != edit_cmpServer) {
                            cmp = edit_cmpServer.getText().toString().trim();
                        }
                        if (null != edit_ServerURL) {
                            tokenServer = edit_ServerURL.getText().toString().trim();
                        }
                        if (!TextUtils.isEmpty(cmp) && !TextUtils.isEmpty(tokenServer) && !Utils.isQuicOrTcp(cmp)) {
                            Toast.makeText(SettingActivity.this, R.string.serverCMPToast, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (null != edit_ServerURL) {
                            String appid = edit_appid.getText().toString().trim();
                            boolean bool_appid = AppRTCUtils.setAppID(appid);
                            CMPAddress address = AppRTCUtils.getCMPAddress(AppRTCUtils.SELECT_KEY);

                            if (!TextUtils.isEmpty(cmp) && !TextUtils.isEmpty(tokenServer) &&
                                    null != address && !TextUtils.isEmpty(address.getServerURL()) &&
                                    address.getCmpServer().equals(cmp) && address.getServerURL().equals(tokenServer)) {
//                                FinLog.v(TAG,"用户继续保存的地址就是第一次进来默认选择的地址，就不用管，防止用户将默认地址保存成自定义的地址," +
//                                        "并将自定义的地址删除 已方便显示成默认地址，然后重新下载证书");
                                SessionManager.getInstance(Utils.getContext()).remove(AppRTCUtils.CUSTOM_CMPKEY);
                                reInitialization();
                                saveSuccess(cmp);
                            } else if (TextUtils.isEmpty(cmp) && TextUtils.isEmpty(tokenServer)) {
                                FinLog.v(TAG, "同时为空 删除自定义的地址！");
                                SessionManager.getInstance(Utils.getContext()).remove(AppRTCUtils.CUSTOM_CMPKEY);
                                reInitialization();

                                saveSuccess(cmp);
                            } else {
                                boolean bool = AppRTCUtils.setCMPAddress(cmp, tokenServer, 1);
                                if (bool && bool_appid) {
                                    //RongRTCEngine.setVOIPServerAddress(cmp);

                                    saveSuccess(cmp);
                                }
                            }
                        } else {
                            FinLog.v(TAG, "ERROR null!");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        FinLog.v(TAG, "ERROR " + e.getMessage());
                    }
                    break;
                case R.id.btn_cancel:
                    if (!SettingActivity.this.isFinishing() && null != dialog && dialog.isShowing())
                        dialog.dismiss();
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != dialog && dialog.isShowing())
            dialog.dismiss();
    }

    private static String cerUrl = "";
    private static String cmpServer = "";

    private void reInitialization() {
        cerUrl = SessionManager.getInstance(Utils.getContext()).getString(AppRTCUtils.CER_URL);
        CMPAddress cmpAddress = AppRTCUtils.getCMPAddress(AppRTCUtils.SELECT_KEY);
        if (null != cmpAddress && !TextUtils.isEmpty(cmpAddress.getServerURL())) {
            cmpServer = cmpAddress.getCmpServer();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    FinLog.v("BinClient", "重新初始化 cmp 地址等==cerUrl=" + cerUrl + ",cmpServer=" + cmpServer);

                    //RongRTCEngine.setVOIPServerAddress(cmpServer);
                    // RongRTCEngine.init(getApplicationContext(), cmpServer, cmpServer);
                }
            });
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStream input = AppRTCUtils.downLoadFromUrl(cerUrl);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = input.read(buffer)) > -1) {
                        baos.write(buffer, 0, len);
                    }
                    baos.flush();
                    InputStream stream1 = new ByteArrayInputStream(baos.toByteArray());
                    InputStream stream2 = new ByteArrayInputStream(baos.toByteArray());
                    //RongRTCEngine.setCertificate(stream1, stream2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void saveSuccess(String cmpServer) {
        settingOptionConnectionType.setText(list_connectionType[Utils.connectionModeConfig(cmpServer) ? 0 : 1]);
        Toast.makeText(SettingActivity.this, R.string.saveSuccess, Toast.LENGTH_SHORT).show();
        if (!SettingActivity.this.isFinishing() && null != dialog && dialog.isShowing())
            dialog.dismiss();
    }
}
