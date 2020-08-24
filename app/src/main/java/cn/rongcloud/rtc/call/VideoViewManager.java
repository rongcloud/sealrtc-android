package cn.rongcloud.rtc.call;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.rongcloud.rtc.BuildConfig;
import cn.rongcloud.rtc.R;
import cn.rongcloud.rtc.api.RCRTCRemoteUser;
import cn.rongcloud.rtc.api.RCRTCRoom;
import cn.rongcloud.rtc.api.callback.IRCRTCResultCallback;
import cn.rongcloud.rtc.api.stream.RCRTCVideoView;
import cn.rongcloud.rtc.base.RCRTCStream;
import cn.rongcloud.rtc.base.RTCErrorCode;
import cn.rongcloud.rtc.call.ContainerLayout.ContainerLayoutGestureEvents;
import cn.rongcloud.rtc.core.RendererCommon;
import cn.rongcloud.rtc.entity.connectedVideoViewEntity;
import cn.rongcloud.rtc.util.CoverView;
import cn.rongcloud.rtc.util.RongRTCTalkTypeUtil;
import cn.rongcloud.rtc.util.SessionManager;
import cn.rongcloud.rtc.util.Utils;
import cn.rongcloud.rtc.utils.FinLog;
import io.rong.imlib.RongIMClient;

import static cn.rongcloud.rtc.util.Utils.SCREEN_SHARING;

/** Created by Huichao on 2016/8/26. */
public class VideoViewManager implements ContainerLayoutGestureEvents {

    private static final String TAG = "VideoViewManager";
    //    private RelativeLayout remoteRenderLayout, remoteRenderLayout2, remoteRenderLayout3,
    // remoteRenderLayout4, remoteRenderLayout5, remoteRenderLayout6, remoteRenderLayout7,
    // remoteRenderLayout8, remoteRenderLayout9;

    private Context context;
    private LinearLayout holderContainer;
    private ContainerLayout holderBigContainer;
    private RenderHolder selectedRender;
    private LinearLayout debugInfoView;
    public boolean isObserver;
    //    private List<RenderHolder> unUsedRemoteRenders = new ArrayList<>();
    private int screenWidth;
    private int screenHeight;
    private RCRTCRoom rongRTCRoom;

    public HashMap<String, RenderHolder> connectedRemoteRenders = new HashMap<>();
    private List<connectedVideoViewEntity> connectedUsers = new ArrayList<>();

    private ArrayList<RenderHolder> positionRenders = new ArrayList<>();

    LinearLayout.LayoutParams remoteLayoutParams;
    /** 自定义视频流的数量 */
    private int customVideoCount = 0;
    //    RelativeLayout.LayoutParams localLayoutParams;
    /** 存储当前显示在大屏幕上的用户id */
    private List<String> selectedUserid = new ArrayList<>();

    public void initViews(Context context, boolean isObserver) {
        this.context = context;
        getSize();
        int base = screenHeight < screenWidth ? screenHeight : screenWidth;
        remoteLayoutParams = new LinearLayout.LayoutParams(base / 4, base / 3);

        SessionManager.getInstance().put(Utils.KEY_screeHeight, base / 3);
        SessionManager.getInstance().put(Utils.KEY_screeWidth, base / 4);

        holderContainer =
                (LinearLayout) ((Activity) context).findViewById(R.id.call_reder_container);
        holderBigContainer =
                (ContainerLayout) ((Activity) context).findViewById(R.id.call_render_big_container);
        holderBigContainer.setGestureEvents(this);
        debugInfoView = (LinearLayout) ((Activity) context).findViewById(R.id.debug_info);
        //        debugInfoView.setOnClickListener(
        //                new View.OnClickListener() {
        //                    @Override
        //                    public void onClick(View v) {
        //                        doubleClick(false);
        //                    }
        //                });

        this.isObserver = isObserver;

        holderContainer.removeAllViews();
        holderBigContainer.removeAllViews();
        toggleTips();
    }

    private void getSize() {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        screenWidth = wm.getDefaultDisplay().getWidth();
        screenHeight = wm.getDefaultDisplay().getHeight();
    }

    public void onCreateEglFailed(String userId, String tag) {
        RenderHolder renderHolder = getViewHolder(userId, tag);
        Log.i(TAG, "onCreateEglFailed() renderHolder = " + renderHolder);
        if (renderHolder != null) {
            renderHolder.coverView.onCreateEglFailed();
        }
    }

    private class RemoteRenderClickListener implements View.OnClickListener {
        private RenderHolder renderHolder;

        public RemoteRenderClickListener(RenderHolder renderHolder) {
            this.renderHolder = renderHolder;
        }

        @Override
        public void onClick(View view) {
            touchRender(renderHolder);
//            doubleClick(true);
        }
    }

    long lastClickTime = 0;
    int tapStep = 0;

    private void switchDebugInfoViewVisibility() {
        if (!BuildConfig.DEBUG) {
            return;
        }
        debugInfoView.setVisibility(
                debugInfoView.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }

    public void touchRender(RenderHolder render) {
        int index = positionRenders.indexOf(render);
        final RenderHolder lastSelectedRender = selectedRender;
        if (index < 0) {
            return;
        }
        positionRenders.set(index, selectedRender);
        selectedRender = render;
        if (!lastSelectedRender.userId.equals(RongIMClient.getInstance().getCurrentUserId())) {
            // 原来的大窗口变小流
            if (rongRTCRoom != null) {
                final String lastId = lastSelectedRender.userId;
                RCRTCRemoteUser remoteUser = rongRTCRoom.getRemoteUser(lastId);
                FinLog.d(TAG,"switchToTinyStream");
                if (remoteUser != null) {
                    remoteUser.switchToTinyStream(new IRCRTCResultCallback() {
                        @Override
                        public void onSuccess() {
                            FinLog.v(TAG, lastId + " exchangeStreamToTinyStream success !");
                        }

                        @Override
                        public void onFailed(RTCErrorCode errorCode) {
                            FinLog.e(TAG, lastId + " exchangeStreamToTinyStream failed ! errorCode: " + errorCode);
                        }
                    });
                } else {
                    FinLog.v(TAG, "not get the remote user = " + lastId);
                }
            }
        }

        holderContainer.removeView(selectedRender.containerLayout);
        holderBigContainer.removeView(lastSelectedRender.containerLayout);
        holderBigContainer.resetGestureView();

        // 大窗口显示于宿主窗口下层
        selectedRender.coverView.getRongRTCVideoView().setZOrderMediaOverlay(false);

        if (!selectedRender.userId.equals(RongIMClient.getInstance().getCurrentUserId())) {
            // 原来的小窗口变大流
            if (rongRTCRoom != null) {
                final String targetId = selectedRender.userId;
                RCRTCRemoteUser remoteUser = rongRTCRoom.getRemoteUser(targetId);
                if (remoteUser != null) {
                    FinLog.d(TAG,"switchToNormalStream");
                    remoteUser.switchToNormalStream(new IRCRTCResultCallback() {
                        @Override
                        public void onSuccess() {
                            FinLog.v(TAG, targetId + " exchangeStreamToNormalStream success !");
                        }

                        @Override
                        public void onFailed(RTCErrorCode errorCode) {
                            FinLog.e(TAG,
                                targetId + " exchangeStreamToNormalStream failed ! errorCode: " + errorCode);
                        }
                    });
                } else {
                    FinLog.v(TAG, "not get the remote user = " + targetId);
                }
            }
        }
        holderBigContainer.addView(selectedRender, screenWidth, screenHeight);

        // 添加之后设置VIdeoView的缩放类型,防止旋转屏幕之后 pc共享 再切换大小 显示不全问题
        selectedRender.coverView.getRongRTCVideoView().setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);

        //        lastSelectedRender.coverView.getBlinkVideoView().setZOrderOnTop(true);
        lastSelectedRender.coverView.getRongRTCVideoView().setZOrderMediaOverlay(true);

        // 防止横竖切换 再 小大切换 导致的小屏尺寸没更新 host——304
        lastSelectedRender.coverView.getRongRTCVideoView().setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        // pc分屏时 必须通知videoview listener 去刷新size//手动转屏不需要通知listerer
        lastSelectedRender.coverView.showNameIndexView();
        holderContainer.addView(lastSelectedRender.containerLayout, index, remoteLayoutParams);
        saveSelectUserId(render);
    }

    private RenderHolder createRenderHolder() {
        RelativeLayout layout = new RelativeLayout(context);
        layout.setLayoutParams(
            new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return new RenderHolder(layout, 0);
    }

    public void userJoin(String userID, String tag, String userName, String talkType) {
        try {
            Log.i(TAG, "connectedUsers=" + connectedUsers.size() + ",userName=" + userName);
            if (connectedUsers.size() == 0) {
                RenderHolder renderHolder = createRenderHolder(); // unUsedRemoteRenders.get(0);
                renderHolder.userName = userName;
                renderHolder.userId = userID;
                renderHolder.tag = tag;
                renderHolder.initCover(talkType);
                addVideoViewEntiry(userID, tag, renderHolder);
                //                unUsedRemoteRenders.remove(0);
            }
            if (connectedUsers.size() != 0 && !containsKeyVideoViewEntiry(userID, tag)) {
                RenderHolder renderHolder = createRenderHolder(); // unUsedRemoteRenders.get(0);
                renderHolder.userName = userName;
                renderHolder.userId = userID;
                renderHolder.tag = tag;
                renderHolder.initCover(talkType);
                renderHolder.coverView.showNameIndexView();
                holderContainer.addView(renderHolder.containerLayout, remoteLayoutParams);
                addVideoViewEntiry(userID, tag, renderHolder);
                //                unUsedRemoteRenders.remove(0);
            }

            toggleTips();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setVideoView(
            boolean isSelf, String userID, String tag, String userName, RCRTCVideoView render, String talkType) {
        Log.i(TAG, ">>>>>>>>>>>>>>>>setVideoView isSelf==" + isSelf);
        if (!connectedRemoteRenders.containsKey(generateKey(userID, tag))) {
            if (isSelf) {
                if (!RCRTCStream.RONG_TAG.equals(tag) && !TextUtils.isEmpty(tag)) { // 自定义视频流的视图
                    customVideoCount = customVideoCount + 1;
                }
                if (connectedRemoteRenders.size() == 0) {
                    largeView(isSelf, userID, tag, userName, render, talkType);
                } else {
                    smallView(isSelf, userID, tag, userName, render, talkType);
                }
            } else {
                if (connectedUsers.size() > 0
                        && !TextUtils.isEmpty(connectedUsers.get(0).getUserId())
                        && connectedUsers.get(0).getKey().equals(generateKey(userID, tag))) {
                    // 放入大屏的远端用户需要切换到大流
                    largeView(isSelf, userID, tag, userName, render, talkType);
                    exchangeToNormalStream(selectedRender.userId);
                } else {
                    smallView(isSelf, userID, tag, userName, render, talkType);
                }
            }
        } else {
            refreshRemoteView(isSelf, userID, tag, userName, render);
        }
    }

    private String generateKey(String userId, String tag) {
        return userId + "_" + tag;
    }

    private void refreshRemoteView(
            boolean isSelf,
            String userID,
            String tag,
            String userName,
            RCRTCVideoView render) {
        try {
            String key = generateKey(userID, tag);
            boolean isBigScreen = selectedUserid != null && selectedUserid.contains(key);
            //            Log.v(TAG, "refreshRemoteView unUsedRemoteRenders size=" +
            // unUsedRemoteRenders.size() + ",isSelf=" + isSelf + ",userID=" + userID + ",userName="
            // + userName + "isBigScreen==" + isBigScreen);
            RenderHolder renderHolder = connectedRemoteRenders.get(key);
            renderHolder.userName = userName;
            renderHolder.userId = userID;
            renderHolder.tag = tag;
            if (isBigScreen) {
                holderBigContainer.removeView(renderHolder.containerLayout);
            } else {
                holderContainer.removeView(renderHolder.containerLayout);
            }

            render.setZOrderOnTop(true);
            render.setZOrderMediaOverlay(true);
            render.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);

            //            renderHolder.release();
            renderHolder.coverView.setRongRTCVideoView(render);

            if (isBigScreen) {
                holderBigContainer.addView(renderHolder, screenWidth, screenHeight);
            } else {
                renderHolder.coverView.showNameIndexView();
                holderContainer.addView(renderHolder.containerLayout, remoteLayoutParams);
            }
            renderHolder.init(isSelf);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "refreshRemoteView Error=" + e.getMessage());
        }
        Log.i(TAG, "refreshRemoteView End");
    }

    private void smallView(
            boolean isSelf,
            String userID,
            String tag,
            String userName,
            RCRTCVideoView render,
            String talkType) {
        RenderHolder renderHolder = null;
        if (containsKeyVideoViewEntiry(userID, tag)) {
            renderHolder = getViewHolder(userID, tag);
            renderHolder.userName = userName;
        } else {
            renderHolder = createRenderHolder(); // unUsedRemoteRenders.get(0);
            renderHolder.userName = userName;
            renderHolder.tag = tag;
            renderHolder.userId = userID;
            renderHolder.initCover(talkType);
            addVideoViewEntiry(userID, tag, renderHolder);

            renderHolder.coverView.showNameIndexView();
            holderContainer.addView(renderHolder.containerLayout, remoteLayoutParams);
            toggleTips();

            //            unUsedRemoteRenders.remove(0);
        }
        renderHolder.userId = userID;

//        render.setOnClickListener(new RemoteRenderClickListener(renderHolder));
        connectedRemoteRenders.put(generateKey(userID, tag), renderHolder);
        positionRenders.add(renderHolder);

        render.setZOrderOnTop(true);
        render.setZOrderMediaOverlay(true);
        render.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);

        renderHolder.coverView.setRongRTCVideoView(render);
        renderHolder.init(talkType, isSelf);

        if (!TextUtils.equals(talkType, RongRTCTalkTypeUtil.C_CAMERA)
                && !TextUtils.equals(talkType, RongRTCTalkTypeUtil.C_CM)) {
            renderHolder.coverView.showBlinkVideoView();
        }
    }

    private void largeView(
            boolean isSelf,
            String userID,
            String tag,
            String userName,
            RCRTCVideoView render,
            String talkType) {
        RenderHolder renderHolder = null;
        if (isSelf) {
            renderHolder = createRenderHolder(); // unUsedRemoteRenders.get(0);
            //            unUsedRemoteRenders.remove(0);
        } else {
            renderHolder = getViewHolder(userID, tag);
        }
        renderHolder.userName = userName;
        renderHolder.userId = userID;
        renderHolder.tag = tag;

        //        render.setOnClickListener(new RemoteRenderClickListener(renderHolder));
        // 添加缩放解决 观察者 横屏 进入pc的共享屏幕 导致的 显示不全问题
        render.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        connectedRemoteRenders.put(generateKey(userID, tag), renderHolder);

        render.setZOrderMediaOverlay(false);
        // init在setBlinkVideoView之前执行，不然 本地正常用户加入房间coverView=null；
        renderHolder.init(talkType, isSelf);
        renderHolder.coverView.setRongRTCVideoView(render);

        if (TextUtils.equals(talkType, RongRTCTalkTypeUtil.C_CAMERA)) {
            renderHolder.coverView.showUserHeader();
        } else {
            renderHolder.coverView.showBlinkVideoView();
        }
        holderBigContainer.addView(renderHolder, screenWidth, screenHeight);

        toggleTips();
        saveSelectUserId(renderHolder);
        selectedRender = renderHolder;

        if (!isSelf) {
        } else {
            addVideoViewEntiry(userID, tag, renderHolder);
        }
    }

    public void rotateView() {
        getSize(); // 重新获取屏幕的宽高 w:1920 h:1080
        holderBigContainer.refreshView(this.screenWidth, this.screenHeight);
    }

    // todo delete
    public void onTrackadd(String userId, String tag) {
        RenderHolder renderHolder = getViewHolder(userId, tag);
        Log.i(TAG, "onTrackadd() renderHolder = " + renderHolder);
        if (renderHolder != null) {
            renderHolder.coverView.setTrackisAdded();
        }
    }

    public void onFirstFrameDraw(String userId, String tag) {
        RenderHolder renderHolder = getViewHolder(userId, tag);
        Log.i(TAG, "onFirstFrameDraw() renderHolder = " + renderHolder);
        if (renderHolder != null) {
            renderHolder.coverView.setFirstDraw();
        }
    }

    /**
     * 退出聊天室 降级时用到
     *
     * @param userID
     */
    public void removeVideoView(String userID) {
        SessionManager.getInstance().remove("color" + userID);
        List<RenderHolder> renderHolderList = getViewHolderByUserId(userID);
        if (renderHolderList.size() > 0) {
            for (RenderHolder renderHolder : renderHolderList) {
                removeVideoView(renderHolder);
            }
            toggleTips();
        }
    }

    private void removeVideoView(RenderHolder renderHolder) {
        if (renderHolder.containerLayout != null) {
            holderContainer.removeView(renderHolder.containerLayout);
            renderHolder.coverView.removeAllViews();
            removeVideoViewEntiry(renderHolder.userId, renderHolder.tag);
        }
        String key = generateKey(renderHolder.userId, renderHolder.tag);
        if (connectedRemoteRenders.containsKey(key)) {

            FinLog.d("render:", "connectedRemoteRenders.containsKey userid =" + renderHolder.userId);
            connectedRemoteRenders.get(key).release();
            RenderHolder releaseTarget = connectedRemoteRenders.remove(key);

            try {
                if (releaseTarget.coverView != null) {
                    if (releaseTarget.coverView.rongRTCVideoView != null
                        && releaseTarget.coverView.mRl_Container != null) {
                        releaseTarget.coverView.mRl_Container.removeView(releaseTarget.coverView.rongRTCVideoView);
                    }
                    releaseTarget.coverView = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (selectedRender == releaseTarget) {
                if (connectedRemoteRenders.size() != 0) {
                    Set set = connectedRemoteRenders.entrySet();
                    Iterator iterator = set.iterator();
                    while (iterator.hasNext()) {
                        Map.Entry mapentry = (Map.Entry) iterator.next();
                        // 从远程连接中获取到新的 渲染器
                        RenderHolder newRender = (RenderHolder) mapentry.getValue();
                        String id = (String) mapentry.getKey();
                        FinLog.d("render:", "删除小窗口：" + id);
                        if (TextUtils.equals(RCRTCStream.RONG_TAG, newRender.tag)) {
                            exchangeToNormalStream(newRender.userId);
                        }
                        holderContainer.removeView(newRender.containerLayout); // 小容器删除 layout
                        // 将新的渲染器添加到大容器中
                        holderBigContainer.addView(newRender, screenWidth, screenHeight);
                        selectedRender = newRender; // 新渲染器辅给大窗口渲染器
                        positionRenders.remove(newRender); //
                        break;
                    }
                }
                FinLog.d("render:", "selectedRender == releaseTaget  remove:" + renderHolder.userId);
                holderBigContainer.removeView(releaseTarget.containerLayout);
            } else {
                FinLog.d("render:", " remove:" + renderHolder.userId);
                holderContainer.removeView(releaseTarget.containerLayout);
                positionRenders.remove(releaseTarget);
            }
            if (null != selectedRender
                    && null != selectedRender.coverView
                    && null != selectedRender.coverView.getRongRTCVideoView()) {
                selectedRender.coverView.getRongRTCVideoView().setZOrderMediaOverlay(false);
            }
            refreshViews();
        }
    }

    public void removeVideoView(boolean isSelf, String userId, String tag) {
        RenderHolder renderHolder = getViewHolder(userId, tag);
        if (renderHolder != null) {
            if (isSelf && customVideoCount > 0) {
                customVideoCount = customVideoCount - 1;
            }
            removeVideoView(renderHolder);
            toggleTips();
        }
    }

    public void refreshViews() {
        for (int i = 0; i < positionRenders.size(); i++) {
            RenderHolder holder = positionRenders.get(i);
            holder.coverView.getRongRTCVideoView().setZOrderMediaOverlay(true);
            holder.coverView.getRongRTCVideoView().requestLayout();
        }
        toggleTips();
    }

    /** 控制屏幕中間的提示 */
    private void toggleTips() {
        if (!hasConnectedUser()) {
            ((CallActivity) context).setWaitingTipsVisiable(true);
        } else {
            ((CallActivity) context).setWaitingTipsVisiable(false);
        }
    }

    public void toggleLocalView(boolean visible) {
        if (visible == (holderBigContainer.getVisibility() == View.VISIBLE)) return;
        if (visible) holderBigContainer.setVisibility(View.VISIBLE);
        else holderBigContainer.setVisibility(View.INVISIBLE);
    }

    /** Method to judge whether has conneted users */
    public boolean hasConnectedUser() {
        int size = isObserver ? 0 + customVideoCount : 1 + customVideoCount;
        // connectedRemoteRenders only contains local render by default. when its size is large than
        // 1, means new user joined
        return connectedRemoteRenders.size() > size;
    }

    public void updateTalkType(String userId, String tag, String talkType) {
        String key = generateKey(userId, tag);
        if (connectedRemoteRenders.containsKey(key)) {
            connectedRemoteRenders.get(key).CameraSwitch(talkType);
        }
    }

    public class RenderHolder implements OnGestureListener, OnTouchListener {
        RelativeLayout containerLayout;
        int targetZindex;
        CoverView coverView;
        public String talkType = "";
        private String userName, userId;
        private String tag = "";

        private GestureDetector gestureDetector;

        RenderHolder(RelativeLayout containerLayout, int index) {
            this.containerLayout = containerLayout;
            this.containerLayout.setOnTouchListener(this);
            this.gestureDetector = new GestureDetector(containerLayout.getContext(), this);
            this.containerLayout.setBackgroundColor(
                    VideoViewManager.this.context.getResources().getColor(R.color.blink_blue));
            targetZindex = index;
        }

        /** 设置摄像头被关闭后的封面视图 */
        public void setCoverView() {
            if (null == coverView) {
                coverView = new CoverView(context);
                coverView.mRenderHolder = this;
            }
            coverView.setUserInfo(userName, userId, tag);
            coverView.showUserHeader();
            RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            removeCoverView();
            this.containerLayout.addView(coverView, p);
        }

        public void updateUserInfo(String userName) {
            this.userName = userName;
            coverView.setUserInfo(userName, userId, tag);
        }

        public void removeCoverView() {
            this.containerLayout.removeView(coverView);
        }

        public void initCover(String talkType) {
            this.talkType = talkType;
            Log.i(TAG, "initCover  blinkTalkType==" + talkType + ",name=" + userName);
            setCoverView(); // 0-音频；1-视频；2-音频+视频；3-无
            coverView.showLoading();
            switch (talkType) {
                case RongRTCTalkTypeUtil.O_CAMERA:
                    break;
                case RongRTCTalkTypeUtil.O_MICROPHONE:
                    break;
                case RongRTCTalkTypeUtil.O_CM:
                    break;
                case RongRTCTalkTypeUtil.C_CAMERA:
                    coverView.closeLoading();
                    break;
                case RongRTCTalkTypeUtil.C_MICROPHONE:
                    break;
                case RongRTCTalkTypeUtil.C_CM:
                    coverView.closeLoading();
                    break;
            }
        }

        public void init(String talktype, boolean isSelf) {
            this.talkType = talktype;
            if (isSelf) {
                setCoverView();
            }
            blinkTalkType();
        }

        @Override
        public boolean onDown(MotionEvent motionEvent) {
            return true;
        }

        @Override
        public void onShowPress(MotionEvent motionEvent) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) {
            Log.d(TAG, "RenderHolder onSingleTapUp: ");
            if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                VideoViewManager.this.touchRender(this);
            }
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent motionEvent) {

        }

        @Override
        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
            return false;
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            return gestureDetector.onTouchEvent(motionEvent);
        }

        public void CameraSwitch(String talkType) {
            this.talkType = talkType;
            blinkTalkType();
        }

        public void init(boolean isSelf) {
            init(talkType, isSelf);
        }
        private void blinkTalkType() {
            switch (talkType) {
                case RongRTCTalkTypeUtil.O_CAMERA:
                    coverView.showBlinkVideoView();
                    break;
                case RongRTCTalkTypeUtil.O_MICROPHONE:
                    break;
                case RongRTCTalkTypeUtil.O_CM:
                    coverView.showBlinkVideoView();
                    break;
                case RongRTCTalkTypeUtil.C_CAMERA:
                    coverView.showUserHeader();
                    break;
                case RongRTCTalkTypeUtil.C_MICROPHONE:
                    break;
                case RongRTCTalkTypeUtil.C_CM:
                    coverView.showUserHeader();
                    break;
            }
        }

        public void release() {
            if (containerLayout != null) {
                containerLayout.requestLayout();
                containerLayout.removeAllViews();
            }
        }

        public String getUserName() {
            return userName;
        }

        public String getUserId() {
            return userId;
        }

        public String getTag() {
            return tag;
        }
    }

    private OnLocalVideoViewClickedListener onLocalVideoViewClickedListener;

    public void setOnLocalVideoViewClickedListener(
            OnLocalVideoViewClickedListener onLocalVideoViewClickedListener) {
        this.onLocalVideoViewClickedListener = onLocalVideoViewClickedListener;
    }

    /** 内部接口：用于本地视频图像的点击事件监听 */
    public interface OnLocalVideoViewClickedListener {
        void onClick();
    }

    private void saveSelectUserId(RenderHolder render) {
        if (null == selectedUserid) return;
        for (String userid : connectedRemoteRenders.keySet()) {
            if (connectedRemoteRenders.get(userid).equals(render)) {
                try {
                    selectedUserid.clear();
                    selectedUserid.add(userid);
                    if (mActivity.isSharing(userid)) {
                        //                        Toast.makeText(context,
                        // context.getResources().getString(R.string.meeting_control_OpenWiteBoard),
                        // Toast.LENGTH_SHORT).show();
                    }
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Boolean isBig(String userid) {
        if (null == selectedUserid) return false;
        else return selectedUserid.contains(userid);
    }

    private CallActivity mActivity = null;

    public void setActivity(CallActivity activity) {
        mActivity = activity;
    }

    public void delSelect(String userid) {
        selectedUserid.remove(userid);
    }

    public List<RenderHolder> idQueryHolder(String userid) {
        List<RenderHolder> renderHolderList = new ArrayList<>();
        //        connectedVideoViewEntity connectedVideoViewEntity=new
        // connectedVideoViewEntity(renderHolder,RongIMClient.getInstance().getCurrentUserId());
        for (int i = 0; i < connectedUsers.size(); i++) {
            if (null != connectedUsers.get(i)
                && !TextUtils.isEmpty(connectedUsers.get(i).getUserId())
                && connectedUsers.get(i).getUserId().equals(userid)) {
                RenderHolder renderHolder = connectedUsers.get(i).getRenderHolder();
                renderHolderList.add(renderHolder);
            }
        }
        return renderHolderList;
    }

    public List<RenderHolder> getViewHolderByUserId(String userid) {
        List<RenderHolder> renderHolderList = new ArrayList<>();
        for (int i = 0; i < connectedUsers.size(); i++) {
            if (null != connectedUsers.get(i)
                && !TextUtils.isEmpty(connectedUsers.get(i).getUserId())
                && connectedUsers.get(i).getUserId().equals(userid)) {
                RenderHolder renderHolder = connectedUsers.get(i).getRenderHolder();
                renderHolderList.add(renderHolder);
            }
        }
        return renderHolderList;
    }

    public RenderHolder getViewHolder(String userId, String tag) {
        RenderHolder renderHolder = null;
        for (int i = 0; i < connectedUsers.size(); i++) {
            if (null != connectedUsers.get(i)
                && !TextUtils.isEmpty(connectedUsers.get(i).getUserId())
                && connectedUsers.get(i).getUserId().equals(userId)
                && connectedUsers.get(i).getTag().equals(tag)) {
                renderHolder = connectedUsers.get(i).getRenderHolder();
                break;
            }
        }
        return renderHolder;
    }

    public RenderHolder getViewHolder(String userId) {
        if (connectedRemoteRenders == null || TextUtils.isEmpty(userId)) {
            return null;
        }
        Iterator it = connectedRemoteRenders.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String key = (String) entry.getKey();
            if (!TextUtils.isEmpty(key) && key.startsWith(userId)) {
                return (RenderHolder) entry.getValue();
            }

        }
        return null;
    }

    /**
     * @param userid
     * @return 存在 true
     */
    public boolean containsKeyVideoViewEntiry(String userid, String tag) {
        boolean bool = false;
        for (int i = 0; i < connectedUsers.size(); i++) {
            if (null != connectedUsers.get(i)
                && !TextUtils.isEmpty(connectedUsers.get(i).getUserId())
                && connectedUsers.get(i).getUserId().equals(userid)
                && connectedUsers.get(i).getTag().equals(tag)) {
                bool = true;
            }
        }
        return bool;
    }

    public void addVideoViewEntiry(String userid, String tag, RenderHolder holder) {
        connectedVideoViewEntity connectedVideoViewEntity = new connectedVideoViewEntity(holder, userid, tag);
        for (int i = 0; i < connectedUsers.size(); i++) {
            if (null != connectedUsers.get(i)
                && !TextUtils.isEmpty(connectedUsers.get(i).getUserId())
                && connectedUsers.get(i).getUserId().equals(userid)
                && connectedUsers.get(i).getTag().equals(tag)) {
                connectedUsers.remove(i);
            }
        }
        connectedUsers.add(connectedVideoViewEntity);
    }

    public void removeVideoViewEntiry(String userid, String tag) {
        for (int i = 0; i < connectedUsers.size(); i++) {
            if (null != connectedUsers.get(i)
                && !TextUtils.isEmpty(connectedUsers.get(i).getUserId())
                && connectedUsers.get(i).getUserId().equals(userid)
                && connectedUsers.get(i).getTag().equals(tag)) {
                connectedUsers.remove(i);
            }
        }
    }

    private boolean userIDEndWithScreenSharing(String userID) {
        return !TextUtils.isEmpty(userID) && userID.endsWith(SCREEN_SHARING);
    }

    public void setRongRTCRoom(RCRTCRoom rongRTCRoom) {
        this.rongRTCRoom = rongRTCRoom;
    }

    private void exchangeToNormalStream(final String targetId) {
        if (rongRTCRoom == null) {
            return;
        }
        RCRTCRemoteUser remoteUser = rongRTCRoom.getRemoteUser(targetId);
        if (remoteUser == null) {
            FinLog.v(TAG, "not get the remote user = " + targetId);
            return;
        }
        FinLog.d(TAG,"exchangeToNormalStream->switchToNormalStream");
        remoteUser.switchToNormalStream(new IRCRTCResultCallback() {
            @Override
            public void onSuccess() {
                FinLog.v(TAG, targetId + " exchangeStreamToNormalStream success !");
            }

            @Override
            public void onFailed(RTCErrorCode errorCode) {
                FinLog.e(TAG, targetId + " exchangeStreamToNormalStream failed ! errorCode: " + errorCode);
            }
        });
    }

    /** ******************* ContainerLayoutGestureEvents ******************************** */
    @Override
    public void onSingleClick() {

        if (onLocalVideoViewClickedListener != null) {
            onLocalVideoViewClickedListener.onClick();
        }
    }

    @Override
    public void onDoubleClick() {
        switchDebugInfoViewVisibility();
    }
}
