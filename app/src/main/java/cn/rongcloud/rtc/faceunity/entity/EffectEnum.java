package cn.rongcloud.rtc.faceunity.entity;

import cn.rongcloud.rtc.R;
import java.util.ArrayList;

/** Created by tujh on 2018/1/30. */
public enum EffectEnum {
    /** 关闭道具 */
    EffectNone("none", R.drawable.ic_delete_all, "none", 1, Effect.EFFECT_TYPE_NONE, 0),
    /** 道具贴纸 */
    Effect_sdlu(
            "sdlu", R.drawable.sdlu, "effect/normal/sdlu.bundle", 4, Effect.EFFECT_TYPE_NORMAL, 0),
    Effect_daisypig(
            "daisypig",
            R.drawable.daisypig,
            "effect/normal/daisypig.bundle",
            4,
            Effect.EFFECT_TYPE_NORMAL,
            0),
    Effect_fashi(
            "fashi",
            R.drawable.fashi,
            "effect/normal/fashi.bundle",
            4,
            Effect.EFFECT_TYPE_NORMAL,
            0),
    Effect_chri1(
            "chri1",
            R.drawable.chri1,
            "effect/normal/chri1.bundle",
            4,
            Effect.EFFECT_TYPE_NORMAL,
            0),
    Effect_xueqiu_lm_fu(
            "xueqiu_lm_fu",
            R.drawable.xueqiu_lm_fu,
            "effect/normal/xueqiu_lm_fu.bundle",
            4,
            Effect.EFFECT_TYPE_NORMAL,
            0),
    Effect_wobushi(
            "wobushi",
            R.drawable.wobushi,
            "effect/normal/wobushi.bundle",
            4,
            Effect.EFFECT_TYPE_NORMAL,
            0),
    Effect_gaoshiqing(
            "gaoshiqing",
            R.drawable.gaoshiqing,
            "effect/normal/gaoshiqing.bundle",
            4,
            Effect.EFFECT_TYPE_NORMAL,
            0);
    /** AR面具 */
    //    Effect_bluebird("bluebird", R.drawable.bluebird, "effect/ar/bluebird.bundle", 4,
    // Effect.EFFECT_TYPE_AR, 0),
    //    Effect_lanhudie("lanhudie", R.drawable.lanhudie, "effect/ar/lanhudie.bundle", 4,
    // Effect.EFFECT_TYPE_AR, 0),
    //    Effect_fenhudie("fenhudie", R.drawable.fenhudie, "effect/ar/fenhudie.bundle", 4,
    // Effect.EFFECT_TYPE_AR, 0),
    //    Effect_tiger_huang("tiger_huang", R.drawable.tiger_huang, "effect/ar/tiger_huang.bundle",
    // 4, Effect.EFFECT_TYPE_AR, 0),
    //    Effect_tiger_bai("tiger_bai", R.drawable.tiger_bai, "effect/ar/tiger_bai.bundle", 4,
    // Effect.EFFECT_TYPE_AR, 0),
    //    Effect_afd("afd", R.drawable.afd, "effect/ar/afd.bundle", 4, Effect.EFFECT_TYPE_AR, 0),
    //    Effect_baozi("baozi", R.drawable.baozi, "effect/ar/baozi.bundle", 4,
    // Effect.EFFECT_TYPE_AR, 0),
    //    Effect_tiger("tiger", R.drawable.tiger, "effect/ar/tiger.bundle", 4,
    // Effect.EFFECT_TYPE_AR, 0),
    //    Effect_xiongmao("xiongmao", R.drawable.xiongmao, "effect/ar/xiongmao.bundle", 4,
    // Effect.EFFECT_TYPE_AR, 0);

    private String bundleName;

    private int resId;
    private String path;
    private int maxFace;
    private int effectType;
    private int description;

    EffectEnum(String name, int resId, String path, int maxFace, int effectType, int description) {
        this.bundleName = name;
        this.resId = resId;
        this.path = path;
        this.maxFace = maxFace;
        this.effectType = effectType;
        this.description = description;
    }

    public String bundleName() {
        return bundleName;
    }

    public int resId() {
        return resId;
    }

    public String path() {
        return path;
    }

    public int maxFace() {
        return maxFace;
    }

    public int effectType() {
        return effectType;
    }

    public int description() {
        return description;
    }

    public Effect effect() {
        return new Effect(bundleName, resId, path, maxFace, effectType, description);
    }

    public static ArrayList<Effect> getEffectsByEffectType(int effectType) {
        ArrayList<Effect> effects = new ArrayList<>(16);
        effects.add(EffectNone.effect());
        for (EffectEnum e : EffectEnum.values()) {
            if (e.effectType == effectType) {
                effects.add(e.effect());
            }
        }
        return effects;
    }
}
