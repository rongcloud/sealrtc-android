package cn.rongcloud.rtc.watersign;

import android.opengl.GLES20;
import android.util.Log;

/** Created by wangw on 2019/3/15. */
public class ShaderHelper {

    private static final String TAG = "ShaderHelper";

    /**
     * 编译顶点着色器并返回
     *
     * @param shaderCode
     * @return
     */
    public static int compileVertexShader(String shaderCode) {
        return compileShader(GLES20.GL_VERTEX_SHADER, shaderCode);
    }

    /**
     * 编译片段着色器
     *
     * @param shaderCode
     * @return
     */
    public static int compileFragmentShader(String shaderCode) {
        return compileShader(GLES20.GL_FRAGMENT_SHADER, shaderCode);
    }

    /**
     * 编译着色器
     *
     * @param type 着色器类型，顶点/片段
     * @param shaderCode 着色器代码
     * @return
     */
    private static int compileShader(int type, String shaderCode) {
        // 1.创建着色器对象，并返回对象的引用地址，如果为0则表示创建失败
        int shaderObjId = GLES20.glCreateShader(type);
        if (shaderObjId == 0) {
            Log.e(TAG, "compileShader: Create Shader Failed");
            return 0;
        }
        // 2.上传着色器源码
        GLES20.glShaderSource(shaderObjId, shaderCode);
        // 3.编译着色器对象
        GLES20.glCompileShader(shaderObjId);

        // 获得编译着色器对象的状态
        int[] status = new int[1];
        GLES20.glGetShaderiv(shaderObjId, GLES20.GL_COMPILE_STATUS, status, 0);
        // 获取OpenGl中编译着色器相关的log
        Log.d(
                TAG,
                "compileShader: status="
                        + status[0]
                        + ", info="
                        + GLES20.glGetShaderInfoLog(shaderObjId));

        // 检查编译是否成功，如果状态是0表示失败，则告诉OpenGL删掉这个着色器
        if (status[0] == 0) {
            GLES20.glDeleteShader(shaderObjId);
            Log.e(TAG, "compileShader: CompileShader Failed");
        }

        return shaderObjId;
    }

    /**
     * 将顶点着色器和片段着色器链接到一起
     *
     * @param vertexShaderId
     * @param fragmentShaderId
     * @return
     */
    public static int linkProgram(int vertexShaderId, int fragmentShaderId) {
        // 创建Opengl程序对象
        final int programObjectId = GLES20.glCreateProgram();
        if (programObjectId == 0) {
            Log.e(TAG, "linkProgram: could not create new program");
            return 0;
        }
        // 将着色器对象引用附加到新建的程序对象上
        GLES20.glAttachShader(programObjectId, vertexShaderId);
        GLES20.glAttachShader(programObjectId, fragmentShaderId);
        // 将着色器联合起来
        GLES20.glLinkProgram(programObjectId);
        // 获取链接状态
        int[] status = new int[1];
        GLES20.glGetProgramiv(programObjectId, GLES20.GL_LINK_STATUS, status, 0);
        Log.d(
                TAG,
                "linkProgram: status="
                        + status[0]
                        + " results of linking program:"
                        + GLES20.glGetProgramInfoLog(programObjectId));
        // 判断是否链接成功，如果status是0表示失败，则删除程序对象
        if (status[0] == 0) {
            GLES20.glDeleteProgram(programObjectId);
            Log.e(TAG, "linkProgram: Linking of program Failed");
            return 0;
        }
        return programObjectId;
    }

    /**
     * 验证OpenGl 程序是否可用
     *
     * @param programObjectId
     * @return
     */
    public static boolean validateProgram(int programObjectId) {
        GLES20.glValidateProgram(programObjectId);
        int[] validateStatus = new int[1];
        GLES20.glGetProgramiv(programObjectId, GLES20.GL_VALIDATE_STATUS, validateStatus, 0);
        Log.d(
                TAG,
                "validateProgram: status="
                        + validateStatus[0]
                        + ", log="
                        + GLES20.glGetProgramInfoLog(programObjectId));
        return validateStatus[0] != 0;
    }

    /**
     * 构建一个着色器程序
     *
     * @param vertexShaderSource
     * @param fragmentShaderSource
     * @return
     */
    public static int buildProgram(String vertexShaderSource, String fragmentShaderSource) {
        int vertexShader = compileVertexShader(vertexShaderSource);
        int fragmentShader = compileFragmentShader(fragmentShaderSource);
        int program = linkProgram(vertexShader, fragmentShader);
        validateProgram(program);
        return program;
    }
}
