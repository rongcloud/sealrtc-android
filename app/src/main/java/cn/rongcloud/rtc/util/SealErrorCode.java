package cn.rongcloud.rtc.util;

/** app server 错误码 */
public enum SealErrorCode {
    /** 未知错误。 */
    UNKNOWN(-1, "unknown"),
    PARAMETER_ERROR(4000, "parameter error"),
    REQUEST_FREQUENCY(4001, "request frequency"),
    UNKNOWN_PHONE_NUMBER(4002, "unknown phone number"),
    VERIFICATION_CODE_EXPIRED(4003, "verification code expired"),
    INVALID_VERIFICATION_CODE(4004, "verification code invalid"),
    SENT_VERIFICATION_CODE_FAILED(4005, "verification code sent failed"),
    REQUEST_IM_TOKEN_ERROR(4006, "request im token error");
    private int code;
    private String msg;

    /**
     * 构造函数。
     *
     * @param code 错误代码。
     * @param msg 错误消息。
     */
    SealErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * 获取错误代码值。
     *
     * @return 错误代码值。
     */
    public int getValue() {
        return this.code;
    }

    /**
     * 获取错误消息。
     *
     * @return 错误消息。
     */
    public String getMessage() {
        return this.msg;
    }

    /**
     * 设置错误代码值。
     *
     * @param code 错误代码。
     * @return 错误代码枚举。
     */
    public static SealErrorCode valueOf(int code) {
        for (SealErrorCode c : SealErrorCode.values()) {
            if (code == c.getValue()) {
                return c;
            }
        }

        SealErrorCode errorCode = UNKNOWN;
        errorCode.code = code;
        return errorCode;
    }

    public static SealErrorCode valueOf(int code, String msg) {
        SealErrorCode errorCode = valueOf(code);
        errorCode.msg = msg;
        return errorCode;
    }

    @Override
    public String toString() {
        return "SealErrorCode{" + "code=" + code + ", msg='" + msg + '\'' + '}';
    }
}
