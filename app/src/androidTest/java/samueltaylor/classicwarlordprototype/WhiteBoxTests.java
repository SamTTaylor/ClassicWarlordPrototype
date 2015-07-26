package samueltaylor.classicwarlordprototype;

import android.test.ActivityInstrumentationTestCase2;
import android.view.View;

import com.google.android.gms.games.Game;
import com.robotium.solo.Solo;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sam on 04/07/2015.
 * NOTE: Tests ran on GT-P7510 and GT-I9300 without translation of touch coordinates, results on devices of other sizes may vary
 * Tests run using worldsmall map
 * Run with 2 devices plugged in using gradlew createDebugCoverageReport in Android Studio terminal
 */
public class WhiteBoxTests extends ActivityInstrumentationTestCase2<GameController> {
    private Solo solo;
    //Coords for regions in worldsmall
    private List<Float[]> mountaincoords;


    private Float[] rockallcoords={160.0f,193.0f};private Float[] orkneyscoords={205.0f,191.0f};private Float[] moraycoords={240.0f,200.0f};
    private Float[] bergencoords={280.0f,205.0f};private Float[] fjordlandcoords={310.0f,180.0f};private Float[] telemarkcoords={332.0f,202.0f};
    private Float[] northumbriacoords={248.0f,285.0f};private Float[] munstercoords={170.0f,348.0f};private Float[] walescoords={217.0f,337.0f};
    private Float[] ardennescoords={334.0f,355.0f};private Float[] juracoords={350.0f,413.0f};private Float[] massifcentralcoords={317.0f,472.0f};
    private Float[] savoiecoords={360.0f,440.0f};private Float[] alpscoords={377.0f,428.0f};private Float[] graubundencoords={400.0f,410.0f};
    private Float[] tyrolcoords={433.0f,421.0f};private Float[] tauerncoords={450.0f,390.0f};private Float[] caledoniacoords={210.0f,243.0f};
    //private Float[] caledoniacoords={210.0f,243.0f};private Float[] caledoniacoords={440.0f,398.0f};private Float[] caledoniacoords={210.0f,243.0f};

    View button;


    //Utility methods
    private void waitForMyTurn(){
        while(!((GameController)getActivity()).iAmCurrentPlayer()){
            solo.sleep(100);
        }
    }

    private void initialiseMountainsForSelection(){
        //Add mountains to list in order of selection
        mountaincoords = new ArrayList<>();
        mountaincoords.add(caledoniacoords);
        mountaincoords.add(telemarkcoords);
        mountaincoords.add(northumbriacoords);
        mountaincoords.add(munstercoords);
        mountaincoords.add(walescoords);
        mountaincoords.add(ardennescoords);
        mountaincoords.add(juracoords);
        mountaincoords.add(massifcentralcoords);
        mountaincoords.add(savoiecoords);
        mountaincoords.add(graubundencoords);
        mountaincoords.add(tauerncoords);
    }




    //Tests
    public WhiteBoxTests() {
        super(GameController.class);
    }

    public void setUp() throws Exception {
        solo = new Solo(getInstrumentation(), getActivity());
    }

    public void testAutoMatch() throws Exception {
        ((GameController)getActivity()).setOpponentsForQuickGame(1);
        solo.clickOnButton("AUTO-MATCH");
        while(!solo.searchText("Loading")){//Wait until game scene is loaded as indicated by the presence of the send button
            solo.sleep(10);
        }
        Assert.assertTrue(solo.searchText("Loading..."));
        while(!solo.searchButton("Send")){//Wait until game scene is loaded as indicated by the presence of the send button
            solo.sleep(10);
        }
        button = solo.getView(R.id.btnShowPlayers);
        solo.clickOnView(button);
        Assert.assertTrue(solo.searchText(((GameController) getActivity()).getName(((GameController) getActivity()).mMyId)));
    }

    public void testMountainSelection() throws Exception{
        testAutoMatch();
        initialiseMountainsForSelection();
        for(Float[] f : mountaincoords){
            if(((GameController)getActivity()).mModel.getCurrentphase()==0){//Only wait while in mountain selection phase, else complete selections and move on
                waitForMyTurn();
            }
            solo.clickOnScreen(f[0], f[1]);
            if(solo.searchButton("Confirm")){//confirmation popup has appeared
                solo.clickOnButton("Confirm");//Claim mountain, else skip this mountain
            }
            if(solo.searchButton("OK")){//MoveToReinforcements triggered
                solo.clickOnButton("OK");
            }
        }
        button = solo.getView(R.id.btnIcon);
        solo.clickOnView(button);
        Assert.assertTrue(solo.searchText("Reinforcement"));//We have moved to the reinforcements stage successfully
        solo.sleep(20000);//Allow other device to finish before quitting
    }


    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
        super.tearDown();
    }



}