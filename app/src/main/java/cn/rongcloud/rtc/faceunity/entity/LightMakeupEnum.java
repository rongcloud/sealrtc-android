package cn.rongcloud.rtc.faceunity.entity;

import android.support.v4.util.Pair;
import cn.rongcloud.rtc.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 轻美妆列表，口红用 JSON 表示，其他都是图片
 *
 * @author Richie on 2018.11.12
 */
public enum LightMakeupEnum {

    /** 美妆项，前几项是预置的效果 排在列表最前方，顺序为桃花妆、雀斑妆、朋克妆（其中朋克没有腮红，3个妆容的眼线、眼睫毛共用1个的） */
    MAKEUP_NONE(
            "卸妆",
            "",
            LightMakeupCombination.FACE_MAKEUP_TYPE_NONE,
            R.drawable.makeup_none_normal,
            R.string.makeup_radio_remove,
            true),

    // 腮红
    MAKEUP_BLUSHER_01(
            "MAKEUP_BLUSHER_01",
            "light_makeup/blusher/mu_blush_01.png",
            LightMakeupCombination.FACE_MAKEUP_TYPE_BLUSHER,
            R.drawable.demo_blush_01,
            R.string.makeup_radio_blusher,
            false),
    MAKEUP_BLUSHER_23(
            "MAKEUP_BLUSHER_23",
            "light_makeup/blusher/mu_blush_23.png",
            LightMakeupCombination.FACE_MAKEUP_TYPE_BLUSHER,
            R.drawable.demo_blush_23,
            R.string.makeup_radio_blusher,
            false),
    MAKEUP_BLUSHER_20(
            "MAKEUP_BLUSHER_20",
            "light_makeup/blusher/mu_blush_20.png",
            LightMakeupCombination.FACE_MAKEUP_TYPE_BLUSHER,
            R.drawable.demo_blush_20,
            R.string.makeup_radio_blusher,
            false),
    MAKEUP_EYEBROW_16(
            "MAKEUP_EYEBROW_16",
            "light_makeup/eyebrow/mu_eyebrow_16.png",
            LightMakeupCombination.FACE_MAKEUP_TYPE_EYEBROW,
            R.drawable.demo_eyebrow_16,
            R.string.makeup_radio_eyebrow,
            false),

    // 眼影
    MAKEUP_EYE_SHADOW_01(
            "MAKEUP_EYESHADOW_01",
            "light_makeup/eyeshadow/mu_eyeshadow_01.png",
            LightMakeupCombination.FACE_MAKEUP_TYPE_EYE_SHADOW,
            R.drawable.demo_eyeshadow_01,
            R.string.makeup_radio_eye_shadow,
            false),
    MAKEUP_EYE_SHADOW_21(
            "MAKEUP_EYESHADOW_21",
            "light_makeup/eyeshadow/mu_eyeshadow_21.png",
            LightMakeupCombination.FACE_MAKEUP_TYPE_EYE_SHADOW,
            R.drawable.demo_eyeshadow_21,
            R.string.makeup_radio_eye_shadow,
            false),
    MAKEUP_BLUSHER_22(
            "MAKEUP_BLUSHER_22",
            "light_makeup/blusher/mu_blush_22.png",
            LightMakeupCombination.FACE_MAKEUP_TYPE_BLUSHER,
            R.drawable.demo_blush_22,
            R.string.makeup_radio_blusher,
            false),
    MAKEUP_EYEBROW_18(
            "MAKEUP_EYEBROW_18",
            "light_makeup/eyebrow/mu_eyebrow_18.png",
            LightMakeupCombination.FACE_MAKEUP_TYPE_EYEBROW,
            R.drawable.demo_eyebrow_18,
            R.string.makeup_radio_eyebrow,
            false),
    MAKEUP_EYE_SHADOW_18(
            "MAKEUP_EYESHADOW_18",
            "light_makeup/eyeshadow/mu_eyeshadow_18.png",
            LightMakeupCombination.FACE_MAKEUP_TYPE_EYE_SHADOW,
            R.drawable.demo_eyeshadow_18,
            R.string.makeup_radio_eye_shadow,
            false),
    // 眉毛
    MAKEUP_EYEBROW_01(
            "MAKEUP_EYEBROW_01",
            "light_makeup/eyebrow/mu_eyebrow_01.png",
            LightMakeupCombination.FACE_MAKEUP_TYPE_EYEBROW,
            R.drawable.demo_eyebrow_01,
            R.string.makeup_radio_eyebrow,
            false),
    MAKEUP_EYEBROW_19(
            "MAKEUP_EYEBROW_19",
            "light_makeup/eyebrow/mu_eyebrow_19.png",
            LightMakeupCombination.FACE_MAKEUP_TYPE_EYEBROW,
            R.drawable.demo_eyebrow_19,
            R.string.makeup_radio_eyebrow,
            false),
    MAKEUP_EYE_SHADOW_20(
            "MAKEUP_EYESHADOW_20",
            "light_makeup/eyeshadow/mu_eyeshadow_20.png",
            LightMakeupCombination.FACE_MAKEUP_TYPE_EYE_SHADOW,
            R.drawable.demo_eyeshadow_20,
            R.string.makeup_radio_eye_shadow,
            false),

    // 口红
    MAKEUP_LIPSTICK_01(
            "MAKEUP_LIPSTICK_01",
            "light_makeup/lipstick/mu_lip_01.json",
            LightMakeupCombination.FACE_MAKEUP_TYPE_LIPSTICK,
            R.drawable.demo_lip_01,
            R.string.makeup_radio_lipstick,
            false),
    MAKEUP_LIPSTICK_21(
            "MAKEUP_LIPSTICK_21",
            "light_makeup/lipstick/mu_lip_21.json",
            LightMakeupCombination.FACE_MAKEUP_TYPE_LIPSTICK,
            R.drawable.demo_lip_21,
            R.string.makeup_radio_lipstick,
            false),
    MAKEUP_LIPSTICK_20(
            "MAKEUP_LIPSTICK_20",
            "light_makeup/lipstick/mu_lip_20.json",
            LightMakeupCombination.FACE_MAKEUP_TYPE_LIPSTICK,
            R.drawable.demo_lip_20,
            R.string.makeup_radio_lipstick,
            false),
    MAKEUP_LIPSTICK_18(
            "MAKEUP_LIPSTICK_18",
            "light_makeup/lipstick/mu_lip_18.json",
            LightMakeupCombination.FACE_MAKEUP_TYPE_LIPSTICK,
            R.drawable.demo_lip_18,
            R.string.makeup_radio_lipstick,
            false);

    private String name;
    private String path;
    private int type;
    private int iconId;
    private int strId;
    /** 轻美妆妆容组合的整体强度 */
    public static final Map<Integer, Float> LIGHT_MAKEUP_OVERALL_INTENSITIES = new HashMap<>(16);
    /** 轻美妆妆容和滤镜的组合，http://confluence.faceunity.com/pages/viewpage.action?pageId=20332259 */
    public static final HashMap<Integer, Pair<Filter, Float>> LIGHT_MAKEUP_FILTERS =
            new HashMap<>(16);

    // 桃花、西柚、清透、男友, 赤茶妆、冬日妆、奶油妆
    static {
        LIGHT_MAKEUP_OVERALL_INTENSITIES.put(R.string.makeup_peach_blossom, 0.9f);
        LIGHT_MAKEUP_OVERALL_INTENSITIES.put(R.string.makeup_grapefruit, 1.0f);
        LIGHT_MAKEUP_OVERALL_INTENSITIES.put(R.string.makeup_clear, 0.9f);
        LIGHT_MAKEUP_OVERALL_INTENSITIES.put(R.string.makeup_boyfriend, 1.0f);
        /*
        MAKEUP_OVERALL_LEVEL.put(R.string.makeup_red_tea, 1.0f);
        MAKEUP_OVERALL_LEVEL.put(R.string.makeup_winter, 0.9f);
        MAKEUP_OVERALL_LEVEL.put(R.string.makeup_cream, 1.0f);
        MAKEUP_OVERALL_LEVEL.put(R.string.makeup_punk, 0.85f);
        MAKEUP_OVERALL_LEVEL.put(R.string.makeup_maple_leaf, 1.0f);
        MAKEUP_OVERALL_LEVEL.put(R.string.makeup_brocade_carp, 0.9f);
        MAKEUP_OVERALL_LEVEL.put(R.string.makeup_plum, 0.85f);
        MAKEUP_OVERALL_LEVEL.put(R.string.makeup_tipsy, 1.0f);
        MAKEUP_OVERALL_LEVEL.put(R.string.makeup_classical, 1.0f);
        MAKEUP_OVERALL_LEVEL.put(R.string.makeup_disgusting, 1.0f);
        MAKEUP_OVERALL_LEVEL.put(R.string.makeup_black_white, 1.0f);
        */
    }

    // 桃花、西柚、清透、男友, 赤茶妆、冬日妆、奶油妆
    static {
        LIGHT_MAKEUP_FILTERS.put(
                R.string.makeup_peach_blossom,
                Pair.create(Filter.create(Filter.Key.FENNEN_3), 1.0f));
        LIGHT_MAKEUP_FILTERS.put(
                R.string.makeup_grapefruit,
                Pair.create(Filter.create(Filter.Key.LENGSEDIAO_4), 0.7f));
        LIGHT_MAKEUP_FILTERS.put(
                R.string.makeup_clear, Pair.create(Filter.create(Filter.Key.XIAOQINGXIN_1), 0.8f));
        LIGHT_MAKEUP_FILTERS.put(
                R.string.makeup_boyfriend,
                Pair.create(Filter.create(Filter.Key.XIAOQINGXIN_3), 0.9f));

        //        MAKEUP_FILTERS.put(R.string.makeup_red_tea,
        // Pair.create(Filter.create(Filter.Key.XIAOQINGXIN_2), 0.75f));
        //        MAKEUP_FILTERS.put(R.string.makeup_winter,
        // Pair.create(Filter.create(Filter.Key.NUANSEDIAO_1), 0.8f));
        //        MAKEUP_FILTERS.put(R.string.makeup_cream,
        // Pair.create(Filter.create(Filter.Key.BAILIANG_1), 0.75f));
        //        MAKEUP_FILTERS.put(R.string.makeup_punk, Pair.create(FilterEnum.dry.filter(),
        // 0.5f));
        //        MAKEUP_FILTERS.put(R.string.makeup_maple_leaf,
        // Pair.create(FilterEnum.delta.filter(), 0.8f));
        //        MAKEUP_FILTERS.put(R.string.makeup_brocade_carp,
        // Pair.create(FilterEnum.linjia.filter(), 0.7f));
        //        MAKEUP_FILTERS.put(R.string.makeup_classical,
        // Pair.create(FilterEnum.hongkong.filter(), 0.85f));
        //        MAKEUP_FILTERS.put(R.string.makeup_plum, Pair.create(FilterEnum.red_tea.filter(),
        // 0.8f));
        //        MAKEUP_FILTERS.put(R.string.makeup_tipsy, Pair.create(FilterEnum.hongrun.filter(),
        // 0.55f));
        //        MAKEUP_FILTERS.put(R.string.makeup_freckles, Pair.create(FilterEnum.warm.filter(),
        // 0.4f));
    }

    private boolean showInMakeup;

    LightMakeupEnum(
            String name, String path, int type, int iconId, int strId, boolean showInMakeup) {
        this.name = name;
        this.path = path;
        this.type = type;
        this.iconId = iconId;
        this.strId = strId;
        this.showInMakeup = showInMakeup;
    }

    /**
     * 轻美妆组合，顺序为：桃花、西柚、清透、男友
     *
     * @return
     */
    public static List<LightMakeupCombination> getLightMakeupCombinations() {
        List<LightMakeupCombination> faceMakeups = new ArrayList<>();
        LightMakeupCombination none =
                new LightMakeupCombination(
                        null, R.string.makeup_radio_remove, R.drawable.makeup_none_normal);
        faceMakeups.add(none);

        // 桃花
        List<LightMakeupItem> peachBlossomMakeups = new ArrayList<>(4);
        peachBlossomMakeups.add(MAKEUP_BLUSHER_01.lightMakeup(0.9f));
        peachBlossomMakeups.add(MAKEUP_EYE_SHADOW_01.lightMakeup(0.9f));
        peachBlossomMakeups.add(MAKEUP_EYEBROW_01.lightMakeup(0.5f));
        peachBlossomMakeups.add(MAKEUP_LIPSTICK_01.lightMakeup(0.9f));
        LightMakeupCombination peachBlossom =
                new LightMakeupCombination(
                        peachBlossomMakeups,
                        R.string.makeup_peach_blossom,
                        R.drawable.demo_makeup_peachblossom);
        faceMakeups.add(peachBlossom);

        // 西柚
        List<LightMakeupItem> grapeMakeups = new ArrayList<>(4);
        grapeMakeups.add(MAKEUP_BLUSHER_23.lightMakeup(1.0f));
        grapeMakeups.add(MAKEUP_EYE_SHADOW_21.lightMakeup(0.75f));
        grapeMakeups.add(MAKEUP_EYEBROW_19.lightMakeup(0.6f));
        grapeMakeups.add(MAKEUP_LIPSTICK_21.lightMakeup(0.8f));
        LightMakeupCombination grape =
                new LightMakeupCombination(
                        grapeMakeups,
                        R.string.makeup_grapefruit,
                        R.drawable.demo_makeup_grapefruit);
        faceMakeups.add(grape);

        // 清透
        List<LightMakeupItem> clearMakeups = new ArrayList<>(4);
        clearMakeups.add(MAKEUP_BLUSHER_22.lightMakeup(0.9f));
        clearMakeups.add(MAKEUP_EYE_SHADOW_20.lightMakeup(0.65f));
        clearMakeups.add(MAKEUP_EYEBROW_18.lightMakeup(0.45f));
        clearMakeups.add(MAKEUP_LIPSTICK_20.lightMakeup(0.8f));
        LightMakeupCombination clear =
                new LightMakeupCombination(
                        clearMakeups, R.string.makeup_clear, R.drawable.demo_makeup_clear);
        faceMakeups.add(clear);

        // 男友
        List<LightMakeupItem> boyFriendMakeups = new ArrayList<>(4);
        boyFriendMakeups.add(MAKEUP_BLUSHER_20.lightMakeup(0.8f));
        boyFriendMakeups.add(MAKEUP_EYE_SHADOW_18.lightMakeup(0.9f));
        boyFriendMakeups.add(MAKEUP_EYEBROW_16.lightMakeup(0.65f));
        boyFriendMakeups.add(MAKEUP_LIPSTICK_18.lightMakeup(1.0f));
        LightMakeupCombination boyFriend =
                new LightMakeupCombination(
                        boyFriendMakeups,
                        R.string.makeup_boyfriend,
                        R.drawable.demo_makeup_boyfriend);
        faceMakeups.add(boyFriend);
        return faceMakeups;
    }

    public LightMakeupItem lightMakeup(float level) {
        return new LightMakeupItem(name, path, type, strId, iconId, level);
    }
}
