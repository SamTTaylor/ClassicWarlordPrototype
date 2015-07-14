package samueltaylor.classicwarlordprototype;

/**
 * Created by Sam on 03/05/2015.
 */
        import android.app.Activity;
        import android.app.FragmentManager;
        import android.app.FragmentTransaction;
        import android.content.Intent;
        import android.net.Uri;
        import android.os.Bundle;
        import android.os.Handler;
        import android.support.v4.app.FragmentActivity;
        import android.util.Log;
        import android.view.KeyEvent;

        import com.google.android.gms.common.ConnectionResult;
        import com.google.android.gms.common.api.GoogleApiClient;
        import com.google.android.gms.games.Games;
        import com.google.android.gms.games.GamesActivityResultCodes;
        import com.google.android.gms.games.GamesStatusCodes;
        import com.google.android.gms.games.multiplayer.Invitation;
        import com.google.android.gms.games.multiplayer.Multiplayer;
        import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
        import com.google.android.gms.games.multiplayer.Participant;
        import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
        import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
        import com.google.android.gms.games.multiplayer.realtime.Room;
        import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
        import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
        import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;
        import com.google.android.gms.plus.Plus;
        import com.google.example.games.basegameutils.BaseGameUtils;

        import org.xmlpull.v1.XmlPullParserException;

        import java.io.BufferedInputStream;
        import java.io.BufferedWriter;
        import java.io.File;
        import java.io.FileNotFoundException;
        import java.io.FileWriter;
        import java.io.IOException;
        import java.io.InputStream;
        import java.nio.ByteBuffer;
        import java.util.ArrayList;
        import java.util.HashMap;
        import java.util.HashSet;
        import java.util.List;
        import java.util.Map;
        import java.util.Set;

        import samueltaylor.classicwarlordprototype.Fragments.fragDialog;
        import samueltaylor.classicwarlordprototype.Fragments.fragGameHUDPlayers;
        import samueltaylor.classicwarlordprototype.Fragments.fragGameMap;
        import samueltaylor.classicwarlordprototype.Fragments.fragIM;
        import samueltaylor.classicwarlordprototype.Fragments.fragInfo;
        import samueltaylor.classicwarlordprototype.Fragments.fragInspect;
        import samueltaylor.classicwarlordprototype.Fragments.fragInvitationReceived;
        import samueltaylor.classicwarlordprototype.Fragments.fragLoading;
        import samueltaylor.classicwarlordprototype.Fragments.fragMain;
        import samueltaylor.classicwarlordprototype.Model.Army;
        import samueltaylor.classicwarlordprototype.Model.Empire;
        import samueltaylor.classicwarlordprototype.Model.GameModel;
        import samueltaylor.classicwarlordprototype.Model.Player;
        import samueltaylor.classicwarlordprototype.Model.Region;
        import samueltaylor.classicwarlordprototype.XMLParsing.SVGtoRegionParser;


/**
 * @author Mateusz Mysliwiec
 * @author www.matim-dev.com
 * @version 1.0
 */
public class GameController extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, RealTimeMessageReceivedListener,
        RoomStatusUpdateListener, RoomUpdateListener, OnInvitationReceivedListener, fragMain.OnFragmentInteractionListener, fragGameMap.OnFragmentInteractionListener, fragInvitationReceived.OnFragmentInteractionListener,
        fragIM.OnFragmentInteractionListener, fragGameHUDPlayers.OnFragmentInteractionListener, fragLoading.OnFragmentInteractionListener, fragInfo.OnFragmentInteractionListener, fragDialog.OnFragmentInteractionListener,
        fragInspect.OnFragmentInteractionListener
{

    //Online gameplay stuff

    final static String TAG = "Classic Warlord";

    // Request codes for the UIs that we show with startActivityForResult:
    final static int RC_SELECT_PLAYERS = 10000;
    final static int RC_INVITATION_INBOX = 10001;
    final static int RC_WAITING_ROOM = 10002;

    // Request code used to invoke sign in user interactions.
    private static final int RC_SIGN_IN = 9001;

    // Client used to interact with Google APIs.
    private GoogleApiClient mGoogleApiClient;

    // Are we currently resolving a connection failure?
    private boolean mResolvingConnectionFailure = false;

    // Has the user clicked the sign-in button?
    private boolean mSignInClicked = false;

    // Set to true to automatically start the sign in flow when the Activity starts.
    // Set to false to require the user to click the button in order to sign in.
    private boolean mAutoStartSignInFlow = true;

    // Room ID where the currently active game is taking place; null if we're
    // not playing.
    String mRoomId = null;

    // Are we playing in multiplayer mode?
    boolean mMultiplayer = false;

    // The participants in the currently active game
    ArrayList<Participant> mParticipants = null;

    // My participant ID in the currently active game
    String mMyId = null;

    // If non-null, this is the id of the invitation we received via the
    // invitation listener
    String mIncomingInvitationId = null;

    // Message buffer for sending messages
    byte[] mMsgBuf = new byte[2];


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Start Activity view
        setContentView(R.layout.activity_main);

        //Initialise simple fragments
        initialiseNonMapFragments();

        //First time load Main Menu fragment
        loadMainMenu();

        // Create the Google Api Client with access to Plus and Games
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        if(mGoogleApiClient.isConnected()==true){
            signedin = true;
        } else {
            signedin = false;
        }

    }


    public void invite() {
        if(mGoogleApiClient.isConnected()){
            Intent intent;
            // show list of invitable players
            intent = Games.RealTimeMultiplayer.getSelectOpponentsIntent(mGoogleApiClient, 1, 6);
            //show loading fragment
            showLoadingFragment("Loading Room...");
            startActivityForResult(intent, RC_SELECT_PLAYERS);
        } else {
            mSignInClicked = true;
            mGoogleApiClient.connect();
            signedin = true;
        }
    }

    public void seeinvites(){
        Intent intent;
        // show list of pending invitations
        intent = Games.Invitations.getInvitationInboxIntent(mGoogleApiClient);
        startActivityForResult(intent, RC_INVITATION_INBOX);
        //show loading fragment
        showLoadingFragment("Loading Invites");
    }

    public boolean signedin;
    public void signout(){
        // user wants to sign out
        // sign out.
        Log.d(TAG, "Sign-out button clicked");
        mSignInClicked = false;
        Games.signOut(mGoogleApiClient);
        mGoogleApiClient.disconnect();
        signedin = false;
    }

    public void signin(){
        // user wants to sign in
        // Check to see the developer who's running this sample code read the instructions :-)
        // NOTE: this check is here only because this is a sample! Don't include this
        // check in your actual production app.
        if (!BaseGameUtils.verifySampleSetup(this, samueltaylor.classicwarlordprototype.R.string.app_id)) {
            Log.w(TAG, "*** Warning: setup problems detected. Sign in may not work!");
        }

        // start the sign-in flow
        Log.d(TAG, "Sign-in button clicked");
        mSignInClicked = true;
        mGoogleApiClient.connect();
        signedin = true;
    }


    public void startQuickGame() {
        // quick-start a game with randomly selected opponents
        if(mGoogleApiClient.isConnected()){
            final int MIN_OPPONENTS = 3, MAX_OPPONENTS = 6;
            Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS,
                    MAX_OPPONENTS, 0);
            RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(this);
            rtmConfigBuilder.setMessageReceivedListener(this);
            rtmConfigBuilder.setRoomStatusUpdateListener(this);
            rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
            resetGameVars();
            Games.RealTimeMultiplayer.create(mGoogleApiClient, rtmConfigBuilder.build());
            //show loading fragment
            showLoadingFragment(null);
        } else {
            mSignInClicked = true;
            mGoogleApiClient.connect();
            signedin = true;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int responseCode,
                                 Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);
        switch (requestCode) {
            case RC_SELECT_PLAYERS:
                // we got the result from the "select players" UI -- ready to create the room
                handleSelectPlayersResult(responseCode, intent);
                break;
            case RC_INVITATION_INBOX:
                // we got the result from the "select invitation" UI (invitation inbox). We're
                // ready to accept the selected invitation:
                handleInvitationInboxResult(responseCode, intent);
                break;
            case RC_WAITING_ROOM:
                // we got the result from the "waiting room" UI.
                if (responseCode == Activity.RESULT_OK) {
                    // ready to start playing
                    Log.d(TAG, "Starting game (waiting room returned OK).");
                    try {
                        startGame(true);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else if (responseCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
                    // player indicated that they want to leave the room
                    leaveRoom();
                } else if (responseCode == Activity.RESULT_CANCELED) {
                    // Dialog was cancelled (user pressed back key, for instance). In our game,
                    // this means leaving the room too. In more elaborate games, this could mean
                    // something else (like minimizing the waiting room UI).
                    leaveRoom();
                }
                break;
            case RC_SIGN_IN:
                Log.d(TAG, "onActivityResult with requestCode == RC_SIGN_IN, responseCode="
                        + responseCode + ", intent=" + intent);
                mSignInClicked = false;
                mResolvingConnectionFailure = false;
                if (responseCode == RESULT_OK) {
                    mGoogleApiClient.connect();
                } else {
                    BaseGameUtils.showActivityResultError(this,requestCode,responseCode, samueltaylor.classicwarlordprototype.R.string.signin_other_error);
                }
                break;
        }
        super.onActivityResult(requestCode, responseCode, intent);
    }

    // Handle the result of the "Select players UI" we launched when the user clicked the
    // "Invite friends" button. We react by creating a room with those players.
    private void handleSelectPlayersResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** select players UI cancelled, " + response);
            loadMainMenu();
            return;
        }

        Log.d(TAG, "Select players UI succeeded.");

        // get the invitee list
        final ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
        Log.d(TAG, "Invitee count: " + invitees.size());

        // get the automatch criteria
        Bundle autoMatchCriteria = null;
        int minAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
        int maxAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
        if (minAutoMatchPlayers > 0 || maxAutoMatchPlayers > 0) {
            autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                    minAutoMatchPlayers, maxAutoMatchPlayers, 0);
            Log.d(TAG, "Automatch criteria: " + autoMatchCriteria);
        }

        // create the room
        Log.d(TAG, "Creating room...");
        RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(this);
        rtmConfigBuilder.addPlayersToInvite(invitees);
        rtmConfigBuilder.setMessageReceivedListener(this);
        rtmConfigBuilder.setRoomStatusUpdateListener(this);
        if (autoMatchCriteria != null) {
            rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
        }
        //show loading fragment
        showLoadingFragment(null);
        resetGameVars();
        Games.RealTimeMultiplayer.create(mGoogleApiClient, rtmConfigBuilder.build());
        Log.d(TAG, "Room created, waiting for it to be ready...");
    }

    // Handle the result of the invitation inbox UI, where the player can pick an invitation
    // to accept. We react by accepting the selected invitation, if any.
    private void handleInvitationInboxResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** invitation inbox UI cancelled, " + response);
            loadMainMenu();
            return;
        }

        Log.d(TAG, "Invitation inbox UI succeeded.");
        Invitation inv = data.getExtras().getParcelable(Multiplayer.EXTRA_INVITATION);

        // accept invitation
        acceptInviteToRoom(inv.getInvitationId());
    }

    // Accept the given invitation.
    void acceptInviteToRoom(String invId) {
        // accept the invitation
        Log.d(TAG, "Accepting invitation: " + invId);
        RoomConfig.Builder roomConfigBuilder = RoomConfig.builder(this);
        roomConfigBuilder.setInvitationIdToAccept(invId)
                .setMessageReceivedListener(this)
                .setRoomStatusUpdateListener(this);
        //show loading fragment
        showLoadingFragment(null);
        resetGameVars();
        Games.RealTimeMultiplayer.join(mGoogleApiClient, roomConfigBuilder.build());
    }

    // Activity is going to the background. We have to leave the current room.
    @Override
    public void onStop() {
        Log.d(TAG, "**** got onStop");

        // if we're in a room, leave it.
        leaveRoom();
        super.onStop();
    }

    // Activity just got to the foreground. We switch to the wait screen because we will now
    // go through the sign-in flow (remember that, yes, every time the Activity comes back to the
    // foreground we go through the sign-in flow -- but if the user is already authenticated,
    // this flow simply succeeds and is imperceptible).
    @Override
    public void onStart() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Log.w(TAG,
                    "GameHelper: client was already connected on onStart()");
        } else {
            Log.d(TAG,"Connecting client.");
            mGoogleApiClient.connect();
        }
        super.onStart();
    }

    // Handle back key to make sure we cleanly leave a game if we are in the middle of one
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent e) {
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
            if(inRoom()==true){
                leaveRoom();
            } else {
                this.finish();
                System.exit(0);
            }

        }
        return true;
    }

    // Leave the room.
    void leaveRoom() {
        if (mRoomId != null) {
            Log.d(TAG, "Leaving room.");
            Games.RealTimeMultiplayer.leave(mGoogleApiClient, this, mRoomId);
        }
    }

    // Show the waiting room UI to track the progress of other players as they enter the
    // room and get connected.
    void showWaitingRoom(Room room) {
        // minimum number of players required for our game
        // For simplicity, we require everyone to join the game before we start it
        // (this is signaled by Integer.MAX_VALUE).
        final int MIN_PLAYERS = Integer.MAX_VALUE;
        Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent(mGoogleApiClient, room, MIN_PLAYERS);

        // show waiting room UI
        startActivityForResult(i, RC_WAITING_ROOM);
    }

    // Called when we get an invitation to play a game. We react by showing that to the user.
    @Override
    public void onInvitationReceived(Invitation invitation) {
        // We got an invitation to play a game! So, store it in
        // mIncomingInvitationId
        // and show the popup on the screen.
        Log.d(TAG, "Invite Received.");
        mIncomingInvitationId = invitation.getInvitationId();
        //show invitation fragment
        showinvitefragment();
    }

    @Override
    public void onInvitationRemoved(String invitationId) {
        if (mIncomingInvitationId.equals(invitationId)) {
            mIncomingInvitationId = null;
            //hide invitation fragment
            hideinvitefragment();
        }
    }

    /*
     * CALLBACKS SECTION. This section shows how we implement the several games
     * API callbacks.
     */

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "onConnected() called. Sign in successful!");

        Log.d(TAG, "Sign-in succeeded.");

        // register listener so we are notified if we receive an invitation to play
        // while we are in the game
        Games.Invitations.registerInvitationListener(mGoogleApiClient, this);

        if (connectionHint != null) {
            Log.d(TAG, "onConnected: connection hint provided. Checking for invite.");
            Invitation inv = connectionHint
                    .getParcelable(Multiplayer.EXTRA_INVITATION);
            if (inv != null && inv.getInvitationId() != null) {
                // retrieve and cache the invitation ID
                Log.d(TAG,"onConnected: connection hint has a room invite!");
                acceptInviteToRoom(inv.getInvitationId());
                return;
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended() called. Trying to reconnect.");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed() called, result: " + connectionResult);

        if (mResolvingConnectionFailure) {
            Log.d(TAG, "onConnectionFailed() ignoring connection failure; already resolving.");
            return;
        }

        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = BaseGameUtils.resolveConnectionFailure(this, mGoogleApiClient,
                    connectionResult, RC_SIGN_IN, getString(samueltaylor.classicwarlordprototype.R.string.signin_other_error));
        }
    }

    // Called when we are connected to the room. We're not ready to play yet! (maybe not everybody
    // is connected yet).
    @Override
    public void onConnectedToRoom(Room room) {
        Log.d(TAG, "onConnectedToRoom.");

        // get room ID, participants and my ID:
        mRoomId = room.getRoomId();
        mParticipants = room.getParticipants();
        mMyId = room.getParticipantId(Games.Players.getCurrentPlayerId(mGoogleApiClient));

        // print out the list of participants (for debug purposes)
        Log.d(TAG, "Room ID: " + mRoomId);
        Log.d(TAG, "My ID " + mMyId);
        Log.d(TAG, "<< CONNECTED TO ROOM>>");
    }

    // Called when we've successfully left the room (this happens a result of voluntarily leaving
    // via a call to leaveRoom(). If we get disconnected, we get onDisconnectedFromRoom()).
    @Override
    public void onLeftRoom(int statusCode, String roomId) {
        // we have left the room; return to main screen.
        Log.d(TAG, "onLeftRoom, code " + statusCode);
        loadMainMenu();
        mRoomId = null;
    }

    // Called when we get disconnected from the room. We return to the main screen.
    @Override
    public void onDisconnectedFromRoom(Room room) {
        mRoomId = null;
        showGameError();
    }

    // Show error message about game being cancelled and return to main screen.
    void showGameError() {
        BaseGameUtils.makeSimpleDialog(this, getString(samueltaylor.classicwarlordprototype.R.string.game_problem));
        loadMainMenu();
    }

    // Called when room has been created
    @Override
    public void onRoomCreated(int statusCode, Room room) {
        Log.d(TAG, "onRoomCreated(" + statusCode + ", " + room + ")");
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomCreated, status " + statusCode);
            showGameError();
            return;
        }
        mRoomId = room.getRoomId();
        // show the waiting room UI
        showWaitingRoom(room);
    }

    // Called when room is fully connected.
    @Override
    public void onRoomConnected(int statusCode, Room room) {
        Log.d(TAG, "onRoomConnected(" + statusCode + ", " + room + ")");
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
            showGameError();
            return;
        }
        updateRoom(room);
    }

    @Override
    public void onJoinedRoom(int statusCode, Room room) {
        Log.d(TAG, "onJoinedRoom(" + statusCode + ", " + room + ")");
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
            showGameError();
            return;
        }

        // show the waiting room UI
        showWaitingRoom(room);
    }

    // We treat most of the room update callbacks in the same way: we update our list of
    // participants and update the display. In a real game we would also have to check if that
    // change requires some action like removing the corresponding player avatar from the screen,
    // etc.
    @Override
    public void onPeerDeclined(Room room, List<String> arg1) {
        updateRoom(room);
    }

    @Override
    public void onPeerInvitedToRoom(Room room, List<String> arg1) {
        updateRoom(room);
    }

    @Override
    public void onP2PDisconnected(String participant) {}

    @Override
    public void onP2PConnected(String participant) {}

    @Override
    public void onPeerJoined(Room room, List<String> arg1) {
        updateRoom(room);
    }

    @Override
    public void onPeerLeft(Room room, List<String> peersWhoLeft) {
        updateRoom(room);
    }

    @Override
    public void onRoomAutoMatching(Room room) {
        updateRoom(room);
    }

    @Override
    public void onRoomConnecting(Room room) {
        updateRoom(room);
    }

    @Override
    public void onPeersConnected(Room room, List<String> peers) {
        updateRoom(room);
    }

    @Override
    public void onPeersDisconnected(Room room, List<String> peers) {
        updateRoom(room);
    }

    void updateRoom(Room room) {
        if (room != null) {
            mParticipants = room.getParticipants();
        }

    }

    /*
     * GAME CONSTRUCTION - Fragments, OpenGL Initialisation etc..
     */

    // Current state of the game:
    int mSecondsLeft = -1; // how long until the game ends (seconds)
    final static int GAME_DURATION = 20; // game duration, seconds.
    String mInfo = ""; // user's current score


    //Check if we are currently in game
    boolean inRoom(){
        if (mRoomId!=null) {
            return true;
        } else {
            return false;
        }
    }

    // Reset game variables in preparation for a new game.
    void resetGameVars() {
        mSecondsLeft = GAME_DURATION;
        mInfo = "";
        mParticipantChoice.clear();
        mFinishedParticipants.clear();
    }

    // Start the gameplay phase of the game.
    void startGame(boolean multiplayer) throws InterruptedException {
        mMultiplayer = multiplayer;
        //Show game related fragments
        loadGame();
        // run the gameTick() method every second to update the game.
        final Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mSecondsLeft <= 0)
                    return;
                gameTick();
                h.postDelayed(this, 1000);
            }
        }, 1000);


        // update the names on screen from room
        updatePlayers();
    }

    // Game tick -- update countdown, check if game ended.
    void gameTick() {}




    /*
        FRAGMENT LOADING/UNLOADING METHODS
    */
    fragLoading loadingfragment;
    fragMain mainfragment;
    fragIM imfragment;
    fragGameHUDPlayers hudfragment;
    fragGameMap mapfragment;
    fragInvitationReceived invitefragment;
    fragInfo infofragment;
    fragDialog dialogfragment;


    void initialiseNonMapFragments(){
        loadingfragment = new fragLoading();
        mainfragment = new fragMain();
        imfragment = new fragIM();
        hudfragment = new fragGameHUDPlayers();
        invitefragment = new fragInvitationReceived();
    }

    void loadMainMenu(){
        mainfragment = new fragMain();
        mapfragment = null;
        imfragment = null;
        hudfragment = null;
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction=manager.beginTransaction();
        transaction.replace(R.id.activity_main_layout, mainfragment, "mainmenu");
        transaction.commit();
    }
    SVGtoRegionParser mParser;
    void loadGame() throws InterruptedException {
        mapfragment = new fragGameMap();
        loadingfragment = new fragLoading();
        imfragment = new fragIM();
        hudfragment = new fragGameHUDPlayers();
        infofragment = new fragInfo();
        loadWorld();
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction=manager.beginTransaction();
        transaction.replace(R.id.activity_main_layout, mapfragment, "game");
        transaction.commit();
        loadingfragment.setText("Loading World...");
        transaction=manager.beginTransaction();
        transaction.add(R.id.activity_main_layout, hudfragment, "hud");
        transaction.add(R.id.activity_main_layout, imfragment, "im");
        transaction.add(R.id.activity_main_layout, loadingfragment, "loading");
        transaction.add(R.id.activity_main_layout, infofragment, "info");
        transaction.commit();
    }

    void loadWorld(){
        //Load game world from xml and pass it to the game map
        List<SVGtoRegionParser.Region> world = new ArrayList<>();
        mParser = new SVGtoRegionParser();
        InputStream inputStream;
        try{
            inputStream = new BufferedInputStream(getResources().openRawResource(R.raw.worldsmall));
            world = mParser.parse(inputStream);
        } catch (FileNotFoundException e){
            Log.e("FileNotFoundException", e.toString());
        } catch (XmlPullParserException e) {
            Log.e("XmlPullParserException", e.toString());
        } catch (IOException e) {
            Log.e("IOException", e.toString());
        }
        mapfragment.mWorld = world;
        initialiseModel(world, mParticipants);
        nextPlayer();
    }

    void showLoadingFragment(String loadingText){
        loadingfragment = new fragLoading();
        if(loadingText!=null){
            loadingfragment.setText(loadingText);
        }
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction=manager.beginTransaction();
        transaction.replace(R.id.activity_main_layout, loadingfragment, "loading");
        transaction.commit();
    }

    public void fadeOutLoadingFragment(){
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction=manager.beginTransaction();
        transaction.setCustomAnimations(R.anim.fadein, R.anim.fadeout);
        transaction.hide(loadingfragment);
        transaction.commit();
    }

    void showinvitefragment(){
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.remove(invitefragment);
        invitefragment = new fragInvitationReceived();
        transaction.add(R.id.activity_main_layout, invitefragment, "invite");
        transaction.commit();
    }

    public void hideinvitefragment(){
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.remove(invitefragment);
        transaction.commit();
    }

    public void showDialogFragment(int type, String s){
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        dialogfragment = new fragDialog();
        if(dialogfragment.isVisible()){
            transaction.remove(dialogfragment);
            transaction.commit();
        }
        dialogfragment.setMessage(s);
        dialogfragment.setType(type);
        transaction = manager.beginTransaction();
        transaction.add(R.id.activity_main_layout, dialogfragment, "alert");
        transaction.commit();
    }
    public void removeDialogFragment(){
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.remove(dialogfragment);
        transaction.commit();
    }

    private void showInspectFragment(){

    }

    public void removeInspectFragment(){

    }


    //Fragment Listeners
    @Override
    public void onMapFragmentInteraction(Uri uri) {}
    @Override
    public void onMainFragmentInteraction(Uri uri) {}
    @Override
    public void onHUDFragmentInteraction(Uri uri) {}
    @Override
    public void onInviteFragmentInteraction(Uri uri) {}
    @Override
    public void onLoadingFragmentInteraction(Uri uri) {}
    @Override
    public void onIMFragmentInteraction(Uri uri) {}
    @Override
    public void onInfoFragmentInteraction(Uri uri) {}
    @Override
    public void onAlertFragmentInteraction(Uri uri) {}
    @Override
    public void onInspectFragmentInteraction(Uri uri) {}





    /*
     * COMMUNICATIONS SECTION. Methods that implement the game's network
     * protocol.
     */

    // Score of other participants. We update this as we receive their scores
    // from the network.
    Map<String, Integer> mParticipantChoice = new HashMap<String, Integer>();

    // Participants who sent us their final score.
    Set<String> mFinishedParticipants = new HashSet<String>();

    // Called when we receive a real-time message from the network.
    // Messages should start with a byte indicating how they should be interpreted
    // e.g. IM starts with M so that the message is sent as an IM
    @Override
    public void onRealTimeMessageReceived(RealTimeMessage rtm) {
        byte[] buf = rtm.getMessageData();
        byte[] b;
        switch(buf[0]){
            case 'M'://IM Message Received UpdateChat()
                buf[0] = ' ';//Clear unwanted character
                String text = new String(buf);
                //Format
                text = getName(rtm.getSenderParticipantId()) + ": " + text + "\n";
                imfragment.appendChat(text);
                break;
            case 'S'://Selection data received sendMySelectionData()
                b = new byte[4];
                for(int i=0;i<b.length;i++){b[i]=buf[i+1];}
                int s= ByteToRegionID(b);
                mModel.getPlayer(rtm.getSenderParticipantId()).setSelectedregionid(s);

                b = new byte[4];
                for(int i=0;i<b.length;i++){b[i]=buf[i+5];}
                s= ByteToRegionID(b);
                mModel.getPlayer(rtm.getSenderParticipantId()).setPrevselectedregionid(s);

                updateClickedRegions();//Update view
                break;
            case 'N'://New empire for current player sendRegionUpdate()
                b = new byte[4];
                for(int i=0;i<b.length;i++){b[i]=buf[i+1];}
                int e = ByteToRegionID(b);
                mModel.getCurrentplayer().newEmpire(mModel.getRegion(e));
                addRegiontoEmpireinView(e);
                nextPlayer();
                break;
            case 'C'://Mountain count reduction sendMountainCountReduction()
                b = new byte[4];
                for(int i=0;i<b.length;i++){b[i]=buf[i+1];}//Unpack the region id byte into byte[4]
                e = ByteToRegionID(b);//Get the ID of centre mountain
                mModel.getRegion(e).setCounted(true);
                mModel.setRemainingmountaincount(-1);//Reduce remaining mountain count by 1
                for(int i=5;i<buf.length;i+=4){//For each remaining adjacent mountain
                    b[0]=buf[i];b[1]=buf[i+1];b[2]=buf[i+2];b[3]=buf[i+3];//Convert it to byte[4]
                    int x=ByteToRegionID(b);//Get id from byte
                    if(mModel.getRegion(x).getCounted()==false){
                        mModel.setRemainingmountaincount(-1);//Reduce remaining mountain count by 1
                        mModel.getRegion(x).setCounted(true);//Set counted to true
                    }
                }
                break;
            case 'R'://sendMoveToReinforcementPrompt()
                moveToReinforcement();
                break;
        }

    }

    // updates the players table
    void updatePlayers() {
        if (mRoomId != null) {
            hudfragment.myName = getName(mMyId);
            for (Participant p : mParticipants) {
                hudfragment.addPlayerName(p.getDisplayName());
                hudfragment.addPlayerColour(mModel.getParticipantColour(p.getParticipantId()));
            }
        }
    }

    //Broadcast an IM from a player
    public void updateChat(String message){
        //Structure message as IM and add it to our own fragment
        int i = message.getBytes().length+1;
        // Buffer message as bytes and broadcast
        byte[] bytes = new byte[i];
        //Label message as IM
        bytes[0] = 'M';
        for(int x=1; x<i; x++){
            bytes[x] = message.getBytes()[x-1];
        }
        for(Participant pa : mParticipants){
            if(mRoomId!=null){
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient,null,bytes,mRoomId,pa.getParticipantId());
            }
        }
        message = getName(mMyId) + ": " + message + "\n";
        imfragment.appendChat(message);
    }


    private void sendMySelectionData(){
        //get current and previous selection data
        int s = mModel.getPlayer(mMyId).getSelectedregionid();
        int p = mModel.getPlayer(mMyId).getPrevselectedregionid();
        // Buffer ints as bytes
        byte[] b = RegionIDToByte(s);
        byte[] bytes = new byte[9];
        for(int i=0;i<b.length;i++){
            bytes[i+1]=b[i];
        }
        //Label message as selection data & add the ints to the array
        bytes[0] = 'S';


        b = RegionIDToByte(p);
        for(int i=0;i<b.length;i++){
            bytes[5+i]=b[i];
        }
        //send it
        for(Participant pa : mParticipants){
            if(mRoomId!=null){
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient,null,bytes,mRoomId,pa.getParticipantId());
            }
        }
    }

    private void sendRegionUpdate(int type, int regionid){
        switch (type){
            case 0://Add new empire for current player from region
                // Buffer ints as bytes
                byte[] b = RegionIDToByte(regionid);
                byte[] bytes = new byte[6];
                for(int i=0;i<b.length;i++){
                    bytes[i+1]=b[i];
                }
                bytes[0] = 'N';//Label as new empire
                //send it
                for(Participant p : mParticipants){
                    if(mRoomId!=null) {
                        Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, bytes, mRoomId, p.getParticipantId());
                    }
                }
                break;
            default://no type supplied
                break;
        }
    }


    private void sendMountainCountReduction(int id){
        List <Byte> regionsasbytes = new ArrayList<>();
        byte label = 'C'; //Mountain count byte

        regionsasbytes.add(label);//Source region goes first
        byte[] bytes = RegionIDToByte(id);
        for(byte b : bytes){
            regionsasbytes.add(b);
        }

        for(Region r : mModel.getRegion(id).getAdjacentregions()){//Package all adjacent mountain IDs, had to be done client side as cannot trust destination to have adjacent regions yet
            if(r.getType().equals("mountain")){
                bytes = RegionIDToByte(mModel.getRegionIDByName(r.getName()));
                for(byte b : bytes){
                    regionsasbytes.add(b);
                }
            }
        }

        bytes = new byte[regionsasbytes.size()];//Convert list to array
        for(int i=0; i<regionsasbytes.size();i++){
            bytes[i] = regionsasbytes.get(i);
        }
        //Send!
        for(Participant p : mParticipants){
            if(mRoomId!=null) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, bytes, mRoomId, p.getParticipantId());
            }
        }
    }

    private void sendMoveToReinforcementPrompt(){
        byte[] bytes = new byte[1];
        bytes[0] = 'R';//Label as Reinforcements Prompt
        //Send!
        for(Participant p : mParticipants){
            if(mRoomId!=null) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, bytes, mRoomId, p.getParticipantId());
            }
        }
    }

    public String getName(String id){
        String name = "No player Found";
        if (mRoomId != null) {
            for (Participant p : mParticipants) {
                if(p.getParticipantId().equals(id)){
                    name = p.getDisplayName();
                }
            }
        }
        return name;
    }

    //Converts 1 region's ID into a byte for transport
    private byte[] RegionIDToByte(int id){
        // Buffer int as bytes
        byte[] bytes = new byte[4];
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(id);

        int i=0;
        for(Byte b : bb.array()){// add selected
            bytes[i]=b;
            i++;
        }
        //return bytes to be sent
        return bytes;
    }
    private int ByteToRegionID(byte[] b){
        ByteBuffer bb = ByteBuffer.allocate(4);

        for(int i=0;i<4;i++){bb.put(b[i]);}//set selected data
        bb.rewind();
        int s = bb.getInt();
        return s;
    }







    /*
    * GAME LOGIC SECTION. Methods that implement the game's rules.
    */
    GameModel mModel;
    private void initialiseModel(List<SVGtoRegionParser.Region> r, List<Participant> plist){
        List<String> pids = new ArrayList<>();
        int i=0;
        for(Participant p : plist){
            pids.add(p.getParticipantId());
        }
        mModel = new GameModel(r, pids);
    }

    private void nextPlayer(){
        mModel.nextPlayer();
        if(mModel.getNextphase()==true){
            mModel.nextPhase();
            infofragment.setPhase(mModel.getCurrentphase());
        }
        infofragment.setColour(mModel.getCurrentplayer().getColour(), mModel.getCurrentplayer().getColourstring());
    }


    //CLICK INTERPRETATION, BIG PART OF CONTROLLER
    public void regionClicked(int id) {
        mModel.getPlayer(mMyId).setSelectedregionid(id);
        sendMySelectionData();
        updateClickedRegions();
        if(mModel.getCurrentplayer()==mModel.getPlayer(mMyId)){//Players can only interact with the phase if it is their turn
            switch (mModel.getCurrentphaseString()){

                case "Mountain":
                    if(mModel.getRegion(id).getType().equals("mountain") && mModel.getRegion(id).isOwned()==false){
                        showDialogFragment(1, "Confirm selection of mountain: '" + mModel.getRegion(id).getName() + "'");//Dialog 1 is mountain dialog
                        dialogfragment.setRegionid(id);
                    }
                    break;

                case "Bombing":
                    break;

                case "Reinforcement":
                    allocateReinforcementsToCurrentPlayer();
                    for(Empire e : mModel.getCurrentplayer().getEmpires()){
                        if(mModel.getRegion(id).getEmpire() == e){
                            showDialogFragment(1, "Confirm selection of mountain: '" + mModel.getRegion(id).getName() + "'");//Dialog 1 is mountain dialog
                            dialogfragment.setRegionid(id);
                        }
                    }
                    break;

                case "Attack":
                    break;

            }
        }
    }
    //Long touch logged
    public void regionLongPressed(int id) {
        mModel.getPlayer(mMyId).setSelectedregionid(id);
        sendMySelectionData();
        updateClickedRegions();
        if(mModel.getRegion(id).getEmpire()!=null){
            openInspectDialog(id);
        }
    }

    private void updateClickedRegions(){
        for(Player p : mModel.getPlayers()){
            if(p.getPrevselectedregionid()!=-1){
                mapfragment.deselectRegion(p.getPrevselectedregionid());
            }
            if (p.getSelectedregionid()!=-1) {
                mapfragment.selectRegion(p.getSelectedregionid(), mModel.getParticipantColour(p.getParticipantid()));
            }

        }
    }

    private void openInspectDialog(int id){
        Region r = mModel.getRegion(id);
        int a=0;
        String b="None";
        if(r.getArmy()!=null){
            a = r.getArmy().getSize();
        }
        if(r.getBomb()!=null){
            b = "Level" + r.getBomb().getSize()+r.getBomb().getTypeString();
        }

    }


    //MOUNTAIN SELECTION PHASE
    public void mountainSelected(int id){
        removeDialogFragment();
        boolean check=true;
        for (Region r : getRegionAdjacentRegions(id)){
            if(r.isOwned()==true && check==true){//Adjacent mountain has been picked already, give error and rollback
                showDialogFragment(2, "Cannot select mountain: '" + mModel.getRegion(id).getName() + "'. Adjacent mountain: '" + r.getName() + "' has already been selected.");//Dialog 2 is basic dialog
                check=false;
            }
        }
        if(check==true){//Allowed mountain
            reduceMountainCountLocal(id);
            sendMountainCountReduction(id);//Dont add this to reducemountaincount or there will be a send/receive loop
            if(checkRemainingMountains()==true){
                mModel.getCurrentplayer().newEmpire(mModel.getRegion(id));
                addRegiontoEmpireinView(id);
                nextPlayer();//Mountain selection complete, move to next player
                sendRegionUpdate(0, id);//This should only be reached by device owner
            } else {//If there aren't enough mountains to go around after this selection
                moveToReinforcement(); //Rollback mountain selections and move to next phase (reinforcement)
                sendMoveToReinforcementPrompt(); //Tell everyone else too aswell
            }
        }
    }


    private void reduceMountainCountLocal(int id){
        int x=0;//1 for mountain passed
        mModel.setRemainingmountaincount(-1);
        mModel.getRegion(id).setCounted(true);
        for (Region r : mModel.getRegion(id).getAdjacentregions()){
            if(r.getType().equals("mountain")&& r.getCounted()==false){
                x++;
                r.setCounted(true);
            }
        }
        mModel.setRemainingmountaincount(-x);
    }
    private boolean checkRemainingMountains(){
        if(mModel.getRemainingmountaincount()/mModel.getPlayers().size()<1){
            //Not enough mountains left to go around
            return false;
        } else {
            return true;
        }
    }

    private void moveToReinforcement(){//Roll back mountains and end mountain selection phase
        showMoveToReinforcementDialog();
        for(Player p : mModel.getPlayers()){//Cycle through the players
            if(p == mModel.getCurrentplayer()){//Don't take anyone's region that has not completed this round
                break;
            } else {
                String name = p.getEmpires().get(p.getEmpires().size()-1).getRegions().get(0).getName();//Find name of first(only) region in last empire taken by player
                int id = mModel.getRegionIDByName(name);
                mapfragment.getRegion(id).setUseGradient(false, null);//Set outline back to black again
                p.getEmpires().remove(p.getEmpires().size()-1);//Remove last empire
            }
        }
        mapfragment.reRender();
        mModel.nextPhase();
        infofragment.setColour(mModel.getCurrentplayer().getColour(), mModel.getCurrentplayer().getColourstring());
        infofragment.setPhase(mModel.getCurrentphase());
        allocateReinforcementsToCurrentPlayer();
    }

    private void showMoveToReinforcementDialog() {
        showDialogFragment(2, "More players than mountains remaining, rolling back most this round's selections and moving to reinforcement phase...");//Dialog 2 is basic dialog
    }


    //REINFORCEMENT PHASE
    private void allocateReinforcementsToCurrentPlayer(){
        for(Empire e : mModel.getCurrentplayer().getEmpires()){
            e.Reinforce();
        }
    }

    public void reinforceRegion(int id){

    }


    private void addRegiontoEmpireinView(int id) {
        mapfragment.getRegion(id).setUseGradient(true, mModel.getCurrentplayer().getColour());
        mapfragment.reRender();
        DeselectForCurrentPlayer();
    }

    public void DeselectForCurrentPlayer(){
        mapfragment.deselectRegion(mModel.getCurrentplayer().getSelectedregionid());
        mModel.getCurrentplayer().setSelectedregionid(-1);
        mModel.getCurrentplayer().setPrevselectedregionid(-1);
    }

    public List<Region> getRegionAdjacentRegions(int id){
        return mModel.getRegion(id).getAdjacentregions();
    }
    //TODO: Finish construction of inspection fragment, finish allocation of reinforcements for current player
    //TODO: Implement allocateReinforcementsToCurrentPlayer & onClick for reinforcement phase

}