package samueltaylor.classicwarlordprototype.Fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import samueltaylor.classicwarlordprototype.GameController;
import samueltaylor.classicwarlordprototype.R;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link fragControls.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link fragControls#newInstance} factory method to
 * create an instance of this fragment.
 */
public class fragControls extends Fragment {


    private OnFragmentInteractionListener mListener;

    private LinearLayout display;
    private Button btnBack;

    public static fragControls newInstance(String param1, String param2) {
        fragControls fragment = new fragControls();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    public fragControls() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //UI calls after fragment has finished loading elements

        display = (LinearLayout)getActivity().findViewById(R.id.lytDisplay);
        //Add buttons and listeners
        btnBack = (Button) getActivity().findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Dismiss self
                ((GameController) getActivity()).removeFragment(getFragmentManager().findFragmentByTag("controls"));
            }});
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_controls, container, false);
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onRulesFragmentInteraction(uri);
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

    public interface OnFragmentInteractionListener {
        public void onRulesFragmentInteraction(Uri uri);
    }

}