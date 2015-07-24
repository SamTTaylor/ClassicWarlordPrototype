package samueltaylor.classicwarlordprototype;

/**
 * Created by Sam on 03/05/2015.
 */
        import android.app.Activity;
        import android.app.Fragment;
        import android.app.FragmentManager;
        import android.app.FragmentTransaction;
        import android.content.Context;
        import android.content.Intent;
        import android.net.Uri;
        import android.os.Bundle;
        import android.os.PowerManager;
        import android.support.v4.app.FragmentActivity;
        import android.util.Log;
        import android.view.KeyEvent;
        import android.view.View;
        import android.view.WindowManager;
        import android.view.inputmethod.InputMethodManager;

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
        import java.io.FileNotFoundException;
        import java.io.IOException;
        import java.io.InputStream;
        import java.nio.ByteBuffer;
        import java.util.ArrayList;
        import java.util.HashMap;
        import java.util.HashSet;
        import java.util.List;
        import java.util.Map;
        import java.util.Random;
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
        import samueltaylor.classicwarlordprototype.Model.Bomb;
        import samueltaylor.classicwarlordprototype.Model.Empire;
        import samueltaylor.classicwarlordprototype.Model.GameModel;
        import samueltaylor.classicwarlordprototype.Model.Player;
        import samueltaylor.classicwarlordprototype.Model.Region;
        import samueltaylor.classicwarlordprototype.XMLParsing.SVGtoRegionParser;



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

        if (!mGoogleApiClient.isConnected()) {
            signedin = false;
        }
        else {
            signedin = true;
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

    public void inviteToExisting(int playerpos) {
        Log.e("InviteToExisting", String.valueOf(playerpos));
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
        Log.e(TAG, "Sign-out button clicked");
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
            Log.e(TAG, "*** Warning: setup problems detected. Sign in may not work!");
        }

        // start the sign-in flow
        Log.e(TAG, "Sign-in button clicked");
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
                    Log.e(TAG, "Starting game (waiting room returned OK).");
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
                Log.e(TAG, "onActivityResult with requestCode == RC_SIGN_IN, responseCode="
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
            Log.e(TAG, "*** select players UI cancelled, " + response);
            loadMainMenu();
            return;
        }

        Log.e(TAG, "Select players UI succeeded.");

        // get the invitee list
        final ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
        Log.e(TAG, "Invitee count: " + invitees.size());

        // get the automatch criteria
        Bundle autoMatchCriteria = null;
        int minAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
        int maxAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
        if (minAutoMatchPlayers > 0 || maxAutoMatchPlayers > 0) {
            autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                    minAutoMatchPlayers, maxAutoMatchPlayers, 0);
            Log.e(TAG, "Automatch criteria: " + autoMatchCriteria);
        }

        // create the room
        Log.e(TAG, "Creating room...");
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
        Log.e(TAG, "Room created, waiting for it to be ready...");
    }

    // Handle the result of the invitation inbox UI, where the player can pick an invitation
    // to accept. We react by accepting the selected invitation, if any.
    private void handleInvitationInboxResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            Log.e(TAG, "*** invitation inbox UI cancelled, " + response);
            loadMainMenu();
            return;
        }

        Log.e(TAG, "Invitation inbox UI succeeded.");
        Invitation inv = data.getExtras().getParcelable(Multiplayer.EXTRA_INVITATION);

        // accept invitation
        acceptInviteToRoom(inv.getInvitationId());
    }

    // Accept the given invitation.
    void acceptInviteToRoom(String invId) {
        // accept the invitation
        Log.e(TAG, "Accepting invitation: " + invId);
        RoomConfig.Builder roomConfigBuilder = RoomConfig.builder(this);
        roomConfigBuilder.setInvitationIdToAccept(invId)
                .setMessageReceivedListener(this)
                .setRoomStatusUpdateListener(this);
        //show loading fragment
        showLoadingFragment(null);
        resetGameVars();
        Games.RealTimeMultiplayer.join(mGoogleApiClient, roomConfigBuilder.build());
    }

    // Activity is going to the background
    @Override
    public void onStop() {
        Log.e(TAG, "**** got onStop");
        super.onStop();
    }

    // Activity is going to the background.
    @Override
    public void onPause() {
        Log.e(TAG, "**** got onPause");
        super.onPause();
    }

    // Activity is going to be destroyed
    @Override
    public void onDestroy() {
        updateChat("Has left the game.");
        Log.e(TAG, "**** got onDestroy");
        leaveRoom();
        super.onDestroy();
    }

    // Activity just got to the foreground. We switch to the wait screen because we will now
    // go through the sign-in flow (remember that, yes, every time the Activity comes back to the
    // foreground we go through the sign-in flow -- but if the user is already authenticated,
    // this flow simply succeeds and is imperceptible).
    @Override
    public void onStart() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Log.e(TAG,
                    "GameHelper: client was already connected on onStart()");
        } else {
            Log.e(TAG,"Connecting client.");
            mGoogleApiClient.connect();
        }
        super.onStart();
    }


    @Override
    public void onResume(){
        if(defensePrompted){
            showDialogFragment(9,"'"+mModel.getRegion(defenceinfomation[0]).getName()+"'\nHas been attacked from\n'"+mModel.getRegion(defenceinfomation[1]).getName()+"'\nBy "+mModel.getRegion(defenceinfomation[1]).getArmy().getPlayer().getColourstring()+" player\nMake defensive guess ("+(defenceinfomation[3]-defenceinfomation[4])+" remaining):", getAttackLimitations(defenceinfomation[0], defenceinfomation[1])[0],getAttackLimitations(defenceinfomation[0], defenceinfomation[1])[1]);
        }
        super.onResume();
    }

    // Handle back key to make sure we cleanly leave a game if we are in the middle of one
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent e) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (inRoom()) {
                    leaveRoom();
                } else {
                    this.finish();
                    System.exit(0);
                }
                break;
        }
        return true;
    }

    public void HideKeyboard(){
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            imfragment.btnSendIMClick();
        }
    }

    // Leave the room.
    void leaveRoom() {
        if (mRoomId != null) {
            Log.e(TAG, "Leaving room.");
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
        Log.e(TAG, "Invite Received.");
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
        Log.e(TAG, "onConnected() called. Sign in successful!");

        Log.e(TAG, "Sign-in succeeded.");

        // register listener so we are notified if we receive an invitation to play
        // while we are in the game
        Games.Invitations.registerInvitationListener(mGoogleApiClient, this);

        if (connectionHint != null) {
            Log.e(TAG, "onConnected: connection hint provided. Checking for invite.");
            Invitation inv = connectionHint
                    .getParcelable(Multiplayer.EXTRA_INVITATION);
            if (inv != null && inv.getInvitationId() != null) {
                // retrieve and cache the invitation ID
                Log.e(TAG,"onConnected: connection hint has a room invite!");
                acceptInviteToRoom(inv.getInvitationId());
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "onConnectionSuspended() called. Trying to reconnect.");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed() called, result: " + connectionResult);

        if (mResolvingConnectionFailure) {
            Log.e(TAG, "onConnectionFailed() ignoring connection failure; already resolving.");
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
        Log.e(TAG, "onConnectedToRoom.");

        // get room ID, participants and my ID:
        mRoomId = room.getRoomId();
        mParticipants = room.getParticipants();
        mMyId = room.getParticipantId(Games.Players.getCurrentPlayerId(mGoogleApiClient));

        // print out the list of participants (for debug purposes)
        Log.e(TAG, "Room ID: " + mRoomId);
        Log.e(TAG, "My ID " + mMyId);
        Log.e(TAG, "<< CONNECTED TO ROOM>>");
    }

    // Called when we've successfully left the room (this happens a result of voluntarily leaving
    // via a call to leaveRoom(). If we get disconnected, we get onDisconnectedFromRoom()).
    @Override
    public void onLeftRoom(int statusCode, String roomId) {
        // we have left the room; return to main screen.
        Log.e(TAG, "onLeftRoom, code " + statusCode);
        loadMainMenu();
        mRoomId = null;
    }

    // Called when we get disconnected from the room. We return to the main screen.
    @Override
    public void onDisconnectedFromRoom(Room room) {
        Log.e("onDisconnectedFromRoom", "Disconnected from room");
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
        Log.e(TAG, "onRoomCreated(" + statusCode + ", " + room + ")");
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
        Log.e(TAG, "onRoomConnected(" + statusCode + ", " + room + ")");
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
            showGameError();
            return;
        }
        updateRoom(room);
    }

    @Override
    public void onJoinedRoom(int statusCode, Room room) {
        Log.e(TAG, "onJoinedRoom(" + statusCode + ", " + room + ")");
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
    public void onP2PDisconnected(String participant) {
        Log.e("onP2PDisconnected", "Player: " + participant);
    }

    @Override
    public void onP2PConnected(String participant) {}

    @Override
    public void onPeerJoined(Room room, List<String> arg1) {updateRoom(room);}

    @Override
    public void onPeerLeft(Room room, List<String> peersWhoLeft) {
        Log.e("onPeerLeft", "Player left: " +peersWhoLeft.get(0));
        disconnectPlayers(peersWhoLeft);
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
        Log.e("onPeersDisconnected", "Player DC: " +peers.get(0));
        disconnectPlayers(peers);
        updateRoom(room);
    }

    void updateRoom(Room room) {
        if (room != null) {
            mParticipants = room.getParticipants();
        }

    }

    void disconnectPlayers(List<String> players){
        for(Participant p : mParticipants){
            if(mModel.getPlayer(p.getParticipantId()).isConnected()){
                for(String s : players){
                    if(s.equals(p.getParticipantId())){
                        mModel.getPlayer(p.getParticipantId()).setConnected(false);
                        imfragment.appendChat(p.getDisplayName() + " disconnected.");
                        hudfragment.disconnectPlayer(p.getDisplayName());
                        if(mModel.getCurrentplayer()==mModel.getPlayer(p.getParticipantId())){
                            switch (mModel.getCurrentphase()){
                                case 0:
                                    nextPlayer();
                                    break;
                                default:
                                    nextPhase();
                                    break;
                            }

                        }
                    }
                }
            }
        }
    }

    /*
     * GAME CONSTRUCTION - Fragments, OpenGL Initialisation etc..
     */


    //Check if we are currently in game
    boolean inRoom(){
        return mRoomId != null;
    }

    // Reset game variables in preparation for a new game.
    void resetGameVars() {
        mParticipantChoice.clear();
        mFinishedParticipants.clear();
    }

    // Start the gameplay phase of the game.
    void startGame(boolean multiplayer) throws InterruptedException {
        mMultiplayer = multiplayer;
        //Show game related fragments
        loadGame();
        // prevent screen from sleeping
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // update the names on screen from room
        updatePlayers();
    }




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
    fragInspect inspectfragment;


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

    public void showDialogFragment(int type, String s, int max, int min){
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Fragment f = manager.findFragmentByTag("alert");
        if(f!=null){
            transaction.remove(f);
            transaction.commit();
        }
        dialogfragment = new fragDialog();
        dialogfragment.setMessage(s);
        dialogfragment.setType(type);
        dialogfragment.setMax(max);
        dialogfragment.setMin(min);
        transaction = manager.beginTransaction();
        transaction.add(R.id.activity_main_layout, dialogfragment, "alert");
        transaction.commit();
    }
    public void removeDialogFragment(){
        if(dialogfragment.isVisible()){
            FragmentManager manager = getFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.remove(dialogfragment);
            transaction.commit();
        }
    }

    private void showInspectFragment(int id){
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        inspectfragment = new fragInspect();
        inspectfragment.setRegion(mModel.getRegion(id));
        transaction.add(R.id.activity_main_layout, inspectfragment, "inspect");
        transaction.commit();
    }

    public void removeInspectFragment(){
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.remove(inspectfragment);
        transaction.commit();
    }

    public void removeInfoFragment(){
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.remove(infofragment);
        transaction.commit();
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
    Map<String, Integer> mParticipantChoice = new HashMap<>();

    // Participants who sent us their final score.
    Set<String> mFinishedParticipants = new HashSet<>();

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
                System.arraycopy(buf, 1, b, 0, b.length);
                int s= ByteToRegionID(b);
                mModel.getPlayer(rtm.getSenderParticipantId()).setSelectedregionid(s);

                b = new byte[4];
                System.arraycopy(buf, 5, b, 0, b.length);
                s= ByteToRegionID(b);
                mModel.getPlayer(rtm.getSenderParticipantId()).setPrevselectedregionid(s);

                updateClickedRegions();//Update view
                break;
            case 'N'://New empire for current player sendRegionUpdate(), used during mountain phase
                b = new byte[4];
                System.arraycopy(buf, 1, b, 0, b.length);
                int e = ByteToRegionID(b);
                mModel.getCurrentplayer().newEmpire(mModel.getRegion(e),1);
                addRegiontoEmpireinView(e);
                nextPlayer();
                if(!checkRemainingMountains() || mModel.getRemainingmountaincount() == 0) {//If there aren't enough mountains to go around after this selection
                    moveToReinforcement(); //Rollback mountain selections and move to next phase (reinforcement)
                }
                break;
            case 'C'://Mountain count reduction sendMountainCountReduction()
                b = new byte[4];
                System.arraycopy(buf, 1, b, 0, b.length);
                e = ByteToRegionID(b);//Get the ID of centre mountain
                mModel.getRegion(e).setCounted(true);
                mModel.setRemainingmountaincount(-1);//Reduce remaining mountain count by 1
                for(int i=5;i<buf.length;i+=4){//For each remaining adjacent mountain
                    b[0]=buf[i];b[1]=buf[i+1];b[2]=buf[i+2];b[3]=buf[i+3];//Convert it to byte[4]
                    int x=ByteToRegionID(b);//Get id from byte
                    if(!mModel.getRegion(x).getCounted()){
                        mModel.setRemainingmountaincount(-1);//Reduce remaining mountain count by 1
                        mModel.getRegion(x).setCounted(true);//Set counted to true
                    }
                }
                break;
            case 'R'://sendMoveToReinforcementPrompt()
                moveToReinforcement();
                break;
            case 'U'://Reinforcement update sendRegionUpdate(1,id)
                b = new byte[4];
                System.arraycopy(buf, 1, b, 0, b.length);
                s= ByteToRegionID(b);

                b = new byte[4];
                System.arraycopy(buf, 5, b, 0, b.length);
                int i= ByteToRegionID(b);//Allocated Forces
                mModel.getRegion(s).getArmy().setSize(i);//Set size rather than increment due to difficulties discerning negative numbers
                break;
            case 'P'://sendNextPhasePrompt()
                nextPhase();
                break;
            case 'T'://New region for current player sendRegionUpdate(2, id)
                b = new byte[4];
                System.arraycopy(buf, 1, b, 0, b.length);
                i= ByteToRegionID(b);//Allocated Forces
                takeRegionForCurrentPlayer(i,mModel.getCurrentplayer().getSelectedregionid(),mModel.getCurrentplayer().getPrevselectedregionid(), true);
                break;
            case 'W'://Move army within empire sendRegionUpdate(3, pledge)
                b = new byte[4];
                System.arraycopy(buf, 1, b, 0, b.length);
                int sel = ByteToRegionID(b);//Selected Region ID
                b = new byte[4];
                System.arraycopy(buf, 5, b, 0, b.length);
                int prev = ByteToRegionID(b);//Prev Selected Region ID
                b = new byte[4];
                System.arraycopy(buf, 9, b, 0, b.length);
                i= ByteToRegionID(b);//Pledged Forces
                moveArmyInsideEmpire(sel,prev,i);
                break;
            case 'D'://Received prompt to defend a region sendDefencePrompt
                defenceinfomation = new int[5];
                //0: sel 1: prev 2: pledge 3: guesses 4: guesses made
                b = new byte[4];
                System.arraycopy(buf, 1, b, 0, b.length);
                defenceinfomation[0] = ByteToRegionID(b);//Selected Region ID
                b = new byte[4];
                System.arraycopy(buf, 5, b, 0, b.length);
                defenceinfomation[1] = ByteToRegionID(b);//Prev Selected Region ID
                b = new byte[4];
                System.arraycopy(buf, 9, b, 0, b.length);
                defenceinfomation[2] = ByteToRegionID(b);//Pledged Forces
                switch (buf[13]){//find number of guesses from byte
                    case '1':
                        defenceinfomation[3]=1;
                        break;
                    case '2':
                        defenceinfomation[3]=2;
                        break;
                    default:
                        defenceinfomation[3]=1;
                        break;
                }
                defenceinfomation[4]=0;//Number of guesses so far
                defensePrompted=true;
                showDialogFragment(9,"'"+mModel.getRegion(defenceinfomation[0]).getName()+"'\nHas been attacked from\n'"+mModel.getRegion(defenceinfomation[1]).getName()+"'\nBy "+mModel.getPlayer(rtm.getSenderParticipantId()).getColourstring()+" player\nMake defensive guess ("+(defenceinfomation[3]-defenceinfomation[4])+" remaining):", getAttackLimitations(defenceinfomation[0], defenceinfomation[1])[0],getAttackLimitations(defenceinfomation[0], defenceinfomation[1])[1]);
                break;
            case 'O'://sendDefenceConclusion, everyone receives the result of the attack
                waitingfordefenceresponse=false;
                b = new byte[4];
                System.arraycopy(buf, 1, b, 0, b.length);
                sel = ByteToRegionID(b);//Selected Region ID
                b = new byte[4];
                System.arraycopy(buf, 5, b, 0, b.length);
                prev = ByteToRegionID(b);//Prev Selected Region ID
                b = new byte[4];
                System.arraycopy(buf, 9, b, 0, b.length);
                i = ByteToRegionID(b);//Pledged Forces
                boolean defwins;
                if(buf[13]=='D'){defwins=true;}else{defwins=false;}
                resolveAttack(prev,sel,i,defwins);
                break;
            case 'B'://sendBombPlacement, everyone is told where a bomb is placed
                b = new byte[4];
                System.arraycopy(buf, 1, b, 0, b.length);
                sel = ByteToRegionID(b);//Selected Region ID
                b = new byte[4];
                System.arraycopy(buf, 5, b, 0, b.length);
                i = ByteToRegionID(b);//Bomb Type
                mModel.getCurrentplayer().allocateBomb(mModel.getRegion(sel), i);
                break;
            case 'F':
                b = new byte[4];
                System.arraycopy(buf, 1, b, 0, b.length);
                prev = ByteToRegionID(b);//Selected Region ID
                b = new byte[4];
                System.arraycopy(buf, 5, b, 0, b.length);
                sel = ByteToRegionID(b);//Bomb Type
                fireBomb(prev,sel);
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
        byte[] b = intToByte(s);
        byte[] bytes = new byte[9];
        System.arraycopy(b, 0, bytes, 1, b.length);
        //Label message as selection data & add the ints to the array
        bytes[0] = 'S';


        b = intToByte(p);
        System.arraycopy(b, 0, bytes, 5, b.length);
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
                byte[] b = intToByte(regionid);
                byte[] bytes = new byte[6];
                System.arraycopy(b, 0, bytes, 1, b.length);
                bytes[0] = 'N';//Label as new empire
                //send it
                for(Participant p : mParticipants){
                    if(mRoomId!=null) {
                        Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, bytes, mRoomId, p.getParticipantId());
                    }
                }
                break;
            case 1: //Reinforcement update
                // Buffer ints as bytes
                b = intToByte(regionid);
                bytes = new byte[9];
                System.arraycopy(b, 0, bytes, 1, b.length);//Package region id
                b = intToByte(mModel.getRegion(regionid).getArmy().getSize());//Converting int to ID using regionID method
                System.arraycopy(b, 0, bytes, 5, b.length);//Package new army size
                bytes[0] = 'U';//Label as reinforcement update
                //send it
                for(Participant p : mParticipants){
                    if(mRoomId!=null) {
                        Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, bytes, mRoomId, p.getParticipantId());
                    }
                }
                break;
            case 2://New region for current player
                // Buffer ints as bytes
                bytes = new byte[5];
                b = intToByte(mModel.getRegion(regionid).getArmy().getSize());//Converting int to ID using regionID method
                System.arraycopy(b, 0, bytes, 1, b.length);//Package new army size
                bytes[0] = 'T';//Label as new region
                //send it
                for(Participant p : mParticipants){
                    if(mRoomId!=null) {
                        Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, bytes, mRoomId, p.getParticipantId());
                    }
                }
                break;
            case 3://Move army within empire
                // Buffer ints as bytes
                bytes = new byte[13];
                b = intToByte(mModel.getCurrentplayer().getSelectedregionid());//Copy selected region id into bytes
                System.arraycopy(b, 0, bytes, 1, b.length);
                b = intToByte(mModel.getCurrentplayer().getPrevselectedregionid());//Copy prev selected region id into bytes
                System.arraycopy(b, 0, bytes, 5, b.length);
                b = intToByte(regionid);//Copy regionid into bytes ***NOTE***regionid is actually the amount of troops moving, its not a region id
                System.arraycopy(b, 0, bytes, 9, b.length);//Package pledge size
                bytes[0] = 'W';//W for Within empire, or, We're running out of labels
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
        byte[] bytes = intToByte(id);
        for(byte b : bytes){
            regionsasbytes.add(b);
        }

        for(Region r : mModel.getRegion(id).getAdjacentregions()){//Package all adjacent mountain IDs, had to be done client side as cannot trust destination to have adjacent regions yet
            if(r.getType().equals("mountain")){
                bytes = intToByte(mModel.getRegionIDByName(r.getName()));
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

    private void sendNextPhasePrompt(){
        byte[] bytes = new byte[1];
        bytes[0] = 'P';//Label as nextPhase prompt
        //Send!
        for(Participant p : mParticipants){
            if(mRoomId!=null) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, bytes, mRoomId, p.getParticipantId());
            }
        }
    }

    private void sendDefencePrompt(int prev, int id, int pledge, int guesses){
        // Buffer ints as bytes
        byte guesseschar;
        defenceinfomation = new int[5];
        switch(guesses){
            case 2:
                guesseschar='2';
                break;
            default:
                guesseschar='1';
                break;
        }
        byte[] bytes = new byte[14];
        byte[] b = intToByte(id);//Copy selected region id into bytes
        System.arraycopy(b, 0, bytes, 1, b.length);//
        b = intToByte(prev);//Copy prev selected region id into bytes
        System.arraycopy(b, 0, bytes, 5, b.length);//
        b = intToByte(pledge);//Copy pledge into bytes
        System.arraycopy(b, 0, bytes, 9, b.length);//Package pledge size
        bytes[0] = 'D';//Defence label
        bytes[13]= guesseschar;
        //send it
        for(Participant p : mParticipants){
            if(mRoomId!=null) {
                if(p.getParticipantId().equals(mModel.getRegion(id).getArmy().getPlayer().getParticipantid())){//Send defence prompt only to owner of defending region
                    Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, bytes, mRoomId, p.getParticipantId());
                }
            }
        }
    }

    private void sendDefenceConclusion(boolean defendervictory){//Tell everyone else about the how the defence went
        byte[] bytes = new byte[14];
        byte[] b = intToByte(defenceinfomation[0]);//Copy target region id into bytes
        System.arraycopy(b, 0, bytes, 1, b.length);//
        b = intToByte(defenceinfomation[1]);//Copy source region id into bytes
        System.arraycopy(b, 0, bytes, 5, b.length);//
        b = intToByte(defenceinfomation[2]);//Copy pledge into bytes
        System.arraycopy(b, 0, bytes, 9, b.length);//Package pledge size
        bytes[0] = 'O';//defencecOonclusion
        if(defendervictory){bytes[13]='D';}else{bytes[13]='A';}
        for(Participant p : mParticipants){
            if(mRoomId!=null) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, bytes, mRoomId, p.getParticipantId());
            }
        }
    }

    //Bomb placed in region
    private void sendBombPlacement(int selectedid, int bombtype){
        byte[] bytes = new byte[9];
        byte[] b = intToByte(selectedid);//Copy target region id into bytes
        System.arraycopy(b, 0, bytes, 1, b.length);//
        b = intToByte(bombtype);//Copy bomb type into bytes
        System.arraycopy(b, 0, bytes, 5, b.length);//
        bytes[0] = 'B';//Bomb placement
        for(Participant p : mParticipants){
            if(mRoomId!=null) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, bytes, mRoomId, p.getParticipantId());
            }
        }
    }

    private void sendFireBomb(int sourceid, int targetid){
        byte[] bytes = new byte[9];
        byte[] b = intToByte(sourceid);//Copy source region id into bytes
        System.arraycopy(b, 0, bytes, 1, b.length);//
        b = intToByte(targetid);//Copy target region id into bytes
        System.arraycopy(b, 0, bytes, 5, b.length);//
        bytes[0] = 'F';//Bomb Firing
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
    private byte[] intToByte(int id){
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
        return bb.getInt();
    }










    /*
    * GAME LOGIC SECTION. Methods that implement the game's rules.
    */
    GameModel mModel;
    private boolean firstclick = true;
    private int abombfromregion=-1;
    private List<Region> pastempire;
    private boolean victory=false;
    private boolean defensePrompted=false;

    private void initialiseModel(List<SVGtoRegionParser.Region> r, List<Participant> plist){
        List<String> pids = new ArrayList<>();
        for(Participant p : plist){
            pids.add(p.getParticipantId());
        }
        mModel = new GameModel(r, pids);
    }

    private void nextPhase(){//Not called on initial movement to reinforcement phase, see moveToReinforcement()
        mModel.nextPhase();
        infofragment.setPhase(mModel.getCurrentphase());
        infofragment.setColour(mModel.getCurrentplayer().getColour(), mModel.getCurrentplayer().getColourstring());

        if(iAmCurrentPlayer() && mModel.getCurrentphase()!=0){//If its my turn and its not the mountain phase, show end turn button
            infofragment.setBtnEndTurnVisibility(true);
        } else {
            infofragment.setBtnEndTurnVisibility(false);
        }

        switch (mModel.getCurrentphase()){
            case 0://Mountain
                break;
            case 1://Bombing
                subphase=0;
                break;
            case 2://Reinforcement
                if(iAmCurrentPlayer()){
                    allocateReinforcementsToCurrentPlayer();
                }
                break;
            case 3://Attack/move
                break;

        }

    }

    private void nextPlayer(){//Used for Mountain selection phase only, see moveToReinforcement()
        mModel.nextPlayer();
        infofragment.setColour(mModel.getCurrentplayer().getColour(), mModel.getCurrentplayer().getColourstring());
    }

    //End Turn
    public void endTurn(boolean confirmed){
        switch (mModel.getCurrentphaseString()){
            case "Reinforcement":
                boolean reinforcementsused=true;
                if(!confirmed){
                    for(Empire e : mModel.getCurrentplayer().getEmpires()){
                        if(e.getUnallocatedforces()>0){
                            reinforcementsused=false;
                        }
                    }
                }
                if(!reinforcementsused && !confirmed){//If confirmed is passed as true (by the confirmation dialog) then reinforcementsused=false does not matter
                    showDialogFragment(4, "You have unallocated reinforcements, are you sure you wish to end this phase?",0,0);//Confirmation Dialog);
                } else {
                    nextPhase();
                    sendNextPhasePrompt();
                }
                break;
            default:
                if(!confirmed){
                    showDialogFragment(4, "Are you sure you wish to end this phase?",0,0);//Confirmation Dialog);
                } else {
                    nextPhase();
                    sendNextPhasePrompt();
                }
                break;

        }
    }

    //PHASE BASED CLICK INTERPRETATION
    public void regionClicked(int id) {
        if(id<mModel.getWorld().size()){
            mModel.getPlayer(mMyId).setSelectedregionid(id);
            sendMySelectionData();
            updateClickedRegions();
            if(mModel.getCurrentplayer()==mModel.getPlayer(mMyId) && !victory){//Players can only interact with the phase if it is their turn and noone has won
                switch (mModel.getCurrentphaseString()){
                    case "Mountain":
                        handleMountainClick(id);
                        break;
                    case "Bombing":
                        handleBombingClick(id);
                        break;
                    case "Reinforcement":
                        handleReinforcementClick(id);
                        break;
                    case "Attack":
                        handleAttackMoveClick(id);
                        break;
                }
            }
        }
    }
    //Long touch logged
    public void regionLongPressed(int id) {
        mModel.getPlayer(mMyId).setSelectedregionid(id);
        sendMySelectionData();
        updateClickedRegions();
        showInspectFragment(id);
    }


    public List<Region> getRegionAdjacentRegions(int id) {
        return mModel.getRegion(id).getAdjacentregions();
    }

    boolean iAmCurrentPlayer(){
        if(mModel.getCurrentplayer().getParticipantid().equals(mMyId)){
            return true;
        }
        return false;
    }

    private void checkVictory(){
        Player p = mModel.checkVictor();
        if(p!=null){
            removeInfoFragment();
            if(dialogfragment.isVisible()){
                removeDialogFragment();
            }
            showDialogFragment(2, p.getColourstring() + " Player is victorious.", 0, 0);//Dialog 2 is basic dialog
            victory=true;
        }
    }



    //MOUNTAIN SELECTION PHASE
    private void handleMountainClick(int id){
        if(mModel.getRegion(id).getType().equals("mountain") && !mModel.getRegion(id).isOwned()) {
            showDialogFragment(1, "Confirm selection of mountain: '" + mModel.getRegion(id).getName() + "'", 0, 0);//Confirmation Dialog
            dialogfragment.setRegionid(id);
        }
    }

    public void mountainSelected(int id){
        removeDialogFragment();
        boolean check=true;
        for (Region r : getRegionAdjacentRegions(id)){
            if(r.isOwned() && check){//Adjacent mountain has been picked already, give error and rollback
                showDialogFragment(2, "Cannot select mountain: '" + mModel.getRegion(id).getName() + "'. Adjacent mountain: '" + r.getName() + "' has already been selected.",0,0);//Dialog 2 is basic dialog
                check=false;
            }
        }
        if(check){//Allowed mountain
            reduceMountainCountLocal(id);
            sendMountainCountReduction(id);//Dont add this to reducemountaincount or there will be a send/receive loop
            mModel.getCurrentplayer().newEmpire(mModel.getRegion(id),1);
            addRegiontoEmpireinView(id);
            nextPlayer();//Mountain selection complete, move to next player
            sendRegionUpdate(0, id);
            if(!checkRemainingMountains() || mModel.getRemainingmountaincount() == 0) {//If there aren't enough mountains to go around after this selection
                moveToReinforcement(); //Rollback mountain selections and move to next phase (reinforcement)
            }
        }
    }


    private void reduceMountainCountLocal(int id){
        int x=0;//1 for mountain passed
        mModel.setRemainingmountaincount(-1);
        mModel.getRegion(id).setCounted(true);
        for (Region r : mModel.getRegion(id).getAdjacentregions()){
            if(r.getType().equals("mountain")&& !r.getCounted()){
                x++;
                r.setCounted(true);
            }
        }
        mModel.setRemainingmountaincount(-x);
    }
    private boolean checkRemainingMountains(){
        int playedthisround=0;
        for(Player p : mModel.getPlayers()){
            if(p.getEmpires().size()==mModel.getCurrentplayer().getEmpires().size()+1 && p.isConnected()){//Nextplayer has already been called so everyone who has played this round has 1 more empire than the current player
                playedthisround++;
            }
        }
        return mModel.getRemainingmountaincount() >= mModel.getPlayers().size()-playedthisround;
    }

    private void moveToReinforcement(){//Roll back mountains and end mountain selection phase
        showMoveToReinforcementDialog();
        if(!evenlyDistributedMountains()) {
            for (Player p : mModel.getPlayers()) {//Cycle through the players
                if (mModel.getPlayers().indexOf(p) < mModel.getPlayers().indexOf(mModel.getCurrentplayer())) {
                    String name = p.getEmpires().get(p.getEmpires().size() - 1).getRegions().get(0).getName();//Find name of first(only) region in last empire taken by player
                    int id = mModel.getRegionIDByName(name);
                    wipeOutRegionInView(id);//Set outline back to black again
                    mModel.getRegion(id).wipeOut();
                }
            }
            mapfragment.reRender();
        }
        nextPhase();
    }

    private boolean evenlyDistributedMountains(){
        for(Player p : mModel.getPlayers()){
            if(p.getEmpires().size()!=mModel.getCurrentplayer().getEmpires().size()){
                return false;
            }
        }
        return true;
    }

    private void showMoveToReinforcementDialog() {
        showDialogFragment(2, "Not enough mountains for remaining players, rolling back any surplus selections and moving to reinforcement phase...",0,0);//Dialog 2 is basic dialog
    }







    //REINFORCEMENT PHASE
    private void handleReinforcementClick(int id){
        for(Empire e : mModel.getCurrentplayer().getEmpires()){
            if(mModel.getRegion(id).getEmpire() == e){                                                                 //Max is forces available in empire, min is -forces allocated to region this phase
                showDialogFragment(3, "Add/Remove reinforcements for '" + mModel.getRegion(id).getName() + "'. \nAllocated here this turn: "+mModel.getRegion(id).getAllocatedforces()+"\nRemaining in Empire: "+e.getUnallocatedforces(),e.getUnallocatedforces(),-mModel.getRegion(id).getAllocatedforces());//Input dialog
                dialogfragment.setRegionid(id);
            }
        }
    }

    private void allocateReinforcementsToCurrentPlayer(){
        for(Empire e : mModel.getCurrentplayer().getEmpires()){
            e.resetUnallocatedforces();
            e.Reinforce();
        }
    }

    public void reinforceRegion(int id, int amount){
        removeDialogFragment();
        //Appropriate amounts verified in fragDialog
        mModel.getRegion(id).getEmpire().adjustUnallocatedforces(-amount);
        mModel.getRegion(id).getArmy().incrementSize(amount);
        mModel.getRegion(id).adjustAllocatedforces(amount);
        sendRegionUpdate(1, id);//Reinforcement Update after each confirmation for simplicity, probably less efficient than sending them all at the end of the turn
                                //Also allows better visibility of current player's actions for other players
    }







    //ATTACK PHASE
    private boolean waitingfordefenceresponse=false;
    private int[] defenceinfomation;


    private void handleAttackMoveClick(int id){
        if(abombfromregion!=-1){//All A bombs must be placed immediately
            //Bombs must be placed in the same empire as the region that was attacked from, bear in mind that that region may now be empty due to all forces moving from it
            //Also the bomb CANNOT be placed on the target region of the attack that generated the bomb
            if(pastempire.contains(mModel.getRegion(id))){
                placeBombForRegion(id);
            } else {
                showDialogFragment(2, "Target region for bomb placement must be within the empire the source region was in prior to your attack.", 0, 0);
            }
        } else if (waitingfordefenceresponse) {
            showDialogFragment(2, "Waiting for a guess from defender for your most recent attack...", 0, 0);
        } else{
            if(prevSelectedIsMine()){
                if(regionIsNotMine(id) && regionIsAdjacentToPrev(id) && prevSelectedHasMoreThan1Army() && !regionIsScorched(id)){
                    //Moving to adjacent region that is not mine
                    if(selectedIsHostile(id)){
                        //ATTACK
                        if(!spaceForAtomBomb(mModel.getCurrentplayer().getPrevselectedregionid())){
                            showDialogFragment(2,"No space for atom bomb in this empire, therefore you cannot attack from it",0,0);
                        } else {
                            attackRegion(id, mModel.getCurrentplayer().getPrevselectedregionid());
                        }
                    } else {
                        //MOVE
                        showDialogFragment(5,"Move\nFrom: '"+mModel.getRegion(mModel.getCurrentplayer().getPrevselectedregionid()).getName()+"'\nTo: '"+mModel.getRegion(mModel.getCurrentplayer().getSelectedregionid()).getName()+"'\nSelect amount:",mModel.getRegion(mModel.getCurrentplayer().getPrevselectedregionid()).getArmy().getSize()-1,0);
                        //See fragDialog type 5for more info
                    }
                } else if (!regionIsNotMine(id) && regionIsWithinPrevSelectedEmpire(id) && mModel.getCurrentplayer().getSelectedregionid()!= -1&&mModel.getCurrentplayer().getPrevselectedregionid()!=-1){
                    //MOVE INSIDE EMPIRE
                    showDialogFragment(6,"Move\nFrom: '"+mModel.getRegion(mModel.getCurrentplayer().getPrevselectedregionid()).getName()+"'\nTo: '"+mModel.getRegion(mModel.getCurrentplayer().getSelectedregionid()).getName()+"'\nSelect amount:",mModel.getRegion(mModel.getCurrentplayer().getPrevselectedregionid()).getArmy().getSize()-1,0);
                    //See fragDialog type 6 for more info
                }

            } else {
                if(!firstclick && mModel.getCurrentplayer().getPrevselectedregionid()!=-1){
                    showDialogFragment(2, "Select one of your regions with more than 1 army as a source, then another region in the same empire, or any adjacent region as a destination", 0, 0);
                    DeselectForCurrentPlayer();
                    firstclick=true;
                } else {
                    firstclick = false;

                }
            }
        }
    }

    private void attackRegion(int id, int previd){
        if(mModel.getRegion(id).getType().equals("sea") && !mModel.getRegion(previd).getType().equals("sea")) {//Land to sea attacks are instant win
            dominateSea(id, previd);
        } else {
            int[] attacklimitations = getAttackLimitations(id, previd);//Max is 0 min is 1
            attackLand(id, previd, attacklimitations[0], attacklimitations[1]);
        }

    }

    private int[] getAttackLimitations(int id, int previd){
        Region r = mModel.getRegion(id);
        Region prev = mModel.getRegion(previd);
        int[] attacklimitations = new int[2];
        int maxlimit=6;
        int minlimit=1;
        switch(r.getType()){
            case "mountain":
                attacklimitations[0]=3;
                attacklimitations[1]=minlimit;
                break;
            case "city":
                attacklimitations[0]=maxlimit;
                attacklimitations[1]=2;
                break;
            default:
                attacklimitations[0]=maxlimit;
                attacklimitations[1]=minlimit;
                break;
        }
        if(prev.getArmy().getSize()<attacklimitations[0]){
            attacklimitations[0]=prev.getArmy().getSize();
        }
        return attacklimitations;
    }

    private void attackLand(int id, int previd, int maxattack, int minattack){
        if(mModel.getRegion(previd).getType().equals("sea")){
            //Attack from the sea (2 guesses)
            showDialogFragment(8, "Attacking from\n'"+mModel.getRegion(previd).getName()+"'\nTo\n'"+mModel.getRegion(id).getName()+"'\nSelect Pledge:",maxattack,minattack);
        } else {
            //Attack from the land, (1 guess)
            showDialogFragment(7, "Attacking from\n'"+mModel.getRegion(previd).getName()+"'\nTo\n'"+mModel.getRegion(id).getName()+"'\nSelect Pledge:",maxattack,minattack);
        }
    }

    private void dominateSea(int id, int previd){
        showDialogFragment(5, "Capture hostile Sea from:\n'" + mModel.getRegion(previd).getName() + "'\nTo\n'" + mModel.getRegion(id).getName() + "'\nSelect Pledge:", mModel.getRegion(previd).getArmy().getSize(), 0);
        abombfromregion=previd;//Automatically win 1 A Bomb
        pastempire = new ArrayList<>();
        for(Region r : mModel.getRegion(previd).getEmpire().getRegions()){
            pastempire.add(r);
        }
    }

    public void attackConfirmed(int pledge, int guesses){
        int id=mModel.getCurrentplayer().getSelectedregionid();
        int prev=mModel.getCurrentplayer().getPrevselectedregionid();
        if(mModel.getRegion(id).getArmy().getPlayer().isConnected()){//Only prompt a defence if target player is connected
            sendDefencePrompt(prev, id, pledge, guesses);
            waitingfordefenceresponse=true;
            infofragment.setBtnEndTurnVisibility(false);
            updateChat("Attacked "+ mModel.getRegion(id).getName()+" from "+mModel.getRegion(prev).getName()+"!");
            //Save the empire that was, in preparation for bomb placement
            pastempire = new ArrayList<>();
            for(Region r : mModel.getRegion(prev).getEmpire().getRegions()){
                pastempire.add(r);
            }
        } else {
            pastempire = new ArrayList<>();
            for(Region r : mModel.getRegion(prev).getEmpire().getRegions()){
                pastempire.add(r);
            }
            randomizedDefence(id, prev, pledge, guesses);
        }
    }

    private void randomizedDefence(int sel, int prev, int pledge, int guesses){
        //defenceinformation: 0: sel    1: prev     2: pledge     3: guesses    4: guesses made
        Random rand = new Random();
        int guess;
        defenceinfomation = new int[5];
        defenceinfomation[0]=sel;
        defenceinfomation[1]=prev;
        defenceinfomation[2]=pledge;
        int upperguess=0;
        if(mModel.getRegion(prev).getArmy().getSize()<getAttackLimitations(sel,prev)[0]){upperguess=mModel.getRegion(prev).getArmy().getSize();} else {
            upperguess=getAttackLimitations(sel,prev)[0];
        }
        for(int i=0;i<guesses;i++){
            guess = rand.nextInt((upperguess - getAttackLimitations(sel,prev)[1]) + 1) + getAttackLimitations(sel,prev)[1];
            Log.e("Guess", String.valueOf(guess));
            checkAIGuess(sel, prev, pledge, guess);
        }
    }

    private void checkAIGuess(int sel, int prev, int pledge, int guess){
        if(guess==pledge){
            updateChat(mModel.getCurrentplayer().getColourstring() + " attacked " + mModel.getRegion(sel).getArmy().getPlayer().getColourstring() + "(AI) with " + pledge + " pledged forces and lost!");
            sendDefenceConclusion(true);
            resolveAttack(prev, sel, pledge, true);
        } else {
            updateChat(mModel.getCurrentplayer().getColourstring() + " attacked " + mModel.getRegion(sel).getArmy().getPlayer().getColourstring() + "(AI) with " + pledge + " pledged forces and won!");
            sendDefenceConclusion(false);
            resolveAttack(prev, sel, pledge, false);
        }
    }

    public void defenceConfirmed(int guess){
        //defenceinformation: 0: sel    1: prev     2: pledge     3: guesses    4: guesses made
        defenceinfomation[4]++;//Increment guess count
        if(guess==defenceinfomation[2]){
            showDialogFragment(2, "Guess successful! Attacker loses " + defenceinfomation[2] + " pledged forces!", 0, 0);
            updateChat(mModel.getCurrentplayer().getColourstring() + " attacked " + mModel.getPlayer(mMyId).getColourstring() + " with " + defenceinfomation[2] + " pledged forces and lost!");
            sendDefenceConclusion(true);
            resolveAttack(defenceinfomation[1], defenceinfomation[0], defenceinfomation[2], true);
            defensePrompted=false;
        } else {
            if(defenceinfomation[3]>defenceinfomation[4]){
                showDialogFragment(9,"Incorrect guess, try again ("+(defenceinfomation[3]-defenceinfomation[4])+" remaining):", getAttackLimitations(defenceinfomation[0], defenceinfomation[1])[0],getAttackLimitations(defenceinfomation[0], defenceinfomation[1])[1]);
            } else {
                showDialogFragment(2,"Incorrect guess, no guesses remaining, you lose 1 from\n'"+mModel.getRegion(defenceinfomation[0]).getName()+"'",0,0);
                updateChat(mModel.getCurrentplayer().getColourstring() + " attacked " + mModel.getPlayer(mMyId).getColourstring() + " with " + defenceinfomation[2] + " pledged forces and won!");
                sendDefenceConclusion(false);
                resolveAttack(defenceinfomation[1], defenceinfomation[0], defenceinfomation[2], false);
                defensePrompted=false;
            }
        }
    }


    private void resolveAttack(int sourceid, int destid, int pledge, boolean defenderwins){
        //0: sel    1: prev    2: pledge    3: guesses    4: guesses made
        if(defenderwins){//Defender wins
            mModel.getRegion(sourceid).getArmy().incrementSize(-pledge);//Attacker loses pledged army
            if(mModel.getRegion(sourceid).getArmy().getSize()<=0){//If attacker has lost all men
                Empire e = mModel.getRegion(sourceid).getEmpire();//In preparation for SplitEmpire check
                mModel.getRegion(sourceid).wipeOut();
                wipeOutRegionInView(sourceid);
                if(e.getRegions().size()>0){
                    e.checkSplitEmpire(mModel.getRegion(sourceid));
                }
            }
            if(iAmCurrentPlayer()){
                showDialogFragment(2,"Defender guessed correctly, you lose "+String.valueOf(pledge)+" from \n'"+mModel.getRegion(sourceid).getName()+"'",0,0);
                infofragment.setBtnEndTurnVisibility(true);
            }
        } else {//Attacker wins
            mModel.getRegion(destid).getArmy().incrementSize(-1);//Defender loses 1 man
            if(iAmCurrentPlayer()){
                abombfromregion=sourceid;//Attacker receives a bomb
                showDialogFragment(2,"You have earned an atom bomb, select a region within the same empire as '"+mModel.getRegion(abombfromregion).getName() + "' was prior to the attack, to place it.",0,0);
            }
            if(mModel.getRegion(destid).getArmy().getSize()<=0){
                Empire e = mModel.getRegion(sourceid).getEmpire();//In preparation for SplitEmpire check
                mModel.getRegion(destid).wipeOut();
                takeRegionForCurrentPlayer(pledge, destid, sourceid, false);
                if(e.getRegions().size()>0){
                    e.checkSplitEmpire(mModel.getRegion(sourceid));
                }
            }
        }
    }

    private boolean spaceForAtomBomb(int id){//Check region's empire for space for a potential atom bomb
        Empire e = mModel.getRegion(id).getEmpire();
        for (Region r : e.getRegions()){
            if(r.getBomb()==null ||  r.getBomb().getTypeString().equals("A")){
                return true;
            }
        }
        return false;//No regions with space for new bomb or A bomb expansion
    }

    private boolean regionIsNotMine(int id){
        if(mModel.getRegion(id).getArmy()==null || mModel.getRegion(id).getArmy().getPlayer() != mModel.getCurrentplayer()) {
            return true;
        }
        return false;
    }

    private boolean selectedIsHostile(int id){
        if(mModel.getRegion(id).getArmy()!=null && mModel.getRegion(id).getArmy().getPlayer()!=mModel.getCurrentplayer()){
            return true;
        }
        return false;
    }

    private boolean regionIsAdjacentToPrev(int id){
        for(Region r : mModel.getRegion(mModel.getCurrentplayer().getPrevselectedregionid()).getAdjacentregions()) {
            if (r == mModel.getRegion(id)) {
                return true;//Selected is adjacent to prev selected
            }
        }
        return false; //Not adjacent
    }

    private boolean regionIsWithinPrevSelectedEmpire(int id){
        Region sel = mModel.getRegion(id);
        Region prev = mModel.getRegion(mModel.getCurrentplayer().getPrevselectedregionid());

        if(sel.getEmpire()!=null && prev.getEmpire()!=null && sel.getEmpire()==prev.getEmpire()){
            return true;
        }
        return false;
    }

    private boolean regionIsScorched(int id){
        return mModel.getRegion(id).getScorched();
    }

    private boolean prevSelectedIsMine(){
        int prev = mModel.getCurrentplayer().getPrevselectedregionid();
        if(prev != -1 && //Have I got a previously selected region?
                mModel.getRegion(prev).getArmy()!=null && //Army exists?
                mModel.getRegion(prev).getArmy().getPlayer().getParticipantid().equals(mMyId)){ //Army is mine?
            return true;
        }
        return false;
    }

    private boolean prevSelectedHasMoreThan1Army(){
        if(mModel.getRegion(mModel.getCurrentplayer().getPrevselectedregionid()).getArmy().getSize()>1){
            return true;
        } else {
            return false;
        }
    }

    private void placeBombForRegion(int id){
        Region sel = mModel.getRegion(id);
        int type=bombTypeForPhase();

        if(sel.getBomb()!=null && sel.getBomb().getBombtype()!=type){//Bomb of a different type found
            showDialogFragment(2,"Different bomb type detected in\n'"+sel.getName()+"'\nYou are unable to place the bomb at this location.",0,0);
        } else {
            if (sel.getBomb()==null){//No bomb
                showDialogFragment(10, "No bomb detected in\n'"+sel.getName()+"'\nAre you sure you wish to place the bomb here?", 0, 0);
            } else if(sel.getBomb().getBombtype()==type){//Existing bomb found
                showDialogFragment(10, sel.getBomb().getTypeString()+"-Bomb detected in\n'"+sel.getName()+"'\nAre you sure you wish to increase the size of this bomb by 1?", 0, 0);
            }
        }
        dialogfragment.setRegionid(id);
    }

    public void confirmBombPlacement(int id){
        Region sel = mModel.getRegion(id);
        int type=bombTypeForPhase();

        mModel.getCurrentplayer().allocateBomb(sel, type);
        if(sel.getBomb().getSize()==1){
            updateChat("Placed " + sel.getBomb().getTypeString() + "-Bomb in " + sel.getName() + "!");
        } else {
            updateChat("Increased size of " + sel.getBomb().getTypeString() + "-Bomb in " + sel.getName() + " to "+String.valueOf(sel.getBomb().getSize())+"!");
        }
        sendBombPlacement(id, type);
        abombfromregion=-1;
        infofragment.setBtnEndTurnVisibility(true);
    }

    private int bombTypeForPhase(){
        switch (mModel.getCurrentphase()){
            case 1://bombing phase, must be Hbomb
                return 1;
            case 3://Attacking phase, must be Abomb
                return 0;
        }
        return 0;
    }

    public void takeRegionForCurrentPlayer(int pledge, int targetregionid, int sourceregionid, boolean tellothers){
        boolean wipedout=false;
        if(targetregionid==-1 && sourceregionid==-1){
            targetregionid=mModel.getCurrentplayer().getSelectedregionid();
            sourceregionid=mModel.getCurrentplayer().getPrevselectedregionid();
        }
        Empire e = mModel.getRegion(sourceregionid).getEmpire();
        if(pledge>0) {
            Region source = mModel.getRegion(sourceregionid);
            Region dest = mModel.getRegion(targetregionid);

            //Move army
            source.getArmy().incrementSize(-pledge);

            if (dest.getArmy() != null) {
                dest.wipeOut();
            }
            //Add dest to a new empire (it will be joined later if it is still with the current empire)
            source.getArmy().getPlayer().newEmpire(dest,pledge);


            if(mModel.getRegion(sourceregionid).getArmy().getSize()<=0){//If previous region's army was wiped out, wipe out the region
               source.wipeOut();
               wipeOutRegionInView(sourceregionid);
                wipedout=true;
            }

            //Tell everyone else
            if (iAmCurrentPlayer() && tellothers) {
                sendRegionUpdate(2, targetregionid);
            }

            //Apply in view
            addRegiontoEmpireinView(targetregionid);
            checkRegionForEmpireMerge(dest);
        } else {
            DeselectForCurrentPlayer();
        }
        if(mModel.getRegion(targetregionid).getType().equals("sea") && abombfromregion!=-1 && iAmCurrentPlayer()){//Assign bomb to player outside of usual attack protocol
            if(wipedout){
                if(e.getRegions().size()>0){
                    e.checkSplitEmpire(mModel.getRegion(sourceregionid));
                }
            }
            showDialogFragment(2, "You have earned an atom bomb, select a region within the same empire as '" + mModel.getRegion(abombfromregion).getName() + "' to place it.", 0, 0);
        }
        checkVictory();
    }

    public void moveArmyInsideEmpire(int sel, int prev, int pledge){
        if(sel==-1&&prev==-1){//If no regions are passed, assume it is current players selected regions
            mModel.getRegion(mModel.getCurrentplayer().getPrevselectedregionid()).getArmy().incrementSize(-pledge);
            mModel.getRegion(mModel.getCurrentplayer().getSelectedregionid()).getArmy().incrementSize(pledge);
        } else {
            mModel.getRegion(prev).getArmy().incrementSize(-pledge);
            mModel.getRegion(sel).getArmy().incrementSize(pledge);
        }
        if(iAmCurrentPlayer()){
            sendRegionUpdate(3, pledge);
        }
        DeselectForCurrentPlayer();
    }

    //Checks around a region for 2 empires belonging to the player, if there are 2 or more, it merges them to a single empire
    private void checkRegionForEmpireMerge(Region r){
        for(Region re : r.getAdjacentregions()){
            if(re.getEmpire()!=null && re.getEmpire()!= r.getEmpire() && re.getArmy().getPlayer() == r.getArmy().getPlayer()){//If new empire found that player owns
                r.getEmpire().joinEmpire(re.getEmpire());//Join it to the source empire
            }
        }
    }







    //BOMBING PHASE
    private int subphase=0;
    private void handleBombingClick(int id){
        if(abombfromregion!=-1){//All A bombs must be placed immediately
            //Bombs must be placed in the same empire as the region that was attacked from, bear in mind that that region may now be empty due to all forces moving from it
            //Also the bomb CANNOT be placed on the target region of the attack that generated the bomb
            if(pastempire.contains(mModel.getRegion(id))){
                placeBombForRegion(id);
            } else {
                showDialogFragment(2, "Target region for bomb placement must be within the empire the source region was in prior to your recent detonation.", 0, 0);
            }
        } else if(prevSelectedIsMine() && prevSelectedHasABomb()){
            Bomb b = mModel.getRegion(mModel.getCurrentplayer().getPrevselectedregionid()).getBomb();
            if(targetIsInRange(b)){
                switch(b.getBombtype()){
                    case 0:
                        if(!detonationWillDestroySourceEmpire(b)){
                            if(subphase==0 && playerHasHydrogenBombs()){
                                showDialogFragment(11,"This will stop you firing any more H-Bombs this turn, are you sure you wish to fire at\n'"+mModel.getRegion(id).getName()+"'?",0,0);
                            } else {
                                showDialogFragment(11,"Are you sure you wish to fire at\n"+mModel.getRegion(id).getName()+"'?",0,0);
                            }
                        } else {
                            showDialogFragment(2, "The resulting explosion of an A-Bomb cannot destroy the source empire.", 0, 0);
                            DeselectForCurrentPlayer();
                            firstclick = true;
                        }
                        break;
                    case 1:
                        if(subphase==0){
                            showDialogFragment(11,"Are you sure you wish to fire at\n'"+mModel.getRegion(id).getName()+"'?",0,0);
                        } else {
                            showDialogFragment(2,"You can no longer fire H-Bombs this turn.",0,0);
                            DeselectForCurrentPlayer();
                            firstclick = true;
                        }
                        break;
                    default:
                        showDialogFragment(11,"Are you sure you wish to fire at\n'"+mModel.getRegion(id).getName()+"'?",0,0);
                        break;
                }
                dialogfragment.setRegionid(id);
            } else {
                showDialogFragment(2,"Not in range.",0,0);
                firstclick = true;
                DeselectForCurrentPlayer();
            }
        } else {
            if(!firstclick && mModel.getCurrentplayer().getPrevselectedregionid()!=-1){
                showDialogFragment(2, "Select one of your regions with a bomb as a source, then another region within range as the target", 0, 0);
                firstclick = true;
                DeselectForCurrentPlayer();
            } else {
                firstclick = false;
            }
        }

    }

    public void confirmFireBomb(int sel){//Accessed from dialog fragment
        //Save the empire that was, in preparation for bomb placement
        pastempire = new ArrayList<>();
        for(Region r : mModel.getRegion(mModel.getCurrentplayer().getPrevselectedregionid()).getEmpire().getRegions()){
            pastempire.add(r);
        }
        switch (mModel.getRegion(mModel.getCurrentplayer().getPrevselectedregionid()).getBomb().getBombtype()){
            case 0://A
                updateChat("Fired an A-Bomb at " +mModel.getRegion(sel).getName()+"!");
                infofragment.setBtnEndTurnVisibility(false);
                break;
            case 1://H
                updateChat("Fired an H-Bomb at " +mModel.getRegion(sel).getName()+"!");
                break;
        }
        fireBomb(mModel.getCurrentplayer().getPrevselectedregionid(), sel);
        sendFireBomb(mModel.getCurrentplayer().getPrevselectedregionid(), sel);
        DeselectForCurrentPlayer();
    }

    private void fireBomb(int sourceid, int targetid){
        List<Region> affectedRegions = new ArrayList<>();
        List<Empire> affectedEmpires = new ArrayList<>();
        Bomb b = mModel.getRegion(sourceid).getBomb();
        int bombType = b.getBombtype();
        if(b.getBombtype()==0&&subphase==0){//End H-Bomb firing subphase
            subphase++;
        }
        b.fireBomb(mModel.getRegion(targetid), affectedEmpires, affectedRegions);
        for(Region r : affectedRegions){
            int id = mModel.getRegionIDByName(r.getName());
            wipeOutRegionInView(id);
            if(r.getScorched()){
                scorchRegionInView(id);
            }
        }
        for(Empire e : affectedEmpires){
            if(e.getRegions()!=null && e.getRegions().size()>0){
                e.checkShatteredEmpire();
            }
        }
        if(iAmCurrentPlayer()&&bombType==0){//If I am current player and I am receiving an H bomb
            abombfromregion=sourceid;//Attacker receives a bomb
            showDialogFragment(2,"You have earned an H-Bomb, select a region within the same empire as '"+mModel.getRegion(abombfromregion).getName() + "' was prior to the detonation, to place it.",0,0);
        }
        checkVictory();
    }


    private boolean prevSelectedHasABomb(){
        if(mModel.getRegion(mModel.getCurrentplayer().getPrevselectedregionid()).getBomb()!=null){
            return true;
        } else {
            return false;
        }
    }

    private boolean detonationWillDestroySourceEmpire(Bomb b){
        Region source = mModel.getRegion(mModel.getCurrentplayer().getPrevselectedregionid());
        Region target;
        if(mModel.getCurrentplayer().getSelectedregionid()==-1){
            target = b.getLocation();
        } else {
            target = mModel.getRegion(mModel.getCurrentplayer().getSelectedregionid());
        }

        return b.willDestroyRegions(source.getEmpire().getRegions(), target, source);
    }

    private boolean targetIsInRange(Bomb b){
        if(mModel.getCurrentplayer().getSelectedregionid()==-1){
            return true;
        } else {
            return b.checkRange(mModel.getRegion(mModel.getCurrentplayer().getSelectedregionid()));
        }
    }

    private boolean playerHasHydrogenBombs(){
        for(Empire e : mModel.getCurrentplayer().getEmpires()){
            for(Region r : e.getRegions()){
                if(r.getBomb()!=null && r.getBomb().getBombtype()>0){
                    return true;
                }
            }
        }
        return false;
    }














    //GENERAL VIEW MANIPULATION
    private void addRegiontoEmpireinView(int id) {
        mapfragment.getRegion(id).setUseGradient(true, mModel.getCurrentplayer().getColour());
        mapfragment.reRender();
        DeselectForCurrentPlayer(); //Bear this in mind when relying on player selection data
    }
    private void wipeOutRegionInView(int id){
        mapfragment.getRegion(id).setUseGradient(false, null);
        mapfragment.reRender();
    }

    private void scorchRegionInView(int id){
        mapfragment.getRegion(id).setScorched(true);
        mapfragment.reRender();
    }

    public void DeselectForCurrentPlayer(){
        mModel.getCurrentplayer().setSelectedregionid(-1);
        sendMySelectionData();
        updateClickedRegions();
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

    //TODO: Look into inviting players into disconnected player slots
    //TODO: Set timer on response for defender
}