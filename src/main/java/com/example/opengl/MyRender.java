package com.example.opengl;

import android.content.Context;
import android.opengl.GLSurfaceView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_LINES;
import static android.opengl.GLES20.GL_POINTS;
import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;

/**
 * Created by jieping_yang on 2017/9/14.
 */

public class MyRender implements GLSurfaceView.Renderer {
    public Context context;
    private static final int POSITION_COMPONENT_COUNT = 4;
    private static final int COLOR_COMPONENT_COUNT = 3;

    private static final int BYTE_PER_FLOAT = 4;
    private static final int STRIDE = (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTE_PER_FLOAT;
    private FloatBuffer vertexData;

    private String A_COLOR = "a_Color";
    private int aColorLocation;

    private String A_POSITION = "a_Position";
    private int aPositionLocation;

    private String U_MATRIX = "u_Matrix";
    private float[] projectionMatrix = new float[16];
    private int uMatrixPosition;

    private float[] modelMatrix = new float[16];

    public MyRender(Context context) {
        this.context = context;
        float[] tableVertices = {
                //opengl在手机上x,y,z坐标范围都是[-1,1]
                //triangle
                //前四个设置为点坐标，分别是x,y,z,w，w控制距离，w越大，矩阵变换后x,y绝对值越大，后面三个代表颜色
                0f, 0f, 0f, 1.5f, 1f, 1f, 1f,
                -0.5f, -0.8f, 0f, 1f, 0.7f, 0.7f, 0.7f,
                0.5f, -0.8f, 0f, 1f, 0.7f, 0.7f, 0.7f,
                0.5f, 0.8f, 0f, 2f, 0.7f, 0.7f, 0.7f,
                -0.5f, 0.8f, 0f, 2f, 0.7f, 0.7f, 0.7f,
                -0.5f, -0.8f, 0f, 1f, 0.7f, 0.7f, 0.7f,

                //line1
                -0.5f, 0f, 0f, 1.5f, 1.0f, 0f, 0f,
                0.5f, 0f, 0f, 1.5f, 1.0f, 0f, 0f,

                //point
                0f, -0.4f, 0f, 1.25f, 0f, 0f, 1.0f,
                0f, 0.4f, 0f, 1.75f, 0f, 0f, 1.0f,
        };
        vertexData = ByteBuffer.allocateDirect(tableVertices.length * BYTE_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexData.put(tableVertices);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {

        glClearColor(0f, 0f, 0f, 0.0f);
        String vertexShadeSource = TextResourceReader.readTextFileFromResource(context, R.raw.simple_vertex_shade);
        String fragmentShadeSource = TextResourceReader.readTextFileFromResource(context, R.raw.simple_fragment_shade);
        int vertexShader = ShadeHelper.compileVertexShade(vertexShadeSource);
        int fragmentShader = ShadeHelper.compileFragemtShade(fragmentShadeSource);
        int program = ShadeHelper.linkProgram(vertexShader, fragmentShader);

        ShadeHelper.validateProgram(program);
        glUseProgram(program);

        aColorLocation = glGetAttribLocation(program, A_COLOR);
        aPositionLocation = glGetAttribLocation(program, A_POSITION);
        uMatrixPosition = glGetUniformLocation(program, U_MATRIX);

        vertexData.position(0);
        glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT, GL_FLOAT, false, STRIDE, vertexData);
        /*第二个参数代表每个vertex由两个值组成，即二维坐标，这个函数的作用是告诉opengl去哪里找a_Position
           STRIDE告诉opengl每个点之间间隔多大，因为现在点后面还有颜色，需要跳过颜色才能获取到下个点
        */
        glEnableVertexAttribArray(aPositionLocation);//使上面的设置生效

        vertexData.position(POSITION_COMPONENT_COUNT);
        glVertexAttribPointer(aColorLocation, COLOR_COMPONENT_COUNT, GL_FLOAT, false, STRIDE, vertexData);
        //告诉opengl如何获取每个点的color值
        glEnableVertexAttribArray(aColorLocation);//使上面的设置生效


       /* glUniform4f(uColorLocation, 1.0f, 1.0f, 1.0f, 1.0f);//更新颜色设置，因为使用vec4，所以是四维的
        glDrawArrays(GL_TRIANGLES, 0, 6);//画三角形，0代表起始位置，6代表读的个数，因为我们告诉opengl每个点是二维的，所以它会读取前12个元素

        glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
        glDrawArrays(GL_LINES, 6, 2);

        glUniform4f(uColorLocation, 0.0f, 0.0f, 1.0f, 1.0f);
        glDrawArrays(GL_POINTS, 8, 1);
        glDrawArrays(GL_POINTS, 9, 1);*/


    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        glViewport(0, 0, width, height);
       /* float aspectRatio = width > height ? (float) width / (float) height : (float) height / (float) width;
        if (width > height) {
            orthoM(projectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f);
            *//*参数从左到右分别是m, mOffset, left, right, bottom, top ,near , far
            构造的矩阵如下,从上到下分别是x,y,z,w,w默认是1：
            2/(right-left)  0               0,             -(right+left)/(right-left)
            0              2/(top-bottom)   0              (top+bottom)/(top-bottom)
            0              0               -2/(far-near)   (far+near)/(far-near)
            0              0                0               1
            结果就是如果width比较大，就会2/(right-left）就会小于1，这样就能缩小width，达到调整宽度作用
             *//*
        } else {
            orthoM(projectionMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f);
        }*/

        //以45度角来观看屏幕，1f,10f控制z轴范围，代表距离从-1到-10，这个范围以外将看不到视图
        MatrixHelper.perspectiveM(projectionMatrix, 45, (float) width / (float) height, 1f, 10f);

        setIdentityM(modelMatrix, 0);
        translateM(modelMatrix, 0, 0f, 0f, -2.5f);//将矩阵沿着z轴偏移，
        rotateM(modelMatrix, 0, -60f, 1f, 0f, 0f);//将矩阵沿着x轴旋转-60度

        float[] temp = new float[16];
        multiplyMM(temp, 0, projectionMatrix, 0, modelMatrix, 0);//两个矩阵做乘法，使得偏移矩阵能够作用与视图
        System.arraycopy(temp, 0, projectionMatrix, 0, temp.length);

    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        glClear(GL_COLOR_BUFFER_BIT);
        glUniformMatrix4fv(uMatrixPosition, 1, false, projectionMatrix, 0);//使得横竖屏宽高比例不变
        glDrawArrays(GL_TRIANGLE_FAN, 0, 6);
        //gl_triangle_fan这种会重复利用点，即前三个画个三角形，然后从第二个开始选三个点继续画，所以6个点可以画4个三角形
        glDrawArrays(GL_LINES, 6, 2);
        glDrawArrays(GL_POINTS, 8, 1);
        glDrawArrays(GL_POINTS, 9, 1);
    }
}
