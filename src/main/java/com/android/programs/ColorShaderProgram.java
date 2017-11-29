package com.android.programs;

import android.content.Context;

import com.example.opengl.R;

import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUniformMatrix4fv;

/**
 * Created by jieping on 2017/9/16.
 */

public class ColorShaderProgram extends ShaderProgram {
    public final int uMatrixLocation;
    public final int uColorLocation;

    private final int aPositionLocation;


    public ColorShaderProgram(Context context){
        super(context, R.raw.simple_vertex_shade,R.raw.simple_fragment_shade);
        uMatrixLocation = glGetUniformLocation(program, U_MATRIX);
        uColorLocation = glGetUniformLocation(program, U_COLOR);


        aPositionLocation = glGetAttribLocation(program, A_POSITION);

    }

    public void setUniforms(float[] matrix,float r,float g, float b){
        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);//使得横竖屏宽高比例不变
        glUniform4f(uColorLocation,r,g,b,1.0f);
    }

    public int getPostionAttributeLocation(){
        return aPositionLocation;
    }

    public int getColorAttributeLocation(){
        return uColorLocation;
    }
}
