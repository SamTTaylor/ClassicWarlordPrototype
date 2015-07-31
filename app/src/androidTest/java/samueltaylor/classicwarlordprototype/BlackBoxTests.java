package samueltaylor.classicwarlordprototype;

import android.test.ActivityInstrumentationTestCase2;
import android.view.View;

import com.robotium.solo.Solo;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sam on 04/07/2015.
 * NOTE: Tests ran on GT-P7510 and GT-I9300 without translation of touch coordinates, results on devices of other sizes may vary
 * Tests run using worldsmall map
 * Run with 2 devices plugged in using gradlew createDebugCoverageReport in Android Studio terminal
 * Tests run through regular actions of a normal game to ensure nothing has been broken during implementation
 */
public class BlackBoxTests extends ActivityInstrumentationTestCase2<GameController> {
    private Solo solo;

    private boolean player1=false;
    private boolean largescreen=false;

    //Coords for regions in worldsmall
    private List<Float[]> mountaincoords;
    private List<Float[]> myRegionCoords;

    //Mountains
    private Float[] rockallcoords={160.0f,193.0f};private Float[] orkneyscoords={205.0f,191.0f};private Float[] moraycoords={240.0f,200.0f};
    private Float[] bergencoords={280.0f,205.0f};private Float[] fjordlandcoords={310.0f,180.0f};private Float[] telemarkcoords={332.0f,202.0f};
    private Float[] northumbriacoords={248.0f,285.0f};private Float[] munstercoords={170.0f,348.0f};private Float[] walescoords={218.0f,337.0f};
    private Float[] ardennescoords={334.0f,355.0f};private Float[] juracoords={350.0f,413.0f};private Float[] massifcentralcoords={317.0f,472.0f};
    private Float[] savoiecoords={360.0f,440.0f};private Float[] alpscoords={377.0f,428.0f};private Float[] graubundencoords={400.0f,410.0f};
    private Float[] tyrolcoords={440.0f,410.0f};private Float[] tauerncoords={450.0f,390.0f};private Float[] caledoniacoords={215.0f,243.0f};

    //Adjacent to mountains
    private Float[] munsterright={200.0f, 346f};private Float[] walesright={245.0f,337.0f};
    private Float[] nothumbiradown={256f, 300f};private Float[] ardennesup={315.0f,338.0f};
    private Float[] nothumbiradownsm={271f, 300f};
    //Extended movement
    private Float[] thames={294.0f, 332.0f};private Float[] london={276.0f, 340.0f};

    View button;


    //Utility methods
    private void waitForMyTurn(){
        while(solo.searchText("Send") && !((GameController)getActivity()).iAmCurrentPlayer()){

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
        mountaincoords.add(graubundencoords);
        mountaincoords.add(tyrolcoords);
        mountaincoords.add(tauerncoords);
        mountaincoords.add(savoiecoords);
    }

    //AUTO MATCH
    private void testAutoMatch() throws Exception {
        ((GameController)getActivity()).setOpponentsForQuickGame(1);
        solo.sleep(4000);//Wait for login
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


    //MOUNTAIN SELECTION
    private void testMountainSelection() throws Exception{
        initialiseMountainsForSelection();
        solo.clickLongOnScreen(nothumbiradown[0], nothumbiradown[1]);//Check if we are large screen or small screen by identifying a click at point, in case it needs distinguishing at some point
        if(solo.searchText("Yorkshire")){largescreen=true;}
        solo.clickOnButton("OK");
        solo.sleep(4000);
        button = solo.getView(R.id.btnIcon);
        solo.clickOnView(button);
        myRegionCoords = new ArrayList<>();
        for(Float[] f : mountaincoords){
            if(((GameController)getActivity()).mModel.getCurrentphase()==0){//Only wait while in mountain selection phase, else complete selections and move on
                waitForMyTurn();
            }
            solo.clickOnScreen(f[0], f[1]);
            if(solo.searchButton("Confirm")){//confirmation popup has appeared
                if(solo.searchText("Caledonia")){player1=true;}
                solo.clickOnButton("Confirm");//Claim mountain, else skip this mountain
                myRegionCoords.add(f);
            }
            if(solo.searchButton("OK")){//Info message triggered
                solo.clickOnButton("OK");
                myRegionCoords.remove(f);
            }
        }
        Assert.assertTrue(solo.searchText("Reinforcement"));//We have moved to the reinforcements stage successfully
    }


    //REINFORCEMENT
    private void testReinforcement() throws Exception{
        waitForMyTurn();
        fillRegions();
        endTurnWithReinforcementConfirmation();
        //Add it back
        solo.clickOnScreen(myRegionCoords.get(0)[0], myRegionCoords.get(0)[1]);
        if(solo.searchButton("Confirm")){//confirmation popup has appeared
            button = solo.getView(R.id.btnPlus);//Take reinforcement
            solo.clickOnView(button);
            solo.clickOnButton("Confirm");//Assign reinforcement
        }
        endTurn();
        Assert.assertTrue(solo.searchText("Attack/Moving"));//We have moved to the reinforcements stage successfully
        endTurn();
    }

    private void fillRegions(){
        for(Float[] f : myRegionCoords){
            solo.clickOnScreen(f[0], f[1]);
            if(solo.searchButton("Confirm")){//confirmation popup has appeared
                button = solo.getView(R.id.btnPlus);//Add reinforcement
                solo.clickOnView(button);
                solo.clickOnButton("Confirm");//Assign reinforcement
            }
        }
    }

    private void endTurnWithReinforcementConfirmation(){
        //Take one off of first region in order to trigger confirmation on end turn
        solo.clickOnScreen(myRegionCoords.get(0)[0], myRegionCoords.get(0)[1]);
        if(solo.searchButton("Confirm")){//confirmation popup has appeared
            button = solo.getView(R.id.btnMinus);//Take reinforcement
            solo.clickOnView(button);
            solo.clickOnButton("Confirm");//Assign reinforcement
        }
        solo.clickOnButton("End Turn");
        if(solo.searchButton("Cancel")){//Info message triggered
            solo.clickOnButton("Cancel");
        }
    }

    private void endTurn(){
        solo.clickOnButton("End Turn");
        if(solo.searchButton("Confirm")){//confirmation popup has appeared
            solo.clickOnButton("Confirm");
        }
    }


    //MOVE
    private void testMove(){
        waitForMyTurn();
        fillRegions();
        endTurn();
        //Begin attack move with 3 armies in each region
        Assert.assertTrue(solo.searchText("Attack/Moving"));
        testIncorrectClick();
        if(player1){
            player1Move(); waitForMyTurn();
        } else {
            player2Move(); waitForMyTurn();
        }
        boolean b = true;
        assertTrue(b);
    }

    private void testIncorrectClick() {
        solo.clickOnScreen(rockallcoords[0], rockallcoords[1]);
        solo.clickOnScreen(bergencoords[0], bergencoords[1]);
        solo.clickOnScreen(rockallcoords[0], rockallcoords[1]);
        solo.clickOnButton("OK");
    }

    private void player2Move(){
        for(Float[] f : myRegionCoords) {
            if (f == munstercoords) {
                solo.clickOnScreen(munstercoords[0],munstercoords[1]);
                solo.clickOnScreen(munsterright[0],munsterright[1]);
                button = solo.getView(R.id.btnPlus);//Add reinforcement
                solo.clickOnView(button);
                solo.clickOnView(button);
                solo.clickOnButton("Confirm");//Assign reinforcement
            }
            if (f == ardennescoords) {
                solo.clickOnScreen(ardennescoords[0],ardennescoords[1]);
                solo.clickOnScreen(ardennesup[0],ardennesup[1]);
                button = solo.getView(R.id.btnPlus);//Add reinforcement
                solo.clickOnView(button);
                solo.clickOnButton("Confirm");//Assign reinforcement
                solo.clickOnScreen(ardennescoords[0],ardennescoords[1]);
                solo.clickOnScreen(ardennesup[0],ardennesup[1]);
                button = solo.getView(R.id.btnPlus);//Add reinforcement
                solo.clickOnView(button);
                solo.clickOnButton("Confirm");//Assign reinforcement

                solo.clickOnScreen(ardennesup[0], ardennesup[1]);
                solo.clickOnScreen(thames[0], thames[1]);
                button = solo.getView(R.id.btnPlus);//Add reinforcement
                solo.clickOnView(button);
                solo.clickOnView(button);
                solo.clickOnButton("Confirm");//Assign reinforcement
            }
        }
        endTurn();
    }
    private void player1Move(){
        for(Float[] f : myRegionCoords) {
            if (f == northumbriacoords) {
                //Capture down from Northumbria
                solo.clickOnScreen(northumbriacoords[0],northumbriacoords[1]);
                if(largescreen){solo.clickOnScreen(nothumbiradown[0],nothumbiradown[1]);} else {
                    solo.clickOnScreen(nothumbiradownsm[0],nothumbiradownsm[1]);
                }
                button = solo.getView(R.id.btnPlus);//Add reinforcement
                solo.clickOnView(button);
                solo.clickOnView(button);
                solo.clickOnButton("Confirm");//Assign reinforcement

                //Take London
                if(largescreen){solo.clickOnScreen(nothumbiradown[0],nothumbiradown[1]);} else {
                    solo.clickOnScreen(nothumbiradownsm[0],nothumbiradownsm[1]);
                }
                solo.clickOnScreen(walesright[0],walesright[1]);
                button = solo.getView(R.id.btnPlus);//Add reinforcement
                solo.clickOnView(button);
                solo.clickOnButton("Confirm");//Assign reinforcement
            }
            if (f == walescoords) {
                //Move inside empire to walesright then to london
                solo.clickOnScreen(walescoords[0], walescoords[1]);
                solo.clickOnScreen(walesright[0], walesright[1]);
                button = solo.getView(R.id.btnPlus);//Add reinforcement
                solo.clickOnView(button);
                solo.clickOnView(button);
                solo.clickOnButton("Confirm");//Assign reinforcement
                solo.clickOnScreen(walesright[0], walesright[1]);
                solo.clickOnScreen(london[0], london[1]);
                button = solo.getView(R.id.btnPlus);//Add reinforcement
                solo.clickOnView(button);
                solo.clickOnView(button);
                solo.clickOnButton("Confirm");//Assign reinforcement
            }
        }
        endTurn();
    }


    //ATTACKS
    /* Both players must:
    Attack from sea to land
    Attack from land to sea
    Attack city
    Attack mountain
    */
    private void testAttack(){
        waitForMyTurn();
        fillRegions();
        endTurn();
        //Begin attack move with 3 armies in each region
        Assert.assertTrue(solo.searchText("Attack/Moving"));
        testIncorrectClick();
        if(player1){
            player1Move(); waitForMyTurn();}
        else {
            player2Move(); waitForMyTurn();}
        solo.sleep(200000);
        boolean b = true;
        assertTrue(b);
    }


    //Tests
    public BlackBoxTests() {
        super(GameController.class);
    }

    public void setUp() throws Exception {
        solo = new Solo(getInstrumentation(), getActivity());
    }

    public void testFull() throws Exception {
        testAutoMatch();
        testMountainSelection();
        testReinforcement();
        testMove();
    }


    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
        super.tearDown();
    }



}