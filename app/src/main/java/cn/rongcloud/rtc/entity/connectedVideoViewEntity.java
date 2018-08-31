package cn.rongcloud.rtc.entity;

import cn.rongcloud.rtc.VideoViewManager;

/**
 * Created by dengxudong on 2018/7/26.
 */

public class connectedVideoViewEntity {
    private VideoViewManager.RenderHolder renderHolder;
    private String userId;

    public connectedVideoViewEntity(){}
    public connectedVideoViewEntity(VideoViewManager.RenderHolder renderHolder, String userId) {
        this.renderHolder = renderHolder;
        this.userId = userId;
    }

    public VideoViewManager.RenderHolder getRenderHolder() {
        return renderHolder;
    }

    public void setRenderHolder(VideoViewManager.RenderHolder renderHolder) {
        this.renderHolder = renderHolder;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
