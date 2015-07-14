package samueltaylor.classicwarlordprototype.OpenGL;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;

import samueltaylor.classicwarlordprototype.Fragments.fragGameMap;

/**
 * A view container where OpenGL ES graphics can be drawn on screen.
 * This view can also be used to capture touch events, such as a user
 * interacting with drawn objects.
 */
public class GameGLSurfaceView extends GLSurfaceView {
    private fragGameMap mRenderer;

    public void customSetRenderer(Renderer renderer){
        mRenderer = (fragGameMap)renderer;
        setRenderer(mRenderer);

        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    GestureDetectorCompat mGDC;
    public GameGLSurfaceView(Context context) {
        super(context);

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);
        mGDC = new GestureDetectorCompat(context,new GLGestureDetector(this));
    }

    private float mPreviousX;
    private float mPreviousY;
    private float mX;
    private float mY;
    double mPrevPinchDistance = 0;

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.
        mX = e.getX();
        mY = e.getY();
        mGDC.onTouchEvent(e);//Handle simple actions
        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE://Drag

                if (e.getPointerCount() > 1) {//Multitouch Zoom
                    double curdistance = Math.sqrt(Math.pow(((int) MotionEventCompat.getX(e, 0) - (int) MotionEventCompat.getX(e, 1)), 2) + Math.pow(((int) MotionEventCompat.getY(e, 0) - (int) MotionEventCompat.getY(e, 1)), 2));
                    if (curdistance < mPrevPinchDistance) {//pinch moved inward
                        mRenderer.incrementZoom(true);//zoom out
                    } else if (curdistance > mPrevPinchDistance) {//pinch moved outward
                        mRenderer.incrementZoom(false);//zoom in
                    }
                    mPrevPinchDistance = curdistance;
                }
                float dx = mX - mPreviousX;
                float dy = mY - mPreviousY;

                mRenderer.moveScreenPosition(
                        dx / 100, dy / 100);
                requestRender();
        }
        mPreviousX = mX;
        mPreviousY = mY;
        return true;
    }

    public void viewTapped(MotionEvent e){
        mRenderer.mClicked=true;
        mRenderer.mClickedPos[0] = e.getX();
        mRenderer.mClickedPos[1] = e.getY();
        requestRender();
    }

    public void viewLongPress(MotionEvent e){
        mRenderer.mClicked=false;
        mRenderer.mClickedPos[0] = e.getX();
        mRenderer.mClickedPos[1] = e.getY();
        requestRender();
    }
}
