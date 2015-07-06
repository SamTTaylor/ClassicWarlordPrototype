package samueltaylor.classicwarlordprototype.Fragments;

import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import samueltaylor.classicwarlordprototype.GameController;
import samueltaylor.classicwarlordprototype.R;


public class fragInfo extends Fragment {


    private OnFragmentInteractionListener mListener;

    //Objects
    Button btnIcon;
    TextView txtInfo;
    Color buttonColor;
    String colorString="";
    int phase;

    public static fragInfo newInstance(String param1, String param2) {
        fragInfo fragment = new fragInfo();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    public fragInfo() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_info, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //UI calls after fragment has finished loading elements
        //Add buttons and listeners
        txtInfo = (TextView) getActivity().findViewById(R.id.txtInfo);
        btnIcon = (Button) getActivity().findViewById(R.id.btnIcon);
        btnIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Update text and toggle visibility
                txtInfo.setText("Player: " + colorString + "\n" + "Phase: " + phaseToString());
                if(txtInfo.getVisibility()==View.GONE){txtInfo.setVisibility(View.VISIBLE);}else{txtInfo.setVisibility(View.GONE);}
            }
        });
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

    public interface OnFragmentInteractionListener {
        public void onInfoFragmentInteraction(Uri uri);
    }

    public void setColour(float[] colour, String cString){
        int[] intcolour = new int[]{(int)(colour[0]*255),(int)(colour[1]*255),(int)(colour[2]*255)};
        btnIcon.setHighlightColor(Color.rgb(intcolour[0], intcolour[1], intcolour[2]));
        colorString=cString;
        //refresh button info
        btnIcon.callOnClick();
        btnIcon.callOnClick();
    }
    public void nextPhase(){
        phase++;
        if(phase>3){
            phase = 1;
        }
    }

    private String phaseToString(){
        switch (phase){
            case 0:
                return "Mountain";
            case 1:
                return "Reinforce";
            case 2:
                return "Bombing";
            case 3:
                return "Attack/Move";
            default:
                return "None";
        }
    }

}
