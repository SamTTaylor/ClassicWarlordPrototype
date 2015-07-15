package samueltaylor.classicwarlordprototype.OpenGL;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.google.android.gms.games.Game;

import samueltaylor.classicwarlordprototype.GameController;

/**
 * Created by Sam on 24/06/2015.
 */
public class GLGestureDetector extends GestureDetector.SimpleOnGestureListener {
    GameGLSurfaceView mSurfaceView;
    public GLGestureDetector(GameGLSurfaceView v){
        mSurfaceView = v;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e)
    {
        mSurfaceView.viewTapped(e);
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e)
    {
        //Possibly add additional options to long select on regions
        mSurfaceView.viewLongPress(e);
    }
}