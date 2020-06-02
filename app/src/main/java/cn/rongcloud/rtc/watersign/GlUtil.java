package cn.rongcloud.rtc.watersign;

import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
import static android.opengl.GLES20.GL_CLAMP_TO_EDGE;
import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_NEAREST;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_S;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_T;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glTexParameterf;
import static android.opengl.GLES20.glTexParameteri;

import android.opengl.GLES20;

/** Created by wangw on 2019/5/6. */
public class GlUtil {

    public static void checkGlError(String op) {
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            String msg = op + ": glError 0x" + Integer.toHexString(error);
            throw new RuntimeException(msg);
        }
    }

    public static void checkLocation(int location, String label) {
        if (location < 0) {
            throw new RuntimeException("Unable to locate '" + label + "' in program");
        }
    }

    /**
     * 创建OES类型的纹理对象，并返回纹理ID
     *
     * @return
     */
    public static int createExternalTextureObject() {
        // 1.创建TextureId
        int[] textures = new int[1];
        glGenTextures(1, textures, 0);
        // 检查是否异常
        checkGlError("GenTexture Id");
        int textureId = textures[0];
        // 2.绑定纹理，注意绑定的目标纹理是GL_TEXTURE_EXTERNAL_OES，
        // 因为Camera使用的输出Texture是一种特殊的格式，同样在Shader中也必须使用SamperExternalOES的变量类型访问该纹理
        glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureId);
        // 设置缩放过滤模式
        glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        checkGlError("GlTextureParameter");
        return textureId;
    }
}
