package cn.rongcloud.rtc.entity;

import cn.rongcloud.rtc.call.VideoViewManager;

/** Created by dengxudong on 2018/7/26. */
public class connectedVideoViewEntity {
    private VideoViewManager.RenderHolder renderHolder;
    private String userId;
    private String tag;

    public connectedVideoViewEntity() {}

    public connectedVideoViewEntity(
            VideoViewManager.RenderHolder renderHolder, String userId, String tag) {
        this.renderHolder = renderHolder;
        this.userId = userId;
        this.tag = tag;
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

    public String getKey() {
        return userId + "_" + tag;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
