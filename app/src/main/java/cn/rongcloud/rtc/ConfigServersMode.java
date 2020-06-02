package cn.rongcloud.rtc;

/** Created by Huichao Li on 2017/3/20. Wrapper class for server mode */
public class ConfigServersMode {

    private String cmptls;
    private String sniffertls;
    private String crt;
    private String name;
    private String nav;
    private String cmp;
    private String sniffer;
    private String token;
    private QuicBean quic;
    private TcpBean tcp;
    private String appkey;

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public String getCmptls() {
        return cmptls;
    }

    public void setCmptls(String cmptls) {
        this.cmptls = cmptls;
    }

    public String getSniffertls() {
        return sniffertls;
    }

    public void setSniffertls(String sniffertls) {
        this.sniffertls = sniffertls;
    }

    public String getCrt() {
        return crt;
    }

    public void setCrt(String crt) {
        this.crt = crt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNav() {
        return nav;
    }

    public void setNav(String nav) {
        this.nav = nav;
    }

    public String getCmp() {
        return cmp;
    }

    public void setCmp(String cmp) {
        this.cmp = cmp;
    }

    public String getSniffer() {
        return sniffer;
    }

    public void setSniffer(String sniffer) {
        this.sniffer = sniffer;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public QuicBean getQuic() {
        return quic;
    }

    public void setQuic(QuicBean quic) {
        this.quic = quic;
    }

    public TcpBean getTcp() {
        return tcp;
    }

    public void setTcp(TcpBean tcp) {
        this.tcp = tcp;
    }

    public static class QuicBean {

        private String nav;
        private String cmp;
        private String token;
        private String appkey;

        public String getAppkey() {
            return appkey;
        }

        public void setAppkey(String appkey) {
            this.appkey = appkey;
        }

        public String getNav() {
            return nav;
        }

        public void setNav(String nav) {
            this.nav = nav;
        }

        public String getCmp() {
            return cmp;
        }

        public void setCmp(String cmp) {
            this.cmp = cmp;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }

    public static class TcpBean {
        private String nav;
        private String cmp;
        private String token;
        private String appkey;

        public String getAppkey() {
            return appkey;
        }

        public void setAppkey(String appkey) {
            this.appkey = appkey;
        }

        public String getNav() {
            return nav;
        }

        public void setNav(String nav) {
            this.nav = nav;
        }

        public String getCmp() {
            return cmp;
        }

        public void setCmp(String cmp) {
            this.cmp = cmp;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}
