package cn.rongcloud.rtc.util;

import java.util.HashMap;
import java.util.Map;

/** */
public class ButtentSolp {

    // 用来记录所有的按钮最后点击时间。
    private static final Map<String, Long> SLOPS_MAP = new HashMap<String, Long>();
    private static int MIN_SLOP = 1500;

    /**
     * 默认500ms内不能再次点击。
     *
     * @param buttonId 一般可以使用view.getId()用来区分不同的按钮。
     * @return true=表示当前不能点击，一般可以弹出提示，你点太快了，同时需要退出onClick。 false = 表示可以再次触发点击了，一般放行，就可以进行处理了。
     */
    public static boolean check(int buttonId) {
        return check(buttonId, MIN_SLOP);
    }
    /**
     * @param buttonId
     * @param holdTimeMills 传入最少需要等待的时间。如果不确定，可以调用上一个函数。默认最少500ms.
     * @return
     */
    public static boolean check(int buttonId, int holdTimeMills) {
        return check(String.valueOf(buttonId), holdTimeMills);
    }

    public static boolean waitInfinte(int buttonId) {
        return waitInfinte(String.valueOf(buttonId));
    }

    public static void cancel(int buttonId) {
        cancel(String.valueOf(buttonId));
    }

    public static boolean check(String buttonTag, int holdTimeMills) {
        if (buttonTag == null || buttonTag.length() <= 0) return true; // 合理的方式是确保参数不能为空，此处应该抛异常才合理。
        if (holdTimeMills < 100) { // 时间太短，没有意义。
            holdTimeMills = 100;
        }
        // 用同步块的方式控制防止多线程中操作失误。
        synchronized (SLOPS_MAP) {
            Long lastTipLong = SLOPS_MAP.get(buttonTag);
            if (lastTipLong == null || System.currentTimeMillis() - lastTipLong >= holdTimeMills) {
                SLOPS_MAP.put(buttonTag, System.currentTimeMillis());
                return false; // 表示第一次点击 或者 两次之间点击差够长了。
            } else {
                // 时间太短不允许再次触发。
                return true;
            }
        }
    }

    public static boolean waitInfinte(String buttonTag) {
        synchronized (SLOPS_MAP) {
            Long lastTipLong = SLOPS_MAP.put(buttonTag, System.currentTimeMillis());
            return lastTipLong != null; // !=null说明已经存储过一次点击事件了，需要调用cancel后才能再次点击。
        }
    }

    public static void cancel(String buttonTag) {
        synchronized (SLOPS_MAP) {
            SLOPS_MAP.remove(buttonTag);
        }
    }
}
