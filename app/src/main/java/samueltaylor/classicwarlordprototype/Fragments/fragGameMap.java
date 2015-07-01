package samueltaylor.classicwarlordprototype.Fragments;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.opengles.GL10;

import samueltaylor.classicwarlordprototype.GameController;
import samueltaylor.classicwarlordprototype.OpenGL.GameGLSurfaceView;
import samueltaylor.classicwarlordprototype.Shapes.Region;
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
    public List<SVGtoRegionParser.Region> mWorld;
    float mWorldWidth = 12.0f;
    float mWorldHeight = 8.9f;
    public float mMoveX;
    public float mMoveY;
    public boolean mClicked = false;//Has the surface been clicked
    public float[] mClickedPos = new float[2];
    //Initial drawing
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        // initialiseWorld();
        initialiseWorld();

        mSurfaceCreated = true;
        //Draw world
        for (Region r : regions) {
            r.draw(mMVPMatrix);
        }
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
                case "city":  color= new float[]{ 1f, 0.965f, 0.58f, 0.0f }; break;
                case "mountain":  color= new float[]{ 0.831f, 0.784f, 0.745f, 1.0f }; break;
                case "light":  color= new float[]{ 1.0f, 1.0f, 1.0f, 1.0f }; break;
                case "sea": color= new float[]{ 0.608f, 0.722f, 0.859f, 1.0f }; break;
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
        for(Region r : regions){
            GLES20.glLineWidth(mOutline);
            r.draw(mMVPMatrix);
        }

    }


    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    
    public void setMovement(float x, float y) {
        if(mMoveX-x>-mWorldWidth/2 && mMoveX-x<mWorldWidth/2){
            mMoveX = mMoveX - x;
        }
        if(mMoveY-y>-mWorldHeight/2 && mMoveY-y<mWorldHeight/2){
            mMoveY = mMoveY - y;
        }

    }

    float mZoom = -4.5f;
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

    void checkCollision(Region t, float x, float y){

    }

    void assignID(int regionnumber){
        int combination  = 0;
        int R=0;
        int G=0;
        int B=0;
        for(int j=0; j<regionnumber;j++){
            if (combination == 0){
                if(R<100){
                    R+=1;
                } else if (G<100){
                    G+=1;
                } else if (B<100){
                    B+=1;
                } else {
                    combination = 1;
                }
            }
            if (combination == 1) {
                if (R > 0) {
                    R -= 1;
                } else if (G > 0) {
                    G -= 1;
                } else if (B > 1) {
                    B -= 1;
                } else {
                    combination = 2;
                }
            }
            if (combination == 2){
                if(G<100){
                    G+=1;
                } else if (B<100){
                    B+=1;
                } else if (R<99){
                    R+=1;
                } else {
                    Log.e("Load world error", "filled all color IDs");
                }
            }
        }
        regions[regionnumber].mColorID[0] = ((float)R)/100;
        regions[regionnumber].mColorID[1] = ((float)G)/100;
        regions[regionnumber].mColorID[2] = ((float)B)/100;
    }

    void checkClickedRegion(GL10 gl){
        //First, check for click
        if(mClicked == true){
            mClicked=false;
            //Draw all the regions in their assigned ID colour
            for(Region r : regions){
                r.drawIDColour = true;
                r.draw(mMVPMatrix);
            }
            ByteBuffer PixelBuffer = ByteBuffer.allocateDirect(4);
            PixelBuffer.order(ByteOrder.nativeOrder());
            PixelBuffer.position(0);
            gl.glReadPixels((int) mClickedPos[0], mGLView.getHeight()- (int)mClickedPos[1], 1, 1, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, PixelBuffer);
            byte b[] = new byte[4];
            PixelBuffer.get(b);
            float resultR = ((float)(((int)b[0]) & 0xFF))/255;
            float resultG = ((float)(((int)b[1]) & 0xFF))/255;
            float resultB = ((float)(((int)b[2]) & 0xFF))/255;
            NumberFormat df = DecimalFormat.getInstance();
            df.setMaximumFractionDigits(2);
            float R = Float.parseFloat( df.format(resultR));
            float G = Float.parseFloat( df.format(resultG));
            float B = Float.parseFloat( df.format(resultB));
            for (Region r : regions){
                if(r.mColorID[0]==R && r.mColorID[1]==G && r.mColorID[2] == B){
                    Log.e("Name:", r.mName);
                }
            }
            Log.e("COLOR", "R:" + R + " G:" + G + " B:" +  B);
        }
    }
}
