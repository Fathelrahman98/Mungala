package com.example.mungala;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.Stack;


public class GameFragment extends Fragment implements View.OnClickListener {

    // game type constants
    public static final int GAME_TYPE_SINGLE_PLAYER = 1;
    public static final int GAME_TYPE_LOCAL_MULTIPLAYER = 2;
    public static final int GAME_TYPE_BLUETOOTH_MULTIPLAYER = 3;

    // member variables:
    // handle UI related to game finish.
    private GameCoordinator.GameFinishListiner gameFinishListiner;
    // used to handle the game logic
    GameCoordinator gameCoordinator;
    // a customized runnable used to perform a single move UI changes
    PlayHoleThread playHole;
    //  a customized runnable used to go back to the previus game state
    SyncThread backMove;
    // a customized runnable used to handle bluetooth data exchange
    private ManageBluetoothMove manageBluetoothMove;
    // a customized runnable used to manage cpu moves. (to allow cpu star calculations about the next move before the ui changes start to occure.
    private ManageSingleMove manageSingleMove;
    // used to store all game states since the game start, to allow going back to resume from any one
    private Stack<GameState> gameStateStack;

    // game type variable
    private int gameType;

    // empty public constructor
    public GameFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // determine game type
        if (getActivity() instanceof SinglePlayerActivity) {
            gameType = GAME_TYPE_SINGLE_PLAYER;
            gameFinishListiner = (SinglePlayerActivity) getActivity();
        } else if (getActivity() instanceof BluetoothMultiplayerActivity) {
            gameType = GAME_TYPE_BLUETOOTH_MULTIPLAYER;
            gameFinishListiner = (BluetoothMultiplayerActivity) getActivity();
        } else if (getActivity() instanceof LocalMultiplayerActivity) {
            gameType = GAME_TYPE_LOCAL_MULTIPLAYER;
            gameFinishListiner = (LocalMultiplayerActivity) getActivity();
        }

        // initialize the stack
        gameStateStack = new Stack<>();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game, container, false);

        // add listiner to the button that performs 'back a move' action
        ImageButton backButton = view.findViewById(R.id.imageButton3);
        if (gameType == GAME_TYPE_BLUETOOTH_MULTIPLAYER) {
            backButton.setEnabled(false);
            backButton.setVisibility(View.INVISIBLE);
        } else {
            backButton.setEnabled(true);
            backButton.setVisibility(View.VISIBLE);
        }
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if a part of this method is already running in another thread, no need to start the method again before finish.
                if (backMove.isLocked()) {
                    return;
                }
                // lock playHole another time to avoid race condition with other blocks waiting for it.
                playHole.lock();
                // lock backMove for synchronization. to avoid running this part many times simultaniously. (see the if block above)
                backMove.lock();
                // stop the current playHole because its results are going to be cancelled ( because of the back action).
                playHole.setShouldStop(true);
                playHole.interrupt();

                // for single player games, lock the bot another time to avoid race condition with other blocks waiting for it.
                if (gameType == GAME_TYPE_SINGLE_PLAYER) {
                    SinglePlayerGameCoordinator g = (SinglePlayerGameCoordinator) gameCoordinator;
                    g.bot.lock();
                    g.bot.setShouldStop(true);
                    manageSingleMove.bot.interrupt();
                }
                // start performing 'back a move' action
                backMove.start();
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // initialize the basic parameters
        if (gameType == GAME_TYPE_SINGLE_PLAYER) {
            int difficulty = getActivity().getIntent().getIntExtra(Constants.EXTRA_DIFFICULTY_LEVEL,1);
            gameCoordinator = new SinglePlayerGameCoordinator(getContext(), gameFinishListiner,difficulty);
            manageSingleMove = new ManageSingleMove((SinglePlayerGameCoordinator) gameCoordinator, this);
        } else if (gameType == GAME_TYPE_BLUETOOTH_MULTIPLAYER) {
            BluetoothMultiplayerActivity activity = (BluetoothMultiplayerActivity) getContext();
            BluetoothService bluetoothService = activity.getBluetoothService();
            boolean hasFirstTurn = activity.hasFirstTurn();
            gameCoordinator = new BluetoothMultiplayerGameCoordinator(getContext(), gameFinishListiner, bluetoothService, hasFirstTurn);
            manageBluetoothMove = new ManageBluetoothMove((BluetoothMultiplayerGameCoordinator) gameCoordinator, this);
        } else {
            gameCoordinator = new LocalMultiplayerGameCoordinator(getContext(), gameFinishListiner);
        }
        // initialize the game and adding listiner to the holes.(buttons)
        gameCoordinator.setView(getView());
        gameCoordinator.setOnClickListener(this);
        gameCoordinator.initiateGame();

        //initialize playHole runnable
        playHole = new PlayHoleThread(getActivity(), gameCoordinator);

        backMove = new SyncThread() {
            @Override
            public void run() {
                if (gameType == GAME_TYPE_SINGLE_PLAYER) {
                    SinglePlayerGameCoordinator g = (SinglePlayerGameCoordinator) gameCoordinator;
                    // wait for bot to parially unlock. [not completely unlock, because it is locked intentionally just before calling this method]
                    while (g.bot.isPartiallyLocked(1)) {
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            if (shouldStop()) {
                                Log.d(Constants.MY_TAG,"out backMove");
                                return;
                            }
                        }
                    }
                    g.bot.setShouldStop(false);
                }
                // wait for playHole to parially unlock. [not completely unlock, because it is locked intentionally just before calling this method]
                while (playHole.isPartiallyLocked(1)) {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        if (shouldStop()) {
                            Log.d(Constants.MY_TAG,"out backMove");
                            return;
                        }
                    }
                }
                playHole.setShouldStop(false);

                // perform the "back a move" action.
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gameCoordinator.restoreGameState(gameStateStack);
                        gameCoordinator.switchPlayerRole();
                        gameCoordinator.switchPlayerRole();
                        playHole.unlock();
                        backMove.unlock();
                        if (gameType == GAME_TYPE_SINGLE_PLAYER) {
                            ((SinglePlayerGameCoordinator)gameCoordinator).bot.unlock();
                        }
                    }
                });
            }
        };

        // for bluetooth games, check if you don't have the first move, so you should strat receiving the move from the other device.
        if (gameType == GAME_TYPE_BLUETOOTH_MULTIPLAYER) {
            final BluetoothMultiplayerGameCoordinator coordinator = (BluetoothMultiplayerGameCoordinator) gameCoordinator;
            manageBluetoothMove.lock();
            Thread tmp = new Thread(new Runnable() {
                @Override
                public void run() {
                    manageBluetoothMove.listenAndPlayOpponentsMove();
                    manageBluetoothMove.unlock();
                }
            });
            manageBluetoothMove.setThread(tmp);
            if (!manageBluetoothMove.shouldStop()) {
                tmp.start();
            }
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // release the resources and stop the running threads
        if (gameStateStack != null) {
            gameStateStack.clear();
            gameStateStack = null;
        }
        if (manageBluetoothMove != null) {
            manageBluetoothMove.stop();
            manageBluetoothMove = null;
        }
        if (manageSingleMove != null) {
            manageSingleMove.stop();
            manageSingleMove = null;
        }
        if (playHole != null) {
            playHole.stop();
            playHole.moveMediaPlayer.release();
            playHole.takeMediaPlayer.release();
            playHole = null;
        }
        if (backMove != null) {
            backMove.stop();
            backMove = null;
        }
    }

    /**
     * performs the UI changes necessary to play a single move in a worker thread. It handles finishing the game and swithing the role.
     * @param view represents the hole that should be played. If the view doesn't represent a hole, the method just returns. If the view represents the middle hole and at the same time the hole doesn't hold symmetric number of marble balls a message is shown to the user and the method returns.
     */
    public void playHole(View view) {
        playHole.lock();
        int id = view.getId();
        Hole hole = null;
        int nextHoleIndex = -1;
        // determine the hole, the direction and the current hole.
        switch (id) {
            case R.id.button1:
                hole = gameCoordinator.holes[0];
                gameCoordinator.direction = false;
                nextHoleIndex = 0;
                break;
            case R.id.button2:
                hole = gameCoordinator.holes[1];
                gameCoordinator.direction = false;
                nextHoleIndex = 1;
                break;
            case R.id.button3:
                if ((gameCoordinator.holes[2].getNumberOfMarbleBalls() % 10) % 9 == 0) {
                    hole = gameCoordinator.holes[2];
                    gameCoordinator.direction = false;
                    nextHoleIndex = 2;
                    break;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), getContext().getResources().getString(R.string.center_hole_message), Toast.LENGTH_LONG).show();
                    }
                });
                playHole.unlock();
                return;
            case R.id.button10:
                hole = gameCoordinator.holes[2];
                gameCoordinator.direction = false;
                nextHoleIndex = 2;
                break;
            case R.id.button11:
                hole = gameCoordinator.holes[2];
                gameCoordinator.direction = true;
                nextHoleIndex = 2;
                break;
            case R.id.button4:
                hole = gameCoordinator.holes[3];
                gameCoordinator.direction = true;
                nextHoleIndex = 3;
                break;
            case R.id.button5:
                hole = gameCoordinator.holes[4];
                gameCoordinator.direction = true;
                nextHoleIndex = 4;
                break;
            case R.id.button:
                hole = gameCoordinator.holes[5];
                gameCoordinator.direction = true;
                nextHoleIndex = 5;
                break;
            case R.id.button6:
                hole = gameCoordinator.holes[6];
                gameCoordinator.direction = true;
                nextHoleIndex = 6;
                break;
            case R.id.button7:
                if ((gameCoordinator.holes[7].getNumberOfMarbleBalls() % 10) % 9 == 0) {
                    hole = gameCoordinator.holes[7];
                    gameCoordinator.direction = true;
                    nextHoleIndex = 7;
                    break;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), getContext().getResources().getString(R.string.center_hole_message), Toast.LENGTH_LONG).show();
                    }
                });
                playHole.unlock();
                return;
            case R.id.imageButton:
                hole = gameCoordinator.holes[7];
                gameCoordinator.direction = true;
                nextHoleIndex = 7;
                break;
            case R.id.imageButton2:
                hole = gameCoordinator.holes[7];
                gameCoordinator.direction = false;
                nextHoleIndex = 7;
                break;
            case R.id.button8:
                hole = gameCoordinator.holes[8];
                gameCoordinator.direction = false;
                nextHoleIndex = 8;
                break;
            case R.id.button9:
                hole = gameCoordinator.holes[9];
                gameCoordinator.direction = false;
                nextHoleIndex = 9;
                break;
        }
        // store the move except for bluetooth games and the cpu moves in single player mode.
        if (!(((gameType == GAME_TYPE_SINGLE_PLAYER) && gameCoordinator.isPlayer1CurrentPlayer)||(gameType == GAME_TYPE_BLUETOOTH_MULTIPLAYER))) {
            if (hole.isHoleAvailabe()) {
                gameCoordinator.storeGameState(gameStateStack);
            }
        }
        // start playing the move
        playHole.setHole(hole);
        playHole.setNextHoleIndex(nextHoleIndex);
        playHole.start();

    }

    @Override
    public void onClick(View v) {
        // if another move is already running return.
        if (playHole.isLocked()) {
            return;
        }

        // if the selected view represents a middle hole and it contains a symmetric number of marble balls (9,10,19,20,29,30,39,40,49,50) change the view to be arbitrarily the left button for the same hole (could be the right button, not a matter).
        switch (v.getId()) {
            case R.id.button3:
                if (((gameCoordinator.holes[2].getNumberOfMarbleBalls()%10)%9) == 0) {
                    v = getView().findViewById(R.id.button10);
                }
                break;
            case R.id.button7:
                if (((gameCoordinator.holes[7].getNumberOfMarbleBalls()%10)%9) == 0) {
                    v = getView().findViewById(R.id.imageButton);
                }
                break;
        }

        if (gameType == GAME_TYPE_BLUETOOTH_MULTIPLAYER) {
            if (manageBluetoothMove.isLocked()) {
                return;
            }

            // handles bluetooth moves. (it calls playHole at some point)
            manageBluetoothMove.setView(v);
            manageBluetoothMove.lock();
            manageBluetoothMove.start();
        } else if (gameType == GAME_TYPE_SINGLE_PLAYER) {
            if (manageSingleMove.isLocked()) {
                return;
            }
            // handle single player moves. (it calls playHole at some point)
            manageSingleMove.setView(v);
            manageSingleMove.lock();
            manageSingleMove.start();
        } else {
            // for local multiplayer games, just play the move.
            playHole(v);
        }

    }

}