package samueltaylor.classicwarlordprototype;

import android.graphics.PointF;
import android.test.ActivityInstrumentationTestCase2;
import android.view.KeyEvent;
import android.view.View;

import com.robotium.solo.Solo;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.List;

import samueltaylor.classicwarlordprototype.GameController;

/**
 * Created by Sam on 04/07/2015.
 * NOTE: Tests ran on GT-P7510 and GT-I9300 without translation of touch coordinates, results on devices of other sizes may vary
 * Tests run using worldsmall map
 * Run with 2 devices plugged in using gradlew createDebugCoverageReport in Android Studio terminal
 * Tests run through regular actions of a normal game to ensure nothing has been broken during implementation
 */
public class IntegrationTests extends ActivityInstrumentationTestCase2<GameController> {
    private Solo solo;

    private boolean player1=false;
    private boolean largescreen=false;

    //Coords for regions in worldsmall
    private List<Float[]> mountaincoords;
    private List<Float[]> myMountainCoords;


    private Float[] rockallcoords={321.0f,103.0f,386.0f,61.0f};private Float[] bergencoords={618.0f,70.0f,525.0f,86.0f};
    //Mountains
    private Float[] walescoords={478.0f,431.0f,415.0f,428.0f};
    private Float[] telemarkcoords={825.0f,111.0f,647.0f,115.0f};
    private Float[] northumbriacoords={557.0f,291.0f,492.0f,292.5f};private Float[] munstercoords={378.0f,477.0f,314.0f,429.0f};
    private Float[] ardennescoords={730.0f,481.0f,635.0f,456.0f};private Float[] juracoords={795.0f,602.0f,704.0f,584.0f};
    private Float[] savoiecoords={796.0f,698.0f,722.0f,653.0f};private Float[] graubundencoords={911.0f,618.0f,829.0f,574.0f};
    private Float[] tyrolcoords={976.0f,604.0f,884.0f,594.0f};private Float[] tauerncoords={1013.0f,569.0f,910.0f,536.0f};
    private Float[] massifcentralcoords={697.0f,737.0f,618.0f,688.0f};private Float[] caledoniacoords={455.0f,188.0f,368.f,177.0f};

    //Adjacent to mountains
    private Float[] stgeorgeschannelcoords ={442.0f, 441.0f,353.0f,421.5f};private Float[] merciacoords ={524.0f,422.0f,472.0f,403.0f};
    private Float[] yorkshirecoords ={557.0f, 344f,474.0f,317.0f};private Float[] belgiumcoords ={706.0f,450.0f,645.0f,427.0f};

    //Extended movement
    private Float[] thamescoords ={669.0f, 419.0f,574.f,412.0f};private Float[] londoncoords ={596.0f, 439.0f,507.0f,422.0f};


    private Float[] sizecheckcoords ={256f, 300f};
    View button;

    //Utility methods
    private void clickRegion(Float[] f){
        if(largescreen){
            solo.clickOnScreen(f[0],f[1]);
        } else {
            solo.clickOnScreen(f[2], f[3]);
        }
    }

    private void interactRegions(Float[] source, Float[] dest){
        clickRegion(source);
        clickRegion(dest);
        if(solo.searchButton("OK")){
            solo.clickOnButton("OK");//Confirm notification
        }
        if(solo.searchButton("Confirm")){
           solo.clickOnButton("Confirm");//Confirm action
        }
    }

    private void zoomIn(){
        solo.clickLongOnScreen(sizecheckcoords[0], sizecheckcoords[1]);//Check if we are large screen or small screen by identifying a click at point, in case it needs distinguishing at some point
        if(solo.searchText("Yorkshire")){largescreen=true;}
        OK();OK();
        PointF p1 = new PointF(0,0);
        PointF p1e = new PointF(0,-13);
        PointF p2 = new PointF(0,0);
        PointF p2e = new PointF(0,13);
        solo.pinchToZoom(p1, p2, p1e, p2e);
        if(largescreen){
            solo.drag(0,425,0,80,50);
        } else {
            solo.drag(0, 380, 0, 80, 50);
        }
        getActivity().mapfragment.allowmovement=false;
    }

    private void waitForMyTurnWithDefenceListener(){
        while(solo.searchText("Send") && !((GameController)getActivity()).iAmCurrentPlayer()){
            if(solo.searchText("Has been attacked from") || solo.searchText("Incorrect guess, try again")){//Been attacked by the other player
                Assert.assertTrue(solo.searchButton("Confirm"));
                solo.clickOnButton("Confirm");
                //Always guess base amount
            }
            OK();
        }
    }

    private void OK(){
        if(solo.searchButton("OK")){
            solo.clickOnButton("OK");
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
        while(!solo.searchButton("AUTO-MATCH")){solo.sleep(10);}
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
        button = solo.getView(R.id.btnIcon);
        solo.clickOnView(button);
        myMountainCoords = new ArrayList<>();
        for(Float[] f : mountaincoords){
            if(((GameController)getActivity()).mModel.getCurrentphase()==0){//Only wait while in mountain selection phase, else complete selections and move on
                waitForMyTurnWithDefenceListener();
            }
            clickRegion(f);
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
        clickRegion(myMountainCoords.get(0));
        if (solo.searchButton("Confirm")){//confirmation popup has appeared
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
            clickRegion(f);
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
        clickRegion(myMountainCoords.get(0));
        if (solo.searchButton("Confirm")){//confirmation popup has appeared
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
        if(solo.searchButton("End Turn")){
            solo.clickOnButton("End Turn");
        }
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
        clickRegion(rockallcoords);
        clickRegion(bergencoords);
        clickRegion(rockallcoords);
        OK();
    }

    private void player1Move(){
        for(Float[] f : myMountainCoords) {
            if (f == northumbriacoords) {
                //Capture down from Northumbria
                clickRegion(northumbriacoords);
                clickRegion(yorkshirecoords);
                button = solo.getView(R.id.btnPlus);//Add reinforcement
                solo.clickOnView(button);
                solo.clickOnView(button);
                Assert.assertTrue(solo.searchButton("Confirm"));
                solo.clickOnButton("Confirm");

                //Take London
                clickRegion(yorkshirecoords);
                clickRegion(merciacoords);
                button = solo.getView(R.id.btnPlus);
                solo.clickOnView(button);
                Assert.assertTrue(solo.searchButton("Confirm"));
                solo.clickOnButton("Confirm");
            }
            if (f == walescoords) {
                //Move inside empire to mercia then to londoncoords
                clickRegion(walescoords);
                clickRegion(merciacoords);
                button = solo.getView(R.id.btnPlus);
                solo.clickOnView(button);
                solo.clickOnView(button);
                Assert.assertTrue(solo.searchButton("Confirm"));
                solo.clickOnButton("Confirm");
                clickRegion(merciacoords);
                clickRegion(londoncoords);
                button = solo.getView(R.id.btnPlus);
                solo.clickOnView(button);
                solo.clickOnView(button);
                Assert.assertTrue(solo.searchButton("Confirm"));
                solo.clickOnButton("Confirm");
            }
        }
        endTurn();
    }
    private void player2Move(){
        for(Float[] f : myMountainCoords) {
            if (f == munstercoords) {
                clickRegion(munstercoords);
                clickRegion(stgeorgeschannelcoords);
                button = solo.getView(R.id.btnPlus);//Add reinforcement
                solo.clickOnView(button);
                solo.clickOnView(button);
                Assert.assertTrue(solo.searchButton("Confirm"));
                solo.clickOnButton("Confirm");
            }
            if (f == ardennescoords) {
                clickRegion(ardennescoords);
                clickRegion(belgiumcoords);
                button = solo.getView(R.id.btnPlus);
                solo.clickOnView(button);
                Assert.assertTrue(solo.searchButton("Confirm"));
                solo.clickOnButton("Confirm");
                clickRegion(ardennescoords);
                clickRegion(belgiumcoords);
                button = solo.getView(R.id.btnPlus);
                solo.clickOnView(button);
                Assert.assertTrue(solo.searchButton("Confirm"));
                solo.clickOnButton("Confirm");

                clickRegion(belgiumcoords);
                clickRegion(thamescoords);
                button = solo.getView(R.id.btnPlus);
                solo.clickOnView(button);
                solo.clickOnView(button);
                Assert.assertTrue(solo.searchButton("Confirm"));
                solo.clickOnButton("Confirm");
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
           endTurn(); waitForMyTurnWithDefenceListener(); endTurn(); player1Attack();
        } else {
            player2Attack(); waitForMyTurnWithDefenceListener(); player2Attack2();
        }
    }

    private void player1Attack(){//Note: Happens AFTER player2Attack
        moveArmy(northumbriacoords,yorkshirecoords, 5);
        clickRegion(bergencoords);//Trigger waiting for defender
        OK();//Dismiss
        placeBomb(bergencoords);//Trigger must be inside previous empire
        OK();//Dismiss
        clickRegion(northumbriacoords);
        Assert.assertTrue(solo.searchButton("Confirm"));
        solo.clickOnButton("Confirm");

        moveArmy(northumbriacoords, yorkshirecoords, 5);
        placeBomb(northumbriacoords);//increase bomb size
        moveArmy(yorkshirecoords, merciacoords, 5); //Split empire from source and target
        placeBomb(yorkshirecoords);
        moveArmy(merciacoords, yorkshirecoords, 1);//Rejoin empire
        moveArmy(merciacoords, londoncoords, 1);//Attack london with 3 men (Attack City)
        placeBomb(yorkshirecoords);
        moveArmy(londoncoords,merciacoords,1);
        moveArmy(londoncoords, thamescoords, 0);//Dominate Sea
        placeBomb(londoncoords);
        moveArmy(merciacoords, walescoords, 1); //Attack mountain
        placeBomb(northumbriacoords);
        moveArmy(walescoords, stgeorgeschannelcoords, 0);//Dominate Sea
        placeBomb(yorkshirecoords);
        endTurn();
    }

    private void player2Attack(){//Player 2 actually attacks first because player 1 has more men to play with and can afford to lose some
        moveArmy(ardennescoords, thamescoords, 2);//Reinforce Thames
        moveArmy(thamescoords, londoncoords, 2);//Attack City && Attack Land from Sea

        clickRegion(bergencoords);//Trigger waiting for defender
        OK();//Dismiss
        placeBomb(bergencoords);//Trigger must be inside previous empire
        OK();//Dismiss
        clickRegion(belgiumcoords);
        Assert.assertTrue(solo.searchButton("Confirm"));
        solo.clickOnButton("Confirm");


        moveArmy(thamescoords, londoncoords, 2);//Triggers split empire at source
        placeBomb(belgiumcoords);//Place bomb in belgium, triggers increase size of bomb
        moveArmy(londoncoords, thamescoords, 1);//Join empire again
        moveArmy(londoncoords, merciacoords, 2);//Attack normal/ split empire at dest
        placeBomb(belgiumcoords);//Place bomb in belgium, triggers increase size of bomb
        moveArmy(merciacoords, londoncoords, 1);//Join empire again
        moveArmy(munstercoords, stgeorgeschannelcoords, 2);//Move all men to stgeorgeschannel
        moveArmy(stgeorgeschannelcoords, walescoords, 2);//Attack wales with 3 men, Attack Mountain
        placeBomb(munstercoords);
        moveArmy(walescoords, merciacoords, 3);
        moveArmy(merciacoords, yorkshirecoords, 1);
        placeBomb(ardennescoords);//For hydrogen bomb denial later
        endTurn();
    }

    private void player2Attack2(){
        endTurn();//End bombing phase
        fillRegions();//Reinforce
        endTurn();
        moveArmy(munstercoords, stgeorgeschannelcoords, 0);//Dominate Sea
        placeBomb(munstercoords);
        moveArmy(ardennescoords, belgiumcoords, 1);
        moveArmy(belgiumcoords, thamescoords, 0);//Dominate sea
        placeBomb(belgiumcoords);
        endTurn();
    }

    private void moveArmy(Float[] source, Float[] dest, int clicks) {
        clickRegion(source);
        clickRegion(dest);
        button = solo.getView(R.id.btnPlus);
        for (int i = 0; i < clicks; i++) {
            solo.clickOnView(button);
        }
        Assert.assertTrue(solo.searchButton("Confirm"));
        solo.clickOnButton("Confirm");//Confirm attack
    }

    private void placeBomb(Float[] target){
        while(!solo.searchText("earned")){solo.sleep(100);
        }//wait for bomb notification
        OK();//Dismiss
        clickRegion(target);
        if(solo.searchText("Confirm")){
            solo.clickOnButton("Confirm");
        }
        OK();
        solo.sleep(1000);
    }

    //BOMBING
    /* Both players must:
    See can't destroy source empire message
    Out of range message
    Cause shattered empire check
    Earn hydrogen bomb
    Can't place hydrogen bomb on atom bomb
    Place hydrogen bomb
    Detonate hydrogen bomb
    */
    private void testBombing(){
        waitForMyTurnWithDefenceListener();

        //Begin bombing
        Assert.assertTrue(solo.searchText("Bombing"));
        if(player1){
            player1ABomb();waitForMyTurnWithDefenceListener();player1HBomb();waitForMyTurnWithDefenceListener();
        } else {
            player2ABomb();waitForMyTurnWithDefenceListener();player2HBomb();solo.sendKey(KeyEvent.KEYCODE_BACK);
        }
    }

    private void player1ABomb(){
        interactRegions(northumbriacoords, merciacoords);//Can't destroy empire
        interactRegions(londoncoords, munstercoords);//Out of range
        //Shattered empire + earn hydrogen bomb
        interactRegions(yorkshirecoords, londoncoords);
        placeBomb(northumbriacoords);//Can't place bomb on atom bomb
        OK();
        clickRegion(yorkshirecoords);
        Assert.assertTrue(solo.searchButton("Confirm"));
        solo.clickOnButton("Confirm");

        interactRegions(yorkshirecoords, merciacoords);//Can't fire Hydrogenbomb anymore
        endTurn();//Move to reinforcement
        endTurn();//Move to attack/Move
        endTurn();//Move to next player
    }
    private void player2ABomb(){
        interactRegions(munstercoords, stgeorgeschannelcoords);//Can't destroy empire
        interactRegions(munstercoords, belgiumcoords);//Out of range
        interactRegions(belgiumcoords,thamescoords);//Shattered empire, earn hydrogen bomb
        placeBomb(ardennescoords);//Can't place bomb on atom bomb
        OK();
        clickRegion(belgiumcoords);
        Assert.assertTrue(solo.searchButton("Confirm"));
        solo.clickOnButton("Confirm");

        interactRegions(munstercoords, walescoords);//earn another hydrogen bomb
        placeBomb(munstercoords);
        interactRegions(munstercoords, stgeorgeschannelcoords);//Can't fire Hydrogen bomb anymore
        endTurn();//Move to reinforcement
        endTurn();//Move to attack/Move
        endTurn();//Move to next player
    }

    private void player1HBomb(){
        //Detonate Hydrogen Bomb
        interactRegions(yorkshirecoords,yorkshirecoords);

        endTurn();//Move to reinforcement
        endTurn();//Move to attack/Move
        endTurn();//Move to next player
    }
    private void player2HBomb(){
        //Detonate Hydrogen Bombs
        interactRegions(munstercoords,munstercoords);
    }


    //Tests
    public IntegrationTests() {
        super(GameController.class);
    }

    public void setUp() throws Exception {
        solo = new Solo(getInstrumentation(), getActivity());
    }

    public void testFull() throws Exception {
        testAutoMatch();
        zoomIn();
        testMountainSelection();
        testReinforcement();
        testMove();
        testAttack();
        testBombing();
        Assert.assertTrue(solo.searchButton("AUTO-MATCH"));//Back to the main menu
    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
        super.tearDown();
    }

}