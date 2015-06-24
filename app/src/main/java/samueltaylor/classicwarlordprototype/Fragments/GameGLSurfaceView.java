package samueltaylor.classicwarlordprototype.Fragments;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.CountDownTimer;
import android.text.method.Touch;
import android.util.Log;
import android.view.MotionEvent;

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
       // setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public GameGLSurfaceView(Context context) {
        super(context);

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

    }

    CountDownTimer mTimer;
    private float mPreviousX;
    private float mPreviousY;
    private float mX;
    private float mY;
    boolean mMoving = false;
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.
        mX = e.getX();
        mY = e.getY();
        startTimer();
        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:

                float dx = mX - mPreviousX;
                float dy = mY - mPreviousY;

                if(dx > 0 || dy > 0){
                    mMoving=true;
                }

                mRenderer.setMovement(
                        dx/100, dy/100);
                requestRender();
            case MotionEvent.ACTION_UP:
               mTimer.cancel();

        }
        mPreviousX = mX;
        mPreviousY = mY;
        return true;
    }

    void startTimer(){
        mTimer =  new CountDownTimer(20, 10) {
            public void onTick(long millisUntilFinished) {
                mMoving = false;
            }
            public void onFinish() {
                if(mMoving == false){
                    mRenderer.clickView(mX, mY);
                }
            }
        }.start();
    }

}
