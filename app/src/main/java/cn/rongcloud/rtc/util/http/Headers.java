package cn.rongcloud.rtc.util.http;

import android.support.v4.util.ArrayMap;

public class Headers {

    private ArrayMap<String, String> headers = new ArrayMap<>();

    public void add(String key, String value) {
        String originalValue = headers.get(key);
        if (originalValue == null) {
            put(key, value);
        } else {
            put(key, originalValue + "," + value);
        }
    }

    public void put(String key, String value) {
        headers.put(key, value);
    }

    public String get(String key) {
        return headers.get(key);
    }

    public ArrayMap<String, String> getHeaders() {
        return headers;
    }

    @Override
    public String toString() {
        return headers.toString();
    }
}
