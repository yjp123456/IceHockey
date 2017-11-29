package com.android.objects;

import com.android.util.Geometry;

import java.util.ArrayList;
import java.util.List;

import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glDrawArrays;

/**
 * Created by jieping on 2017/9/17.
 */

public class ObjectBuilder {
    private static final int FLOATS_PER_VERTEX = 3;
    private final float[] vertexData;
    private int offSet = 0;

    private List<DrawCommand> drawList = new ArrayList<DrawCommand>();

    private ObjectBuilder(int sizeInVertices) {
        vertexData = new float[sizeInVertices * FLOATS_PER_VERTEX];
    }

    private static int sizeOfCircleInVertices(int numPoints) {
        return 1 + (numPoints + 1);
    }

    private static int sizeOfOpenCylinderInVertices(int numPoints) {
        return (numPoints + 1) * 2;
    }

    static GeneratedData createPuck(Geometry.Cylinder puck, int numPoints) {
        int size = sizeOfCircleInVertices(numPoints) + sizeOfOpenCylinderInVertices(numPoints);
        ObjectBuilder builder = new ObjectBuilder(size);

        Geometry.Circle puckTop = new Geometry.Circle(puck.center.translateY(puck.height / 2f), puck.radius);

        builder.appendCircle(puckTop, numPoints);
        builder.appendOpenCylinder(puck, numPoints);
        return builder.build();
    }

    static GeneratedData createMallet(Geometry.Point center, float radius, float height, int numPoints) {

        int size = sizeOfCircleInVertices(numPoints) * 2 + sizeOfOpenCylinderInVertices(numPoints) * 2;
        ObjectBuilder builder = new ObjectBuilder(size);

        float baseHeight = height * 0.25f;//底部的圆柱

        Geometry.Circle baseCircle = new Geometry.Circle(center.translateY(-baseHeight), radius);
        Geometry.Cylinder baseCylinder = new Geometry.Cylinder(baseCircle.center.translateY(-baseHeight / 2), radius, baseHeight);

        builder.appendCircle(baseCircle, numPoints);//画圆柱顶部的椭圆
        builder.appendOpenCylinder(baseCylinder, numPoints);//画圆柱的柱面

        float handleHeight = height * 0.75f;//顶部的圆柱
        float handleRadius = radius / 3f;

        Geometry.Circle handleCircle = new Geometry.Circle(center.translateY(height * 0.5f), handleRadius);
        Geometry.Cylinder handleCylinder = new Geometry.Cylinder(handleCircle.center.translateY(-handleHeight / 2f), handleRadius, handleHeight);

        builder.appendCircle(handleCircle, numPoints);
        builder.appendOpenCylinder(handleCylinder, numPoints);

        return builder.build();


    }

    private void appendCircle(Geometry.Circle circle, final int numPoints) {
        final int startVertex = offSet / FLOATS_PER_VERTEX;
        final int numVertices = sizeOfCircleInVertices(numPoints);
        vertexData[offSet++] = circle.center.x;
        vertexData[offSet++] = circle.center.y;
        vertexData[offSet++] = circle.center.z;

        for (int i = 0; i <= numPoints; i++) {
            float angleInRadians = ((float) i / (float) numPoints) * ((float) Math.PI * 2f);
            vertexData[offSet++] = circle.center.x + circle.radius * (float) Math.cos(angleInRadians);
            vertexData[offSet++] = circle.center.y;
            vertexData[offSet++] = circle.center.z + circle.radius * (float) Math.sin(angleInRadians);
        }

        drawList.add(new DrawCommand() {
            @Override
            public void draw() {
                //通过画三角形来画圆形，即三角形个数足够多的情况下就类似于圆了
                glDrawArrays(GL_TRIANGLE_FAN, startVertex, numVertices);
            }
        });
    }

    private void appendOpenCylinder(Geometry.Cylinder cylinder, int numPoints) {
        final int startVertex = offSet / FLOATS_PER_VERTEX;
        final int numVertices = sizeOfOpenCylinderInVertices(numPoints);
        final float yStart = cylinder.center.y - (cylinder.height / 2f);
        final float yEnd = cylinder.center.y + (cylinder.height / 2f);

        for (int i = 0; i <= numPoints; i++) {
            float angleInRadians = ((float) i / (float) numPoints) * ((float) Math.PI * 2f);
            float xPosition = cylinder.center.x + cylinder.radius * (float) Math.cos(angleInRadians);
            float zPosition = cylinder.center.z + cylinder.radius * (float) Math.sin(angleInRadians);
            vertexData[offSet++] = xPosition;
            vertexData[offSet++] = yStart;
            vertexData[offSet++] = zPosition;

            vertexData[offSet++] = xPosition;
            vertexData[offSet++] = yEnd;
            vertexData[offSet++] = zPosition;

        }

        drawList.add(new DrawCommand() {
            @Override
            public void draw() {
                glDrawArrays(GL_TRIANGLE_STRIP, startVertex, numVertices);
            }
        });
    }

    static interface DrawCommand {
        void draw();
    }

    static class GeneratedData {
        public final float[] vertexData;
        public List<DrawCommand> drawList = new ArrayList<DrawCommand>();

        GeneratedData(float[] vertexData, List<DrawCommand> drawList) {
            this.vertexData = vertexData;
            this.drawList = drawList;
        }


    }

    private GeneratedData build() {
        return new GeneratedData(vertexData, drawList);
    }
}
