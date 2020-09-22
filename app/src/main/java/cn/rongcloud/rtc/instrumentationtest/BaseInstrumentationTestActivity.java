package cn.rongcloud.rtc.instrumentationtest;

import static cn.rongcloud.rtc.instrumentationtest.OperationModel.REQUESTING;
import static cn.rongcloud.rtc.instrumentationtest.OperationModel.SUCCESS;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.rongcloud.rtc.R;
import cn.rongcloud.rtc.api.RCRTCConfig;
import cn.rongcloud.rtc.api.RCRTCEngine;
import cn.rongcloud.rtc.api.RCRTCLocalUser;
import cn.rongcloud.rtc.api.RCRTCRoom;
import cn.rongcloud.rtc.api.stream.RCRTCLiveInfo;
import cn.rongcloud.rtc.base.RCRTCRoomType;
import cn.rongcloud.rtc.base.RTCErrorCode;
import cn.rongcloud.rtc.base.RongRTCBaseActivity;
import cn.rongcloud.rtc.instrumentationtest.OperationAdapter.OnClickItemChildListener;
import cn.rongcloud.rtc.instrumentationtest.OperationAdapter.OperationViewHolder;
import cn.rongcloud.rtc.instrumentationtest.widget.VideoViewManager;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.RongIMClient.ConnectCallback;
import io.rong.imlib.RongIMClient.ConnectionErrorCode;
import io.rong.imlib.RongIMClient.ConnectionStatusListener.ConnectionStatus;
import io.rong.imlib.RongIMClient.DatabaseOpenStatus;
import java.util.ArrayList;
import java.util.List;

public class BaseInstrumentationTestActivity extends RongRTCBaseActivity implements OnClickListener, OnClickItemChildListener<OperationModel, OperationViewHolder> {

    final int TYPE_START_CAMERA = 1;
    final int TYPE_STOP_CAMERA = 2;
    final int TYPE_START_MIC = 3;
    final int TYPE_STOP_MIC = 4;

    View mRTCLayout;
    View mJoinLayout;
    View mImLayout;
    Button mBtnJoin;
    CheckBox mCbLive;
    EditText mEvRoomId;
    Button mBtnIMConnect;
    EditText mEvToken;
    EditText mEvAppKey;
    RecyclerView mRecyclerView;
    TextView mTvRoomId;
    RCRTCRoom room;
    RCRTCLocalUser localUser;
    boolean isLive;
    RCRTCLiveInfo mLiveInfo;
    VideoViewManager videoViewManager;
    List<OperationModel> operations = new ArrayList<>();
    OperationAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rtc_instrumentationtest);
        findViews();
        addOutRoomOperation();
        bindData();
    }

    /**
     * 添加不在房间时允许的操作
     */
    protected void addOutRoomOperation() {
        addOperation("start_camera", TYPE_START_CAMERA);
        addOperation("stop_camera", TYPE_STOP_CAMERA);
        addOperation("start_mic", TYPE_START_MIC);
        addOperation("stop_mic", TYPE_STOP_MIC);
    }

    protected void addOperation(String desc, int operationType) {
        operations.add(new OperationModel(desc, operationType));
    }

    private void bindData() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new OperationAdapter(operations);
        mAdapter.setOnClickItemChildListener(this);
        mRecyclerView.setAdapter(mAdapter);
    }

    void findViews() {
        mRTCLayout = findViewById(R.id.rtc_layout);
        mImLayout = findViewById(R.id.im_layout);
        mJoinLayout = findViewById(R.id.join_layout);
        mBtnJoin = (Button) findViewById(R.id.btn_join);
        mCbLive = (CheckBox) findViewById(R.id.cb_live);
        mEvRoomId = (EditText) findViewById(R.id.ev_roomid);
        mBtnIMConnect = (Button) findViewById(R.id.btn_imconnect);
        mEvToken = (EditText) findViewById(R.id.ev_token);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mEvAppKey = findViewById(R.id.ev_appkey);
        mTvRoomId = findViewById(R.id.tv_roomid);
        mBtnJoin.setOnClickListener(this);
        mBtnIMConnect.setOnClickListener(this);
        videoViewManager = new VideoViewManager((LinearLayout) findViewById(R.id.ll_videoviews));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_imconnect:
                onConnectIM();
                break;
            case R.id.btn_join:
                onJoinRoom();
                break;
        }
    }

    void onJoinRoom() {
        room = RCRTCEngine.getInstance().getRoom();
        if (room != null) {
            onJoinRoomSuccess();
            return;
        }
        mBtnJoin.setTextColor(Color.BLACK);
        mBtnJoin.setText(REQUESTING);
        mBtnJoin.setEnabled(false);
        isLive = mCbLive.isChecked();
        final RCRTCRoomType roomType = isLive ? RCRTCRoomType.LIVE_AUDIO_VIDEO : RCRTCRoomType.MEETING;
        RCRTCEngine.getInstance().joinRoom(mEvRoomId.getText().toString(), roomType, new RTCResultDataCallbackWrapper<RCRTCRoom>(this) {
            @Override
            protected void onUISuccess(RCRTCRoom data) {
                room = data;
                localUser = data.getLocalUser();
                onJoinRoomSuccess();
            }

            @Override
            protected void onUIFailed(RTCErrorCode errorCode) {
                mBtnJoin.setText("" + errorCode.getValue());
                mBtnJoin.setTextColor(Color.RED);
                mBtnJoin.setEnabled(true);
            }
        });
    }

    protected void initRTC() {
        RCRTCEngine.getInstance().init(getApplication(), getRTCConfig());
        RCRTCEngine.getInstance().enableSpeaker(false);
    }

    protected void onJoinRoomSuccess() {
        mBtnJoin.setEnabled(true);
        mBtnJoin.setTextColor(Color.BLACK);
        mBtnJoin.setText(SUCCESS);
        mRecyclerView.setVisibility(View.VISIBLE);
//        mJoinLayout.setVisibility(View.GONE);
        mTvRoomId.setText(room.getRoomId());
        addInRoomOperation();
    }

    /**
     * 添加在房间内时的操作
     */
    protected void addInRoomOperation() {

    }

    private RCRTCConfig getRTCConfig() {
        return RCRTCConfig.Builder.create().build();
    }

    void onConnectIM() {
        if (RongIMClient.getInstance().getCurrentConnectionStatus() == ConnectionStatus.CONNECTED) {
            onConnectIMSuccess();
            return;
        }

        String token = mEvToken.getText().toString();
        String appKey = mEvAppKey.getText().toString();
        if (TextUtils.isEmpty(token) || TextUtils.isEmpty(appKey)) {
            mBtnIMConnect.setText("" + RTCErrorCode.RongRTCCodeParameterError.getValue());
            mBtnIMConnect.setTextColor(Color.RED);
            return;
        }
        mBtnIMConnect.setTextColor(Color.BLACK);
        mBtnIMConnect.setText(REQUESTING);
        mBtnIMConnect.setEnabled(false);
        RongIMClient.init(getApplication(), appKey, false);
        RongIMClient.connect(token, new ConnectCallback() {
            @Override
            public void onSuccess(String t) {
                onConnectIMSuccess();
            }

            @Override
            public void onError(ConnectionErrorCode e) {
                mBtnIMConnect.setTextColor(Color.RED);
                mBtnIMConnect.setText(e.getValue() + "");
                mBtnIMConnect.setEnabled(true);
            }

            @Override
            public void onDatabaseOpened(DatabaseOpenStatus code) {

            }
        });
    }

    private void onConnectIMSuccess() {
        mBtnIMConnect.setTextColor(Color.BLACK);
        mBtnIMConnect.setText(SUCCESS);
        mRTCLayout.setVisibility(View.VISIBLE);
//        mImLayout.setVisibility(View.GONE);
        initRTC();
    }

    @Override
    public void onBackPressed() {
        leaveRoom();
        super.onBackPressed();
    }

    private void leaveRoom() {
        RCRTCEngine.getInstance().leaveRoom(null);
    }

    @Override
    public void onClickItem(OperationModel data, OperationViewHolder viewHolder) {
    }
}
