package com.android.programs;

import android.content.Context;

import com.example.opengl.ShadeHelper;
import com.example.opengl.TextResourceReader;

import static android.opengl.GLES20.glUseProgram;

/**
 * Created by jieping on 2017/9/16.
 */

public class ShaderProgram {
    protected static final String U_MATRIX = "u_Matrix";
    protected static final String U_TEXTURE_UNIT = "u_TextureUnit";
    protected static final String U_COLOR = "u_Color";

    protected static final String A_POSITION = "a_Position";
    protected static final String A_COLOR = "a_Color";
    protected static final String A_TEXTURE_COORDINATES = "a_TextureCoordinates";

    protected final int program;

    protected ShaderProgram(Context context, int vertexShaderResourceId, int fragmentShaderResourceId) {
        program = ShadeHelper.buildProgram(TextResourceReader.readTextFileFromResource(context, vertexShaderResourceId), TextResourceReader.readTextFileFromResource(context, fragmentShaderResourceId));

    }

    public void useProgram() {
        glUseProgram(program);
    }
}
