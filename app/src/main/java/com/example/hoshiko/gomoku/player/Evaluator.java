package com.example.hoshiko.gomoku.player;




public class Evaluator {

    private static final Evaluator INSTANCE = new Evaluator();
    private static final int[] SCORES = {19, 15, 11, 7, 3};

    private Evaluator() {}

    /**
     * Get the evaluator instance.
     * @return Evaluator
     */
    public static Evaluator getInstance() {
        return INSTANCE;
    }

    /**
     * Given some array representing a vertical/horizontal/diagonal direction
     * on the board, calculate a score based on how many possible fives can be
     * formed and in how many moves.
     *
     * @param direction A 1D field array representing a direction on the board
     * @return Score for this direction
     */
    private static int scoreDirection(Field[] direction, int index) {
        int score = 0;

        // Pass a window of 5 across the field array
        for(int i = 0; (i + 4) < direction.length; i++) {
            int empty = 0;
            int stones = 0;
            for(int j = 0; j <= 4; j++) {
                if(direction[i + j].index == 0) {
                    empty++;
                }
                else if(direction[i + j].index == index) {
                    stones++;
                } else {
                    // Opponent stone in this window, can't form a five
                    break;
                }
            }
            // Ignore already formed fives, and empty windows
            if(empty == 0 || empty == 5) continue;

            // Window contains only empty spaces and player stones, can form
            // a five, get score based on how many moves needed
            if(stones + empty == 5) {
                score += SCORES[empty];
            }
        }
        return score;
    }

    /**
     * Evaluate a state from the perspective of the current player.
     * @param state State to evaluate
     * @return Score from the current players perspective
     */
    protected int evaluateState(State state, int depth) {
        int playerIndex = state.currentIndex;
        int opponentIndex = playerIndex == 1 ? 2 : 1;

        // Check for a winning/losing position
        int terminal = state.terminal();
        if(terminal == playerIndex) return 10000 + depth;
        if(terminal == opponentIndex) return -10000 - depth;

        // Evaluate each field separately, subtracting from the score if the
        // field belongs to the opponent, adding if it belongs to the player
        int score = 0;
        for(int i = 0; i < state.board.length; i++) {
            for(int j = 0; j < state.board.length; j++) {
                if(state.board[i][j].index == opponentIndex) {
                    score -= evaluateField(state, i, j, opponentIndex);
                } else if(state.board[i][j].index == playerIndex) {
                    score += evaluateField(state, i, j, playerIndex);
                }
            }
        }
        return score;
    }

    protected int evaluateField(State state, int row, int col, int index) {
        int score = 0;
        for(int direction = 0; direction < 4; direction++) {
            score += scoreDirection(state.directions[row][col][direction],
                    index);
        }
        return score;
    }

}