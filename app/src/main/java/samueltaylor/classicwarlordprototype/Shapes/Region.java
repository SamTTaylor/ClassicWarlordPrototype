/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package samueltaylor.classicwarlordprototype.Shapes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import android.opengl.GLES20;


import samueltaylor.classicwarlordprototype.Fragments.fragGameMap;
import samueltaylor.classicwarlordprototype.poly2tri.Poly2Tri;
import samueltaylor.classicwarlordprototype.poly2tri.geometry.polygon.Polygon;
import samueltaylor.classicwarlordprototype.poly2tri.geometry.polygon.PolygonPoint;
import samueltaylor.classicwarlordprototype.poly2tri.triangulation.delaunay.DelaunayTriangle;

/**
 * A two-dimensional triangle for use as a drawn object in OpenGL ES 2.0.
 */
public class Region {

    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    private FloatBuffer vertexBuffer;
    private ShortBuffer drawListBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;
    private short drawOrder[];
    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    static float regionCoords[] = {};
    float mColor[];
    float mPlayerColor[];
    public float[] mColorID = { 0.00f, 0.00f, 0.00f, 0.00f };
    float cBlack[] = { 0.0f, 0.0f, 0.0f, 1.0f };
    float mPlayerOutline[] = cBlack;
    int fillVertexCount;
    int outlineVertexCount;
    float mFillColor[];//Used to determine shape fill colour on Draw
    float mOutlineColor[];//Used to determine shape outline colour on Draw
    public String mName= "UnNamed";
    Polygon poly;

    private int mDrawMode = 0;
    int prevMode=0;
    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public Region(fragGameMap renderer, float[] coords, float[] color) {
        regionCoords = coords;
        fillVertexCount = regionCoords.length / COORDS_PER_VERTEX;
        List<PolygonPoint> PointList = new ArrayList<>();
        for (int i=0;i<regionCoords.length;i+=3){
            PolygonPoint pp = new PolygonPoint(regionCoords[i], regionCoords[i+1],0.0f);
            PointList.add(pp);
        }
        poly = new Polygon(PointList);

        Poly2Tri.triangulate(poly);
        List<Float> newCoords = new ArrayList<>();
        for(DelaunayTriangle p : poly.getTriangles()){
            newCoords.add(p.points[0].getXf());
            newCoords.add(p.points[0].getYf());
            newCoords.add(0.0f);
            newCoords.add(p.points[1].getXf());
            newCoords.add(p.points[1].getYf());
            newCoords.add(0.0f);
            newCoords.add(p.points[2].getXf());
            newCoords.add(p.points[2].getYf());
            newCoords.add(0.0f);
        }

        fillVertexCount = newCoords.size() / COORDS_PER_VERTEX;

        //Add outline
        for(float f : coords){
            newCoords.add(f);
        }
        outlineVertexCount = coords.length / COORDS_PER_VERTEX;

        drawOrder = new short[fillVertexCount];
        int j=0;
        for(short s : drawOrder){
            drawOrder[j] = (short) j;
            j++;
        }
        regionCoords = new float[newCoords.size()];
        for(int i=0;i<newCoords.size();i++){
            regionCoords[i] = newCoords.get(i);
        }
        mColor = color;
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                regionCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(regionCoords);
        vertexBuffer.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        // prepare shaders and OpenGL program
        int vertexShader = renderer.loadShader(
                GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = renderer.loadShader(
                GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables
    }

    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     *
     * @param mvpMatrix - The Model View Project matrix in which to draw
     * this shape.
     */
    public void draw(float[] mvpMatrix) {
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the region vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the region coordinate data
        GLES20.glVertexAttribPointer(
                mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");


        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        switch (mDrawMode){
            case 0://Initial
                mFillColor = mColor;
                mOutlineColor = mPlayerOutline;
                prevMode = 0;
                break;
            case 1://Colour Identification for pixel grab
                mFillColor = mColorID;
                mOutlineColor = mColorID;
                mDrawMode=prevMode;
                break;
            case 2://Selected
                mFillColor = mPlayerColor;
                mOutlineColor = mPlayerOutline;
                prevMode = 3;
                break;
            default://default
                mFillColor = mColor;
                mOutlineColor = mPlayerOutline;
                prevMode = 0;
                break;
       }

        // Draw the region
        GLES20.glUniform4fv(mColorHandle, 1, mFillColor, 0);//Set region colour
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0, fillVertexCount);


        //Draw outline
        GLES20.glUniform4fv(mColorHandle, 1, mOutlineColor, 0);//Set black colour
        GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, fillVertexCount, outlineVertexCount);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    public void toggleDrawMode(int i) {
        mDrawMode=i;
    }
    public void setmPlayerColor(float[] f){mPlayerColor = f;}
    public void resetmPlayerOutline(){mOutlineColor=cBlack;}
}
