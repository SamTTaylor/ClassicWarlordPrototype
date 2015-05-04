package samueltaylor.classicwarlordprototype.scenes;

/**
 * Created by Sam on 03/05/2015.
 */

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.util.GLState;

import samueltaylor.classicwarlordprototype.GameActivity;
import samueltaylor.classicwarlordprototype.manager.SceneManager.SceneType;

/**
 * @author Mateusz Mysliwiec
 * @author www.matim-dev.com
 * @version 1.0
 */
public class SplashScene extends BaseScene
{

    private Sprite splash;


    @Override
    public void createScene()
    {
        splash = new Sprite(0, 0, resourcesManager.splash_region, vbom)
        {
            @Override
            protected void preDraw(GLState pGLState, Camera pCamera)
            {
                super.preDraw(pGLState, pCamera);
                pGLState.enableDither();
            }
        };
        //splash.setScale(1.5f);
        splash.setPosition(175, 150);
        attachChild(splash);
    }

    @Override
    public void onBackKeyPressed()
    {

    }

    @Override
    public SceneType getSceneType()
    {
        return SceneType.SCENE_SPLASH;
    }

    @Override
    public void disposeScene()
    {
        splash.detachSelf();
        splash.dispose();
        this.detachSelf();
        this.dispose();
    }

}