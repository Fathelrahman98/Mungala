package com.example.mungala;

public interface SyncRunnable extends Runnable {

    boolean isLocked();

    boolean isPartiallyLocked(int x);

    void lock();

    void unlock();

    void stop();

    void setShouldStop(boolean shouldStop);

    void setThread(Thread thread);

    boolean shouldStop();

    void interrupt();

    void start();


}

