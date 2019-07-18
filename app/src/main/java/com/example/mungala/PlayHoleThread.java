package com.example.mungala;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

class PlayHoleThread extends SyncThread {

    private static final int MOVE_DELAY = 400;
    private static final int TAKE_DELAY = 400;

    private Hole hole;
    private int nextHoleIndex;
    private GameCoordinator gameCoordinator;
    private Activity activity;
    MediaPlayer moveMediaPlayer;
    MediaPlayer takeMediaPlayer;


    public PlayHoleThread(Activity activity, GameCoordinator gameCoordinator) {
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

            // take marbel balls from the played hole and distribute them in the holes according to the rule of the game. with a delay between any two moves to make them recognizable by human eye.
            int marbleBalls = hole.playHole();
            while(marbleBalls > 0) {
                moveMediaPlayer.start();
                nextHoleIndex = gameCoordinator.getNextHoleIndex(nextHoleIndex);
                gameCoordinator.holes[nextHoleIndex].addMarbleBall();
                marbleBalls--;
                Thread.sleep(MOVE_DELAY);
            }

            // take marbel balls from the last holes and add them to the score of the player according to the roles of the game.
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

            // switch the role according to the rules of the game. Finish the game if no player has a valid move.
            if (!gameCoordinator.switchPlayerRole()) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gameCoordinator.finishGame();

                    }
                });
            }
        } catch (Hole.HoleNotAvailableException e) {
            final String message = activity.getResources().getString(R.string.few_marble_balls);
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity,message,Toast.LENGTH_LONG).show();
                }
            });
        } catch (NullPointerException e) {
            Log.d(Constants.MY_TAG,e.getMessage());
        } catch (InterruptedException e) {
            Log.d(Constants.MY_TAG,e.toString());
            if (shouldStop()) {
                Log.d(Constants.MY_TAG,"out playHoleThread");
                return;
            }
        }
        finally {
            unlock();
        }
    }

}
