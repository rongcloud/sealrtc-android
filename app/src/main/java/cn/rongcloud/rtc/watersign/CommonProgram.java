package cn.rongcloud.rtc.watersign;

import android.opengl.GLES20;

/** Created by wangw on 2019/5/7. */
public class CommonProgram {

    private static final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;\n"
                    + "attribute vec4 aPosition;\n"
                    + "attribute vec4 aTextureCoord;\n"
                    + "varying vec2 vTextureCoord;\n"
                    + "void main(){\n"
                    + "    gl_Position = uMVPMatrix * aPosition;\n"
                    +
                    //                    "    gl_Position =  aPosition;\n" +
                    "    vTextureCoord = aTextureCoord.xy;\n"
                    + "}";
    private static final String FRAGMENT_SHADER =
            "precision mediump float;\n"
                    + "varying vec2 vTextureCoord;\n"
                    + "uniform sampler2D sTexture;\n"
                    + "void main(){\n"
                    + "    gl_FragColor = texture2D(sTexture,vTextureCoord);\n"
                    + "}";
    public final int uMVPMatrixLoc;
    public final int aPositionLoc;
    public final int aTextureCoordLoc;
    public final int sTextureLoc;

    private int programId = -1;

    public CommonProgram() {
        programId = ShaderHelper.buildProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        uMVPMatrixLoc = GLES20.glGetUniformLocation(programId, "uMVPMatrix");
        GlUtil.checkLocation(uMVPMatrixLoc, "uMVPMatrix");
        aPositionLoc = GLES20.glGetAttribLocation(programId, "aPosition");
        GlUtil.checkLocation(aPositionLoc, "aPosition");
        aTextureCoordLoc = GLES20.glGetAttribLocation(programId, "aTextureCoord");
        GlUtil.checkLocation(aTextureCoordLoc, "aTextureCoord");
        sTextureLoc = GLES20.glGetUniformLocation(programId, "sTexture");
        GlUtil.checkLocation(sTextureLoc, "sTexture");
    }

    public int getShaderProgramId() {
        return programId;
    }

    public void release() {
        if (programId != -1) {
            GLES20.glDeleteProgram(programId);
            programId = -1;
        }
    }
}
