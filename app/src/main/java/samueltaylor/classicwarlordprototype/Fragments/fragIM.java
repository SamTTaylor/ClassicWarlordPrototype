package samueltaylor.classicwarlordprototype.Fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;

import samueltaylor.classicwarlordprototype.GameController;
import samueltaylor.classicwarlordprototype.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link fragIM.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link fragIM#newInstance} factory method to
 * create an instance of this fragment.
 */
public class fragIM extends Fragment {

    private OnFragmentInteractionListener mListener;

    //Objects
    Button btnSendIM;
    TextView IM;
    TextView Chat;

    public static fragIM newInstance(String param1, String param2) {
        fragIM fragment = new fragIM();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public fragIM() {
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
        return inflater.inflate(R.layout.fragment_frag_im, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //UI calls after fragment has finished loading elements

        //Add buttons and listeners
        Chat = (TextView) getActivity().findViewById(R.id.txtChat);
        IM = (TextView) getActivity().findViewById(R.id.txtMessage);
        btnSendIM = (Button) getActivity().findViewById(R.id.btnSend);
        btnSendIM.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){
            //Don't send blank messages
            if (IM.getText().length()>0) {
                //Ask controller to update everyone's chat and clear the text field
                ((GameController) getActivity()).updateChat(IM.getText().toString());
                IM.setText("");
            }
        }});
    }

    public void appendChat(String message){
        Chat.append(message);
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onIMFragmentInteraction(uri);
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
        public void onIMFragmentInteraction(Uri uri);
    }

}
