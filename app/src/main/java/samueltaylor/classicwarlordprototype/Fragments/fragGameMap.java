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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import samueltaylor.classicwarlordprototype.OpenGL.GameGLSurfaceView;
import samueltaylor.classicwarlordprototype.Shapes.Region;
import samueltaylor.classicwarlordprototype.XMLParsing.SVGtoRegionParser;


public class fragGameMap extends Fragment implements GLSurfaceView.Renderer{
    private OnFragmentInteractionListener mListener;

    public fragGameMap() {}

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
        GLES20.glLineWidth(mOutline);
        mSurfaceCreated = true;
        // initialiseWorld();
        initialiseWorld();
        //Draw world
        for (Region r : regions) {
            r.draw(mMVPMatrix);
        }
    }

    static float regionCoords[];
    public List<SVGtoRegionParser.Region> mWorld;
    public void initialiseWorld(){
        int i = 0;
        regions = new Region[mWorld.size()];
        float[] color = null;
        for(SVGtoRegionParser.Region r : mWorld){
            regionCoords = r.path;
            switch (r.type){
                case "rural": color= new float[]{ 0.651f, 0.871f, 0.78f, 0.0f }; break;
                case "dense": color= new float[]{ 0.965f, 0.722f, 0.729f, 0.0f }; break;
                case "city":  color= new float[]{ 1f, 0.965f, 0.58f, 0.0f }; break;
                case "mountain":  color= new float[]{ 0.831f, 0.784f, 0.745f, 0.0f }; break;
                case "light":  color= new float[]{ 0.831f, 0.784f, 0.745f, 0.0f }; break;
                case "sea": color= new float[]{ 0.608f, 0.722f, 0.859f, 0.0f }; break;
            }
            regions[i] = new Region(this, regionCoords, color);
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
        float ratio = (float) mGLView.getWidth() / mGLView.getHeight();
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        // Set the camera position (View matrix)

        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 25, mMoveX, mMoveY, 0f, 0f, 1.0f, 0.0f);
        Matrix.orthoM(mOrthographicMatrix, 0, ratio * mZoom, -ratio * mZoom, -1 * mZoom, 1 * mZoom, 3, 30);
        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mOrthographicMatrix, 0, mViewMatrix, 0);
        Matrix.translateM(mMVPMatrix, 0, -6.0f, -4.5f, 0.0f);

        //Draw all the regions loaded from the world
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

    float mZoom = -4.5f;
    float mOutline = 1.0f;
    float mSensitivity = 0.3f;
    public void incrementZoom(boolean direction){
        if (mZoom <=-1.1 && direction == false){
            //in
            mZoom+=mSensitivity;
        }
        if (mZoom >= -4.4 && direction == true){
            //out
            mZoom-=mSensitivity;
        }
    }

    //Click has been triggered at the supplied points
    public void clickView(float x, float y){
        mGLView.getWidth();
        mGLView.getHeight();
    }

    void checkCollision(Region t, float x, float y){

    }
}
