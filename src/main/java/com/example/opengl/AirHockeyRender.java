package com.example.opengl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.android.objects.Mallets;
import com.android.objects.Puck;
import com.android.objects.Table;
import com.android.programs.ColorShaderProgram;
import com.android.programs.TextureShaderProgram;
import com.android.util.Geometry;
import com.android.util.MatrixHelper;
import com.android.util.Ray;
import com.android.util.TextureHelper;


import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.invertM;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.multiplyMV;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.setLookAtM;
import static android.opengl.Matrix.translateM;

/**
 * Created by jieping on 2017/9/16.
 */

public class AirHockeyRender implements GLSurfaceView.Renderer {
    public Context context;
    private float[] viewMatrix = new float[16];
    private float[] viewProjectionMatrix = new float[16];
    private float[] modelViewProjectionMatrix = new float[16];
    private float[] projectionMatrix = new float[16];
    private float[] modelMatrix = new float[16];

    private Table table;
    private Mallets mallets;
    private Puck puck;

    private TextureShaderProgram textureProgram;
    private ColorShaderProgram colorProgram;

    private int texture;

    private boolean blueMalletPressed = false;
    private Geometry.Point blueMalletPosition;
    private Geometry.Point previousBlueMalletPosition;
    private boolean redMalletPressed = false;
    private Geometry.Point redMalletPosition;
    private Geometry.Point previousRedMalletPosition;
    private final float[] invertedViewProjectionMatrix = new float[16];

    private float leftBound = -0.5f;
    private final float rightBound = 0.5f;
    private final float farBound = -0.8f;
    private final float nearBoound = 0.8f;

    private Geometry.Point puckPosition;
    private Geometry.Vector puckVector;

    public AirHockeyRender(Context context) {
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {

        glClearColor(1f, 1.0f, 1.0f, 0.0f);
        table = new Table();
        mallets = new Mallets(0.08f, 0.15f, 32);
        puck = new Puck(0.06f, 0.02f, 32);
        puckPosition = new Geometry.Point(0f, puck.height / 2f, 0f);
        puckVector = new Geometry.Vector(0f, 0f, 0f);
        blueMalletPosition = new Geometry.Point(0f, mallets.height / 2, 0.4f);
        redMalletPosition = new Geometry.Point(0f, mallets.height / 2f, -0.4f);

        textureProgram = new TextureShaderProgram(context);
        colorProgram = new ColorShaderProgram(context);

        texture = TextureHelper.loadTexture(context, R.drawable.test);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        glViewport(0, 0, width, height);
        //调整观看距离，角度越大代表眼睛离的越远，那么物体就会越小，反之物体会越大
        MatrixHelper.perspectiveM(projectionMatrix, 45, (float) width / (float) height, 1f, 10f);

        setLookAtM(viewMatrix, 0, 0f, 1.2f, 2.2f, 0f, 0f, 0f, 0f, 1f, 0f);//设置观看的视角
        //从左到右分别是rm,rmOffset,eyeX,eyeY,eyeZ,centerX,centerY,centerZ,upX,upY,upZ
      /*  setIdentityM(modelMatrix, 0);
        translateM(modelMatrix, 0, 0f, 0f, -2.5f);//将矩阵沿着z轴偏移，
        rotateM(modelMatrix, 0, -60f, 1f, 0f, 0f);//将矩阵沿着x轴旋转-60度

        float[] temp = new float[16];
        multiplyMM(temp, 0, projectionMatrix, 0, modelMatrix, 0);//两个矩阵做乘法，使得偏移矩阵能够作用与视图
        System.arraycopy(temp, 0, projectionMatrix, 0, temp.length);*/
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        puckPosition = puckPosition.translate(puckVector);
        //碰到边界需要调整方向
        if (puckPosition.x < leftBound + puck.radius || puckPosition.x > rightBound - puck.radius) {
            puckVector = new Geometry.Vector(-puckVector.x, puckVector.y, puckVector.z);
        }
        if (puckPosition.z < farBound + puck.radius || puckPosition.z > nearBoound - puck.radius) {
            puckVector = new Geometry.Vector(puckVector.x, puckVector.y, -puckVector.z);
        }
        puckPosition = new Geometry.Point(clamp(puckPosition.x, leftBound + puck.radius, rightBound - puck.radius), puckPosition.y, clamp(puckPosition.z, farBound + puck.radius, nearBoound - puck.radius));
        //碰到对方需要反方向走
        float distance = Geometry.vectorBetween(blueMalletPosition, puckPosition).length();
        float distance2 = Geometry.vectorBetween(redMalletPosition, puckPosition).length();
        if (distance < puck.radius + mallets.radius || distance2 < puck.radius + mallets.radius) {
            puckVector = new Geometry.Vector(-puckVector.x,-puckVector.y,-puckVector.z);
            puckPosition = puckPosition.translate(puckVector);
        }

        puckVector = puckVector.scale(0.99f);//让速度逐渐降下来
        glClear(GL_COLOR_BUFFER_BIT);
        multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        invertM(invertedViewProjectionMatrix, 0, viewProjectionMatrix, 0);//求viewProjectionMatrix逆矩阵
        positionTableInScene();
        textureProgram.useProgram();
        textureProgram.setUniforms(modelViewProjectionMatrix, texture);
        table.bindData(textureProgram);
        table.draw();

        positionObjectInScene(redMalletPosition.x, redMalletPosition.y, redMalletPosition.z);
        colorProgram.useProgram();
        colorProgram.setUniforms(modelViewProjectionMatrix, 1.0f, 0.0f, 0.0f);
        mallets.bindData(colorProgram);
        mallets.draw();

        positionObjectInScene(blueMalletPosition.x, blueMalletPosition.y, blueMalletPosition.z);
        colorProgram.setUniforms(modelViewProjectionMatrix, 0f, 0.0f, 1.0f);
        mallets.draw();

        positionObjectInScene(puckPosition.x, puckPosition.y, puckPosition.z);
        colorProgram.useProgram();
        colorProgram.setUniforms(modelViewProjectionMatrix, 0.8f, 0.8f, 1.0f);
        puck.bindData(colorProgram);
        puck.draw();
    }

    private void positionTableInScene() {
        setIdentityM(modelMatrix, 0);
        rotateM(modelMatrix, 0, -90f, 1f, 0f, 0f);//将矩阵沿着x轴旋转-90度
        //这里不需要将视图沿着z轴平移是因为viewMatrix已经做到这点了
        multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix, 0, modelMatrix, 0);//两个矩阵做乘法，使得偏移矩阵能够作用与视图

    }

    private void positionObjectInScene(float x, float y, float z) {
        setIdentityM(modelMatrix, 0);
        translateM(modelMatrix, 0, x, y, z);//将矩阵沿着z轴偏移，
        multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix, 0, modelMatrix, 0);//两个矩阵做乘法，使得偏移矩阵能够作用与视图

    }


    public void handleTouchPress(float normalizedX, float normalizedY) {
        Ray ray = convertNormalized2DPointToRay(normalizedX, normalizedY);//将touch坐标转换成三维中的两个点组成的一条直线

        //将物体用一个圆的范围来代表它
        Geometry.Sphere blueMalletBoundingSphere = new Geometry.Sphere(new Geometry.Point(blueMalletPosition.x, blueMalletPosition.y, blueMalletPosition.z), mallets.height / 2f);
        //计算touch点是否在物体范围内
        blueMalletPressed = Geometry.intersects(blueMalletBoundingSphere, ray);


        Geometry.Sphere redMalletBoundingSphere = new Geometry.Sphere(new Geometry.Point(redMalletPosition.x, redMalletPosition.y, redMalletPosition.z), mallets.height / 2f);
        redMalletPressed = Geometry.intersects(redMalletBoundingSphere, ray);
        Log.d("test", "blueMalletPressed is" + blueMalletPressed + " redMalletPressed is" + redMalletPressed);
    }

    public void handleTouchDrag(float normalizedX, float normalizedY) {
        if (blueMalletPressed || redMalletPressed) {
            Ray ray = convertNormalized2DPointToRay(normalizedX, normalizedY);
            Geometry.Plane plane = new Geometry.Plane(new Geometry.Point(0, 0, 0), new Geometry.Vector(0, 1, 0));
            Geometry.Point touchPoint = Geometry.intersectionPoint(ray, plane);
            float distance;
            if (blueMalletPressed) {
                previousBlueMalletPosition = blueMalletPosition;
                blueMalletPosition = new Geometry.Point(clamp(touchPoint.x, leftBound + mallets.radius, rightBound - mallets.radius), mallets.height / 2f, clamp(touchPoint.z, 0 + mallets.radius, nearBoound - mallets.radius));
                distance = Geometry.vectorBetween(blueMalletPosition, puckPosition).length();
                if (distance < puck.radius + mallets.radius) {
                    puckVector = Geometry.vectorBetween(previousBlueMalletPosition, blueMalletPosition);
                }
            } else {
                previousRedMalletPosition = redMalletPosition;
                redMalletPosition = new Geometry.Point(clamp(touchPoint.x, leftBound + mallets.radius, rightBound - mallets.radius), mallets.height / 2f, clamp(touchPoint.z, farBound + mallets.radius, 0 - mallets.radius));
                distance = Geometry.vectorBetween(redMalletPosition, puckPosition).length();
                if (distance < puck.radius + mallets.radius) {
                    puckVector = Geometry.vectorBetween(previousRedMalletPosition, redMalletPosition);
                }
            }

        }
    }

    private float clamp(float value, float min, float max) {
        return Math.min(max, Math.max(value, min));
    }

    private Ray convertNormalized2DPointToRay(float normalizedX, float normalizedY) {
        final float[] nearPointNdc = {normalizedX, normalizedY, -1, 1};
        final float[] farPointNdc = {normalizedX, normalizedY, 1, 1};
        final float[] nearPointWorld = new float[4];
        final float[] farPointWorld = new float[4];

        //得到坐标在viewProjectionMatrix变换前的坐标，这样才能拿来和blueMallet转换之前的坐标进行求距离
        multiplyMV(nearPointWorld, 0, invertedViewProjectionMatrix, 0, nearPointNdc, 0);
        multiplyMV(farPointWorld, 0, invertedViewProjectionMatrix, 0, farPointNdc, 0);

        devideByW(nearPointWorld);
        devideByW(farPointWorld);


        Geometry.Point nearPointRay = new Geometry.Point(nearPointWorld[0], nearPointWorld[1], nearPointWorld[2]);
        Geometry.Point farPointRay = new Geometry.Point(farPointWorld[0], farPointWorld[1], farPointWorld[2]);
        return new Ray(nearPointRay, Geometry.vectorBetween(nearPointRay, farPointRay));
    }

    private void devideByW(float[] vector) {
        //x, y, z分别和w做比值
        vector[0] /= vector[3];
        vector[1] /= vector[3];
        vector[2] /= vector[3];
    }
}
