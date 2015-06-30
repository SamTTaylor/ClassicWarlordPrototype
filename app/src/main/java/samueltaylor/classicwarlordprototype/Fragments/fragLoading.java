package samueltaylor.classicwarlordprototype.Fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import samueltaylor.classicwarlordprototype.R;


public class fragLoading extends Fragment {

    private OnFragmentInteractionListener mListener;
    TextView loadingtext;
    public fragLoading() {
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
        View view =  inflater.inflate(R.layout.fragment_loading, container, false);
        loadingtext = (TextView) view.findViewById(R.id.txtLoading);
        if(mLoadingText!=null){//For custom loading text
            loadingtext.setText(mLoadingText);
        }
        return view;
    }

    public void onButtonPressed(Uri uri) {

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //UI calls after fragment has finished loading elements

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
        public void onLoadingFragmentInteraction(Uri uri);
    }
    String mLoadingText;
    public void setText(String s){
        mLoadingText=s;
    }

}
