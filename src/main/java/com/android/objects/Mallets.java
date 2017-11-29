package com.android.objects;

import com.android.data.VertexArray;
import com.android.programs.ColorShaderProgram;
import com.android.util.Geometry;

import java.util.List;

/**
 * Created by jieping on 2017/9/16.
 */

public class Mallets {
    private static final int POSTION_COMPONENT_COUNT = 3;

    public float radius, height;

    private List<ObjectBuilder.DrawCommand> drawList;
    private static final int COLOR_COMPONENT_COUNT = 3;
    //private static final int STRIDE = (POSTION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTE_PER_FLOAT;

    private final float[] VERTEX_DATA = {
            //x,y,R,G,B
            0f, -0.4f, 1.0f, 0f, 0f,
            0f, 0.4f, 1.0f, 0f, 0f,
    };

    private final VertexArray vertexArray;

    public Mallets(float radius, float height, int numPointsAroundMallet) {
        ObjectBuilder.GeneratedData generatedData = ObjectBuilder.createMallet(new Geometry.Point(0f, 0f, 0f), radius, height, numPointsAroundMallet);
        this.radius = radius;
        this.height = height;

        vertexArray = new VertexArray(generatedData.vertexData);
        drawList = generatedData.drawList;
    }

    public void bindData(ColorShaderProgram colorShaderProgram) {
        vertexArray.setVertexAttribPointer(0, colorShaderProgram.getPostionAttributeLocation(), POSTION_COMPONENT_COUNT, 0);
        //vertexArray.setVertexAttribPointer(POSTION_COMPONENT_COUNT,colorShaderProgram.getColorAttributeLocation(),COLOR_COMPONENT_COUNT,STRIDE);
    }

    public void draw() {
        for (ObjectBuilder.DrawCommand drawCommand : drawList) {
            drawCommand.draw();
        }
    }
}
