package samueltaylor.classicwarlordprototype.Fragments;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import samueltaylor.classicwarlordprototype.GameController;
import samueltaylor.classicwarlordprototype.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link fragGameHUDPlayers.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link fragGameHUDPlayers#newInstance} factory method to
 * create an instance of this fragment.
 */
public class fragGameHUDPlayers extends Fragment {

    private OnFragmentInteractionListener mListener;

    //Attributes
    boolean playersshown = false;
    List<String> playernames;
    List<float[]> playerColours;
    public String myName;

    //Objects
    Button btnShowPlayers;
    Button btnMyColour;

    // TODO: Rename and change types and number of parameters
    public static fragGameHUDPlayers newInstance(String param1, String param2) {
        fragGameHUDPlayers fragment = new fragGameHUDPlayers();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public fragGameHUDPlayers() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_game_hud_players, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //UI calls after fragment has finished loading elements
        if(playersshown == false){
            getActivity().findViewById(R.id.tblPlayers).setVisibility(View.GONE);
        }
        //Add buttons and listeners
        btnShowPlayers = (Button) getActivity().findViewById(R.id.btnShowPlayers);
        btnShowPlayers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playersshown == false) {
                    getActivity().findViewById(R.id.tblPlayers).setVisibility(View.VISIBLE);
                    playersshown = true;
                } else {
                    getActivity().findViewById(R.id.tblPlayers).setVisibility(View.GONE);
                    playersshown = false;
                }
            }
        });
        tblPlayers = (TableLayout)getActivity().findViewById(R.id.tblPlayers);
        for(int i=0; i<8;i++){
            if(i<playernames.size()){
                addPlayer(playernames.get(i), i, playerColours.get(i));
            } else {
                tblPlayers.getChildAt(i).setVisibility(View.GONE);
            }
        }
    }

    public void addPlayerName(String name){
        if(playernames==null){
            playernames = new ArrayList<String>();
        }
        playernames.add(name);
    }
    public void addPlayerColour (float[] colour){
        if(playerColours==null){
            playerColours = new ArrayList<float[]>();
        }
        playerColours.add(colour);
    }


    TableLayout tblPlayers;
    public void addPlayer(String name, int index, float[] colour){
        TableRow row;
        Button btnPlayerColours;
        TextView txtName;

        btnMyColour =  (Button)getActivity().findViewById(R.id.btnPlayerColour);
        row = (TableRow)tblPlayers.getChildAt(index);
        btnPlayerColours = (Button)row.getChildAt(0);
        txtName = (TextView)row.getChildAt(1);
        int[] intcolour = new int[]{(int)(colour[0]*255),(int)(colour[1]*255),(int)(colour[2]*255)};
        if(name == myName){
            btnMyColour.setBackgroundColor(Color.rgb(intcolour[0], intcolour[1], intcolour[2]));
            btnPlayerColours.setBackgroundColor(Color.rgb(intcolour[0], intcolour[1], intcolour[2]));
        } else {
            btnPlayerColours.setBackgroundColor(Color.rgb(intcolour[0], intcolour[1], intcolour[2]));
        }
        txtName.setText(name);
    }


    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onHUDFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onHUDFragmentInteraction(Uri uri);
    }

}
