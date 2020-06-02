package cn.rongcloud.rtc.util;

import android.content.Context;
import android.content.SharedPreferences;

/** Created by Huichao on 2016/11/24. */
public class SessionManager {
    private static final String SETTIING_OPTIONS = "SETTIING_OPTIONS";
    private static SharedPreferences sharedPreferences;
    private static SessionManager sessionManager;
    public static final String ROOM_NUM = "room_id";
    private static Context mContext;

    public static void initContext(Context context) {
        mContext = context;
    }

    public static SessionManager getInstance() {
        synchronized (SessionManager.class) {
            if (sessionManager == null && mContext != null) {
                sessionManager = new SessionManager(mContext);
            }
        }
        return sessionManager;
    }

    private SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(SETTIING_OPTIONS, Context.MODE_PRIVATE);
    }

    public String put(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
        return value;
    }

    public long put(String key, Long value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(key, value);
        editor.commit();
        return value;
    }

    public Float put(String key, Float value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(key, value);
        editor.commit();
        return value;
    }

    public int put(String key, int value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.commit();
        return value;
    }

    public boolean put(String key, Boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
        return value;
    }

    public boolean contains(String key) {
        if (null == sharedPreferences) {
            return false;
        }
        return sharedPreferences.contains(key);
    }

    /**
     * 移除某个key值已经对应的值
     *
     * @param key
     */
    public void remove(String key) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.commit();
    }

    public String getString(String key) {
        return sharedPreferences.getString(key, "");
    }

    public String getString(String key, String defVal) {
        return sharedPreferences.getString(key, defVal);
    }

    public Boolean getBoolean(String key) {
        return sharedPreferences.getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defaultVal) {
        return sharedPreferences.getBoolean(key, defaultVal);
    }

    public Boolean getIsSupportTiny(String key) {
        return sharedPreferences.getBoolean(key, true);
    }

    public Long getLong(String key) {
        return sharedPreferences.getLong(key, 0);
    }

    public int getInt(String key) {
        return sharedPreferences.getInt(key, 0);
    }

    public int getInt(String key, int defValue) {
        return sharedPreferences.getInt(key, defValue);
    }

    public float getFloat(String key, float defValue) {
        return sharedPreferences.getFloat(key, defValue);
    }
}
