package com.example.mungala;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.widget.Toast;

class PlayHoleThread implements Runnable {

    private static final int MOVE_DELAY = 400;
    private static final int TAKE_DELAY = 400;
    private static boolean running;
    private Hole hole;
    private int nextHoleIndex;
    private GameCoordinator gameCoordinator;
    private Activity activity;
    private MediaPlayer moveMediaPlayer;
    private MediaPlayer takeMediaPlayer;

    public boolean isRunning() {
        return running;
    }

    public void lock() {
        running = true;
    }

    public void unlock() {
        running = false;
    }

    public PlayHoleThread(Activity activity, GameCoordinator gameCoordinator) {
        unlock();
        this.gameCoordinator = gameCoordinator;
        this.activity = activity;
        moveMediaPlayer = MediaPlayer.create(activity,R.raw.nff_select_04);
        takeMediaPlayer = MediaPlayer.create(activity,R.raw.nff_switchy);
    }

    public void setHole(Hole hole) {
        this.hole = hole;
    }

    public void setNextHoleIndex(int nextHoleIndex) {
        this.nextHoleIndex = nextHoleIndex;
    }

    @Override
    public void run() {
        try {

            int marbleBalls = hole.playHole();
            while(marbleBalls > 0) {
                moveMediaPlayer.start();
                nextHoleIndex = gameCoordinator.getNextHoleIndex(nextHoleIndex);
                gameCoordinator.holes[nextHoleIndex].addMarbleBall();
                marbleBalls--;
                Thread.sleep(MOVE_DELAY);
            }

            gameCoordinator.direction = !gameCoordinator.direction;
            while (gameCoordinator.holes[nextHoleIndex].isHoleTaken() && !gameCoordinator.isHoleBelongsToCurrentPlayer(nextHoleIndex)) {
                takeMediaPlayer.start();
                final int increment = gameCoordinator.holes[nextHoleIndex].playHole();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gameCoordinator.addToScore(increment);
                    }
                });
                nextHoleIndex = gameCoordinator.getNextHoleIndex(nextHoleIndex);
                Thread.sleep(TAKE_DELAY);
            }
            gameCoordinator.direction = !gameCoordinator.direction;

            if (!gameCoordinator.switchPlayerRole()) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gameCoordinator.finishGame();
                        moveMediaPlayer.release();
                        takeMediaPlayer.release();
                    }
                });
            };
        } catch (Hole.HoleNotAvailableException e) {
            final String message = e.getMessage();
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity,message,Toast.LENGTH_LONG).show();
                }
            });
        } catch (NullPointerException e) {

        } catch (InterruptedException e) {

        }
        finally {
            unlock();
        }
    }

}
