package com.example.mungala;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public abstract class GameCoordinator {
    Player player1;
    Player player2;
    Hole[] holes;
    ImageButton[] buttons;
    boolean isPlayer1CurrentPlayer;
    boolean direction;
    Context context;
    View view;
    View.OnClickListener onClickListener;

    GameCoordinator(Context context) {
        this.context = context;
    }

    public void setView(View view) {
        this.view = view;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    void initiateGame() {
        initiatePlayers();
        initiateHoles();
        initiateRole();
    }

    void initiatePlayers() {
        TextView tv = view.findViewById(R.id.textView1);
        player1 = new Player(tv);
        tv = view.findViewById(R.id.textView2);
        player2 = new Player(tv);
    }

    abstract void initiateHoles();

    void initiateRole() {
        isPlayer1CurrentPlayer = false;
        moveRoleToPlayer2();
    }

    abstract void moveRoleToPlayer1();

    abstract void moveRoleToPlayer2();

    int getNextHoleIndex(int currentHoleIndex) {
        if (currentHoleIndex < 0 || currentHoleIndex > 9) {
            return -1;
        }
        if (direction == true) {
            if (currentHoleIndex < 4) {
                return currentHoleIndex + 1;
            }
            if (currentHoleIndex == 4) {
                return 9;
            }
            if (currentHoleIndex == 5) {
                return 0;
            }
            if (currentHoleIndex > 5) {
                return currentHoleIndex - 1;
            }
        }
        if (direction == false) {
            if (currentHoleIndex == 0) {
                return 5;
            }
            if (currentHoleIndex < 5) {
                return currentHoleIndex - 1;
            }
            if (currentHoleIndex < 9) {
                return currentHoleIndex + 1;
            }
            if (currentHoleIndex == 9) {
                return 4;
            }
        }
        return -1;
    }

    boolean isHoleBelongsToCurrentPlayer(int holeIndex) {
        if (isPlayer1CurrentPlayer) {
            return (holeIndex < 5 || holeIndex == 10 || holeIndex == 11);
        } else {
            return (holeIndex > 4 && holeIndex != 10 && holeIndex != 11);
        }
    }

    void addToScore(int increment) {
        if (isPlayer1CurrentPlayer) {
            player1.incrementScore(increment);
        } else {
            player2.incrementScore(increment);
        }
    }

    boolean hasPlayer1Moves() {
        int i = 0;
        while (i<5) {
            if (holes[i].isHoleAvailabe()) {
                return true;
            }
            i++;
        }
        return false;
    }

    boolean hasPlayer2Moves() {
        int i = 5;
        while (i<10) {
            if (holes[i].isHoleAvailabe()) {
                return true;
            }
            i++;
        }
        return false;
    }

    boolean switchPlayerRole () { // true if game has not finished yet
        isPlayer1CurrentPlayer = !isPlayer1CurrentPlayer;
        if (isPlayer1CurrentPlayer) {
            if (hasPlayer1Moves()) {
                moveRoleToPlayer1();
            } else if (hasPlayer2Moves()) {
                isPlayer1CurrentPlayer = !isPlayer1CurrentPlayer;
                moveRoleToPlayer2();
            } else {
                return false;
            }
        } else {
            if (hasPlayer2Moves()) {
                moveRoleToPlayer2();
            } else if (hasPlayer1Moves()){
                isPlayer1CurrentPlayer = !isPlayer1CurrentPlayer;
                moveRoleToPlayer1();
            } else {
                return false;
            }
        }
        return true;
    }

    void finishGame() {
        disableActiveButtons();
        try {
            for (int i = 0; i < 5; i++) {
                player1.incrementScore(holes[i].clearHole());
            }
            for (int i = 5; i < 10; i++) {
                player2.incrementScore(holes[i].clearHole());
            }
        } catch (Hole.HoleNotClearableException e) {
            Toast.makeText(context,e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    abstract void disableActiveButtons();

    void setPlayer1ButtonsClickable(boolean clickable) {
        buttons[0].setClickable(clickable);
        buttons[1].setClickable(clickable);
        buttons[2].setClickable(clickable);
        buttons[3].setClickable(clickable);
        buttons[4].setClickable(clickable);
        buttons[10].setClickable(clickable);
        buttons[11].setClickable(clickable);
    }

    void setPlayer2ButtonsClickable(boolean clickable) {
        buttons[5].setClickable(clickable);
        buttons[6].setClickable(clickable);
        buttons[7].setClickable(clickable);
        buttons[8].setClickable(clickable);
        buttons[9].setClickable(clickable);
        buttons[12].setClickable(clickable);
        buttons[13].setClickable(clickable);
    }
}
