package cn.rongcloud.rtc.util;

import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author DengXudong.
 * @Time 2018/2/2.
 * @Description:
 */
public class UserUtils {

    public static final String USERNAME_KEY = "USER_NAME";

    private static final String NAMETYPE = "请检查名字不含有除汉字、英文以外的其他字符！";
    private static final String NAMESIZE = "请确保名字长度不超过12个字节！";

    public static boolean isNumber(String string) {
        Pattern p = Pattern.compile("[0-9]*");
        Matcher m = p.matcher(string);
        if (m.matches()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isLetter(String txt) {
        Pattern p = Pattern.compile("[a-zA-Z]");
        Matcher m = p.matcher(txt);
        if (m.matches()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isChinese(String str) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        if (m.matches()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 是否是特殊字符
     *
     * @param str
     * @return
     */
    public static boolean isSpecialString(String str) {
        String speChat = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Pattern pattern = Pattern.compile(speChat);
        Matcher matcher = pattern.matcher(str);
        if (matcher.matches()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 是否符合中英文 空格  ,主要判断emoji表情
     *
     * @param str
     * @return true：符合
     */
    public static boolean isProvisions(String str) {
        String mPttern = "^[a-zA-Z\\u4e00-\\u9fa5\\s]{1,12}$";
        Pattern pattern = Pattern.compile(mPttern);
        Matcher matcher = pattern.matcher(str);
        if (matcher.matches()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     */
    public static boolean containsEmoji(String source) {
        int len = source.length();
        boolean isEmoji = false;
        for (int i = 0; i < len; i++) {
            char hs = source.charAt(i);
            if (0xd800 <= hs && hs <= 0xdbff) {
                if (source.length() > 1) {
                    char ls = source.charAt(i + 1);
                    int uc = ((hs - 0xd800) * 0x400) + (ls - 0xdc00) + 0x10000;
                    if (0x1d000 <= uc && uc <= 0x1f77f) {
                        return true;
                    }
                }
            } else {
                // non surrogate
                if (0x2100 <= hs && hs <= 0x27ff && hs != 0x263b) {
                    return true;
                } else if (0x2B05 <= hs && hs <= 0x2b07) {
                    return true;
                } else if (0x2934 <= hs && hs <= 0x2935) {
                    return true;
                } else if (0x3297 <= hs && hs <= 0x3299) {
                    return true;
                } else if (hs == 0xa9 || hs == 0xae || hs == 0x303d || hs == 0x3030 || hs == 0x2b55 || hs == 0x2b1c || hs == 0x2b1b || hs == 0x2b50 || hs == 0x231a) {
                    return true;
                }
                if (!isEmoji && source.length() > 1 && i < source.length() - 1) {
                    char ls = source.charAt(i + 1);
                    if (ls == 0x20e3) {
                        return true;
                    }
                }
            }
        }
        return isEmoji;
    }

    public static String startInspect(String str) {
        try {
            int userNameCount = 0;
            boolean isLetter = false, isChinese = false;
            char[] chars = str.toCharArray();
            int length = chars.length;
            for (int i = 0; i < length; i++) {
                String txt = String.valueOf(chars[i]);
                if (UserUtils.isSpecialString(txt) || !UserUtils.isProvisions(txt)) {
                    return NAMETYPE;
                }
                if (UserUtils.isLetter(txt)) {
                    userNameCount += 1;
                    isLetter = true;
                }
                if (UserUtils.isChinese(txt)) {
                    userNameCount += 2;
                    isChinese = true;
                }
                if (txt.equals(" ")) {
                    userNameCount += 1;
                }
            }
            if (isChinese && isLetter && userNameCount > 12) {
                return NAMESIZE;
            } else if (isChinese && userNameCount > 12) {
                return NAMESIZE;
            } else if (isLetter && userNameCount > 12) {
                return NAMESIZE;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
        return "";
    }

    public static String truncatameUserName(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        String userName = "";
        int strLength = str.length();
        try {
            switch (strType(str)) {
                case 0://全英文最多5字母 遇空格抛掉
                    userName = str.substring(strLength - (strLength > 5 ? 5 : strLength), strLength).trim();
                    if (userName.indexOf(" ") != -1) {
                        userName = userName.substring(userName.indexOf(' ') + 1);
                    }
                    break;
                case 1:
                    userName = str.substring(strLength - (strLength > 2 ? 2 : strLength), strLength).trim();
                    break;
                case 2:
                    userName = hybridCapture(str);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            userName = "null";
        }
        return userName;
    }

    /**
     * 名称串类型
     *
     * @param str
     * @return 2：中英文 1：纯中文 0：纯英文
     */
    private static int strType(String str) {
        int userNameCount = 0;
        boolean isLetter = false, isChinese = false;
        char[] chars = str.toCharArray();
        int length = chars.length;
        for (int i = 0; i < length; i++) {
            String txt = String.valueOf(chars[i]);
            if (UserUtils.isLetter(txt)) {
                isLetter = true;
            }
            if (UserUtils.isChinese(txt)) {
                isChinese = true;
            }
        }
        if (isChinese && isLetter) {
            userNameCount = 2;
        } else if (isChinese) {
            userNameCount = 1;
        } else if (isLetter) {
            userNameCount = 0;
        }
        return userNameCount;
    }

    /**
     * 中英文混合不超过5个字节，
     */
    private static String hybridCapture(String str) {
        String name = " ";
        try {
            StringBuffer stringBuffer = new StringBuffer();
            int userNameCount = 0;
            char[] chars = str.toCharArray();
            int length = chars.length;
            for (int i = length - 1; i >= 0; i--) {
                String txt = String.valueOf(chars[i]);
                if (UserUtils.isLetter(txt)) {
                    userNameCount += 1;
                }
                if (UserUtils.isChinese(txt)) {
                    userNameCount += 2;
                }
                if (txt.equals(" ")) {
                    userNameCount += 1;
                }
                stringBuffer.insert(0, txt);
                if (userNameCount == 5) {
                    name = stringBuffer.toString().trim();
                    if (name.indexOf(" ") != -1) {
                        name = name.substring(name.indexOf(' ') + 1);
                    }
                    break;
                } else if (userNameCount == 4 && i - 1 >= 0) {
                    String next = String.valueOf(chars[i - 1]);
                    if (UserUtils.isChinese(next)) {
                        break;
                    }
                }
            }
            if (userNameCount != 5 && null != stringBuffer) {
                name = stringBuffer.toString().trim();
                if (name.indexOf(" ") != -1) {
                    name = name.substring(name.indexOf(' ') + 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            name = "ERROR";
        }
        return name;
    }
}
