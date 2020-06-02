package cn.rongcloud.rtc.entity;

import cn.rongcloud.rtc.util.Utils;
import me.yokeyword.indexablerv.IndexableEntity;

/** Created by wangw on 2019/4/8. */
public class CountryInfo implements IndexableEntity {

    /** region : 1 locale : {"en":"Canada","zh":"加拿大"} */
    public String region;

    public String en;
    public String zh;
    public String pinyin;

    public CountryInfo() {}

    public CountryInfo(String region) {
        this.region = region;
    }

    public CountryInfo(String region, String en, String zh) {
        this.region = region;
        this.en = en;
        this.zh = zh;
    }

    @Override
    public String getFieldIndexBy() {
        if (Utils.isZhLanguage()) return zh;
        else return en;
    }

    @Override
    public void setFieldIndexBy(String indexField) {
        this.pinyin = indexField;
    }

    @Override
    public void setFieldPinyinIndexBy(String pinyin) {
        this.pinyin = pinyin;
    }

    public static CountryInfo createDefault() {
        return new CountryInfo("86", "China", "中国");
    }
}
