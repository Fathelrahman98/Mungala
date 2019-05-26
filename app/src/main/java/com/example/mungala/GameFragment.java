package com.example.mungala;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


public class GameFragment extends Fragment implements View.OnClickListener {
    GameCoordinator gameCoordinator;
    PlayHoleThread playHoleThread;

    public GameFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_game, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (isGameSinglePlayer()) {
            gameCoordinator = new SinglePlayerGameCoordinator(getContext());
        } else {
            gameCoordinator = new LocalMultiplayerGameCoordinator(getContext());
        }
        gameCoordinator.setView(getView());
        gameCoordinator.setOnClickListener(this);
        //gameCoordinator = new SinglePlayerGameCoordinator(this);
        gameCoordinator.initiateGame();
        playHoleThread = new PlayHoleThread(getActivity(),gameCoordinator);
    }

    public void playHole(View view) {
        playHoleThread.lock();
        int id = view.getId();
        Hole hole = null;
        int nextHoleIndex = -1;
        switch(id) {
            case R.id.button1:
                hole = gameCoordinator.holes[0];
                gameCoordinator.direction = false;
                nextHoleIndex = 0;
                break;
            case R.id.button2:
                hole = gameCoordinator.holes[1];
                gameCoordinator.direction = false;
                nextHoleIndex = 1;
                break;
            case R.id.button3:
                Toast.makeText(getContext(),"select either the left or the right button",Toast.LENGTH_LONG).show();
                playHoleThread.unlock();
                return;
            case R.id.button10:
                hole = gameCoordinator.holes[2];
                gameCoordinator.direction = false; // todo , bidirectional
                nextHoleIndex = 2;
                break;
            case R.id.button11:
                hole = gameCoordinator.holes[2];
                gameCoordinator.direction = true; // todo , bidirectional
                nextHoleIndex = 2;
                break;
            case R.id.button4:
                hole = gameCoordinator.holes[3];
                gameCoordinator.direction = true;
                nextHoleIndex = 3;
                break;
            case R.id.button5:
                hole = gameCoordinator.holes[4];
                gameCoordinator.direction = true;
                nextHoleIndex = 4;
                break;
            case R.id.button:
                hole = gameCoordinator.holes[5];
                gameCoordinator.direction = true;
                nextHoleIndex = 5;
                break;
            case R.id.button6:
                hole = gameCoordinator.holes[6];
                gameCoordinator.direction = true;
                nextHoleIndex = 6;
                break;
            case R.id.button7:
                Toast.makeText(getContext(),"select either the left or the right button",Toast.LENGTH_LONG).show();
                playHoleThread.unlock();
                return;
            case R.id.imageButton:
                hole = gameCoordinator.holes[7];
                gameCoordinator.direction = true;  // todo , bidirectional
                nextHoleIndex = 7;
                break;
            case R.id.imageButton2:
                hole = gameCoordinator.holes[7];
                gameCoordinator.direction = false;  // todo , bidirectional
                nextHoleIndex = 7;
                break;
            case R.id.button8:
                hole = gameCoordinator.holes[8];
                gameCoordinator.direction = false;
                nextHoleIndex = 8;
                break;
            case R.id.button9:
                hole = gameCoordinator.holes[9];
                gameCoordinator.direction = false;
                nextHoleIndex = 9;
                break;
        }
        playHoleThread.setHole(hole);
        playHoleThread.setNextHoleIndex(nextHoleIndex);
        new Thread(playHoleThread).start();

        }


    boolean isGameSinglePlayer() {
        return (getActivity() instanceof SinglePlayerActivity);
    }

    @Override
    public void onClick(View v) {
        if (playHoleThread.isRunning()) {
            return;
        }
        playHole(v);
        if (isGameSinglePlayer()) {
            final SinglePlayerGameCoordinator coordinator = (SinglePlayerGameCoordinator) gameCoordinator;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (playHoleThread.isRunning());
                    if (!gameCoordinator.isPlayer1CurrentPlayer) {
                        return;
                    }
                    playHole(coordinator.decodeView(coordinator.getSelectedHoleIndex()));
                    while (playHoleThread.isRunning());
                    if (gameCoordinator.isPlayer1CurrentPlayer) {
                        run();
                    }
                }
            }).start();
        }
    }
}
