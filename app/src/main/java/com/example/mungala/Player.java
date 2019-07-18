package com.example.mungala;

import android.widget.TextView;

public class Player {

    private int score;
    private TextView scoreView;

    Player(TextView scoreView) {
        score = 0;
        this.scoreView = scoreView;
        this.scoreView.setText(""+score);
    }

    public boolean incrementScore(int increment) {
        if (increment == 2 || increment == 4 || increment == 1 || increment == 0) {
            score += increment;
            scoreView.setText(""+score);
            return true;
        }
        return false;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
        scoreView.setText(""+score);
    }
}
