package samueltaylor.classicwarlordprototype.Fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import samueltaylor.classicwarlordprototype.GameController;
import samueltaylor.classicwarlordprototype.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link fragDialog.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link fragDialog#newInstance} factory method to
 * create an instance of this fragment.
 */
public class fragDialog extends Fragment {

    int type;
    int regionid;
    String message;
    Button btnConfirm;
    Button btnCancel;
    TextView txtMessage;
    EditText txtInput;

    private OnFragmentInteractionListener mListener;


    public static fragDialog newInstance(String param1, String param2) {
        fragDialog fragment = new fragDialog();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public fragDialog() {
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
        return inflater.inflate(R.layout.fragment_dialog, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //UI calls after fragment has finished loading elements

        //Add buttons and listeners
        txtMessage = (TextView) getActivity().findViewById(R.id.txtDialogMessage);
        txtInput = (EditText) getActivity().findViewById(R.id.txtInput);
        btnConfirm = (Button) getActivity().findViewById(R.id.btnConfirm);
        btnCancel = (Button) getActivity().findViewById(R.id.btnCancel);

        switch (type){//Adjust layout to type
            case 1: //Mountain
                txtInput.setVisibility(View.GONE);
                txtMessage.setText(message);
                break;
            case 2:
                txtInput.setVisibility(View.GONE);
                btnCancel.setVisibility(View.GONE);
                btnConfirm.setText("OK");
                txtMessage.setText(message);
                break;
            default:
                break;
        }

        btnConfirm.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){
            //Confirm based on dialog type
            switch (type){
                case 1: //Mountain Confirmation
                    ((GameController) getActivity()).mountainSelected(regionid);
                    break;

                default:
                    ((GameController) getActivity()).removeDialogFragment();
                    break;
            }

        }});


        btnCancel.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){
            //Dismiss self
            ((GameController) getActivity()).removeDialogFragment();
        }});
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onAlertFragmentInteraction(uri);
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
        public void onAlertFragmentInteraction(Uri uri);
    }

    public void setType(int t){type = t;}
    public void setMessage(String m){message = m;}

    public void setRegionid(int id){regionid=id;}
}
