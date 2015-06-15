package samueltaylor.classicwarlordprototype.scenes;

/**
 * Created by Sam on 04/05/2015.
 */
import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.hud.HUD;

import org.andengine.entity.scene.menu.MenuScene;
import org.andengine.entity.scene.menu.item.IMenuItem;
import org.andengine.entity.scene.menu.item.SpriteMenuItem;
import org.andengine.entity.scene.menu.item.decorator.ScaleMenuItemDecorator;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.opengl.util.GLState;
import org.andengine.util.HorizontalAlign;

import samueltaylor.classicwarlordprototype.manager.SceneManager;
import samueltaylor.classicwarlordprototype.scenes.BaseScene;
import samueltaylor.classicwarlordprototype.manager.SceneManager.SceneType;

public class GameScene extends BaseScene implements MenuScene.IOnMenuItemClickListener
{

    private HUD gameHUD;
    private Text infoText;
    private String info;

    private MenuScene gameChildScene;
    private final int LANCS = 0;
    private final int YORKS = 1;

    @Override
    public void createScene()
    {
        createBackground();
        createHUD();
        createMenuChildScene();
    }

    private void createBackground()
    {
        attachChild(new Sprite(0, 0, resourcesManager.game_background_region, vbom) {
            @Override
            protected void preDraw(GLState pGLState, Camera pCamera) {
                super.preDraw(pGLState, pCamera);
                pGLState.enableDither();
            }
        });
    }

    @Override
    public void onBackKeyPressed()
    {
        SceneManager.getInstance().loadMenuScene(engine);
    }

    @Override
    public SceneType getSceneType()
    {
        return SceneType.SCENE_GAME;
    }

    private void createHUD()
    {
        gameHUD = new HUD();

        // CREATE INFO TEXT
        infoText = new Text(20, 20, resourcesManager.font, SceneManager.getInstance().gameInfo, new TextOptions(HorizontalAlign.LEFT), vbom);
        gameHUD.attachChild(infoText);

        camera.setHUD(gameHUD);
    }

    public void setInfo(String s)
    {
        info = s;
        infoText.setText(info);
    }

    public void appendInfo(String s)
    {
        info = info + "\n" + s;
        infoText.setText(info);
    }
    @Override
    public void disposeScene()
    {
        camera.setHUD(null);
        camera.setCenter(400, 240);

        // TODO code responsible for disposing scene
        // removing all game scene objects.
    }

    private void createMenuChildScene()
    {
        gameChildScene = new MenuScene(camera);
        gameChildScene.setPosition(0, 0);

        final IMenuItem lancsItem = new ScaleMenuItemDecorator(new SpriteMenuItem(LANCS, resourcesManager.lancs_region, vbom), 1f, 0.8f);
        final IMenuItem yorksItem = new ScaleMenuItemDecorator(new SpriteMenuItem(YORKS, resourcesManager.yorks_region, vbom), 1f, 0.8f);

        gameChildScene.addMenuItem(lancsItem);
        gameChildScene.addMenuItem(yorksItem);

        gameChildScene.buildAnimations();
        gameChildScene.setBackgroundEnabled(false);

        lancsItem.setPosition(lancsItem.getX()-85, lancsItem.getY() + 130);
        yorksItem.setPosition(yorksItem.getX()+45, yorksItem.getY()-270);

        gameChildScene.setOnMenuItemClickListener(this);

        setChildScene(gameChildScene);
    }

    @Override
    public boolean onMenuItemClicked(MenuScene pMenuScene, IMenuItem pMenuItem, float pMenuItemLocalX, float pMenuItemLocalY) {
        switch (pMenuItem.getID()) {
            case LANCS:
                return true;
            case YORKS:
                return true;
            default:
                return false;
        }
    }


}
