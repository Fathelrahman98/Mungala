package com.example.mungala;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SinglePlayerGameSetupFragment.OnSetupFinishListener} interface
 * to handle interaction events.
 */
public class SinglePlayerGameSetupFragment extends Fragment {

    private static final String DIFFICULTY_KEY = BuildConfig.APPLICATION_ID + "difficulty_key";
    private OnSetupFinishListener mListener;
    private int difficulty;

    public SinglePlayerGameSetupFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_single_player_game_setup, container, false);
        RadioGroup radioGroup = view.findViewById(R.id.difficulty_group);

        if (savedInstanceState != null) {
            difficulty = savedInstanceState.getInt(DIFFICULTY_KEY);
        }

        if (difficulty == 0) {
            difficulty = 1;
        }

        switch (difficulty) {
            case 1:
                radioGroup.check(R.id.v_easy_button);
                break;
            case 2:
                radioGroup.check(R.id.easy_button);
                break;
            case 3:
                radioGroup.check(R.id.med_button);
                break;
            case 4:
                radioGroup.check(R.id.hard_button);
                break;
            case 5:
                radioGroup.check(R.id.v_hard_button);
                break;
            }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.v_easy_button:
                        difficulty = 1;
                        break;
                    case R.id.easy_button:
                        difficulty = 2;
                        break;
                    case R.id.med_button:
                        difficulty = 3;
                        break;
                    case R.id.hard_button:
                        difficulty = 4;
                        break;
                    case R.id.v_hard_button:
                        difficulty = 5;
                        break;
                }
            }
        });

        Button okButton = view.findViewById(R.id.button12);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onSetupFinish(difficulty);
            }
        });
        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(DIFFICULTY_KEY,difficulty);
        super.onSaveInstanceState(outState);
    }

    /*public void onButtonPressed() {
        if (mListener != null) {
            mListener.onSetupFinish(difficultyLevel);
        }
    }*/

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSetupFinishListener) {
            mListener = (OnSetupFinishListener) context;
        } else {
            throw new RuntimeException(context.toString()
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
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnSetupFinishListener {
        void onSetupFinish(int difficultyLevel);
    }
}
