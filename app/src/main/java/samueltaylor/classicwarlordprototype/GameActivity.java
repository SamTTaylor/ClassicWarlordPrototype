package samueltaylor.classicwarlordprototype;

/**
 * Created by Sam on 03/05/2015.
 */
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.andengine.engine.Engine;
import org.andengine.engine.LimitedFPSEngine;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.WakeLockOptions;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.menu.MenuScene;
import org.andengine.entity.scene.menu.item.IMenuItem;
import org.andengine.ui.activity.BaseGameActivity;

import samueltaylor.classicwarlordprototype.manager.ResourcesManager;
import samueltaylor.classicwarlordprototype.manager.SceneManager;
import samueltaylor.classicwarlordprototype.scenes.GameScene;
import samueltaylor.classicwarlordprototype.scenes.MainMenuScene;

/**
 * @author Mateusz Mysliwiec
 * @author www.matim-dev.com
 * @version 1.0
 */
public class GameActivity extends BaseGameActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, RealTimeMessageReceivedListener,
        RoomStatusUpdateListener, RoomUpdateListener, OnInvitationReceivedListener
{
    private Camera camera;
    private ResourcesManager resourcesManager;
    public EngineOptions onCreateEngineOptions()

    {
        camera = new Camera(0, 0, 800, 480);
        EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(800, 480), this.camera);
        engineOptions.getAudioOptions().setNeedsMusic(true).setNeedsSound(true);
        engineOptions.setWakeLockOptions(WakeLockOptions.SCREEN_ON);
        return engineOptions;
    }

    public void onCreateResources(OnCreateResourcesCallback pOnCreateResourcesCallback) throws IOException
    {
        ResourcesManager.prepareManager(mEngine, this, camera, getVertexBufferObjectManager());
        resourcesManager = ResourcesManager.getInstance();
        pOnCreateResourcesCallback.onCreateResourcesFinished();
    }

    public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) throws IOException
    {
        SceneManager.getInstance().createSplashScene(pOnCreateSceneCallback);
    }

    public void onPopulateScene(Scene pScene, OnPopulateSceneCallback pOnPopulateSceneCallback) throws IOException
    {
        mEngine.registerUpdateHandler(new TimerHandler(2f, new ITimerCallback()
        {
            public void onTimePassed(final TimerHandler pTimerHandler)
            {
                mEngine.unregisterUpdateHandler(pTimerHandler);
                SceneManager.getInstance().createMenuScene();
            }
        }));
        pOnPopulateSceneCallback.onPopulateSceneFinished();
    }

    @Override
    public Engine onCreateEngine(EngineOptions pEngineOptions)
    {
        return new LimitedFPSEngine(pEngineOptions, 60);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        System.exit(0);
    }













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

        Intent intent;
        // show list of invitable players
        intent = Games.RealTimeMultiplayer.getSelectOpponentsIntent(mGoogleApiClient, 1, 3);
        SceneManager.getInstance().setScene(SceneManager.SceneType.SCENE_LOADING);
        startActivityForResult(intent, RC_SELECT_PLAYERS);
    }

    public void playgame() {
        //Load Game Scene!
        //SceneManager.getInstance().loadGameScene(mEngine);
        // user wants to play against a random opponent right now
        startQuickGame();
    }

    public void seeinvites(){
        Intent intent;
        // show list of pending invitations
        intent = Games.Invitations.getInvitationInboxIntent(mGoogleApiClient);
        SceneManager.getInstance().setScene(SceneManager.SceneType.SCENE_LOADING);
        startActivityForResult(intent, RC_INVITATION_INBOX);
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


    void startQuickGame() {
        // quick-start a game with 1 randomly selected opponent
        if(mGoogleApiClient.isConnected()){
            final int MIN_OPPONENTS = 1, MAX_OPPONENTS = 1;
            Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS,
                    MAX_OPPONENTS, 0);
            RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(this);
            rtmConfigBuilder.setMessageReceivedListener(this);
            rtmConfigBuilder.setRoomStatusUpdateListener(this);
            rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
            resetGameVars();
            Games.RealTimeMultiplayer.create(mGoogleApiClient, rtmConfigBuilder.build());
            SceneManager.getInstance().loadGameScene(mEngine);
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
                boolean hello = false;
                if (responseCode == Activity.RESULT_OK) {
                    // ready to start playing
                    Log.d(TAG, "Starting game (waiting room returned OK).");
                    startGame(true);
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
            switchToMainScreen();
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
        SceneManager.getInstance().setScene(SceneManager.SceneType.SCENE_LOADING);
        resetGameVars();
        Games.RealTimeMultiplayer.create(mGoogleApiClient, rtmConfigBuilder.build());
        Log.d(TAG, "Room created, waiting for it to be ready...");
    }

    // Handle the result of the invitation inbox UI, where the player can pick an invitation
    // to accept. We react by accepting the selected invitation, if any.
    private void handleInvitationInboxResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** invitation inbox UI cancelled, " + response);
            switchToMainScreen();
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
        SceneManager.getInstance().setScene(SceneManager.SceneType.SCENE_LOADING);
        resetGameVars();
        Games.RealTimeMultiplayer.join(mGoogleApiClient, roomConfigBuilder.build());
    }

    // Activity is going to the background. We have to leave the current room.
    @Override
    public void onStop() {
        Log.d(TAG, "**** got onStop");

        // if we're in a room, leave it.
        leaveRoom();

        // stop trying to keep the screen on

        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()){
            SceneManager.getInstance().loadMenuScene(mEngine);
        }
        else {
            SceneManager.getInstance().setScene(SceneManager.SceneType.SCENE_LOADING);
        }
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
        if (keyCode == KeyEvent.KEYCODE_BACK && SceneManager.getInstance().getCurrentScene().getClass().toString().equals("GameScene")) {
            leaveRoom();
            SceneManager.getInstance().getCurrentScene().onBackKeyPressed();
            return true;
        } else {
            SceneManager.getInstance().getCurrentScene().onBackKeyPressed();
        }
        return super.onKeyDown(keyCode, e);
    }

    // Leave the room.
    void leaveRoom() {
        Log.d(TAG, "Leaving room.");
        mSecondsLeft = 0;
        if (mRoomId != null) {
            Games.RealTimeMultiplayer.leave(mGoogleApiClient, this, mRoomId);
            mRoomId = null;
            SceneManager.getInstance().setScene(SceneManager.SceneType.SCENE_LOADING);
        } else {
            switchToMainScreen();
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
        mIncomingInvitationId = invitation.getInvitationId();
        ((TextView) findViewById(samueltaylor.classicwarlordprototype.R.id.incoming_invitation_text)).setText(
                invitation.getInviter().getDisplayName() + " " +
                        getString(samueltaylor.classicwarlordprototype.R.string.is_inviting_you));
        SceneManager.getInstance().setScene(SceneManager.getInstance().getCurrentScene()); // This will show the invitation popup
    }

    @Override
    public void onInvitationRemoved(String invitationId) {
        if (mIncomingInvitationId.equals(invitationId)) {
            mIncomingInvitationId = null;
            SceneManager.getInstance().setScene(SceneManager.getInstance().getCurrentScene()); // This will hide the invitation popup
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
        switchToMainScreen();
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
        switchToMainScreen();
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
    public void onP2PDisconnected(String participant) {
    }

    @Override
    public void onP2PConnected(String participant) {
    }

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
        if (mParticipants != null) {
            updatePeerScoresDisplay();
        }
    }

    /*
     * GAME LOGIC SECTION. Methods that implement the game's rules.
     */

    // Current state of the game:
    int mSecondsLeft = -1; // how long until the game ends (seconds)
    final static int GAME_DURATION = 20; // game duration, seconds.
    String mInfo = ""; // user's current score


    // Reset game variables in preparation for a new game.
    void resetGameVars() {
        mSecondsLeft = GAME_DURATION;
        mInfo = "";
        mParticipantScore.clear();
        mFinishedParticipants.clear();
    }

    // Start the gameplay phase of the game.
    void startGame(boolean multiplayer) {
        mMultiplayer = multiplayer;
        broadcastScore(false);

        SceneManager.getInstance().loadGameScene(mEngine);

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
    }

    // Game tick -- update countdown, check if game ended.
    void gameTick() {
        if (mSecondsLeft > 0)
            --mSecondsLeft;

        // update countdown
        ((TextView) findViewById(samueltaylor.classicwarlordprototype.R.id.countdown)).setText("0:" +
                (mSecondsLeft < 10 ? "0" : "") + String.valueOf(mSecondsLeft));

        if (mSecondsLeft <= 0) {
            // finish game
            findViewById(samueltaylor.classicwarlordprototype.R.id.button_click_me).setVisibility(View.GONE);
            broadcastScore(true);
        }
    }

    // indicates the player scored one point
    void scoreOnePoint() {
        if (mSecondsLeft <= 0)
            return; // too late!
        updatePeerScoresDisplay();

        // broadcast our new score to our peers
        broadcastScore(false);
    }

    /*
     * COMMUNICATIONS SECTION. Methods that implement the game's network
     * protocol.
     */

    // Score of other participants. We update this as we receive their scores
    // from the network.
    Map<String, Integer> mParticipantScore = new HashMap<String, Integer>();

    // Participants who sent us their final score.
    Set<String> mFinishedParticipants = new HashSet<String>();

    // Called when we receive a real-time message from the network.
    // Messages in our game are made up of 2 bytes: the first one is 'F' or 'U'
    // indicating
    // whether it's a final or interim score. The second byte is the score.
    // There is also the
    // 'S' message, which indicates that the game should start.
    @Override
    public void onRealTimeMessageReceived(RealTimeMessage rtm) {
        byte[] buf = rtm.getMessageData();
        String sender = rtm.getSenderParticipantId();
        Log.d(TAG, "Message received: " + (char) buf[0] + "/" + (int) buf[1]);

        if (buf[0] == 'F' || buf[0] == 'U') {
            // score update.
            int existingScore = mParticipantScore.containsKey(sender) ?
                    mParticipantScore.get(sender) : 0;
            int thisScore = (int) buf[1];
            if (thisScore > existingScore) {
                // this check is necessary because packets may arrive out of
                // order, so we
                // should only ever consider the highest score we received, as
                // we know in our
                // game there is no way to lose points. If there was a way to
                // lose points,
                // we'd have to add a "serial number" to the packet.
                mParticipantScore.put(sender, thisScore);
            }

            // update the scores on the screen
            updatePeerScoresDisplay();

            // if it's a final score, mark this participant as having finished
            // the game
            if ((char) buf[0] == 'F') {
                mFinishedParticipants.add(rtm.getSenderParticipantId());
            }
        }
    }

    // Broadcast my score to everybody else.
    void broadcastScore(boolean finalScore) {
        if (!mMultiplayer)
            return; // playing single-player mode

        // First byte in message indicates whether it's a final score or not
        mMsgBuf[0] = (byte) (finalScore ? 'F' : 'U');

        // Second byte is the score.
        mMsgBuf[1] = (byte) 0;

        // Send to every other participant.
        for (Participant p : mParticipants) {
            if (p.getParticipantId().equals(mMyId))
                continue;
            if (p.getStatus() != Participant.STATUS_JOINED)
                continue;
            if (finalScore) {
                // final score notification must be sent via reliable message
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, mMsgBuf,
                        mRoomId, p.getParticipantId());
            } else {
                // it's an interim score notification, so we can use unreliable
                Games.RealTimeMultiplayer.sendUnreliableMessage(mGoogleApiClient, mMsgBuf, mRoomId,
                        p.getParticipantId());
            }
        }
    }


    int mCurScreen = -1;

    void switchToMainScreen() {
        SceneManager.getInstance().loadGameScene(mEngine);
    }



    // updates the label that shows my score
    void updateInfoDisplay() {
        GameScene gs = (GameScene)SceneManager.getInstance().getCurrentScene();
        gs.setInfo(mInfo);
    }

    // updates the screen with the scores from our peers
    void updatePeerScoresDisplay() {
        mInfo = "Info:";
        updateInfoDisplay();
        int i = 0;

        if (mRoomId != null) {
            for (Participant p : mParticipants) {
                String pid = p.getParticipantId();
                if (pid.equals(mMyId))
                    continue;
                if (p.getStatus() != Participant.STATUS_JOINED)
                    continue;
                int score = mParticipantScore.containsKey(pid) ? mParticipantScore.get(pid) : 0;
                mInfo += score + p.getDisplayName();
                updateInfoDisplay();
                ++i;
            }
        }
    }

}