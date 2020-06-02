package cn.rongcloud.rtc.entity;

/** @Author DengXuDong. @Time 2018/1/29. @Description: */
public class CMPAddress {
    private String cmpServer;
    private String serverURL;

    public CMPAddress(String cmpServer, String serverURL) {
        this.cmpServer = cmpServer;
        this.serverURL = serverURL;
    }

    public String getCmpServer() {
        return cmpServer;
    }

    public void setCmpServer(String cmpServer) {
        this.cmpServer = cmpServer;
    }

    public String getServerURL() {
        return serverURL;
    }

    public void setServerURL(String serverURL) {
        this.serverURL = serverURL;
    }
}
