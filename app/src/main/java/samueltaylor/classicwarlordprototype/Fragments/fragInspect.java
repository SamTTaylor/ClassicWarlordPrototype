package samueltaylor.classicwarlordprototype.Fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import samueltaylor.classicwarlordprototype.R;


public class fragInspect extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;


    public static fragInspect newInstance(String param1, String param2) {
        fragInspect fragment = new fragInspect();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public fragInspect() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //UI calls after fragment has finished loading elements

        //Add buttons and listeners
//        txtMessage = (TextView) getActivity().findViewById(R.id.txtDialogMessage);
//        txtInput = (EditText) getActivity().findViewById(R.id.txtInput);
//        btnConfirm = (Button) getActivity().findViewById(R.id.btnConfirm);
//        btnCancel = (Button) getActivity().findViewById(R.id.btnCancel);
//
//        switch (type){//Adjust layout to type
//            case 1: //Mountain confirmation
//                txtInput.setVisibility(View.GONE);
//                txtMessage.setText(message);
//                break;
//            case 2://Basic Message
//                txtInput.setVisibility(View.GONE);
//                btnCancel.setVisibility(View.GONE);
//                btnConfirm.setText("OK");
//                txtMessage.setText(message);
//                break;
//            case 3://Reinforcement Deployment
//                txtMessage.setText(message);
//                break;
//            default:
//                break;
//        }
//
//        btnConfirm.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){
//            //Confirm based on dialog type
//            switch (type){
//                case 1: //Mountain Confirmation
//                    ((GameController) getActivity()).mountainSelected(regionid);
//                    break;
//                case 3://Reinforcement Deployment
//                    ((GameController)getActivity()).reinforceRegion(regionid);
//                    break;
//
//                default:
//                    ((GameController) getActivity()).removeDialogFragment();
//                    break;
//            }
//
//        }});
//
//
//        btnCancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //Dismiss self
//                ((GameController) getActivity()).removeDialogFragment();
//            }
//        });
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_inspect, container, false);
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onInspectFragmentInteraction(uri);
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
        // TODO: Update argument type and name
        public void onInspectFragmentInteraction(Uri uri);
    }

}
