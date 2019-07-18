package com.example.mungala;

import android.util.Log;
import android.view.View;

public class ManageSingleMove extends SyncThread {

    private SinglePlayerGameCoordinator coordinator;
    private int move;
    private GameFragment gameFragment;
    private View view;
    Bot bot;

    public ManageSingleMove(SinglePlayerGameCoordinator coordinator, GameFragment gameFragment) {
        this.coordinator = coordinator;
        this.gameFragment = gameFragment;
        this.bot = coordinator.bot;
    }

    public void setView(View view) {
        this.view = view;
        this.move = coordinator.boardFormat.getIndex(view);
    }


    @Override
    public void run() {

        sendMoveToOpponent();

        gameFragment.playHole(view);

        playOpponentsMoves();

        unlock();
    }

    private void sendMoveToOpponent() {
        bot.setBoard();
        bot.setMove(move);
        if (view.getId() != R.id.button7 || ((coordinator.holes[7].getNumberOfMarbleBalls() % 10) % 9) == 0) {
            bot.start();
        }
    }

    private void playOpponentsMoves() {
        while (true) {
            while (gameFragment.playHole.isLocked()) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.d(Constants.MY_TAG, "thread error");
                    if (shouldStop()) {
                        Log.d(Constants.MY_TAG,"out ManageSingleMove");
                        return;
                    }
                }
            }

            if (!coordinator.isPlayer1CurrentPlayer) {
                return;
            }

            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
                if (shouldStop()) {
                    Log.d(Constants.MY_TAG,"out ManageSingleMove");
                    return;
                }
            }

            while (bot.isLocked()) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    if (shouldStop()) {
                        Log.d(Constants.MY_TAG,"out ManageSingleMove");
                        return;
                    }
                }
            }

            bot.setBoard();
            int move = bot.selectHole();
            bot.setMove(move);
            bot.start();

            View v = coordinator.boardFormat.getView(move);
            View v2 = v;
            int id = v.getId();
            if (id == R.id.button10 || id == R.id.button11) {
                v2 = gameFragment.getView().findViewById(R.id.button3);
            }
            final View view = v2;
            gameFragment.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    view.setHovered(true);
                }
            });

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                if (shouldStop()) {
                    Log.d(Constants.MY_TAG,"out ManageSingleMove");
                    return;
                }
            }

            gameFragment.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    view.setHovered(false);
                }
            });

            while (gameFragment.playHole.isLocked()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    if (shouldStop()) {
                        Log.d(Constants.MY_TAG,"out ManageSingleMove");
                        return;
                    }
                }
            }

            if (!coordinator.isPlayer1CurrentPlayer) {
                return;
            }

            gameFragment.playHole(v);
        }
    }

    @Override
    public void stop() {
        super.stop();
        bot.stop();
    }
}
