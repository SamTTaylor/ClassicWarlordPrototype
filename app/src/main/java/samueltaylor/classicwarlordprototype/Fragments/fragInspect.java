package samueltaylor.classicwarlordprototype.Fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import samueltaylor.classicwarlordprototype.GameController;
import samueltaylor.classicwarlordprototype.Model.Region;
import samueltaylor.classicwarlordprototype.R;


public class fragInspect extends Fragment {


    private OnFragmentInteractionListener mListener;

    private Region region;
    private TextView txtTitle;
    private TextView txtArmy;
    private TextView txtBomb;
    private TextView txtReinforcements;
    private Button btnOk;

    public static fragInspect newInstance(String param1, String param2) {
        fragInspect fragment = new fragInspect();
        Bundle args = new Bundle();

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
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //UI calls after fragment has finished loading elements

        //Add buttons and listeners
        txtTitle = (TextView) getActivity().findViewById(R.id.txtInspectTitle);
        txtArmy = (TextView) getActivity().findViewById(R.id.txtInspectArmy);
        txtBomb = (TextView) getActivity().findViewById(R.id.txtInspectBomb);
        txtReinforcements = (TextView) getActivity().findViewById(R.id.txtInspectReinforcements);
        btnOk = (Button) getActivity().findViewById(R.id.btnInspectConfirm);

        txtTitle.setText(region.getName() + "\n(" + region.getType().replaceFirst(region.getType().substring(0,1),region.getType().substring(0,1).toUpperCase()) + ")");
        if(region.getArmy()!=null){
            txtArmy.setText(String.valueOf(region.getArmy().getSize()));
        } else {
            txtArmy.setText("-");
        }
        if(region.getBomb()!=null){
            txtBomb.setText(region.getBomb().getTypeString() + " : " + String.valueOf(region.getBomb().getSize()));
        } else {
            txtBomb.setText("-");
        }
        if(region.getEmpire()!=null){
            txtReinforcements.setText(String.valueOf(region.getEmpire().countReinforcements()));
        } else {
            txtReinforcements.setText("-");
        }

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Dismiss self
                ((GameController) getActivity()).removeFragment(getFragmentManager().findFragmentByTag("inspect"));
            }});
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
        public void onInspectFragmentInteraction(Uri uri);
    }

    public void setRegion(Region r){region = r;}


}
