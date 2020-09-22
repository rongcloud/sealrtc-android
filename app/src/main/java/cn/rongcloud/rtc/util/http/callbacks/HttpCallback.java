package cn.rongcloud.rtc.util.http.callbacks;

public interface HttpCallback<T> {

    void onSuccess(T t);

    void onFail(HttpErrorCode errorCode);
}
