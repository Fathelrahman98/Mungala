package com.example.mungala;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageButton;

public class SinglePlayerGameCoordinator extends GameCoordinator {
    Bot bot;

    SinglePlayerGameCoordinator(Context context) {
        super(context);
        bot = new SimpleBot();
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

    View decodeView(int x) {
        if (x == 2 || x == 3) {
            return buttons[x+8];
        }
        if (x < 2) {
            return buttons[x];
        } return buttons[x-1];
    }

    int getSelectedHoleIndex() {
        int x = bot.selectHole();
        //int x = 1;
        if (x == 2 || x == 3) {
            if (holes[2].isHoleAvailabe()) {
                return x;
            } return getSelectedHoleIndex();
        }
        if (x < 2) {
            if (holes[x].isHoleAvailabe()) {
                return x;
            }
            return getSelectedHoleIndex();
        }
        if (holes[x-1].isHoleAvailabe()) {
            return x;
        } return getSelectedHoleIndex();
    }
}

