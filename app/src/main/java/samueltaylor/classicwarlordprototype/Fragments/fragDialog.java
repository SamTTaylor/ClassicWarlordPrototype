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
import android.widget.LinearLayout;
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
    int max=0;
    int min=0;
    int current=0;
    String message;
    Button btnConfirm;
    Button btnCancel;
    TextView txtMessage;
    EditText txtInput;
    Button btnPlus;
    Button btnMinus;
    LinearLayout inputLayout;

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
        btnPlus = (Button) getActivity().findViewById(R.id.btnPlus);
        btnMinus = (Button) getActivity().findViewById(R.id.btnMinus);
        inputLayout = (LinearLayout) getActivity().findViewById(R.id.inputLayout);

        switch (type){//Adjust layout to type, check btnConfirm onclick for what each type means
            case 1:
                confirmationMessage();
                break;
            case 2:
                basicMessage();
                break;
            case 3:
                inputMessage();
                break;
            case 4:
                confirmationMessage();
                break;
            case 5:
                inputMessage();
                break;
            case 6:
                inputMessage();
                break;
            case 7:
                inputMessage();
                break;
            case 8:
                inputMessage();
                break;
            case 9:
                inputMessage();
                btnCancel.setVisibility(View.GONE);
                break;
            case 10:
                confirmationMessage();
                break;
            default:
                txtMessage.setText("Default dialog type");
                break;
        }

        btnConfirm.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){
            //Confirm based on dialog type... This should probably be sent to the controller to decide but I'm in too deep now
            switch (type){
                case 1: //Confirmation
                    ((GameController) getActivity()).mountainSelected(regionid);
                    break;
                case 3://Reinforcement Deployment
                    ((GameController)getActivity()).reinforceRegion(regionid, current);
                    break;
                case 4://End turn confirmation
                    ((GameController) getActivity()).endTurn(true);
                    ((GameController) getActivity()).removeDialogFragment();
                    break;
                case 5://Move army to unoccupied region
                    ((GameController) getActivity()).takeRegionForCurrentPlayer(current, -1, -1, true);
                    ((GameController) getActivity()).removeDialogFragment();
                    break;
                case 6://Move army around within empire
                    ((GameController) getActivity()).moveArmyInsideEmpire(-1, -1, current);
                    ((GameController) getActivity()).removeDialogFragment();
                    break;
                case 7://Attack from land, 1 guess
                    ((GameController) getActivity()).attackConfirmed(current, 1);
                    ((GameController) getActivity()).removeDialogFragment();
                    break;
                case 8://Attack from sea, 2 guesses
                    ((GameController) getActivity()).attackConfirmed(current, 2);
                    ((GameController) getActivity()).removeDialogFragment();
                    break;
                case 9://Defence number has been guessed
                    ((GameController) getActivity()).defenceConfirmed(current);
                    ((GameController) getActivity()).removeDialogFragment();
                    break;
                case 10://Bomb Placement
                    ((GameController) getActivity()).confirmBombPlacement();
                    ((GameController) getActivity()).removeDialogFragment();
                    break;
                default:
                    ((GameController) getActivity()).removeDialogFragment();
                    break;
            }

        }});


        btnPlus.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){
            if(current<max){
                current++;
                txtInput.setText(String.valueOf(current));
            }
        }});
        btnMinus.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){
            if(current > min){
                current--;
                txtInput.setText(String.valueOf(current));
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

    private void basicMessage(){
        inputLayout.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);
        btnConfirm.setText("OK");
        txtMessage.setText(message);
    }

    private void inputMessage(){
        txtMessage.setText(message);
        if(min>0){
            txtInput.setText(String.valueOf(min));
            current=min;
        }
    }

    private void confirmationMessage(){
        inputLayout.setVisibility(View.GONE);
        txtMessage.setText(message);
    }

    public void setType(int t){type = t;}
    public void setMessage(String m){message = m;}
    public void setMax(int i){max=i;}
    public void setMin(int i){min=i;}
    public void setRegionid(int id){regionid=id;}
}
