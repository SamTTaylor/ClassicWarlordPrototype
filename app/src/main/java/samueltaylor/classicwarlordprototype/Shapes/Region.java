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
import java.util.Vector;


import android.opengl.GLES20;


import samueltaylor.classicwarlordprototype.Fragments.fragGameMap;
import samueltaylor.classicwarlordprototype.poly2tri.Poly2Tri;
import samueltaylor.classicwarlordprototype.poly2tri.geometry.polygon.Polygon;
import samueltaylor.classicwarlordprototype.poly2tri.geometry.polygon.PolygonPoint;
import samueltaylor.classicwarlordprototype.poly2tri.triangulation.delaunay.DelaunayTriangle;


public class Region {

    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
                    "uniform vec4 vColor;" +
                    "uniform vec4 playercolour;" +
                    "attribute vec4 vPosition;" +
                    "varying vec4 color;" +
                    "uniform int usegradient;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vec4(vPosition.x,vPosition.y,0,1);" +
                    "  if((usegradient>1) && (vPosition.z>0.0))" +
                    "    color = playercolour;" +
                    "  else" +
                    "    color = vColor;"+
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "varying vec4 color;" +
                    "void main() {" +
                    "  gl_FragColor = color;" +
                    "}";

    private FloatBuffer vertexBuffer;
    private ShortBuffer drawListBuffer;

    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mGradientHandle;
    private int mPlayerColourHandle;
    private int mMVPMatrixHandle;
    private short drawOrder[];
    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    static float regionCoords[] = {};
    private float mOutlineCoords[] = {};//For checking adjacent Regions from fragGameMap

    int fillVertexCount;
    int outlineVertexCount;

    //Lots of colours
    float mColor[];
    public float[] mColorID = { 0.00f, 0.00f, 0.00f, 0.00f };
    float cBlack[] = { 0.0f, 0.0f, 0.0f, 1.0f };
    float cWhite[] = { 1.0f, 1.0f, 1.0f, 1.0f };
    float mPlayerColor[] = cBlack;
    float mSelectedColour[] = cBlack;
    float mFillColor[];//Used to determine shape fill colour on Draw
    float mOutlineColor[];//Used to determine shape outline colour on Draw

    public String mName= "UnNamed";
    Polygon poly;

    private int mDrawMode = 0;
    int prevMode=0;
    private boolean scorched=false;
    private fragGameMap mRenderer;

    private TextObject armyInfo = new TextObject();
    private TextObject bombInfo = new TextObject();
    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public Region(fragGameMap renderer, float[] coords, float[] color) {
        mOutlineCoords = coords;
        regionCoords = coords;
        mRenderer=renderer;

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
            newCoords.add(1.0f);
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
     */
    int mUseGradient=0;
    boolean mUseGradientSetting=false;
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
        mGradientHandle = GLES20.glGetUniformLocation(mProgram, "usegradient");
        mPlayerColourHandle = GLES20.glGetUniformLocation(mProgram, "playercolour");

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        if(mUseGradientSetting==true){
            mUseGradient=2;
        } else {
            mUseGradient=0;
        }
        if(!scorched){//If a region is scorched it is blackened permanently
            switch (mDrawMode){
                case 0://Initial
                    mFillColor = mColor;
                    mOutlineColor = cBlack;
                    prevMode = 0;
                    break;
                case 1://Colour Identification for pixel grab
                    mUseGradient=0;
                    mFillColor = mColorID;
                    mOutlineColor = mColorID;
                    mDrawMode=prevMode;
                    break;
                case 2://Selected
                    mFillColor = mSelectedColour;
                    mOutlineColor = cBlack;
                    prevMode = 2;
                    break;
                default://default
                    mFillColor = mColor;
                    mOutlineColor = cBlack;
                    prevMode = 0;
                    break;
            }
        } else {
            switch (mDrawMode){
                case 1://Colour Identification for pixel grab
                    mUseGradient=0;
                    mFillColor = mColorID;
                    mOutlineColor = mColorID;
                    mDrawMode=prevMode;
                    break;
                default://default scorched is always blackened
                    mUseGradient=2;
                    mPlayerColor=cBlack;
                    mFillColor = mColor;
                    mOutlineColor = cBlack;
                    mDrawMode=0;
                    break;
            }
        }




        // Draw the region
        GLES20.glUniform1i(mGradientHandle, mUseGradient);
        GLES20.glUniform4fv(mColorHandle, 1, mFillColor, 0);//Set region colour
        GLES20.glUniform4fv(mPlayerColourHandle, 1, mPlayerColor, 0);//Set region gradient colour
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, fillVertexCount);

        //Draw outline
        GLES20.glUniform4fv(mPlayerColourHandle, 1, cBlack, 0);//Set region gradient colour
        GLES20.glUniform4fv(mColorHandle, 1, mOutlineColor, 0);//Set outline colour
        GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, fillVertexCount, outlineVertexCount);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);

        SetupText();
    }

    float letterwidth=0.05f;
    float letterheight=0.1f;
    DelaunayTriangle T1;
    private void findCentrePoint() {
        T1 = poly.getTriangles().get(0);
        for(int i=0;i<poly.getTriangles().size();i++){
            if(poly.getTriangles().get(i).area()> T1.area()){
                T1 =poly.getTriangles().get(i);
            }
        }
    }

    public void SetupText()
    {
            armyInfo.setText("R1");
            bombInfo.setText("A1");
            findCentrePoint();
            Vector<TextObject> col = mRenderer.getTextManager().txtcollection;

            armyInfo.setX(T1.centroid().getXf()-(letterwidth*armyInfo.text.length())/2);
            armyInfo.setY(T1.centroid().getYf()-letterheight);
            bombInfo.setX(T1.centroid().getXf()-(letterwidth*bombInfo.text.length())/2);
            bombInfo.setY(T1.centroid().getYf());

            if (!col.contains(armyInfo)) {
                col.add(armyInfo);
            }
        if (!col.contains(bombInfo)) {
            col.add(bombInfo);
        }
    }

    public void toggleDrawMode(int i) {
        mDrawMode=i;
    }
    public void setmPlayerColor(float[] f){mPlayerColor = f;}
    public void setmSelectedColour(float[] f){mSelectedColour = f;}
    public void setUseGradient(boolean b, float[] colour){
        mUseGradientSetting=b;
        mPlayerColor = colour;
        if(colour == null){
            mPlayerColor = cBlack;
        }
    }
    public void setScorched(boolean b){scorched=b;}

    public float[] getmOutlineCoords(){
        return mOutlineCoords;
    }
}


