package cn.rongcloud.rtc.util.http.callbacks;


public enum HttpErrorCode {

    /** 未知错误 */
    UnknownError("unknown error, please check the error code", -1),
    HttpTimeoutError("http request timeout ", 1),
    HttpResponseError("http response error", 2);


    private String reason;
    private int value;

    HttpErrorCode(String reason, int value) {
        this.reason = reason;
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getReason() {
        return reason;
    }

    public static HttpErrorCode valueOf(int value) {
        for (HttpErrorCode errorCode : HttpErrorCode.values()) {
            if (errorCode.value == value) {
                return errorCode;
            }
        }
        UnknownError.setValue(value);
        return UnknownError;
    }

    @Override
    public String toString() {
        return "HttpErrorCode{" + "code: " + value + ", reason: \'" + reason + "\'}";
    }
}
