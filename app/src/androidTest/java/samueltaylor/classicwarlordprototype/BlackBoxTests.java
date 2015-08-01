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
    private List<Float[]> myMountainCoords;

    //Mountains
    private Float[] rockallcoords={160.0f,193.0f};private Float[] orkneyscoords={205.0f,191.0f};private Float[] moraycoords={240.0f,200.0f};
    private Float[] bergencoords={280.0f,205.0f};private Float[] fjordlandcoords={310.0f,180.0f};private Float[] telemarkcoords={332.0f,202.0f};
    private Float[] northumbriacoords={248.0f,285.0f};private Float[] munstercoords={170.0f,348.0f};private Float[] walescoords={218.0f,337.0f};
    private Float[] ardennescoords={334.0f,355.0f};private Float[] juracoords={350.0f,413.0f};private Float[] massifcentralcoords={317.0f,472.0f};
    private Float[] savoiecoords={360.0f,440.0f};private Float[] alpscoords={377.0f,428.0f};private Float[] graubundencoords={400.0f,410.0f};
    private Float[] tyrolcoords={440.0f,410.0f};private Float[] tauerncoords={450.0f,390.0f};private Float[] caledoniacoords={215.0f,243.0f};

    //Adjacent to mountains
    private Float[] stgeorgeschannelcoords ={200.0f, 346f};private Float[] merciacoords ={245.0f,337.0f};
    private Float[] yorkshirecoords ={256f, 300f};private Float[] belgiumcoords ={315.0f,338.0f};
    private Float[] yorkshirecoordssm ={271f, 300f};

    //Extended movement
    private Float[] thamescoords ={294.0f, 332.0f};private Float[] londoncoords ={276.0f, 340.0f};

    View button;


    //Utility methods
    private void waitForMyTurnWithDefenceListener(){
        while(solo.searchText("Send") && !((GameController)getActivity()).iAmCurrentPlayer()){
            if(solo.searchText("Has been attacked from") || solo.searchText("Incorrect guess, try again")){//Been attacked by the other player
                Assert.assertTrue(solo.searchButton("Confirm"));
                solo.clickOnButton("Confirm");
                //Always guess base amount
            }
            if(solo.searchButton("OK")){//Dismiss any other notifications
                solo.clickOnButton("OK");
            }
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
        solo.clickLongOnScreen(yorkshirecoords[0], yorkshirecoords[1]);//Check if we are large screen or small screen by identifying a click at point, in case it needs distinguishing at some point
        if(solo.searchText("Yorkshire")){largescreen=true;}
        solo.clickOnButton("OK");
        solo.sleep(4000);
        button = solo.getView(R.id.btnIcon);
        solo.clickOnView(button);
        myMountainCoords = new ArrayList<>();
        for(Float[] f : mountaincoords){
            if(((GameController)getActivity()).mModel.getCurrentphase()==0){//Only wait while in mountain selection phase, else complete selections and move on
                waitForMyTurnWithDefenceListener();
            }
            solo.clickOnScreen(f[0], f[1]);
            if(solo.searchButton("Confirm")){//confirmation popup has appeared
                if(solo.searchText("Caledonia")){player1=true;}
                solo.clickOnButton("Confirm");//Claim mountain, else skip this mountain
                myMountainCoords.add(f);
            }
            if(solo.searchButton("OK")){//Info message triggered
                solo.clickOnButton("OK");
                myMountainCoords.remove(f);
            }
        }
        Assert.assertTrue(solo.searchText("Reinforcement"));//We have moved to the reinforcements stage successfully
    }


    //REINFORCEMENT
    private void testReinforcement() throws Exception{
        waitForMyTurnWithDefenceListener();
        fillRegions();
        endTurnWithReinforcementConfirmation();
        //Add it back
        solo.clickOnScreen(myMountainCoords.get(0)[0], myMountainCoords.get(0)[1]);
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
        for(Float[] f : myMountainCoords){
            solo.clickOnScreen(f[0], f[1]);
            if(solo.searchButton("Confirm")){//confirmation popup has appeared
                button = solo.getView(R.id.btnPlus);//Add reinforcement
                solo.clickOnView(button);
                solo.clickOnView(button);
                solo.clickOnView(button);
                solo.clickOnView(button);
                solo.clickOnButton("Confirm");//Assign reinforcement
            }
        }
    }

    private void endTurnWithReinforcementConfirmation(){
        //Take one off of first region in order to trigger confirmation on end turn
        solo.clickOnScreen(myMountainCoords.get(0)[0], myMountainCoords.get(0)[1]);
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
        waitForMyTurnWithDefenceListener();
        fillRegions();
        endTurn();
        //Begin attack move with 3 armies in each region
        Assert.assertTrue(solo.searchText("Attack/Moving"));
        testIncorrectClick();
        if(player1){
            player1Move();
        } else {
            player2Move();
        }
    }

    private void testIncorrectClick() {
        solo.clickOnScreen(rockallcoords[0], rockallcoords[1]);
        solo.clickOnScreen(bergencoords[0], bergencoords[1]);
        solo.clickOnScreen(rockallcoords[0], rockallcoords[1]);
        solo.clickOnButton("OK");
    }

    private void player1Move(){
        for(Float[] f : myMountainCoords) {
            if (f == northumbriacoords) {
                //Capture down from Northumbria
                solo.clickOnScreen(northumbriacoords[0],northumbriacoords[1]);
                if(largescreen){solo.clickOnScreen(yorkshirecoords[0], yorkshirecoords[1]);} else {
                    solo.clickOnScreen(yorkshirecoordssm[0], yorkshirecoordssm[1]);
                }
                button = solo.getView(R.id.btnPlus);//Add reinforcement
                solo.clickOnView(button);
                solo.clickOnView(button);
                solo.clickOnButton("Confirm");//Assign reinforcement

                //Take London
                if(largescreen){solo.clickOnScreen(yorkshirecoords[0], yorkshirecoords[1]);} else {
                    solo.clickOnScreen(yorkshirecoordssm[0], yorkshirecoordssm[1]);
                }
                solo.clickOnScreen(merciacoords[0], merciacoords[1]);
                button = solo.getView(R.id.btnPlus);//Add reinforcement
                solo.clickOnView(button);
                solo.clickOnButton("Confirm");//Assign reinforcement
            }
            if (f == walescoords) {
                //Move inside empire to mercia then to londoncoords
                solo.clickOnScreen(walescoords[0], walescoords[1]);
                solo.clickOnScreen(merciacoords[0], merciacoords[1]);
                button = solo.getView(R.id.btnPlus);//Add reinforcement
                solo.clickOnView(button);
                solo.clickOnView(button);
                solo.clickOnButton("Confirm");//Assign reinforcement
                solo.clickOnScreen(merciacoords[0], merciacoords[1]);
                solo.clickOnScreen(londoncoords[0], londoncoords[1]);
                button = solo.getView(R.id.btnPlus);//Add reinforcement
                solo.clickOnView(button);
                solo.clickOnView(button);
                solo.clickOnButton("Confirm");//Assign reinforcement
            }
        }
        endTurn();
    }
    private void player2Move(){
        for(Float[] f : myMountainCoords) {
            if (f == munstercoords) {
                solo.clickOnScreen(munstercoords[0], munstercoords[1]);
                solo.clickOnScreen(stgeorgeschannelcoords[0], stgeorgeschannelcoords[1]);
                button = solo.getView(R.id.btnPlus);//Add reinforcement
                solo.clickOnView(button);
                solo.clickOnView(button);
                solo.clickOnButton("Confirm");//Assign reinforcement
            }
            if (f == ardennescoords) {
                solo.clickOnScreen(ardennescoords[0],ardennescoords[1]);
                solo.clickOnScreen(belgiumcoords[0], belgiumcoords[1]);
                button = solo.getView(R.id.btnPlus);//Add reinforcement
                solo.clickOnView(button);
                solo.clickOnButton("Confirm");//Assign reinforcement
                solo.clickOnScreen(ardennescoords[0],ardennescoords[1]);
                solo.clickOnScreen(belgiumcoords[0], belgiumcoords[1]);
                button = solo.getView(R.id.btnPlus);//Add reinforcement
                solo.clickOnView(button);
                solo.clickOnButton("Confirm");//Assign reinforcement

                solo.clickOnScreen(belgiumcoords[0], belgiumcoords[1]);
                solo.clickOnScreen(thamescoords[0], thamescoords[1]);
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
    Split an empire (from source and dest)
    */
    private void testAttack(){
        waitForMyTurnWithDefenceListener();
        fillRegions();
        endTurn();
        endTurn();
        waitForMyTurnWithDefenceListener();
        fillRegions();
        endTurn();//Building up enough men to play out all attacks in 1 sweep
        //Begin attack move
        Assert.assertTrue(solo.searchText("Attack/Moving"));
        if(player1){
           endTurn(); waitForMyTurnWithDefenceListener(); endTurn(); player1Attack(); waitForMyTurnWithDefenceListener();}
        else {
            player2Attack(); waitForMyTurnWithDefenceListener(); player2Attack2();
        }
        solo.sleep(400000);
        boolean b = true;
        assertTrue(b);
    }

    private void player1Attack(){//Note: Happens AFTER player2Attack

        if(largescreen){moveArmy(northumbriacoords,yorkshirecoords, 5);} else {moveArmy(northumbriacoords,yorkshirecoordssm,5);}
        solo.clickOnScreen(bergencoords[0], bergencoords[1]);//Trigger waiting for defender
        Assert.assertTrue(solo.searchButton("OK"));
        solo.clickOnButton("OK");//Dismiss
        placeBomb(bergencoords);//Trigger must be inside previous empire
        Assert.assertTrue(solo.searchButton("OK"));
        solo.clickOnButton("OK");//Dismiss
        solo.clickOnScreen(northumbriacoords[0], northumbriacoords[1]);
        if(solo.searchButton("Confirm")){
            solo.clickOnButton("Confirm");
        }
        if(largescreen){moveArmy(northumbriacoords, yorkshirecoords, 5);} else {moveArmy(northumbriacoords, yorkshirecoordssm, 5);}
        placeBomb(northumbriacoords);//increase bomb size
        if(largescreen){moveArmy(yorkshirecoords, merciacoords, 5);} else {moveArmy(yorkshirecoordssm, merciacoords, 5);} //Split empire from source and target
        if(largescreen){placeBomb(yorkshirecoords);} else {placeBomb(yorkshirecoordssm);}
        if(largescreen){moveArmy(merciacoords, yorkshirecoords, 1);} else {moveArmy(merciacoords, yorkshirecoordssm, 1);}//Rejoin empire
        moveArmy(merciacoords,londoncoords,1);//Attack london with 3 men (Attack City)
        if(largescreen){placeBomb(yorkshirecoords);} else {placeBomb(yorkshirecoordssm);}
        moveArmy(londoncoords,merciacoords,1);
        moveArmy(londoncoords, thamescoords, 1);//Dominate Sea
        placeBomb(londoncoords);
        moveArmy(merciacoords, walescoords, 1); //Attack mountain
        placeBomb(northumbriacoords);
        moveArmy(walescoords, stgeorgeschannelcoords, 1);//Dominate Sea
        if(largescreen) {placeBomb(yorkshirecoords);} else {placeBomb(yorkshirecoordssm);}
        endTurn();
    }

    private void player2Attack(){//Player 2 actually attacks first because player 1 has more men to play with and can afford to lose some
        moveArmy(ardennescoords, thamescoords, 2);//Reinforce Thames
        moveArmy(thamescoords, londoncoords, 2);//Attack City && Attack Land from Sea

        solo.clickOnScreen(bergencoords[0], bergencoords[1]);//Trigger waiting for defender
        Assert.assertTrue(solo.searchButton("OK"));
        solo.clickOnButton("OK");//Dismiss
        placeBomb(bergencoords);//Trigger must be inside previous empire
        Assert.assertTrue(solo.searchButton("OK"));
        solo.clickOnButton("OK");//Dismiss
        solo.clickOnScreen(belgiumcoords[0], belgiumcoords[1]);
        if(solo.searchButton("Confirm")){
            solo.clickOnButton("Confirm");
        }

        moveArmy(thamescoords, londoncoords, 2);//Triggers split empire at source
        placeBomb(belgiumcoords);//Place bomb in belgium, triggers increase size of bomb

        moveArmy(londoncoords, thamescoords, 1);//Join empire again
        moveArmy(londoncoords, merciacoords, 2);//Attack normal/ split empire at dest
        placeBomb(belgiumcoords);//Place bomb in belgium, triggers increase size of bomb
        moveArmy(merciacoords, londoncoords, 1);//Join empire again
        moveArmy(munstercoords, stgeorgeschannelcoords, 2);//Move all men to stgeorgeschannel
        moveArmy(stgeorgeschannelcoords,walescoords,2);//Attack wales with 3 men, Attack Mountain
        placeBomb(munstercoords);
        moveArmy(walescoords,merciacoords,3);
        if(largescreen){moveArmy(merciacoords, yorkshirecoords, 1);} else {moveArmy(merciacoords, yorkshirecoordssm, 1);}
        placeBomb(ardennescoords);//For hydrogen bomb denial later
        endTurn();
    }

    private void player2Attack2(){
        endTurn();//End bombing phase
        fillRegions();//Reinforce
        endTurn();
        moveArmy(munstercoords, stgeorgeschannelcoords, 1);//Dominate Sea
        placeBomb(munstercoords);
        moveArmy(ardennescoords,belgiumcoords,1);
        moveArmy(belgiumcoords,thamescoords,1);//Dominate sea
        placeBomb(belgiumcoords);
        endTurn();
    }

    private void moveArmy(Float[] source, Float[] dest, int clicks) {
        solo.clickOnScreen(source[0], source[1]);
        solo.clickOnScreen(dest[0], dest[1]);
        button = solo.getView(R.id.btnPlus);
        for (int i = 0; i < clicks; i++) {
            solo.clickOnView(button);
        }
        solo.clickOnButton("Confirm");//Confirm attack
    }

    private void placeBomb(Float[] target){
        while(!solo.searchText("atom bomb")){solo.sleep(100);}//wait for atom bomb notification
        Assert.assertTrue(solo.searchButton("OK"));
        solo.clickOnButton("OK");//Dismiss
        solo.clickOnScreen(target[0], target[1]);
        if(solo.searchButton("Confirm")){
            solo.clickOnButton("Confirm");
        }
    }

    //BOMBING
    /* Both players must:
    Attack from sea to land
    Attack from land to sea
    Attack city
    Attack mountain
    Split an empire (from source and dest)
    */
    private void testBombing(){
        waitForMyTurnWithDefenceListener();
        fillRegions();
        endTurn();
        endTurn();
        waitForMyTurnWithDefenceListener();
        fillRegions();
        endTurn();//Building up enough men to play out all attacks in 1 sweep
        //Begin attack move
        Assert.assertTrue(solo.searchText("Attack/Moving"));
        if(player1){
            endTurn(); waitForMyTurnWithDefenceListener(); endTurn(); player1Attack(); waitForMyTurnWithDefenceListener();}
        else {
            player2Attack(); waitForMyTurnWithDefenceListener(); player2Attack2();
        }
        solo.sleep(400000);
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
        testAttack();
    }


    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
        super.tearDown();
    }



}