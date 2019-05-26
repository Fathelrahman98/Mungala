package com.example.mungala;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageButton;

public class LocalMultiplayerGameCoordinator extends GameCoordinator {

    LocalMultiplayerGameCoordinator(Context context) {
        super(context);
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
        buttons[0].setOnClickListener(onClickListener);
        buttons[1].setOnClickListener(onClickListener);
        buttons[2].setOnClickListener(onClickListener);
        buttons[3].setOnClickListener(onClickListener);
        buttons[4].setOnClickListener(onClickListener);
        buttons[5].setOnClickListener(onClickListener);
        buttons[6].setOnClickListener(onClickListener);
        buttons[7].setOnClickListener(onClickListener);
        buttons[8].setOnClickListener(onClickListener);
        buttons[9].setOnClickListener(onClickListener);
        buttons[10].setOnClickListener(onClickListener);
        buttons[11].setOnClickListener(onClickListener);
        buttons[12].setOnClickListener(onClickListener);
        buttons[13].setOnClickListener(onClickListener);
    }

    @Override
    void moveRoleToPlayer1() {
        setPlayer2ButtonsClickable(false);
        setPlayer1ButtonsClickable(true);
    }

    @Override
    void moveRoleToPlayer2() {
        setPlayer2ButtonsClickable(true);
        setPlayer1ButtonsClickable(false);
    }

    @Override
    void disableActiveButtons() {
        setPlayer1ButtonsClickable(false);
        setPlayer2ButtonsClickable(false);
    }

}
