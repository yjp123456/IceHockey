package com.android.objects;

import com.android.data.VertexArray;
import com.android.programs.ColorShaderProgram;
import com.android.util.Geometry;

import java.util.List;

/**
 * Created by jieping on 2017/9/17.
 */

public class Puck {
    private static final int POSTION_COMPONENT_COUNT = 3;

    public float radius,height;

    private VertexArray vertexArray;
    private List<ObjectBuilder.DrawCommand> drawList;

    public Puck(float radius, float height, int numPointsAroundPuck){
        ObjectBuilder.GeneratedData generatedData = ObjectBuilder.createPuck(new Geometry.Cylinder(new Geometry.Point(0f,0f,0f),radius,height),numPointsAroundPuck);
        this.radius = radius;
        this.height = height;

        vertexArray = new VertexArray(generatedData.vertexData);
        drawList = generatedData.drawList;
    }

    public void bindData(ColorShaderProgram colorShaderProgram){
        vertexArray.setVertexAttribPointer(0,colorShaderProgram.getPostionAttributeLocation(),POSTION_COMPONENT_COUNT,0);
    }

    public void draw(){
        for(ObjectBuilder.DrawCommand drawCommand: drawList){
            drawCommand.draw();
        }
    }
}
