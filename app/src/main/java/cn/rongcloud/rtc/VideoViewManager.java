package cn.rongcloud.rtc;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;


import cn.rongcloud.rtc.entity.connectedVideoViewEntity;

import cn.rongcloud.rtc.util.SessionManager;
import cn.rongcloud.rtc.util.Utils;
import cn.rongcloud.rtc.engine.context.RongRTCContext;
import cn.rongcloud.rtc.core.RendererCommon;
import cn.rongcloud.rtc.engine.binstack.util.FinLog;
import cn.rongcloud.rtc.engine.view.RongRTCVideoView;
import cn.rongcloud.rtc.util.RongRTCTalkTypeUtil;
import cn.rongcloud.rtc.util.CoverView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.rongcloud.rtc.engine.context.RongRTCContext.ConfigParameter.userID;

/**
 * Created by Huichao on 2016/8/26.
 */
public class VideoViewManager {

    private static final String TAG="VideoViewManager";
    private RelativeLayout remoteRenderLayout, remoteRenderLayout2, remoteRenderLayout3, remoteRenderLayout4, remoteRenderLayout5, remoteRenderLayout6, remoteRenderLayout7, remoteRenderLayout8, remoteRenderLayout9;

    private Context context;
    private LinearLayout holderContainer;
    private ContainerLayout holderBigContainer;
    private RenderHolder selectedRender;
    private LinearLayout debugInfoView;
    public boolean isObserver;
    private List<RenderHolder> unUsedRemoteRenders = new ArrayList<>();
    private int screenWidth;
    private int screenHeight;

    public HashMap<String, RenderHolder> connetedRemoteRenders = new HashMap<>();
    private List<connectedVideoViewEntity> connectedUsers=new ArrayList<>();

    private ArrayList<RenderHolder> positionRenders = new ArrayList<>();

    LinearLayout.LayoutParams remoteLayoutParams;
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

        remoteRenderLayout = (RelativeLayout) ((Activity) context).findViewById(R.id.remote_video_layout);
        remoteRenderLayout2 = (RelativeLayout) ((Activity) context).findViewById(R.id.remote_video_layout2);
        remoteRenderLayout3 = (RelativeLayout) ((Activity) context).findViewById(R.id.remote_video_layout3);
        remoteRenderLayout4 = (RelativeLayout) ((Activity) context).findViewById(R.id.remote_video_layout4);
        remoteRenderLayout5 = (RelativeLayout) ((Activity) context).findViewById(R.id.remote_video_layout5);
        remoteRenderLayout6 = (RelativeLayout) ((Activity) context).findViewById(R.id.remote_video_layout6);
        remoteRenderLayout7 = (RelativeLayout) ((Activity) context).findViewById(R.id.remote_video_layout7);
        remoteRenderLayout8 = (RelativeLayout) ((Activity) context).findViewById(R.id.remote_video_layout8);
        remoteRenderLayout9 = (RelativeLayout) ((Activity) context).findViewById(R.id.remote_video_layout9);

        // Create video renderers.
        initRemoteRendersList();

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
        ArrayList<RongRTCContext.MediaStreamTypeMode> mediaStreamTypeModeList = new ArrayList<RongRTCContext.MediaStreamTypeMode>();
        int index = positionRenders.indexOf(render);
        RenderHolder lastSelectedRender = selectedRender;
        positionRenders.set(index, selectedRender);
        selectedRender = render;

        if (!lastSelectedRender.userId.equals(userID)) {
            //原来的大窗口变小流
            RongRTCContext.MediaStreamTypeMode mediaStreamTypeModeTiny = new RongRTCContext.MediaStreamTypeMode();
            mediaStreamTypeModeTiny.uid = lastSelectedRender.userId;
            mediaStreamTypeModeTiny.flowType = "2";
            mediaStreamTypeModeList.add(mediaStreamTypeModeTiny);
        }

        holderContainer.removeView(selectedRender.containerLayout);
        holderBigContainer.removeView(lastSelectedRender.containerLayout);

        //大窗口显示于宿主窗口下层
        selectedRender.coverView.getRongRTCVideoView().setZOrderMediaOverlay(false);

        if (!selectedRender.userId.equals(userID)) {
            //原来的小窗口变大流
            RongRTCContext.MediaStreamTypeMode mediaStreamTypeMode = new RongRTCContext.MediaStreamTypeMode();
            mediaStreamTypeMode.uid = selectedRender.userId;
            mediaStreamTypeMode.flowType = "1";
            mediaStreamTypeModeList.add(mediaStreamTypeMode);
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
        RongRTCEngine.getInstance().subscribeStream(mediaStreamTypeModeList);
    }

    private void initRemoteRendersList() {
        unUsedRemoteRenders.add(new RenderHolder(remoteRenderLayout, 0));
        unUsedRemoteRenders.add(new RenderHolder(remoteRenderLayout2, 1));
        unUsedRemoteRenders.add(new RenderHolder(remoteRenderLayout3, 2));
        unUsedRemoteRenders.add(new RenderHolder(remoteRenderLayout4, 3));
        unUsedRemoteRenders.add(new RenderHolder(remoteRenderLayout5, 4));
        unUsedRemoteRenders.add(new RenderHolder(remoteRenderLayout6, 5));
        unUsedRemoteRenders.add(new RenderHolder(remoteRenderLayout7, 6));
        unUsedRemoteRenders.add(new RenderHolder(remoteRenderLayout8, 7));
        unUsedRemoteRenders.add(new RenderHolder(remoteRenderLayout9, 8));
    }

    public void userJoin(String userID, String userName, String talkType) {
        try {
            Log.i(TAG,"connectedUsers="+connectedUsers.size()+",userName="+userName);
            if (connectedUsers.size() == 0) {
                RenderHolder renderHolder = unUsedRemoteRenders.get(0);
                renderHolder.userName = userName;
                renderHolder.userId = userID;
                renderHolder.initCover(talkType);

                addVideoViewEntiry(userID,renderHolder);
                unUsedRemoteRenders.remove(0);
                ((CallActivity) context).setWaitingTipsVisiable(false);
            }
            if (connectedUsers.size() != 0 && connectedUsers != null && !containsKeyVideoViewEntiry(userID)) {
                RenderHolder renderHolder = unUsedRemoteRenders.get(0);
                renderHolder.userName = userName;
                renderHolder.userId = userID;
                renderHolder.initCover(talkType);
                holderContainer.addView(renderHolder.containerLayout, remoteLayoutParams);

                addVideoViewEntiry(userID,renderHolder);
                unUsedRemoteRenders.remove(0);
                ((CallActivity) context).setWaitingTipsVisiable(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setVideoView(boolean isSelf, String userID, String userName, RongRTCVideoView render, String talkType) {
        HashMap<String,RongRTCContext.MediaStreamTypeMode> mediaStreamTypeModeMap=new HashMap<>();
        RongRTCContext.MediaStreamTypeMode mediaStreamTypeMode=null;
        Log.i(TAG,">>>>>>>>>>>>>>>>setVideoView isSelf=="+isSelf);
        if(!connetedRemoteRenders.containsKey(userID)){
            if(isSelf){
                if (connetedRemoteRenders.size() == 0) {
                    mediaStreamTypeMode=largeView(isSelf,userID,userName,render,talkType);
                }else{
                    mediaStreamTypeMode=smallView(isSelf,userID,userName,render,talkType);
                }
            }else{
                if (null!=connectedUsers && null!=connectedUsers.get(0) && !TextUtils.isEmpty(connectedUsers.get(0).getUserId()) &&
                        connectedUsers.get(0).getUserId().equals(userID)) {
                    mediaStreamTypeMode=largeView(isSelf,userID,userName,render,talkType);
                }else{
                    mediaStreamTypeMode=smallView(isSelf,userID,userName,render,talkType);
                }
            }
        }else{
            refreshRemoteView(isSelf,userID,userName,render,mediaStreamTypeModeMap);
        }
        if(null!=mediaStreamTypeMode){
            mediaStreamTypeModeMap.put(userID,mediaStreamTypeMode);
        }
        sendSubscribeStream(mediaStreamTypeModeMap);
    }

    private void refreshRemoteView(boolean isSelf, String userID, String userName, RongRTCVideoView render, HashMap<String, RongRTCContext.MediaStreamTypeMode> mediaStreamTypeModeMap) {
        RongRTCContext.MediaStreamTypeMode mediaStreamTypeMode = new RongRTCContext.MediaStreamTypeMode();
        try{
            boolean isBigScreen=selectedUserid!=null && selectedUserid.contains(userID);
            Log.i(TAG,"refreshRemoteView unUsedRemoteRenders size="+unUsedRemoteRenders.size()+",isSelf="+isSelf+",userID="+userID+",userName="+userName+"isBigScreen=="+isBigScreen);
            RenderHolder renderHolder = connetedRemoteRenders.get(userID);
            renderHolder.userName = userName;
            renderHolder.userId = userID;
            if(isBigScreen){
                holderBigContainer.removeView(renderHolder.containerLayout);
            }else{
                holderContainer.removeView(renderHolder.containerLayout);
            }

            render.setOnClickListener(new RemoteRenderClickListener(renderHolder));
            render.setZOrderOnTop(true);
            render.setZOrderMediaOverlay(true);
            render.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);

//            renderHolder.release();
            renderHolder.coverView.setRongRTCVideoView(render);

            if(isBigScreen){
                holderBigContainer.addView(renderHolder, screenWidth, screenHeight);
            }else{
                holderContainer.addView(renderHolder.containerLayout, remoteLayoutParams);
            }
            renderHolder.init(isSelf);

            if(mediaStreamTypeModeMap!=null && mediaStreamTypeModeMap.containsKey(userID)){
                mediaStreamTypeMode.uid = userID;
                mediaStreamTypeMode.flowType = mediaStreamTypeModeMap.get(userID).flowType;
                mediaStreamTypeModeMap.put(userID,mediaStreamTypeModeMap.get(userID));
            }
        }catch (Exception e){
            e.printStackTrace();
            Log.i(TAG,"refreshRemoteView Error="+e.getMessage());
        }
        Log.i(TAG,"refreshRemoteView End");
    }

    private RongRTCContext.MediaStreamTypeMode smallView(boolean isSelf, String userID, String userName, RongRTCVideoView render, String talkType) {
        RongRTCContext.MediaStreamTypeMode mediaStreamTypeMode = new RongRTCContext.MediaStreamTypeMode();
        RenderHolder renderHolder = null;
        Log.i(TAG,"smallView unUsedRemoteRenders size="+unUsedRemoteRenders.size()+",isSelf="+isSelf+",userID="+userID+",userName="+userName+",talkType="+talkType);
        if (containsKeyVideoViewEntiry(userID)) {
            renderHolder = idQueryHolder(userID);
            renderHolder.userName = userName;
        } else {
            renderHolder = unUsedRemoteRenders.get(0);
            renderHolder.userName = userName;
            renderHolder.initCover(talkType);
            addVideoViewEntiry(userID,renderHolder);

            holderContainer.addView(renderHolder.containerLayout, remoteLayoutParams);
            ((CallActivity) context).setWaitingTipsVisiable(false);

            unUsedRemoteRenders.remove(0);
        }
        renderHolder.userId = userID;

        render.setOnClickListener(new RemoteRenderClickListener(renderHolder));
        connetedRemoteRenders.put(userID, renderHolder);
        positionRenders.add(renderHolder);

        render.setZOrderOnTop(true);
        render.setZOrderMediaOverlay(true);
        render.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);

        renderHolder.coverView.setRongRTCVideoView(render);
        renderHolder.init(talkType, isSelf);

        if(talkType != RongRTCTalkTypeUtil.C_CAMERA && talkType!= RongRTCTalkTypeUtil.C_CM){
            renderHolder.coverView.showBlinkVideoView();
        }

        mediaStreamTypeMode.uid = userID;
        mediaStreamTypeMode.flowType = "2";
        return mediaStreamTypeMode;
    }

    private RongRTCContext.MediaStreamTypeMode largeView(boolean isSelf, String userID, String userName, RongRTCVideoView render, String talkType) {
        RongRTCContext.MediaStreamTypeMode mediaStreamTypeMode = null;
        RenderHolder renderHolder = null;
        Log.i(TAG,"largeView unUsedRemoteRenders size="+unUsedRemoteRenders.size()+",isSelf="+isSelf+",userID="+userID+",userName="+userName+",talkType="+talkType);
        if (isSelf) {
            renderHolder = unUsedRemoteRenders.get(0);
            unUsedRemoteRenders.remove(0);
        } else {
            renderHolder = idQueryHolder(userID);
        }
        renderHolder.userName = userName;
        renderHolder.userId = userID;

        render.setOnClickListener(new RemoteRenderClickListener(renderHolder));
        //添加缩放解决 观察者 横屏 进入pc的共享屏幕 导致的 显示不全问题
        render.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_BALANCED);
        connetedRemoteRenders.put(userID, renderHolder);

        render.setZOrderMediaOverlay(false);
        //init在setBlinkVideoView之前执行，不然 本地正常用户加入房间coverView=null；
        renderHolder.init(talkType, isSelf);
        renderHolder.coverView.setRongRTCVideoView(render);

        if (isSelf && talkType == RongRTCTalkTypeUtil.C_CAMERA) {
            renderHolder.coverView.showUserHeader();
        }else{
            renderHolder.coverView.showBlinkVideoView();
        }
        holderBigContainer.addView(renderHolder, screenWidth, screenHeight);

        toggleTips();
        saveSelectUserId(renderHolder);
        selectedRender = renderHolder;

        if(!isSelf){
            mediaStreamTypeMode = new RongRTCContext.MediaStreamTypeMode();
            mediaStreamTypeMode.uid = userID;
            mediaStreamTypeMode.flowType = "1";
        }else{
            addVideoViewEntiry(userID,renderHolder);
        }
        return mediaStreamTypeMode;
    }

    private void sendSubscribeStream(HashMap<String, RongRTCContext.MediaStreamTypeMode> mediaStreamTypeModeMap) {
        try{
            if(mediaStreamTypeModeMap!=null){
                ArrayList<RongRTCContext.MediaStreamTypeMode> mediaStreamTypeModeList = new ArrayList<RongRTCContext.MediaStreamTypeMode>();
                for (RongRTCContext.MediaStreamTypeMode mode:mediaStreamTypeModeMap.values()) {
                    mediaStreamTypeModeList.add(mode);
                }
                RongRTCEngine.getInstance().subscribeStream(mediaStreamTypeModeList);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void rotateView() {
        getSize();//重新获取屏幕的宽高 w:1920 h:1080
        holderBigContainer.refreshView(this.screenWidth, this.screenHeight);
    }

    /**
     * 退出聊天室 降级时用到
     *
     * @param userID
     */
    public void removeVideoView(String userID) {
        SessionManager.getInstance(Utils.getContext()).remove("color" + userID);
        if (containsKeyVideoViewEntiry(userID)) {
            if (null != idQueryHolder(userID).containerLayout) {
                holderContainer.removeView(idQueryHolder(userID).containerLayout);
            }
            idQueryHolder(userID).coverView.removeAllViews();
            removeVideoViewEntiry(userID);
        }

        if (connetedRemoteRenders.containsKey(userID)) {

            connetedRemoteRenders.get(userID).release();
            RenderHolder releaseTaget = connetedRemoteRenders.remove(userID);
            int index = 0;
            for (int i = 0; i < unUsedRemoteRenders.size(); i++) {
                RenderHolder compare = unUsedRemoteRenders.get(i);
                if (releaseTaget.targetZindex < compare.targetZindex) {
                    index = i;
                    break;
                }
            }
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

            unUsedRemoteRenders.add(index, releaseTaget);

            if (selectedRender == releaseTaget) {
                if (connetedRemoteRenders.size() != 0) {
                    Set set = connetedRemoteRenders.entrySet();
                    Iterator iterator = set.iterator();
                    while (iterator.hasNext()) {
                        Map.Entry mapentry = (Map.Entry) iterator.next();
                        RenderHolder newRender = (RenderHolder) mapentry.getValue();// 从远程连接中获取到新的 渲染器
                        String id = (String) mapentry.getKey();
                        FinLog.e("render:", "删除小窗口：" + id);
                        holderContainer.removeView(newRender.containerLayout);// 小容器删除 layout
                        holderBigContainer.addView(newRender, screenWidth, screenHeight);//将新的渲染器添加到大容器中
                        selectedRender = newRender;// 新渲染器辅给大窗口渲染器
                        positionRenders.remove(newRender);//
                        break;
                    }
                }
                FinLog.e("render:", "selectedRender == releaseTaget  remove:" + userID);
                holderBigContainer.removeView(releaseTaget.containerLayout);
            } else {
                FinLog.e("render:", " remove:" + userID);
                holderContainer.removeView(releaseTaget.containerLayout);
                positionRenders.remove(releaseTaget);
            }
            if (null != selectedRender && null != selectedRender.coverView && null != selectedRender.coverView.getRongRTCVideoView()) {
                selectedRender.coverView.getRongRTCVideoView().setZOrderMediaOverlay(false);
            }
            refreshViews();
        } else {
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
        int size = isObserver ? 0 : 1;
        if (connetedRemoteRenders.size() == size) {
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
        int size = isObserver ? 0 : 1;
        //connectedRemoteRenders only contains local render by default. when its size is large than 1, means new user joined
        return connetedRemoteRenders.size() > size;
    }

    public void updateTalkType(String userId, String talkType) {
        if (connetedRemoteRenders.containsKey(userId)) {
            connetedRemoteRenders.get(userId).CameraSwitch(talkType);
        }
    }

    public class RenderHolder {
        RelativeLayout containerLayout;
        int targetZindex;
        CoverView coverView;
        public String talkType = "";
        private String userName, userId;

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
            coverView.setUserInfo(userName, userId);
            coverView.showUserHeader();
            RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            removeCoverView();
            this.containerLayout.addView(coverView, p);
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

    public VideoViewManager.RenderHolder idQueryHolder(String userid){
        VideoViewManager.RenderHolder renderHolder=null;
        connectedVideoViewEntity connectedVideoViewEntity=new connectedVideoViewEntity(renderHolder,userID);
        if(null!=connectedUsers){
            for (int i = 0; i < connectedUsers.size(); i++) {
                if(null!=connectedUsers.get(i) && !TextUtils.isEmpty(connectedUsers.get(i).getUserId()) && connectedUsers.get(i).getUserId().equals(userid)){
                    renderHolder=connectedUsers.get(i).getRenderHolder();
                }
            }
        }
        return renderHolder;
    }

    /**
     *
     * @param userid
     * @return 存在 true
     */
    public boolean containsKeyVideoViewEntiry(String userid){
        boolean bool=false;
        if(null!=connectedUsers){
            for (int i = 0; i < connectedUsers.size(); i++) {
                if(null!=connectedUsers.get(i) && !TextUtils.isEmpty(connectedUsers.get(i).getUserId()) && connectedUsers.get(i).getUserId().equals(userid)){
                    bool=true;
                }
            }
        }
        return bool;
    }

    public void addVideoViewEntiry(String userid,RenderHolder holder){
        if(null!=connectedUsers){
            connectedVideoViewEntity connectedVideoViewEntity=new connectedVideoViewEntity(holder,userid);
            for (int i = 0; i < connectedUsers.size(); i++) {
                if(null!=connectedUsers.get(i) && !TextUtils.isEmpty(connectedUsers.get(i).getUserId()) && connectedUsers.get(i).getUserId().equals(userid)){
                    connectedUsers.remove(i);
                }
            }
            connectedUsers.add(connectedVideoViewEntity);
        }
    }

    public void removeVideoViewEntiry(String userid){
        if(null!=connectedUsers){
            for (int i = 0; i < connectedUsers.size(); i++) {
                if(null!=connectedUsers.get(i) && !TextUtils.isEmpty(connectedUsers.get(i).getUserId()) && connectedUsers.get(i).getUserId().equals(userid)){
                    connectedUsers.remove(i);
                }
            }
        }
    }
}