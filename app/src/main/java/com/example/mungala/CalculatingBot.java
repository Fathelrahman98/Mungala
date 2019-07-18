package com.example.mungala;

import android.util.Log;

public class CalculatingBot extends SyncThread implements Bot{

    private static final int MAXIMUM_POSSIPLE_SCORE_DIFFERENCE = 50;
    private static final int MINIMUM_POSSIPLE_SCORE_DIFFERENCE = -50;
    private static final int STEP_BACK_DEPTH = 8;
    private GameCoordinator coordinator;
    private int[] board;
    private int level;
    private int move;
    private int calcMove;


    public CalculatingBot(GameCoordinator coordinator, int level) {
        this.coordinator = coordinator;
        board = new int[10];
        this.level = level;
        this.calcMove = -1;
    }

    /**
     * reads the board from the UI
     */
    public void setBoard() {
        for (int i = 0; i < 10 ; i++) {
            board[i] = coordinator.holes[i].getNumberOfMarbleBalls();
        }
    }

    /**
     * sets a move to be played before calculating the next cpu move.
     * @param move the move to be played in the board before calculating the next cpu move.
     */
    public void setMove(int move) {
        this.move = move;
    }

    @Override
    public int selectHole() {
        setBoard();
        while (isLocked());
        return calcMove;
    }

    /**
     * @param board the board in which the check is made.
     * @return whether player 1 has at least one valid move in the board.
     */
    private boolean hasPlayer1Move(int[] board) {
        for (int i = 0; i < 5; i++) {
            if (board[i] > 1) return true;
        }
        return false;
    }

    /**
     * @param board the board in which the check is made.
     * @return whether player 2 has at least one valid move in the board.
     */
    private boolean hasPlayer2Move(int[] board) {
        for (int i = 5; i < 10; i++) {
            if (board[i] > 1) return true;
        }
        return false;
    }

    /**
     * @param current the index of the current hole
     * @param direction the direction of movement
     * @return the index of the next hole according to the direction.
     */
    private int goToNext(int current, boolean direction) {
        if (direction) {
            if (current < 4) {
                return current + 1;
            }
            if (current == 4) {
                return 9;
            }
            if (current == 5) {
                return 0;
            }
            return current - 1;
        }
        if (current == 0) {
            return 5;
        }
        if (current < 5) {
            return current - 1;
        }
        if (current == 9) {
            return 4;
        }
        return current+1;
    }

    /**
     * check if a hole belongs to player 1.
     * @param move the hole index to be checked.
     * @return true if the hole belongs to player 1.
     */
    private boolean isPlayer1Move(int move) {
        return  ((move < 5) || (move == 10));
    }

    /**
     * apply a single move to a board.
     * @param board the board in which the move is to be done.
     * @param move the index of the hole from which the move is to start.
     * @return the number of taken marble balls by the move.
     */
    private int playMove(int[] board, int move) {
        boolean direction = false;
        boolean player1Move = isPlayer1Move(move);
        if ((move > 1) && (move < 8)) {
            direction = true;
        }
        if (move == 10) {
            move = 2;
        }
        if (move == 11) {
            move = 7;
        }
        int numberOfMarbleBalls = board[move];
        if (numberOfMarbleBalls < 2) {
            return -1;
        }
        board[move] = 0;
        while (numberOfMarbleBalls > 0) {
            move = goToNext(move, direction);
            board[move] = board[move] + 1;
            numberOfMarbleBalls = numberOfMarbleBalls - 1;
        }
        int takenMarbleBalls = 0;
        if (player1Move) {
            while (true) {
                if (move < 5) break;
                if (!((board[move] == 2) || (board[move] == 4))) break;
                takenMarbleBalls = takenMarbleBalls + board[move];
                board[move] = 0;
                move = goToNext(move, !(direction));
            }
        } else {
            while (true) {
                if (move > 4) break;
                if (!((board[move] == 2) || (board[move] == 4))) break;
                takenMarbleBalls = takenMarbleBalls + board[move];
                board[move] = 0;
                move = goToNext(move, !(direction));
            }
        }
        return takenMarbleBalls;
    }

    /**
     * try all options for the next (depth) moves in the board and use min max method and return the best found move.
     * @param board the board to start from.
     * @param depth the number of future moves to take into account.
     * @param turn determine whether player1 has the current turn or not.
     * @return the difference between player 1's and player 2's scores using the best options for both. from player 2's prespective, the answer should be multiplied by -1 before use.
     */
    private int getBestMove(int[] board, int depth, boolean turn) {
        int boardCopy[];
        if (depth <= 0) {
            return 0;
        }
        if (depth > STEP_BACK_DEPTH) {
            try {
                Thread.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
                if (shouldStop()) {
                    Log.d(Constants.MY_TAG,"out CalculatingBot");
                    unlock();
                    return 0;
                }
            }
        }
        if (turn) {
            int minimum = MAXIMUM_POSSIPLE_SCORE_DIFFERENCE;
            for (int i = 0; i < 5; i++) {
                if (board[i] < 2) {
                    continue;
                }
                boardCopy = board.clone();
                int x = playMove(boardCopy, i);
                int y = 0;
                if (!hasPlayer2Move(boardCopy)) {
                    if (hasPlayer1Move(boardCopy)) {
                        y = getBestMove(boardCopy, depth - 1, turn);
                    } else {
                        for (int j = 0; j < 5; j++) {
                            y = y - boardCopy[j];
                        }
                        for (int j = 5; j < 10; j++) {
                            y = y + boardCopy[j];
                        }
                    }
                } else {
                    y = getBestMove(boardCopy, depth - 1, ! turn);
                }
                int z = y - x;
                if (z < minimum) {
                    minimum = z;
                }
            }
            int i = 10;
            if (board[2] < 2) {
                return minimum;
            }
            boardCopy = board.clone();
            int x = playMove(boardCopy, i);
            int y = 0;
            if (!hasPlayer2Move(boardCopy)) {
                if (hasPlayer1Move(boardCopy)) {
                    y = getBestMove(boardCopy, depth - 1, turn);
                } else {
                    for (int j = 0; j < 5; j++) {
                        y = y - boardCopy[j];
                    }
                    for (int j = 5; j < 10; j++) {
                        y = y + boardCopy[j];
                    }
                }
            } else {
                y = getBestMove(boardCopy, depth - 1, !turn);
            }
            int z = y - x;
            if (z < minimum) {
                minimum = z;
            }
            return minimum;
        } else {
            int maximum = MINIMUM_POSSIPLE_SCORE_DIFFERENCE;
            for (int i = 5; i < 10; i++) {
                if (board[i] < 2) {
                    continue;
                }
                boardCopy = board.clone();
                int x = playMove(boardCopy, i);
                int y = 0;
                if (!hasPlayer1Move(boardCopy)) {
                    if (hasPlayer2Move(boardCopy)) {
                        y = getBestMove(boardCopy, depth - 1, turn);
                    } else {
                        for (int j = 0; j < 5; j++) {
                            y = y - boardCopy[j];
                        }
                        for (int j = 5; j < 10; j++) {
                            y = y + boardCopy[j];
                        }
                    }
                } else {
                    y = getBestMove(boardCopy, depth - 1, !turn);
                }
                int z = y + x;
                if (z > maximum) {
                    maximum = z;
                }
            }
            int i = 11;
            if (board[7] < 2) {
                return maximum;
            }
            boardCopy = board.clone();
            int x = playMove(boardCopy, i);
            int y = 0;
            if (!hasPlayer1Move(boardCopy)) {
                if (hasPlayer2Move(boardCopy)) {
                    y = getBestMove(boardCopy, depth - 1, turn);
                } else {
                    for (int j = 0; j < 5; j++) {
                        y = y - boardCopy[j];
                    }
                    for (int j = 5; j < 10; j++) {
                        y = y + boardCopy[j];
                    }
                }
            } else {
                y = getBestMove(boardCopy, depth - 1, !turn);
            }
            int z = y + x;
            if (z > maximum) {
                maximum = z;
            }
            return maximum;
        }
    }

    /**
     * try all options for the next (depth) moves in the board and use min max method and return the best found move and the score difference.
     * @param board the board to start from.
     * @param depth the number of future moves to take into account.
     * @param turn determine whether player1 has the current turn or not.
     * @return an array containing: 1. the difference between player 1's and player 2's scores using the best options for both. from player 2's prespective, the answer should be multiplied by -1 before use. 2. the first move in the selected scenario.
     */
    private int[] getBestMoveAndScoreDifference(int[] board, int depth, boolean turn) {
        int boardCopy[];
        if (depth <= 0) {
            return new int[] {0,-1};
        }
        if (turn) {
            int minimum = MAXIMUM_POSSIPLE_SCORE_DIFFERENCE;
            int move = -1;
            for (int i = 0; i < 5; i++) {
                if (board[i] < 2) {
                    continue;
                }
                boardCopy = board.clone();
                int x = playMove(boardCopy, i);
                int y = 0;
                if (!hasPlayer2Move(boardCopy)) {
                    if (hasPlayer1Move(boardCopy)) {
                        y = getBestMove(boardCopy, depth - 1, turn);
                    } else {
                        for (int j = 0; j < 5; j++) {
                            y = y - boardCopy[j];
                        }
                        for (int j = 5; j < 10; j++) {
                            y = y + boardCopy[j];
                        }
                    }
                } else {
                    y = getBestMove(boardCopy, depth - 1, ! turn);
                }
                int z = y - x;
                if (z < minimum) {
                    minimum = z;
                    move = i;
                }
            }
            int i = 10;
            if (board[2] < 2) {
                return new int[] {minimum,move};
            }
            boardCopy = board.clone();
            int x = playMove(boardCopy, i);
            int y = 0;
            if (!hasPlayer2Move(boardCopy)) {
                if (hasPlayer1Move(boardCopy)) {
                    y = getBestMove(boardCopy, depth - 1, turn);
                } else {
                    for (int j = 0; j < 5; j++) {
                        y = y - boardCopy[j];
                    }
                    for (int j = 5; j < 10; j++) {
                        y = y + boardCopy[j];
                    }
                }
            } else {
                y = getBestMove(boardCopy, depth - 1, !turn);
            }
            int z = y - x;
            if (z < minimum) {
                minimum = z;
                move = i;
            }
            return new int[] {minimum,move};
        } else {
            int maximum = MINIMUM_POSSIPLE_SCORE_DIFFERENCE;
            int move = -1;
            for (int i = 5; i < 10; i++) {
                if (board[i] < 2) {
                    continue;
                }
                boardCopy = board.clone();
                int x = playMove(boardCopy, i);
                int y = 0;
                if (!hasPlayer1Move(boardCopy)) {
                    if (hasPlayer2Move(boardCopy)) {
                        y = getBestMove(boardCopy, depth - 1, turn);
                    } else {
                        for (int j = 0; j < 5; j++) {
                            y = y - boardCopy[j];
                        }
                        for (int j = 5; j < 10; j++) {
                            y = y + boardCopy[j];
                        }
                    }
                } else {
                    y = getBestMove(boardCopy, depth - 1, !turn);
                }
                int z = y + x;
                if (z > maximum) {
                    maximum = z;
                    move = i;
                }
            }
            int i = 11;
            if (board[7] < 2) {
                return new int[] {maximum,move};
            }
            boardCopy = board.clone();
            int x = playMove(boardCopy, i);
            int y = 0;
            if (!hasPlayer1Move(boardCopy)) {
                if (hasPlayer2Move(boardCopy)) {
                    y = getBestMove(boardCopy, depth - 1, turn);
                } else {
                    for (int j = 0; j < 5; j++) {
                        y = y - boardCopy[j];
                    }
                    for (int j = 5; j < 10; j++) {
                        y = y + boardCopy[j];
                    }
                }
            } else {
                y = getBestMove(boardCopy, depth - 1, !turn);
            }
            int z = y + x;
            if (z > maximum) {
                maximum = z;
                move = i;
            }
            return new int[] {maximum,move};
        }
    }

    @Override
    public void run() {
        lock();
        boolean plr1 = isPlayer1Move(move);

        playMove(board,move);
        if (plr1) {
            if (!hasPlayer2Move(board)) {
                if (!hasPlayer1Move(board)) {
                    unlock();
                    return;
                } else {
                    int res[] = getBestMoveAndScoreDifference(board,level,true);
                    calcMove = res[1];
                    unlock();
                    return;
                }
            } else {
                unlock();
                return;
            }
        } else {
            if (hasPlayer1Move(board)) {
                int res[] = getBestMoveAndScoreDifference(board,level,true);
                calcMove = res[1];
                unlock();
                return;
            } else {
                return;
            }
        }
    }


//    boolean isFirstTurn() {
//        for (int i = 0; i < 10; i++) {
//            if (board[i] != 5) return false;
//        }
//        return true;
//    }

}
