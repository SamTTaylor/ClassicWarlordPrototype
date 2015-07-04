package samueltaylor.classicwarlordprototype;

import android.app.Instrumentation;
import android.content.Intent;
import android.content.IntentFilter;
import android.test.ActivityInstrumentationTestCase2;

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

//    @SuppressWarnings("unchecked")
//    public ExampleTestTest() throws ClassNotFoundException {
//        super(launcherActivityClass);
//    }

    public void setUp() throws Exception {
        solo = new Solo(getInstrumentation(), getActivity());
    }

    public void testQuickMatch() throws Exception {
//        Instrumentation inst = getInstrumentation();
//        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_VIEW);
//
//        Instrumentation.ActivityMonitor monitor = inst.addMonitor(intentFilter, null, true); //true is imporant it blocks the activity from launching so that your test can continue.
//
//        assertEquals(0, monitor.getHits());
//
////do action that fires activity
//
//        assertEquals(1, monitor.getHits());
//        inst.removeMonitor(monitor);

        solo.clickOnButton("QUICKMATCH");
        Assert.assertTrue(solo.searchText("Loading..."));
        solo.sleep(3000);
        Assert.assertTrue(solo.searchText("Loading World..."));
        solo.sleep(4000);
        solo.clickOnButton("Players");
        Assert.assertTrue(solo.searchText("Terry Esther"));
//        solo.clickOnText("txt");
//        solo.clearEditText(2);
//        solo.enterText(2, "robotium");
//        solo.clickOnButton("Save");
//        solo.goBack();
//        solo.clickOnText("Edit File Extensions");
//        Assert.assertTrue(solo.searchText("application/robotium"));

    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
        super.tearDown();
    }

    public void testRun() {
        solo.waitForActivity("GameController", 2000);
    }

}