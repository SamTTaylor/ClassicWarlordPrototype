package samueltaylor.classicwarlordprototype.scenes;

/**
 * Created by Sam on 04/05/2015.
 */
import org.andengine.engine.camera.hud.HUD;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.scene.menu.MenuScene;
import org.andengine.entity.scene.menu.item.IMenuItem;
import org.andengine.entity.scene.menu.item.SpriteMenuItem;
import org.andengine.entity.scene.menu.item.decorator.ScaleMenuItemDecorator;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.util.HorizontalAlign;
import org.andengine.util.color.Color;

import samueltaylor.classicwarlordprototype.manager.SceneManager;
import samueltaylor.classicwarlordprototype.scenes.BaseScene;
import samueltaylor.classicwarlordprototype.manager.SceneManager.SceneType;

public class GameScene extends BaseScene implements MenuScene.IOnMenuItemClickListener
{

    private HUD gameHUD;
    private Text infoText;
    private String info = "";

    private MenuScene gameChildScene;
    private final int LANCS = 0;
    private final int YORKS = 1;

    @Override
    public void createScene()
    {
        setBackground(new Background(Color.BLUE));
        createHUD();
        createMenuChildScene();
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

        // CREATE SCORE TEXT
        infoText = new Text(0, 0, resourcesManager.font, "", new TextOptions(HorizontalAlign.LEFT), vbom);
        infoText.setText(" ");
        gameHUD.attachChild(infoText);

        camera.setHUD(gameHUD);
    }

    private void setInfo(String s)
    {
        info = s;
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

        final IMenuItem lancsItem = new ScaleMenuItemDecorator(new SpriteMenuItem(LANCS, resourcesManager.lancs_region, vbom), 1.2f, 1);
        final IMenuItem yorksItem = new ScaleMenuItemDecorator(new SpriteMenuItem(YORKS, resourcesManager.yorks_region, vbom), 1.2f, 1);

        gameChildScene.addMenuItem(lancsItem);
        gameChildScene.addMenuItem(yorksItem);

        gameChildScene.buildAnimations();
        gameChildScene.setBackgroundEnabled(false);

        lancsItem.setPosition(lancsItem.getX()-100, lancsItem.getY() + 200);
        yorksItem.setPosition(yorksItem.getX()+60, yorksItem.getY()-220);

        gameChildScene.setOnMenuItemClickListener(this);

        setChildScene(gameChildScene);
    }


    @Override
    public boolean onMenuItemClicked(MenuScene pMenuScene, IMenuItem pMenuItem, float pMenuItemLocalX, float pMenuItemLocalY)
    {
        switch(pMenuItem.getID())
        {
            case LANCS:
                return true;
            case YORKS:
                return true;
            default:
                return false;
        }
    }

}
