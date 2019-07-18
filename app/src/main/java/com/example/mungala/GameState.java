package com.example.mungala;

public class GameState {

    private int[] board;
    private int score1;
    private int score2;
    private boolean isPlayer1CurrentPlayer;

    public GameState(int[] board, int score1, int score2, boolean isPlayer1CurrentPlayer) {
        this.board = board;
        this.score1 = score1;
        this.score2 = score2;
        this.isPlayer1CurrentPlayer = isPlayer1CurrentPlayer;
    }

    public int[] getBoard() {
        return board;
    }

    public int getScore1() {
        return score1;
    }

    public int getScore2() {
        return score2;
    }

    public boolean isPlayer1CurrentPlayer() {
        return isPlayer1CurrentPlayer;
    }
}
