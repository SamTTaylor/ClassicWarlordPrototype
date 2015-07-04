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

import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
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
        GLES20.glDisable(GLES20.GL_DITHER);
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

    void assignID(int regionnumber){
        int combination  = 0;
        int R=0;
        int G=0;
        int B=0;
        int inc=1;//1/(2^5-1) multiplied by 5 for more distinction between values
        int incgreen=1;//1/(2^6-1) multiplied by 5 for more distinction between values
        int UBR=33;
        int UG=63;
        for(int j=0; j<regionnumber;j++){
            if (combination == 0){//ID Combination 1: Fill up red values
                if(R<UBR-inc){
                    R+=inc;
                } else if (G<UG-incgreen){//Then green
                    G+=incgreen;
                } else if (B<UBR-inc){//Then blue
                    B+=inc;
                } else {
                    combination = 1;
                }
            }
            if (combination == 1) {//Combination 2: Deplete values in reverse order
                if (R > inc) {
                    R -= inc;
                } else if (G > incgreen) {
                    G -= incgreen;
                } else if (B > inc) {
                    B -= inc;
                } else {
                    combination = 2;
                }
            }
            if (combination == 2){//Combination 3: Fill up green values
                if(G<UG-incgreen){
                    G+=incgreen;
                } else if (B<UBR-inc*2){//Then blue
                    B+=inc;
                } else if (R<UBR-inc*2){//Then red
                   R+=inc;
                } else {
                    combination=3;
                }
            }
            if (combination == 3) {//Combination 4: Deplete in reverse order
                if (G > incgreen) {
                    G -= incgreen;
                } else if (R > inc*2) {
                    R -= inc;
                } else if (B > inc*2) {
                    B -= inc;
                } else {
                    combination=4;
                }
            }
            if (combination == 4){//Combination 5: Fill up blue values
                if(R<UBR-inc){
                    R+=inc;
                } else if (B<UBR-inc){//Then red
                    B+=inc;
                } else if (G<UG-incgreen){//Then green
                    G+=incgreen;
                } else {
                    combination=5;
                }
            }
            if (combination == 5) {//Combination 6: Then deplete in reverse order
                if (B > inc) {
                    B -= inc;
                } else if (R > inc) {
                    R -= inc;
                } else if (G > incgreen) {
                    G -= incgreen;
                } else {
                    Log.e("Load world error", "filled all color IDs");
                }
            }
        }
        //Total regions required to be filled: 605
        DecimalFormat df = new DecimalFormat("#.####");
        df.setRoundingMode(RoundingMode.HALF_UP);
        float fR = Float.parseFloat( df.format(((double)R)/UBR));
        float fG = Float.parseFloat( df.format(((double)G)/UG));
        float fB = Float.parseFloat( df.format(((double)B)/UBR));
        regions[regionnumber].mColorID[0] = fR;
        regions[regionnumber].mColorID[1] = fG;
        regions[regionnumber].mColorID[2] = fB;

        Log.e("COLOR", regionnumber+ " Name:" +regions[regionnumber].mName+ " R:" + regions[regionnumber].mColorID[0] + " G:" + regions[regionnumber].mColorID[1] + " B:" + regions[regionnumber].mColorID[2]);
    }

    void checkClickedRegion(GL10 gl){
        //First, check for click
        if(mClicked == true){
            mClicked=false;
            //Draw all the regions in their assigned ID colour

            for(Region r : regions){
                r.toggleDrawMode(1);//Colour ID mode
                r.draw(mMVPMatrix);
            }
            ByteBuffer PixelBuffer = ByteBuffer.allocateDirect(4);
            gl.glReadPixels((int) mClickedPos[0], mGLView.getHeight() - (int) mClickedPos[1], 1, 1, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, PixelBuffer);
            byte b[] = new byte[4];
            PixelBuffer.get(b);

            double R = ((double)(b[0] & 0xFF))/255;
            double G = ((double)(b[1] & 0xFF))/255;
            double B = ((double)(b[2] & 0xFF))/255;
            DecimalFormat df = new DecimalFormat("#.##");
            df.setRoundingMode(RoundingMode.HALF_UP);
            float fR = Float.parseFloat( df.format(R));
            float fG = Float.parseFloat( df.format(G));
            float fB = Float.parseFloat( df.format(B));
            float rR; float rG; float rB;
            for (Region r : regions){
                rR=Float.parseFloat( df.format(r.mColorID[0]));
                rG=Float.parseFloat( df.format(r.mColorID[1]));
                rB=Float.parseFloat( df.format(r.mColorID[2]));
                if(rR==fR && rG==fG && rB == fB){
                    Log.e("Name:", r.mName);
                    r.toggleDrawMode(2);//Selected
                }
            }
            Log.e("COLOR", "R:" + fR + " G:" + fG + " B:" +  fB);
                   }
    }
}
