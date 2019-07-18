package com.example.mungala;

import android.util.Log;

public abstract class SyncThread implements SyncRunnable {
    private int lockLevel;
    private boolean shouldStop;
    private Thread thread;

    public SyncThread() {
        lockLevel = 0;
        shouldStop = false;
    }

    public boolean isLocked() {
        return this.lockLevel > 0;
    }

    public boolean isPartiallyLocked(int x) {
        return this.lockLevel > x;
    }

    public void lock() {
        this.lockLevel++;
    }

    public void unlock() {
        this.lockLevel--;
        if (this.lockLevel < 0) {
            this.lockLevel = 0;
        }
    }

    public void stop() {
        shouldStop =  true;
        if (thread != null) {
            thread.interrupt();
        }
        while (isLocked()) {
            Log.d(Constants.MY_TAG,this.getClass().getName()+" locked: "+lockLevel);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setShouldStop(boolean shouldStop) {
        this.shouldStop = shouldStop;
    }

    @Override
    public void setThread(Thread thread) {
        this.thread = thread;
    }

    @Override
    public boolean shouldStop() {
        return shouldStop;
    }

    @Override
    public void interrupt() {
        if (thread != null) {
            thread.interrupt();
        }
    }

    @Override
    public void start() {
        thread = new Thread(this);
        thread.start();
    }
}
