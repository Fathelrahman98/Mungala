package com.example.mungala;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Stack;

/**
 * handles the game logic with interaction with the UI
 */
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
    private GameFinishListiner gameFinishListiner;

    GameCoordinator(Context context, GameFinishListiner gameFinishListiner) {
        this.context = context;
        this.gameFinishListiner = gameFinishListiner;
    }

    // the view that contains the holes buttons
    public void setView(View view) {
        this.view = view;
    }

    // sets the listiner for the holes buttons
    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    /**
     * assign veriables with the initial game values.
     */
    void initiateGame() {
        initiatePlayers();
        initiateHoles();
        initiateRole();
    }

    /**
     * assign players veriables with the initial game values.
     */
    private void initiatePlayers() {
        TextView tv = view.findViewById(R.id.textView1);
        player1 = new Player(tv);
        tv = view.findViewById(R.id.textView2);
        player2 = new Player(tv);
    }

    /**
     * should assign holes variables and deal with the buttons.
     */
    abstract void initiateHoles();

    /**
     * gives the first role to player2 (the default)
     */
    void initiateRole() {
        isPlayer1CurrentPlayer = false;
        moveRoleToPlayer2();
    }

    /**
     * should disable any buttons that allow player 2 to play.
     * should enable any buttons that allow player 1 to play.
     */
    abstract void moveRoleToPlayer1();

    /**
     * should disable any buttons that allow player 1 to play.
     * should enable any buttons that allow player 2 to play.
     */
    abstract void moveRoleToPlayer2();

    /**
     *
     * @param currentHoleIndex the index of the hole we are in while distributing the marbel balls.
     * @return the index of the next hole to move to so as to put the next marbel ball, returns -1 for invalid currentHoleIndex
     */
    int getNextHoleIndex(int currentHoleIndex) {
        if (currentHoleIndex < 0 || currentHoleIndex > 9) {
            return -1;
        }
        if (direction) {
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
        } else {
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

    /**
     * @param holeIndex the index of the hole to be checked
     * @return true if the hole of holeIndex belongs to the player that has the current role. false otherwise.
     */
    boolean isHoleBelongsToCurrentPlayer(int holeIndex) {
        if (isPlayer1CurrentPlayer) {
            return (holeIndex < 5 || holeIndex == 10 || holeIndex == 11);
        } else {
            return (holeIndex > 4 && holeIndex != 10 && holeIndex != 11);
        }
    }

    /**
     * adds the value of increment to the score of the that player has the current role.
     * @param increment the value to be added to the score of the player.
     */
    void addToScore(int increment) {
        if (isPlayer1CurrentPlayer) {
            player1.incrementScore(increment);
        } else {
            player2.incrementScore(increment);
        }
    }

    /**
     *
     * @return true if player 1 has at least one valid move. flase otherwise.
     */
    private boolean hasPlayer1Moves() {
        int i = 0;
        while (i<5) {
            if (holes[i].isHoleAvailabe()) {
                return true;
            }
            i++;
        }
        return false;
    }

    /**
     *
     * @return true if player 2 has at least one valid move. flase otherwise.
     */
    private boolean hasPlayer2Moves() {
        int i = 5;
        while (i<10) {
            if (holes[i].isHoleAvailabe()) {
                return true;
            }
            i++;
        }
        return false;
    }

    /**
     * switches the current role to the other player if he has at least one valid move. if the other player has not, the role remains to the current player if has at least one valid move. if both don't have any valid moves, the role remains.
     * @return true if any of the players has at least one valid move. false otherwise.
     */
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

    /**
     * finishes the game by adding any single marbel ball found alone in a hole to the score of the player who owns the hole.
     * and disable the holes.
     */
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
        gameFinishListiner.finishGame(getFinishGameTextId());
    }

    abstract int getFinishGameTextId();

    /**
     * should disable all the holes.
     */
    abstract void disableActiveButtons();

    /**
     * sets all the holes the belong to player 1 to be clickable or not.
     * @param clickable determines whether the holes will be clickable or not.
     */
    void setPlayer1ButtonsClickable(boolean clickable) {
        buttons[0].setEnabled(clickable);
        buttons[1].setEnabled(clickable);
        buttons[2].setEnabled(clickable);
        buttons[3].setEnabled(clickable);
        buttons[4].setEnabled(clickable);
        buttons[10].setEnabled(clickable);
        buttons[11].setEnabled(clickable);
    }

    /**
     * sets all the holes the belong to player 2 to be clickable or not.
     * @param clickable determines whether the holes will be clickable or not.
     */
    void setPlayer2ButtonsClickable(boolean clickable) {
        buttons[5].setEnabled(clickable);
        buttons[6].setEnabled(clickable);
        buttons[7].setEnabled(clickable);
        buttons[8].setEnabled(clickable);
        buttons[9].setEnabled(clickable);
        buttons[12].setEnabled(clickable);
        buttons[13].setEnabled(clickable);
    }


    /**
     * @param view a view represents a hole.
     * @return the index in holes of the hole with the view attached to it.
     */
    int getPlayer2HoleIndex(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.button:
                return 5;
            case R.id.button6:
                return 6;
            case R.id.imageButton:
                return 7;
            case R.id.imageButton2:
                return 7;
            case R.id.button8:
                return 8;
            case R.id.button9:
                return 9;
        }
        Log.d(Constants.MY_TAG, "invalid view");
        return -1;
    }

    /**
     * stores the current state of the game in the top of the passed stack.
     * @param gameStateStack where the current state of the game is stored.
     */
    public void storeGameState (Stack<GameState> gameStateStack) {
        int score1 = player1.getScore();
        int score2 = player2.getScore();
        int board[] = new int[] {holes[0].getNumberOfMarbleBalls(),holes[1].getNumberOfMarbleBalls(),holes[2].getNumberOfMarbleBalls(),
                holes[3].getNumberOfMarbleBalls(),holes[4].getNumberOfMarbleBalls(),holes[5].getNumberOfMarbleBalls(),
                holes[6].getNumberOfMarbleBalls(),holes[7].getNumberOfMarbleBalls(),holes[8].getNumberOfMarbleBalls(),
                holes[9].getNumberOfMarbleBalls()};
        boolean p1 = isPlayer1CurrentPlayer;
        GameState gameState = new GameState(board,score1,score2,isPlayer1CurrentPlayer);
        gameStateStack.push(gameState);
    }

    /**
     * restore the last stored game state from the passed stack.
     * @param gameStateStack from where the next game state to be restored.
     */
    public void restoreGameState(Stack<GameState> gameStateStack) {
        if (gameStateStack.empty()) return;
        GameState gameState = gameStateStack.pop();
        player1.setScore(gameState.getScore1());
        player2.setScore(gameState.getScore2());
        int board[] = gameState.getBoard();
        holes[0].setNumberOfMarbleBalls(board[0]);
        holes[1].setNumberOfMarbleBalls(board[1]);
        holes[2].setNumberOfMarbleBalls(board[2]);
        holes[3].setNumberOfMarbleBalls(board[3]);
        holes[4].setNumberOfMarbleBalls(board[4]);
        holes[5].setNumberOfMarbleBalls(board[5]);
        holes[6].setNumberOfMarbleBalls(board[6]);
        holes[7].setNumberOfMarbleBalls(board[7]);
        holes[8].setNumberOfMarbleBalls(board[8]);
        holes[9].setNumberOfMarbleBalls(board[9]);
        isPlayer1CurrentPlayer = gameState.isPlayer1CurrentPlayer();
    }

    interface GameFinishListiner {
        void finishGame(int finishGameMessageId);
    }

}
