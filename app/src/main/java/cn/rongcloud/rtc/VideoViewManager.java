package cn.rongcloud.rtc;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;


import cn.rongcloud.rtc.callback.RongRTCResultUICallBack;
import cn.rongcloud.rtc.entity.connectedVideoViewEntity;

import cn.rongcloud.rtc.room.RongRTCRoom;
import cn.rongcloud.rtc.stream.MediaStreamTypeMode;
import cn.rongcloud.rtc.user.RongRTCRemoteUser;
import cn.rongcloud.rtc.util.SessionManager;
import cn.rongcloud.rtc.util.Utils;
import cn.rongcloud.rtc.core.RendererCommon;
import cn.rongcloud.rtc.utils.FinLog;
import cn.rongcloud.rtc.engine.view.RongRTCVideoView;
import cn.rongcloud.rtc.util.RongRTCTalkTypeUtil;
import cn.rongcloud.rtc.util.CoverView;
import io.rong.imlib.RongIMClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.rongcloud.rtc.util.Utils.SCREEN_SHARING;

/**
 * Created by Huichao on 2016/8/26.
 */
public class VideoViewManager {

    private static final String TAG = "VideoViewManager";
//    private RelativeLayout remoteRenderLayout, remoteRenderLayout2, remoteRenderLayout3, remoteRenderLayout4, remoteRenderLayout5, remoteRenderLayout6, remoteRenderLayout7, remoteRenderLayout8, remoteRenderLayout9;

    private Context context;
    private LinearLayout holderContainer;
    private ContainerLayout holderBigContainer;
    private RenderHolder selectedRender;
    private LinearLayout debugInfoView;
    public boolean isObserver;
//    private List<RenderHolder> unUsedRemoteRenders = new ArrayList<>();
    private int screenWidth;
    private int screenHeight;
    private RongRTCRoom rongRTCRoom;

    public HashMap<String, RenderHolder> connetedRemoteRenders = new HashMap<>();
    private List<connectedVideoViewEntity> connectedUsers = new ArrayList<>();

    private ArrayList<RenderHolder> positionRenders = new ArrayList<>();

    LinearLayout.LayoutParams remoteLayoutParams;
    /**
     * 自定义视频流的数量
     */
    private int customVideoCount = 0;
//    RelativeLayout.LayoutParams localLayoutParams;
    /**
     * 存储当前显示在大屏幕上的用户id
     **/
    private List<String> selectedUserid = new ArrayList<>();

    public void initViews(Context context, boolean isObserver) {
        this.context = context;
        getSize();
        int base = screenHeight < screenWidth ? screenHeight : screenWidth;
        remoteLayoutParams = new LinearLayout.LayoutParams(base / 4, base / 3);

        SessionManager.getInstance(Utils.getContext()).put(Utils.KEY_screeHeight, base / 3);
        SessionManager.getInstance(Utils.getContext()).put(Utils.KEY_screeWidth, base / 4);

        holderContainer = (LinearLayout) ((Activity) context).findViewById(R.id.call_reder_container);
        holderBigContainer = (ContainerLayout) ((Activity) context).findViewById(R.id.call_render_big_container);
        debugInfoView = (LinearLayout) ((Activity) context).findViewById(R.id.debug_info);
        debugInfoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doubleClick(false);
            }
        });


        this.isObserver = isObserver;

        holderBigContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onLocalVideoViewClickedListener != null)
                    onLocalVideoViewClickedListener.onClick();
            }
        });

        holderContainer.removeAllViews();
        holderBigContainer.removeAllViews();
        toggleTips();
    }

    private void getSize() {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);

        screenWidth = wm.getDefaultDisplay().getWidth();
        screenHeight = wm.getDefaultDisplay().getHeight();
    }

    public void onCreateEglFailed(String userId,String tag) {
        VideoViewManager.RenderHolder renderHolder = getViewHolder(userId, tag);
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
            doubleClick(true);
        }
    }

    long lastClickTime = 0;
    int tapStep = 0;

    private void doubleClick(boolean isShow) {
        if (!BuildConfig.DEBUG)
            return;
        long timeDuration = System.currentTimeMillis() - lastClickTime;
        if (timeDuration > 300) {
            tapStep = 0;
            lastClickTime = 0;
        } else {
            tapStep++;
            if (tapStep == 1) {
                if (isShow)
                    debugInfoView.setVisibility(View.VISIBLE);
                else debugInfoView.setVisibility(View.GONE);
            }
        }
        lastClickTime = System.currentTimeMillis();
    }

    public void touchRender(RenderHolder render) {
        if (render == selectedRender) {//被点击的是大窗口，不做窗口切换
            if (onLocalVideoViewClickedListener != null)
                onLocalVideoViewClickedListener.onClick();
            return;
        }
        ArrayList<MediaStreamTypeMode> mediaStreamTypeModeList = new ArrayList<MediaStreamTypeMode>();
        int index = positionRenders.indexOf(render);
        final RenderHolder lastSelectedRender = selectedRender;
        positionRenders.set(index, selectedRender);
        selectedRender = render;
        if (!lastSelectedRender.userId.equals(RongIMClient.getInstance().getCurrentUserId())) {
            //原来的大窗口变小流
            MediaStreamTypeMode mediaStreamTypeModeTiny = new MediaStreamTypeMode();
            mediaStreamTypeModeTiny.uid = lastSelectedRender.userId;
            mediaStreamTypeModeTiny.flowType = "2";
            mediaStreamTypeModeList.add(mediaStreamTypeModeTiny);
            if (rongRTCRoom != null) {
                final String lastId = lastSelectedRender.userId;
                RongRTCRemoteUser remoteUser = rongRTCRoom.getRemoteUser(lastId);
                if (remoteUser != null) {
                    remoteUser.exchangeStreamToTinyStream(new RongRTCResultUICallBack() { //切换成小流
                        @Override
                        public void onUiSuccess() {
                            FinLog.v(TAG, lastId + " exchangeStreamToTinyStream success !");
                        }

                        @Override
                        public void onUiFailed(RTCErrorCode errorCode) {
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

        //大窗口显示于宿主窗口下层
        selectedRender.coverView.getRongRTCVideoView().setZOrderMediaOverlay(false);

        if (!selectedRender.userId.equals(RongIMClient.getInstance().getCurrentUserId())) {
            //原来的小窗口变大流
            MediaStreamTypeMode mediaStreamTypeMode = new MediaStreamTypeMode();
            mediaStreamTypeMode.uid = selectedRender.userId;
            mediaStreamTypeMode.flowType = "1";
            mediaStreamTypeModeList.add(mediaStreamTypeMode);
            if (rongRTCRoom != null) {
                final String targetId = selectedRender.userId;
                RongRTCRemoteUser remoteUser = rongRTCRoom.getRemoteUser(targetId);
                if (remoteUser != null) {
                    remoteUser.exchangeStreamToNormalStream(new RongRTCResultUICallBack() { //切换大流
                        @Override
                        public void onUiSuccess() {
                            FinLog.v(TAG, targetId + " exchangeStreamToNormalStream success !");
                        }

                        @Override
                        public void onUiFailed(RTCErrorCode errorCode) {
                            FinLog.e(TAG, targetId + " exchangeStreamToNormalStream failed ! errorCode: " + errorCode);
                        }
                    });
                } else {
                    FinLog.v(TAG, "not get the remote user = " + targetId);
                }
            }
        }
        holderBigContainer.addView(selectedRender, screenWidth, screenHeight);

        //添加之后设置VIdeoView的缩放类型,防止旋转屏幕之后 pc共享 再切换大小 显示不全问题
        selectedRender.coverView.getRongRTCVideoView().setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);

//        lastSelectedRender.coverView.getBlinkVideoView().setZOrderOnTop(true);
        lastSelectedRender.coverView.getRongRTCVideoView().setZOrderMediaOverlay(true);

        //防止横竖切换 再 小大切换 导致的小屏尺寸没更新 host——304
        lastSelectedRender.coverView.getRongRTCVideoView().setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        //pc分屏时 必须通知videoview listener 去刷新size//手动转屏不需要通知listerer
        holderContainer.addView(lastSelectedRender.containerLayout, index, remoteLayoutParams);
        saveSelectUserId(render);
    }

    private RenderHolder createRenderHolder(){
        RelativeLayout layout = new RelativeLayout(context);
        layout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
        return new RenderHolder(layout, 0);
    }

    public void userJoin(String userID, String tag, String userName, String talkType) {
        try {
            Log.i(TAG, "connectedUsers=" + connectedUsers.size() + ",userName=" + userName);
            if (connectedUsers.size() == 0) {
                RenderHolder renderHolder = createRenderHolder();//unUsedRemoteRenders.get(0);
                renderHolder.userName = userName;
                renderHolder.userId = userID;
                renderHolder.tag = tag;
                renderHolder.initCover(talkType);

                addVideoViewEntiry(userID, tag, renderHolder);
//                unUsedRemoteRenders.remove(0);
            }
            if (connectedUsers.size() != 0 && connectedUsers != null && !containsKeyVideoViewEntiry(userID, tag)) {
                RenderHolder renderHolder = createRenderHolder();//unUsedRemoteRenders.get(0);
                renderHolder.userName = userName;
                renderHolder.userId = userID;
                renderHolder.tag = tag;
                renderHolder.initCover(talkType);
                holderContainer.addView(renderHolder.containerLayout, remoteLayoutParams);

                addVideoViewEntiry(userID, tag, renderHolder);
//                unUsedRemoteRenders.remove(0);
            }

            toggleTips();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setVideoView(boolean isSelf, String userID, String tag, String userName, RongRTCVideoView render, String talkType) {
        HashMap<String, MediaStreamTypeMode> mediaStreamTypeModeMap = new HashMap<>();
        MediaStreamTypeMode mediaStreamTypeMode = null;
        Log.i(TAG, ">>>>>>>>>>>>>>>>setVideoView isSelf==" + isSelf);
        if (!connetedRemoteRenders.containsKey(generateKey(userID,tag))) {
            if (isSelf) {

                if (!CenterManager.RONG_TAG.equals(tag) && !TextUtils.isEmpty(tag)) {//自定义视频流的视图
                    customVideoCount = customVideoCount + 1;
                }

                if (connetedRemoteRenders.size() == 0) {
                    mediaStreamTypeMode = largeView(isSelf, userID, tag, userName, render, talkType);
                } else {
                    mediaStreamTypeMode = smallView(isSelf, userID, tag, userName, render, talkType);
                }
            } else {
                if (null != connectedUsers && connectedUsers.size() > 0 && null != connectedUsers.get(0) && !TextUtils.isEmpty(connectedUsers.get(0).getUserId()) &&
                        connectedUsers.get(0).getKey().equals(generateKey(userID, tag))) {
                    //放入大屏的远端用户需要切换到大流
                    mediaStreamTypeMode = largeView(isSelf, userID, tag, userName, render, talkType);
                    if (rongRTCRoom != null) {
                        final String targetId = selectedRender.userId;
                        RongRTCRemoteUser remoteUser = rongRTCRoom.getRemoteUser(userID);
                        //todo 切换大流
                        remoteUser.exchangeStreamToNormalStream(new RongRTCResultUICallBack() { //切换大流
                            @Override
                            public void onUiSuccess() {
                                FinLog.v(TAG,targetId+" exchangeStreamToNormalStream success !");
                            }

                            @Override
                            public void onUiFailed(RTCErrorCode errorCode) {
                                FinLog.e(TAG,targetId+" exchangeStreamToNormalStream failed ! errorCode: "+errorCode);
                            }
                        });
                    }
                } else {
                    mediaStreamTypeMode = smallView(isSelf, userID, tag, userName, render, talkType);
                }
            }
        } else {
            refreshRemoteView(isSelf, userID, tag, userName, render, mediaStreamTypeModeMap);
        }
        if (null != mediaStreamTypeMode) {
            mediaStreamTypeModeMap.put(userID, mediaStreamTypeMode);
        }
        //默认加载小流，新加入流时不需要切换大小流
        //sendSubscribeStream(mediaStreamTypeModeMap);
    }

    private String generateKey(String userId, String tag) {
        return userId + "_" + tag;
    }

    private void refreshRemoteView(boolean isSelf, String userID, String tag, String userName, RongRTCVideoView render, HashMap<String, MediaStreamTypeMode> mediaStreamTypeModeMap) {
        MediaStreamTypeMode mediaStreamTypeMode = new MediaStreamTypeMode();
        try {
            String key = generateKey(userID, tag);
            boolean isBigScreen = selectedUserid != null && selectedUserid.contains(key);
//            Log.v(TAG, "refreshRemoteView unUsedRemoteRenders size=" + unUsedRemoteRenders.size() + ",isSelf=" + isSelf + ",userID=" + userID + ",userName=" + userName + "isBigScreen==" + isBigScreen);
            RenderHolder renderHolder = connetedRemoteRenders.get(key);
            renderHolder.userName = userName;
            renderHolder.userId = userID;
            renderHolder.tag  = tag;
            if (isBigScreen) {
                holderBigContainer.removeView(renderHolder.containerLayout);
            } else {
                holderContainer.removeView(renderHolder.containerLayout);
            }

            render.setOnClickListener(new RemoteRenderClickListener(renderHolder));
            render.setZOrderOnTop(true);
            render.setZOrderMediaOverlay(true);
            render.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);

//            renderHolder.release();
            renderHolder.coverView.setRongRTCVideoView(render);

            if (isBigScreen) {
                holderBigContainer.addView(renderHolder, screenWidth, screenHeight);
            } else {
                holderContainer.addView(renderHolder.containerLayout, remoteLayoutParams);
            }
            renderHolder.init(isSelf);

            if (mediaStreamTypeModeMap != null && mediaStreamTypeModeMap.containsKey(userID)) {
                mediaStreamTypeMode.uid = userID;
                mediaStreamTypeMode.flowType = mediaStreamTypeModeMap.get(userID).flowType;
                mediaStreamTypeMode.tag = tag;
                mediaStreamTypeModeMap.put(key, mediaStreamTypeModeMap.get(userID));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "refreshRemoteView Error=" + e.getMessage());
        }
        Log.i(TAG, "refreshRemoteView End");
    }

    private MediaStreamTypeMode smallView(boolean isSelf, String userID, String tag, String userName, RongRTCVideoView render, String talkType) {
        MediaStreamTypeMode mediaStreamTypeMode = new MediaStreamTypeMode();
        RenderHolder renderHolder = null;
//        Log.v(TAG, "smallView unUsedRemoteRenders size=" + unUsedRemoteRenders.size() + ",isSelf=" + isSelf + ",userID=" + userID + ",userName=" + userName + ",talkType=" + talkType);
        if (containsKeyVideoViewEntiry(userID,tag)) {
            renderHolder = getViewHolder(userID,tag);
            renderHolder.userName = userName;
        } else {
            renderHolder = createRenderHolder();//unUsedRemoteRenders.get(0);
            renderHolder.userName = userName;
            renderHolder.tag = tag;
            renderHolder.userId = userID;
            renderHolder.initCover(talkType);
            addVideoViewEntiry(userID, tag, renderHolder);

            holderContainer.addView(renderHolder.containerLayout, remoteLayoutParams);
            toggleTips();

//            unUsedRemoteRenders.remove(0);
        }
        renderHolder.userId = userID;

        render.setOnClickListener(new RemoteRenderClickListener(renderHolder));
        connetedRemoteRenders.put(generateKey(userID, tag), renderHolder);
        positionRenders.add(renderHolder);

        render.setZOrderOnTop(true);
        render.setZOrderMediaOverlay(true);
        render.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);

        renderHolder.coverView.setRongRTCVideoView(render);
        renderHolder.init(talkType, isSelf);

        if (!TextUtils.equals(talkType,RongRTCTalkTypeUtil.C_CAMERA) && !TextUtils.equals(talkType,RongRTCTalkTypeUtil.C_CM)) {
            renderHolder.coverView.showBlinkVideoView();
        }

        mediaStreamTypeMode.uid = userID;
        mediaStreamTypeMode.flowType = "2";
        mediaStreamTypeMode.tag = tag;
        if (userIDEndWithScreenSharing(userID)) {
            mediaStreamTypeMode = null;
        }
        return mediaStreamTypeMode;
    }

    private MediaStreamTypeMode largeView(boolean isSelf, String userID, String tag,String userName, RongRTCVideoView render, String talkType) {
        MediaStreamTypeMode mediaStreamTypeMode = null;
        RenderHolder renderHolder = null;
//        Log.v(TAG, "largeView unUsedRemoteRenders size=" + unUsedRemoteRenders.size() + ",isSelf=" + isSelf + ",userID=" + userID + ",userName=" + userName + ",talkType=" + talkType);
        if (isSelf) {
            renderHolder = createRenderHolder();//unUsedRemoteRenders.get(0);
//            unUsedRemoteRenders.remove(0);
        } else {
            renderHolder = getViewHolder(userID, tag);
        }
        renderHolder.userName = userName;
        renderHolder.userId = userID;
        renderHolder.tag =tag;

        render.setOnClickListener(new RemoteRenderClickListener(renderHolder));
        //添加缩放解决 观察者 横屏 进入pc的共享屏幕 导致的 显示不全问题
        render.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        connetedRemoteRenders.put(generateKey(userID, tag), renderHolder);

        render.setZOrderMediaOverlay(false);
        //init在setBlinkVideoView之前执行，不然 本地正常用户加入房间coverView=null；
        renderHolder.init(talkType, isSelf);
        renderHolder.coverView.setRongRTCVideoView(render);

        if (isSelf && TextUtils.equals(talkType ,RongRTCTalkTypeUtil.C_CAMERA)) {
            renderHolder.coverView.showUserHeader();
        } else {
            renderHolder.coverView.showBlinkVideoView();
        }
        holderBigContainer.addView(renderHolder, screenWidth, screenHeight);

        toggleTips();
        saveSelectUserId(renderHolder);
        selectedRender = renderHolder;

        if (!isSelf) {
            mediaStreamTypeMode = new MediaStreamTypeMode();
            mediaStreamTypeMode.uid = userID;
            mediaStreamTypeMode.flowType = "1";
            mediaStreamTypeMode.tag = tag;
        } else {
            addVideoViewEntiry(userID, tag,renderHolder);
        }
        if (userIDEndWithScreenSharing(userID)) {
            mediaStreamTypeMode = null;
        }
        return mediaStreamTypeMode;
    }

    public void rotateView() {
        getSize();//重新获取屏幕的宽高 w:1920 h:1080
        holderBigContainer.refreshView(this.screenWidth, this.screenHeight);
    }

    //todo delete
    public void onTrackadd(String userId, String tag) {
        VideoViewManager.RenderHolder renderHolder = getViewHolder(userId, tag);
        Log.i(TAG, "onTrackadd() renderHolder = " + renderHolder);
        if (renderHolder != null) {
            renderHolder.coverView.setTrackisAdded();
        }
    }

    public void onFirstFrameDraw(String userId, String tag) {
        VideoViewManager.RenderHolder renderHolder = getViewHolder(userId, tag);
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
        SessionManager.getInstance(Utils.getContext()).remove("color" + userID);
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
        if (connetedRemoteRenders.containsKey(key)) {

            FinLog.d("render:", "connetedRemoteRenders.containsKey userid =" + renderHolder.userId);
            connetedRemoteRenders.get(key).release();
            RenderHolder releaseTaget = connetedRemoteRenders.remove(key);

            try {
                if (releaseTaget.coverView != null) {
                    if (releaseTaget.coverView.rongRTCVideoView != null) {
                        if (releaseTaget.coverView.mRl_Container != null) {
                            releaseTaget.coverView.mRl_Container.removeView(releaseTaget.coverView.rongRTCVideoView);
                        }
                    }
                    releaseTaget.coverView = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (selectedRender == releaseTaget) {
                if (connetedRemoteRenders.size() != 0) {
                    Set set = connetedRemoteRenders.entrySet();
                    Iterator iterator = set.iterator();
                    while (iterator.hasNext()) {
                        Map.Entry mapentry = (Map.Entry) iterator.next();
                        RenderHolder newRender = (RenderHolder) mapentry.getValue();// 从远程连接中获取到新的 渲染器
                        String id = (String) mapentry.getKey();
                        FinLog.d("render:", "删除小窗口：" + id);
                        holderContainer.removeView(newRender.containerLayout);// 小容器删除 layout
                        holderBigContainer.addView(newRender, screenWidth, screenHeight);//将新的渲染器添加到大容器中
                        selectedRender = newRender;// 新渲染器辅给大窗口渲染器
                        positionRenders.remove(newRender);//
                        break;
                    }
                }
                FinLog.d("render:", "selectedRender == releaseTaget  remove:" + renderHolder.userId);
                holderBigContainer.removeView(releaseTaget.containerLayout);
            } else {
                FinLog.d("render:", " remove:" + renderHolder.userId);
                holderContainer.removeView(releaseTaget.containerLayout);
                positionRenders.remove(releaseTaget);
            }
            if (null != selectedRender && null != selectedRender.coverView && null != selectedRender.coverView.getRongRTCVideoView()) {
                selectedRender.coverView.getRongRTCVideoView().setZOrderMediaOverlay(false);
            }
            refreshViews();
        }
    }

    public void removeVideoView(boolean isSelf,String userId, String tag) {
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

    /**
     * 控制屏幕中間的提示
     */
    private void toggleTips() {
        if (!hasConnectedUser()) {
            ((CallActivity) context).setWaitingTipsVisiable(true);
        } else {
            ((CallActivity) context).setWaitingTipsVisiable(false);
        }
    }

    public void toggleLocalView(boolean visible) {
        if (visible == (holderBigContainer.getVisibility() == View.VISIBLE))
            return;
        if (visible)
            holderBigContainer.setVisibility(View.VISIBLE);
        else holderBigContainer.setVisibility(View.INVISIBLE);
    }

    /**
     * Method to judge whether has conneted users
     */
    public boolean hasConnectedUser() {
        int size = isObserver ? 0 +customVideoCount: 1+customVideoCount;
        //connectedRemoteRenders only contains local render by default. when its size is large than 1, means new user joined
        return connetedRemoteRenders.size() > size;
    }

    public void updateTalkType(String userId,String tag, String talkType) {
        String key = generateKey(userId,tag);
        if (connetedRemoteRenders.containsKey(key)) {
            connetedRemoteRenders.get(key).CameraSwitch(talkType);
        }
    }

    public class RenderHolder {
        RelativeLayout containerLayout;
        int targetZindex;
        CoverView coverView;
        public String talkType = "";
        private String userName, userId;
        private String tag="";

        RenderHolder(RelativeLayout containerLayout, int index) {
            this.containerLayout = containerLayout;
            targetZindex = index;
        }

        /**
         * 设置摄像头被关闭后的封面视图
         */
        public void setCoverView() {
            if (null == coverView) {
                coverView = new CoverView(context);
                coverView.mRenderHolder = this;
            }
            coverView.setUserInfo(userName, userId, tag);
            coverView.showUserHeader();
            RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
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
            setCoverView();//0-音频；1-视频；2-音频+视频；3-无
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
    }

    private OnLocalVideoViewClickedListener onLocalVideoViewClickedListener;

    public void setOnLocalVideoViewClickedListener(OnLocalVideoViewClickedListener onLocalVideoViewClickedListener) {
        this.onLocalVideoViewClickedListener = onLocalVideoViewClickedListener;
    }

    /**
     * 内部接口：用于本地视频图像的点击事件监听
     */
    public interface OnLocalVideoViewClickedListener {
        void onClick();
    }

    private void saveSelectUserId(RenderHolder render) {
        if (null == selectedUserid) return;
        for (String userid : connetedRemoteRenders.keySet()) {
            if (connetedRemoteRenders.get(userid).equals(render)) {
                try {
                    selectedUserid.clear();
                    selectedUserid.add(userid);
                    if (mActivity.isSharing(userid)) {
//                        Toast.makeText(context, context.getResources().getString(R.string.meeting_control_OpenWiteBoard), Toast.LENGTH_SHORT).show();
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

    public List<VideoViewManager.RenderHolder> idQueryHolder(String userid) {
        List<VideoViewManager.RenderHolder> renderHolderList = new ArrayList<>();
//        connectedVideoViewEntity connectedVideoViewEntity=new connectedVideoViewEntity(renderHolder,RongIMClient.getInstance().getCurrentUserId());
        if (null != connectedUsers) {
            for (int i = 0; i < connectedUsers.size(); i++) {
                if (null != connectedUsers.get(i) && !TextUtils.isEmpty(connectedUsers.get(i).getUserId()) && connectedUsers.get(i).getUserId().equals(userid)) {
                    VideoViewManager.RenderHolder renderHolder = connectedUsers.get(i).getRenderHolder();
                    renderHolderList.add(renderHolder);

                }
            }
        }
        return renderHolderList;
    }

    public List<VideoViewManager.RenderHolder> getViewHolderByUserId(String userid) {
        List<VideoViewManager.RenderHolder> renderHolderList = new ArrayList<>();
        if (null != connectedUsers) {
            for (int i = 0; i < connectedUsers.size(); i++) {
                if (null != connectedUsers.get(i) && !TextUtils.isEmpty(connectedUsers.get(i).getUserId()) && connectedUsers.get(i).getUserId().equals(userid)) {
                    VideoViewManager.RenderHolder renderHolder = connectedUsers.get(i).getRenderHolder();
                    renderHolderList.add(renderHolder);

                }
            }
        }
        return renderHolderList;
    }

    public VideoViewManager.RenderHolder getViewHolder(String userId, String tag) {
        VideoViewManager.RenderHolder renderHolder = null;
        if (null != connectedUsers) {
            for (int i = 0; i < connectedUsers.size(); i++) {
                if (null != connectedUsers.get(i)
                        && !TextUtils.isEmpty(connectedUsers.get(i).getUserId()) && connectedUsers.get(i).getUserId().equals(userId)
                         && connectedUsers.get(i).getTag().equals(tag)) {
                    renderHolder = connectedUsers.get(i).getRenderHolder();
                    break;
                }
            }
        }
        return renderHolder;
    }

    /**
     * @param userid
     * @return 存在 true
     */
    public boolean containsKeyVideoViewEntiry(String userid,String tag) {
        boolean bool = false;
        if (null != connectedUsers) {
            for (int i = 0; i < connectedUsers.size(); i++) {
                if (null != connectedUsers.get(i)
                        && !TextUtils.isEmpty(connectedUsers.get(i).getUserId()) && connectedUsers.get(i).getUserId().equals(userid)
                        && connectedUsers.get(i).getTag().equals(tag) ) {
                    bool = true;
                }
            }
        }
        return bool;
    }

    public void addVideoViewEntiry(String userid, String tag, RenderHolder holder) {
        if (null != connectedUsers) {
            connectedVideoViewEntity connectedVideoViewEntity = new connectedVideoViewEntity(holder, userid, tag);
            for (int i = 0; i < connectedUsers.size(); i++) {
                if (null != connectedUsers.get(i)
                        && !TextUtils.isEmpty(connectedUsers.get(i).getUserId()) && connectedUsers.get(i).getUserId().equals(userid)
                        && connectedUsers.get(i).getTag().equals(tag)) {
                    connectedUsers.remove(i);
                }
            }
            connectedUsers.add(connectedVideoViewEntity);
        }
    }

    public void removeVideoViewEntiry(String userid, String tag) {
        if (null != connectedUsers) {
            for (int i = 0; i < connectedUsers.size(); i++) {
                if (null != connectedUsers.get(i)
                        && !TextUtils.isEmpty(connectedUsers.get(i).getUserId()) && connectedUsers.get(i).getUserId().equals(userid)
                        && connectedUsers.get(i).getTag().equals(tag)) {
                    connectedUsers.remove(i);
                }
            }
        }
    }

    private boolean userIDEndWithScreenSharing(String userID) {
        return !TextUtils.isEmpty(userID) && userID.endsWith(SCREEN_SHARING);
    }

    public void setRongRTCRoom(RongRTCRoom rongRTCRoom) {
        this.rongRTCRoom = rongRTCRoom;
    }
}