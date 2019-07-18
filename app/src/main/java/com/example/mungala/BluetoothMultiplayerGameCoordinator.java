package com.example.mungala;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

public class BluetoothMultiplayerGameCoordinator extends GameCoordinator {

    BluetoothService bluetoothService;
    private boolean hasFirstTurn;
    public com.example.mungala.BoardFormat boardFormat;

    BluetoothMultiplayerGameCoordinator(Context context, GameFinishListiner gameFinishListiner, BluetoothService bluetoothService, boolean hasFirstTurn) {
        super(context,gameFinishListiner);
        this.bluetoothService = bluetoothService;
        this.hasFirstTurn = hasFirstTurn;
        boardFormat = new BoardFormat();
    }

    @Override
    void initiateHoles() {
        buttons = new ImageButton[]{view.findViewById(R.id.button1),
                view.findViewById(R.id.button2), view.findViewById(R.id.button3),
                view.findViewById(R.id.button4), view.findViewById(R.id.button5),
                view.findViewById(R.id.button) , view.findViewById(R.id.button6),
                view.findViewById(R.id.button7), view.findViewById(R.id.button8),
                view.findViewById(R.id.button9), view.findViewById(R.id.button10),
                view.findViewById(R.id.button11), view.findViewById(R.id.imageButton),
                view.findViewById(R.id.imageButton2)};
        holes = new Hole[]{
                new Hole(buttons[0]), new Hole(buttons[1]), new Hole(buttons[2]), new Hole(buttons[3]),
                new Hole(buttons[4]), new Hole(buttons[5]), new Hole(buttons[6]), new Hole(buttons[7]),
                new Hole(buttons[8]), new Hole(buttons[9])};
        setPlayer1ButtonsClickable(false);
        buttons[5].setOnClickListener(onClickListener);
        buttons[6].setOnClickListener(onClickListener);
        buttons[7].setOnClickListener(onClickListener);
        buttons[8].setOnClickListener(onClickListener);
        buttons[9].setOnClickListener(onClickListener);
        buttons[12].setOnClickListener(onClickListener);
        buttons[13].setOnClickListener(onClickListener);
    }

    @Override
    void moveRoleToPlayer1() {
        setPlayer2ButtonsClickable(false);
    }

    @Override
    void moveRoleToPlayer2() {
        setPlayer2ButtonsClickable(true);
    }

    @Override
    void disableActiveButtons() {
        setPlayer2ButtonsClickable(false);
    }

    @Override
    void initiateRole() {
        if (hasFirstTurn) {
            isPlayer1CurrentPlayer = false;
            moveRoleToPlayer2();
        } else {
            isPlayer1CurrentPlayer = true;
            moveRoleToPlayer1();
        }
    }

    class BoardFormat implements com.example.mungala.BoardFormat {

        @Override
        public View getView(int x) {
            if (x < 0 || x > 5) {
                Log.d(Constants.MY_TAG,"invalid view");
                return null;
            }
            if (x == 2 || x == 3) {
                return buttons[x+8];
            }
            if (x < 2) {
                return buttons[x];
            }
            return buttons[x-1];
        }

        @Override
        public int getIndex(View v) {
            switch (v.getId()) {
                case R.id.button9:
                    return 5;
                case R.id.button8:
                    return 4;
                case R.id.imageButton2:
                    return 3;
                case R.id.imageButton:
                    return 2;
                case R.id.button6:
                    return 1;
                case R.id.button:
                    return 0;
                case R.id.button7:
                    return 16;
                default:
                    return 15;
            }
        }
    }

    @Override
    int getFinishGameTextId() {
        if (player1.getScore() > 25) {
            return R.string.you_lost;
        } else if (player2.getScore() > 25) {
            return R.string.you_win;
        } else {
            return R.string.draw;
        }
    }
}
