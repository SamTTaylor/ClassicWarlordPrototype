package samueltaylor.classicwarlordprototype.Fragments;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.List;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import samueltaylor.classicwarlordprototype.GameController;
import samueltaylor.classicwarlordprototype.OpenGL.GameGLSurfaceView;
import samueltaylor.classicwarlordprototype.Shapes.Region;
import samueltaylor.classicwarlordprototype.Shapes.TextManager;
import samueltaylor.classicwarlordprototype.Shapes.TextObject;
import samueltaylor.classicwarlordprototype.Shapes.shaderCodes;
import samueltaylor.classicwarlordprototype.XMLParsing.SVGtoRegionParser;


public class fragGameMap extends Fragment implements GLSurfaceView.Renderer{
    private OnFragmentInteractionListener mListener;

    public fragGameMap() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //UI calls after fragment has finished loading elements

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        initialize();
        return mGLView;
    }



    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onMapFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGLView!=null)
            mGLView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGLView!=null)
            mGLView.onPause();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onMapFragmentInteraction(Uri uri);
    }





    //OPENGLES & DRAWING THE MAP

    GameGLSurfaceView mGLView;
    boolean mSurfaceCreated;


    //Check if device supports OpenGLES2
    private boolean hasGLES20() {
        ActivityManager am = (ActivityManager)
                getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo info = am.getDeviceConfigurationInfo();
        return info.reqGlEsVersion >= 0x20000;
    }

    //Initialise OpenGL
    private void initialize() {
        if (hasGLES20()) {
            mGLView = new GameGLSurfaceView(getActivity());
            mGLView.setEGLContextClientVersion(2);
            mGLView.setPreserveEGLContextOnPause(true);
            mGLView.customSetRenderer(this);
            mGLView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        } else {
            // Time to get a new phone, OpenGL ES 2.0 not supported.
        }
    }



    //Drawing
    Region[] regions;
    static float regionCoords[];
    private int mRegionProgram;
    public List<SVGtoRegionParser.Region> mWorld;
    float mWorldWidth = 12.0f;
    float mWorldHeight = 8.9f;
    public float mMoveX;
    public float mMoveY;
    public boolean mClicked = false;//Has the surface been clicked
    public boolean mLongPressed=false;
    public float[] mTouchedPos = new float[2];
    private GL10 mGl;
    private TextManager tm;

    //Initial drawing
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glClearColor(0.608f, 0.722f, 0.859f, 1.0f);
        mGl = gl;
        GLES20.glDisable(GLES20.GL_DITHER);


        // Create the triangles
        SetupTriangle();
        // Create the image information
        SetupImage();
        // Create our texts
        SetupText();
        // Create the shaders, images

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        //Image shader
        int vertexShader = shaderCodes.loadShader(GLES20.GL_VERTEX_SHADER, shaderCodes.vertexBG);
        int fragmentShader = shaderCodes.loadShader(GLES20.GL_FRAGMENT_SHADER, shaderCodes.fragBG);

        shaderCodes.progBg = GLES20.glCreateProgram();             // create empty OpenGL ES Program
        GLES20.glAttachShader(shaderCodes.progBg, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(shaderCodes.progBg, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(shaderCodes.progBg);                  // creates OpenGL ES program executables

        // Text shader
        int vshadert = shaderCodes.loadShader(GLES20.GL_VERTEX_SHADER, shaderCodes.vertexText);
        int fshadert = shaderCodes.loadShader(GLES20.GL_FRAGMENT_SHADER, shaderCodes.fragText);

        shaderCodes.progText = GLES20.glCreateProgram();
        GLES20.glAttachShader(shaderCodes.progText, vshadert);
        GLES20.glAttachShader(shaderCodes.progText, fshadert); 		// add the fragment shader to program
        GLES20.glLinkProgram(shaderCodes.progText);                  // creates OpenGL ES program executables

        // Set our shader programm
        GLES20.glUseProgram(shaderCodes.progBg);

        // Regions shaters
        int regionVShader = loadShader(
                GLES20.GL_VERTEX_SHADER, shaderCodes.regionVertexShader);
        int regionFShader = loadShader(
                GLES20.GL_FRAGMENT_SHADER, shaderCodes.regionFragmentShader);


        mRegionProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mRegionProgram, regionVShader);   // add the vertex shader to program
        GLES20.glAttachShader(mRegionProgram, regionFShader); // add the fragment shader to program
        GLES20.glLinkProgram(mRegionProgram);                  // create OpenGL program executables

        // initialiseWorld();
        initialiseWorld();
        mSurfaceCreated = true;
        //Draw world
        drawRegions();
        ((GameController)getActivity()).fadeOutLoadingFragment();
    }




    public void initialiseWorld(){
        int i = 0;
        regions = new Region[mWorld.size()];
        float[] color = null;
        for(SVGtoRegionParser.Region r : mWorld){
            regionCoords = r.path;
            switch (r.type){
                case "rural": color= new float[]{ 0.651f, 0.871f, 0.78f, 1.0f }; break;
                case "dense": color= new float[]{ 0.965f, 0.722f, 0.729f, 1.0f }; break;
                case "city":  color= new float[]{ 1f, 0.965f, 0.58f, 1.0f }; break;
                case "mountain":  color= new float[]{ 0.831f, 0.784f, 0.745f, 1.0f }; break;
                case "light":  color= new float[]{ 1.0f, 1.0f, 1.0f, 1.0f }; break;
                case "sea": color= new float[]{ 0.608f, 0.722f, 0.859f, 1.0f}; break;
                default: color= new float[]{0.0f,0.0f,0.0f,1.0f}; break;
            }
            regions[i] = new Region(this, regionCoords, color);
            if(r.name!=null){
                regions[i].mName=r.name;
            }
            assignID(i);
            i++;
        }
    }


    //Manipulation of drawing
    private final float[] mMVPMatrix = new float[16];
    private final float[] mOrthographicMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float)width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.orthoM(mOrthographicMatrix, 0, ratio, -ratio, 1, -1, 3, 30);

    }

    //Redrawing
    @Override
    public void onDrawFrame(GL10 gl) {
        checkClickedRegion(gl);

        float ratio = (float) mGLView.getWidth() / mGLView.getHeight();
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        // Set the camera position (View matrix)

        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 25, mMoveX, mMoveY, 0f, 0f, 1.0f, 0.0f);
        Matrix.orthoM(mOrthographicMatrix, 0, ratio * mZoom, -ratio * mZoom, -1 * mZoom, 1 * mZoom, 3, 30);
        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mOrthographicMatrix, 0, mViewMatrix, 0);
        Matrix.translateM(mMVPMatrix, 0, -mWorldWidth/2, -mWorldHeight/2, 0.0f);
        //Draw all the regions loaded from the world
        drawRegions();
    }


    public static int loadShader(int type, String shaderCode){
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    public boolean allowmovement=true;
    public void moveScreenPosition(float x, float y) {
        if(allowmovement){
            if(mMoveX-x>-mWorldWidth/2 && mMoveX-x<mWorldWidth/2){
                mMoveX = mMoveX - x;
            }
            if(mMoveY-y>-mWorldHeight/2 && mMoveY-y<mWorldHeight/2){
                mMoveY = mMoveY - y;
            }
        }
    }

    float mZoom = -4.5f;
    float mZoomRenderLimit = -2.0f;
    float mOutline = 1.0f;
    float mSensitivity = 0.2f;
    public void incrementZoom(boolean direction){
        if (mZoom <=-1.1 && direction == false){
            //in
            mZoom+=mSensitivity;
            mOutline+=mSensitivity;
        }
        if (mZoom >= -4.4 && direction == true){
            //out
            mZoom-=mSensitivity;
            mOutline-=mSensitivity;
        }
    }

    void assignID(int regionnumber){
        int UBR=31; //Upper boundary for blue and red
        int UG=63; //Upper boundary for green

        // split regionnumber into 3/4/3 bits:
        int R = (regionnumber >> 7) & 7;
        int G = (regionnumber >> 3) & 15;
        int B = regionnumber & 7;

        // space out the values by multiplying by 4 and adding 2:
        R = R * 4 + 2;
        G = G * 4 + 2;
        B = B * 4 + 2;

//        // combine into an RGB565 value if needed in future:
//        int RGB565 = (R << 11) | (G << 5) | B;

        // assign the colors
        regions[regionnumber].mColorID[0] = ((float)R)/UBR;
        regions[regionnumber].mColorID[1] = ((float)G)/UG;
        regions[regionnumber].mColorID[2] = ((float)B)/UBR;
    }

    void checkClickedRegion(GL10 gl){
        //First, check for click
        if(mClicked == true){
            mClicked=false;
            //Draw all the regions in their assigned ID colour
            drawColourIDRegions();
            ByteBuffer PixelBuffer = ByteBuffer.allocateDirect(4);
            gl.glReadPixels((int) mTouchedPos[0], mGLView.getHeight() - (int) mTouchedPos[1], 1, 1, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, PixelBuffer);
            byte b[] = new byte[4];
            PixelBuffer.get(b);

            int R = (b[0] & 0xFF) >> 5;//Read RGB565 code
            int G = (b[1] & 0xFF) >> 4;
            int B = (b[2] & 0xFF) >> 5;

            //Manipulate it back to ID
            int regionnumber = (R << 7) | (G << 3) | B;
            Log.e(regions[regionnumber].mName,String.valueOf(mTouchedPos[0]) + " : " + String.valueOf(mTouchedPos[1]));
            ((GameController)getActivity()).regionClicked(regionnumber);
        }

        if(mLongPressed== true){
            mLongPressed=false;
            //Draw all the regions in their assigned ID colour
            drawColourIDRegions();
            ByteBuffer PixelBuffer = ByteBuffer.allocateDirect(4);
            gl.glReadPixels((int) mTouchedPos[0], mGLView.getHeight() - (int) mTouchedPos[1], 1, 1, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, PixelBuffer);
            byte b[] = new byte[4];
            PixelBuffer.get(b);

            int R = (b[0] & 0xFF) >> 5;//Read RGB565 code
            int G = (b[1] & 0xFF) >> 4;
            int B = (b[2] & 0xFF) >> 5;

            //Manipulate it back to ID
            int regionnumber = (R << 7) | (G << 3) | B;
            ((GameController)getActivity()).regionLongPressed(regionnumber);
        }
    }

    public void selectRegion(int id, float[] playercolour){
        if(id<regions.length){
            regions[id].setmSelectedColour(playercolour);
            regions[id].toggleDrawMode(2);
            reRender();
        }
    }
    public void deselectRegion(int id){
        if(id<regions.length){
            regions[id].toggleDrawMode(0);
            reRender();
        }
    }

    public Region getRegion(int id){
        return regions[id];
    }
    public void reRender(){
        mGLView.requestRender();
    }

    private void drawRegions(){
        GLES20.glLineWidth(mOutline);
        for(Region r : regions){
            r.draw(mMVPMatrix);
        }
        if(mZoom>mZoomRenderLimit){
            prepareDraw();
            tm.Draw(mMVPMatrix);
        }
    }

    private void drawColourIDRegions(){
        GLES20.glDisable(GLES20.GL_BLEND);
        for(Region r : regions){
            r.toggleDrawMode(1);//Colour ID mode
            r.draw(mMVPMatrix);
        }
        GLES20.glEnable(GLES20.GL_BLEND);
    }





    // Text Drawing methods
    public static float vertices[];
    public static short indices[];
    public static float uvs[];
    public FloatBuffer vertexBuffer;
    public ShortBuffer drawListBuffer;
    public FloatBuffer uvBuffer;
    float 	ssu = 0.004f;
    public void SetupText()
    {
        // Create our text manager
        tm = new TextManager();

        // Tell our text manager to use index 1 of textures loaded
        tm.setTextureID(1);

        // Pass the uniform scale
        tm.setUniformscale(ssu);

    }

    public void prepareDraw(){
        // Prepare the text for rendering
        tm.PrepareDraw();
    }
    public void SetupImage()
    {
        // We will use a randomizer for randomizing the textures from texture atlas.
        // This is strictly optional as it only effects the output of our app,
        // Not the actual knowledge.
        Random rnd = new Random();

        // 30 imageobjects times 4 vertices times (u and v)
        uvs = new float[30*4*2];

        // We will make 30 randomly textures objects
        for(int i=0; i<30; i++)
        {
            int random_u_offset = rnd.nextInt(2);
            int random_v_offset = rnd.nextInt(2);

            // Adding the UV's using the offsets
            uvs[(i*8) + 0] = random_u_offset * 0.5f;
            uvs[(i*8) + 1] = random_v_offset * 0.5f;
            uvs[(i*8) + 2] = random_u_offset * 0.5f;
            uvs[(i*8) + 3] = (random_v_offset+1) * 0.5f;
            uvs[(i*8) + 4] = (random_u_offset+1) * 0.5f;
            uvs[(i*8) + 5] = (random_v_offset+1) * 0.5f;
            uvs[(i*8) + 6] = (random_u_offset+1) * 0.5f;
            uvs[(i*8) + 7] = random_v_offset * 0.5f;
        }

        // The texture buffer
        ByteBuffer bb = ByteBuffer.allocateDirect(uvs.length * 4);
        bb.order(ByteOrder.nativeOrder());
        uvBuffer = bb.asFloatBuffer();
        uvBuffer.put(uvs);
        uvBuffer.position(0);

        // Generate Textures, if more needed, alter these numbers.
        int[] texturenames = new int[2];
        GLES20.glGenTextures(2, texturenames, 0);



        // Again for the text texture
       int id = getActivity().getResources().getIdentifier("drawable/font", null, getActivity().getPackageName());
       Bitmap bmp = BitmapFactory.decodeResource(getActivity().getResources(), id);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + 1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texturenames[1]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
        bmp.recycle();
    }

    public void SetupTriangle()
    {
        // We will need a randomizer
        Random rnd = new Random();

        // Our collection of vertices
        vertices = new float[30*4*3];

        // Create the vertex data
        for(int i=0;i<30;i++)
        {
            int offset_x = rnd.nextInt((int)mGLView.getWidth());
            int offset_y = rnd.nextInt((int)mGLView.getHeight());

            // Create the 2D parts of our 3D vertices, others are default 0.0f
            vertices[(i*12) + 0] = offset_x;
            vertices[(i*12) + 1] = offset_y + (30.0f*ssu);
            vertices[(i*12) + 2] = 0f;
            vertices[(i*12) + 3] = offset_x;
            vertices[(i*12) + 4] = offset_y;
            vertices[(i*12) + 5] = 0f;
            vertices[(i*12) + 6] = offset_x + (30.0f*ssu);
            vertices[(i*12) + 7] = offset_y;
            vertices[(i*12) + 8] = 0f;
            vertices[(i*12) + 9] = offset_x + (30.0f*ssu);
            vertices[(i*12) + 10] = offset_y + (30.0f*ssu);
            vertices[(i*12) + 11] = 0f;
        }

        // The indices for all textured quads
        indices = new short[30*6];
        int last = 0;
        for(int i=0;i<30;i++)
        {
            // We need to set the new indices for the new quad
            indices[(i*6) + 0] = (short) (last + 0);
            indices[(i*6) + 1] = (short) (last + 1);
            indices[(i*6) + 2] = (short) (last + 2);
            indices[(i*6) + 3] = (short) (last + 0);
            indices[(i*6) + 4] = (short) (last + 2);
            indices[(i*6) + 5] = (short) (last + 3);

            // Our indices are connected to the vertices so we need to keep them
            // in the correct order.
            // normal quad = 0,1,2,0,2,3 so the next one will be 4,5,6,4,6,7
            last = last + 4;
        }

        // The vertex buffer.
        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(indices.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(indices);
        drawListBuffer.position(0);
    }

    public TextManager getTextManager(){return tm;}
    public void setTextManager(TextManager t){tm=t;}

    public float getmZoom(){return mZoom;}
    public void setmZoomRenderLimit(float f){mZoomRenderLimit=f;}
    public float getmZoomRenderLimit(){return mZoomRenderLimit;}

    public int getmRegionProgram(){return mRegionProgram;}
}
