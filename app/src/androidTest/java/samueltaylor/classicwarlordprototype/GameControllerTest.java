package samueltaylor.classicwarlordprototype;

import android.test.ActivityInstrumentationTestCase2;

import com.google.android.gms.games.Game;
import com.robotium.solo.Solo;

import junit.framework.Assert;

/**
 * Created by Sam on 04/07/2015.
 */
public class GameControllerTest extends ActivityInstrumentationTestCase2<GameController> {
    private Solo solo;

    public GameControllerTest() {
        super(GameController.class);

    }

    public void setUp() throws Exception {
        solo = new Solo(getInstrumentation(), getActivity());
    }

    public void testAutoMatch() throws Exception {
        ((GameController)getActivity()).setOpponentsForQuickGame(1);
        solo.clickOnButton("AUTO-MATCH");
        Assert.assertTrue(solo.searchText("Loading..."));
//        while(solo.searchText("Loading...") || solo.searchText("Loading World...")){
//            solo.sleep(10);
//        }
//
//        solo.clickOnButton("Players");
//        Assert.assertTrue(solo.searchText(((GameController)getActivity()).getName(((GameController)getActivity()).mMyId)));
    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
        super.tearDown();
    }

}