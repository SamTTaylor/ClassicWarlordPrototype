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


import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import samueltaylor.classicwarlordprototype.OpenGL.GameGLSurfaceView;
import samueltaylor.classicwarlordprototype.R;
import samueltaylor.classicwarlordprototype.Shapes.Region;
import samueltaylor.classicwarlordprototype.XMLParsing.SVGtoRegionParser;


public class fragGameMap extends Fragment implements GLSurfaceView.Renderer{
    private OnFragmentInteractionListener mListener;

    public fragGameMap() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
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

    //Initial drawing
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.8f, 0.8f, 0.8f, 1f);
        GLES20.glLineWidth(5.0f);
        mSurfaceCreated = true;
        // initialiseWorld();
        initialiseWorld();
        //Draw world
        for (Region r : regions) {
            r.draw(mMVPMatrix);
        }
    }

    static float regionCoords[];

    SVGtoRegionParser mParser;
    public void initialiseWorld(){
        List<SVGtoRegionParser.Region> world;
        mParser = new SVGtoRegionParser();
        InputStream inputStream;
        try{
            inputStream = new BufferedInputStream(getResources().openRawResource(R.raw.world));
            world = mParser.parse(inputStream);
            int i = 0;
            regions = new Region[world.size()];
            for(SVGtoRegionParser.Region r : world){
                regionCoords = r.path;
                regions[i] = new Region(this, regionCoords);
                i++;
            }
        } catch (FileNotFoundException e){
            Log.e("FileNotFoundException", e.toString());
        } catch (XmlPullParserException e) {
            Log.e("XmlPullParserException", e.toString());
        } catch (IOException e) {
            Log.e("IOException", e.toString());
        }
    }



    //Manipulation of drawing
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, ratio, -ratio, 1, -1, 2, 19);
    }

    //Redrawing
    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, mZoom, mMoveX, mMoveY, 0f, 0f, 1.0f, 0.0f);
        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);


        for(Region r : regions){
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

    public float mMoveX;
    public float mMoveY;

    public void setMovement(float x, float y) {
        mMoveX = mMoveX - x*2;
        mMoveY = mMoveY - y*2;
    }

    float mZoom = -18.5f;
    float mSensitivity = 0.5f;
    public void incrementZoom(boolean direction){
        if (mZoom <=-4 && direction == false){
            //in
            mZoom+=mSensitivity;
        }
        if (mZoom >= -15.5 && direction == true){
            //out
            mZoom-=mSensitivity;
        }
    }

    //Click has been triggered at the supplied points
    public void clickView(float x, float y){
        String sx = String.valueOf(x);
        String sy = String.valueOf(y);

        mGLView.getWidth();
        mGLView.getHeight();

    }

    void checkCollision(Region t, float x, float y){

    }
}
