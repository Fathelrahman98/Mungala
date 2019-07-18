package com.example.mungala;

import android.util.Log;
import android.view.View;

public class ManageBluetoothMove extends SyncThread {
    private BluetoothMultiplayerGameCoordinator coordinator;
    private byte move;
    private GameFragment gameFragment;
    private View view;

    public ManageBluetoothMove(BluetoothMultiplayerGameCoordinator coordinator, GameFragment gameFragment) {
        this.coordinator = coordinator;
        this.gameFragment = gameFragment;
    }

    // sets the view of the next move.
    public void setView(View view) {
        this.view = view;
        this.move = (byte)coordinator.boardFormat.getIndex(view);
    }

    @Override
    public void run() {
        // send the move to the other device
        if (!sendMoveToOpponent()) {
            unlock();
            return;
        }

        // play the move.
        gameFragment.playHole(view);

        // start receiving the move and play it.
        listenAndPlayOpponentsMove();

        // unlock to allow the next move.
        unlock();

    }

    /**
     * sends the move to the other device if the move to be sent is invalid just return.
     * @return true if the move is successfully sent.
     */
    private boolean sendMoveToOpponent()  {
        if (move == Constants.INVALID_MOVE) {
            gameFragment.playHole(view);
            return false;
        }
        if (!coordinator.holes[coordinator.getPlayer2HoleIndex(view)].isHoleAvailabe()) {
            gameFragment.playHole(view);
            return false;
        }

        while (!coordinator.bluetoothService.sendMove(move)) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
                if (shouldStop()) {
                    Log.d(Constants.MY_TAG,"out ManageBluetoothMove");
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * waits untill receiving a move from the other device and play it.
     */
    public void listenAndPlayOpponentsMove() {
        while (true) {
            while (gameFragment.playHole.isLocked()) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.d(Constants.MY_TAG, "thread error");
                    if (shouldStop()) {
                        Log.d(Constants.MY_TAG,"out ManageBluetoothMove");
                        return;
                    }
                }
            }
            if (!coordinator.isPlayer1CurrentPlayer) {
                return;
            }

            byte move;
            move = coordinator.bluetoothService.receiveMove();
            while (move == Constants.INVALID_MOVE) {
                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    if (shouldStop()) {
                        Log.d(Constants.MY_TAG,"out ManageBluetoothMove");
                        return;
                    }
                }
                move = coordinator.bluetoothService.receiveMove();
            }
            if (move == Constants.NO_CONNECTION) {
                return;
            }
            gameFragment.playHole(coordinator.boardFormat.getView(move));
        }
    }


}
