package samueltaylor.classicwarlordprototype.scenes;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.scene.menu.MenuScene;
import org.andengine.entity.scene.menu.item.IMenuItem;
import org.andengine.entity.scene.menu.item.SpriteMenuItem;
import org.andengine.entity.scene.menu.item.decorator.ScaleMenuItemDecorator;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.util.GLState;

import samueltaylor.classicwarlordprototype.manager.SceneManager;

/**
 * Created by Sam on 03/05/2015.
 */
public class MainMenuScene extends BaseScene implements MenuScene.IOnMenuItemClickListener {

    private MenuScene menuChildScene;
    private final int MENU_PLAY = 0;
    private final int MENU_OPTIONS = 1;
    private final int MENU_INVITE = 2;
    private final int MENU_SEEINVITES = 3;
    private final int MENU_SIGNOUT = 4;

    @Override
    public void createScene() {
        createBackground();
        createMenuChildScene();
    }

    @Override
    public void onBackKeyPressed() {
        System.exit(0);
    }

    @Override
    public SceneManager.SceneType getSceneType() {
        return SceneManager.SceneType.SCENE_MENU;
    }

    @Override
    public void disposeScene() {

    }

    private void createBackground()
    {
        attachChild(new Sprite(0, 0, resourcesManager.menu_background_region, vbom)
        {
            @Override
            protected void preDraw(GLState pGLState, Camera pCamera)
            {
                super.preDraw(pGLState, pCamera);
                pGLState.enableDither();
            }
        });
    }

    private void createMenuChildScene()
    {
        menuChildScene = new MenuScene(camera);
        menuChildScene.setPosition(0, 0);

        final IMenuItem playMenuItem = new ScaleMenuItemDecorator(new SpriteMenuItem(MENU_PLAY, resourcesManager.play_region, vbom), 0.8f, 0.5f);
        final IMenuItem optionsMenuItem = new ScaleMenuItemDecorator(new SpriteMenuItem(MENU_OPTIONS, resourcesManager.options_region, vbom), 0.8f, 0.5f);
        final IMenuItem inviteMenuItem = new ScaleMenuItemDecorator(new SpriteMenuItem(MENU_INVITE, resourcesManager.invite_region, vbom), 0.8f, 0.5f);
        final IMenuItem seeinvitesMenuItem = new ScaleMenuItemDecorator(new SpriteMenuItem(MENU_SEEINVITES, resourcesManager.seeinvites_region, vbom), 0.8f, 0.5f);
        final IMenuItem signoutMenuItem = new ScaleMenuItemDecorator(new SpriteMenuItem(MENU_SIGNOUT, resourcesManager.signout_region, vbom), 0.8f, 0.5f);

        playMenuItem.setScale(0.5f);
        optionsMenuItem.setScale(0.5f);
        inviteMenuItem.setScale(0.5f);
        seeinvitesMenuItem.setScale(0.5f);
        signoutMenuItem.setScale(0.5f);

        menuChildScene.addMenuItem(playMenuItem);
        menuChildScene.addMenuItem(optionsMenuItem);
        menuChildScene.addMenuItem(inviteMenuItem);
        menuChildScene.addMenuItem(seeinvitesMenuItem);
        menuChildScene.addMenuItem(signoutMenuItem);

        menuChildScene.buildAnimations();
        menuChildScene.setBackgroundEnabled(false);

        playMenuItem.setPosition(playMenuItem.getX()-100, playMenuItem.getY()+160);
        optionsMenuItem.setPosition(optionsMenuItem.getX()-100, playMenuItem.getY()+60);
        inviteMenuItem.setPosition(inviteMenuItem.getX()-100, playMenuItem.getY()+120);
        seeinvitesMenuItem.setPosition(seeinvitesMenuItem.getX()-100, playMenuItem.getY()+180);
        signoutMenuItem.setPosition(signoutMenuItem.getX()-100, playMenuItem.getY()+240);

        menuChildScene.setOnMenuItemClickListener(this);
        setChildScene(menuChildScene);
    }

    @Override
    public boolean onMenuItemClicked(MenuScene pMenuScene, IMenuItem pMenuItem, float pMenuItemLocalX, float pMenuItemLocalY)
    {
        switch(pMenuItem.getID())
        {
            case MENU_PLAY:
                //Load Game Scene!
                SceneManager.getInstance().loadGameScene(engine);
                return true;
            case MENU_OPTIONS:
                return true;
            case MENU_INVITE:
                return true;
            case MENU_SEEINVITES:
                return true;
            case MENU_SIGNOUT:
                return true;
            default:
                return false;
        }
    }
}
