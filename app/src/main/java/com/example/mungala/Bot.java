package com.example.mungala;

public interface Bot extends SyncRunnable {

    void setBoard();
    void setMove(int move);

    int selectHole();
}
