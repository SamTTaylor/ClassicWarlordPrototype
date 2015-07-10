package samueltaylor.classicwarlordprototype.Fragments;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import samueltaylor.classicwarlordprototype.R;


public class fragInfo extends Fragment {


    private OnFragmentInteractionListener mListener;

    //Objects
    LinearLayout buttonbackground;
    Button btnIcon;
    TextView txtInfo;
    int backgroundColour;
    String colorString="";
    int phase=-1;

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
        buttonbackground = (LinearLayout) getActivity().findViewById(R.id.lBackground);
        btnIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Update text and toggle visibility
                txtInfo.setText("Player: " + colorString + "\n" + "Phase: " + phaseToString());
                if(txtInfo.getVisibility()==View.GONE){txtInfo.setVisibility(View.VISIBLE);}else{txtInfo.setVisibility(View.GONE);}
            }
        });
        buttonbackground.setBackgroundColor(backgroundColour);
        //Refresh button
        btnIcon.callOnClick();
        txtInfo.setVisibility(View.GONE);
        nextPhase();
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
        backgroundColour = Color.rgb(intcolour[0], intcolour[1], intcolour[2]);
        colorString=cString;
        if(buttonbackground!=null){
            buttonbackground.setBackgroundColor(backgroundColour);
        }
        if(txtInfo!=null){
            txtInfo.setText("Player: " + colorString + "\n" + "Phase: " + phaseToString());
        }
    }

    public void nextPhase(){
        phase++;
        if(phase>3){
            phase = 1;//Move back to reinforcement phase on loop
        }
        switch (phase){
            case 0://Mountain
                if(LargeScreen()==true){
                    setButtonBG(getResources().getDrawable(R.drawable.mountainiconlg));
                } else {
                    setButtonBG(getResources().getDrawable(R.drawable.mountainiconmd));
                }
                break;
            case 1://Reinforcement
                if(LargeScreen()==true){
                    setButtonBG(getResources().getDrawable(R.drawable.reinforceiconlg));
                } else {
                    setButtonBG(getResources().getDrawable(R.drawable.reinforceiconmd));
                }
                break;
            case 2://Firing bombs

                break;
            case 3://Attack/move

                break;
            default://None
                break;
        }
        //Refresh the button and info text
        btnIcon.callOnClick();
        btnIcon.callOnClick();
    }

    private boolean LargeScreen(){
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int heightPixels = metrics.heightPixels;
        float scaleFactor = metrics.density;
        float heightDp = heightPixels / scaleFactor;
        if(heightDp>=600){
            return true;
        } else {
            return false;
        }
    }

    private void setButtonBG(Drawable drawable){
        int sdk = android.os.Build.VERSION.SDK_INT;
        if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            btnIcon.setBackgroundDrawable(drawable);
        } else {
            btnIcon.setBackground(drawable);
        }
    }

    private String phaseToString(){
        switch (phase){
            case 0:
                return "Choosing Mountain";
            case 1:
                return "Reinforcement";
            case 2:
                return "Firing Bombs";
            case 3:
                return "Attack/Moving";
            default:
                return "None";
        }
    }

}
