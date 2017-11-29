package com.android.util;

/**
 * Created by jieping on 2017/9/16.
 */

public class MatrixHelper {
    public static void perspectiveM(float[] m, float yFovInDegrees, float aspect, float n, float f) {
        //将opengl右手坐标系转换成显卡的左手坐标系
        final float angelInRadians = (float) (yFovInDegrees * Math.PI / 180.0);//把角度转换成radian
        final float a = (float) (1.0 / Math.tan(angelInRadians / 2.0));
        //第一列
        m[0] = a / aspect;
        m[1] = 0f;
        m[2] = 0f;
        m[3] = 0f;

        //第二列
        m[4] = 0f;
        m[5] = a;
        m[6] = 0f;
        m[7] = 0f;

        //第三列
        m[8] = 0f;
        m[9] = 0f;
        m[10] = -((f + n) / (f - n));
        m[11] = -1f;

        //第四列
        m[12] = 0f;
        m[13] = 0f;
        m[14] = -((2f * f * n) / (f - n));
        m[15] = 0f;
        //opengl矩阵是按列来写的，比如m前四个是第一列

    }
}
