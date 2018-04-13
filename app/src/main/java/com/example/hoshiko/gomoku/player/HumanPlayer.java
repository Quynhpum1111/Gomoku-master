package com.example.hoshiko.gomoku.player;

import com.example.hoshiko.gomoku.game.GameInfo;
import com.example.hoshiko.gomoku.game.GameState;
import com.example.hoshiko.gomoku.game.Move;

public class HumanPlayer extends  Player {
    private Move move;

    public HumanPlayer(GameInfo info) {
        super(info);
    }

    public void setMove(Move move) {
        this.move = move;
    }

    @Override
    public Move getMove(GameState state) {
        // Suspend until the user clicks a valid move (handled by the game)
        try {
            synchronized(this) {
                this.wait();
            }
        } catch(InterruptedException e) {
            return null;
        }
        return move;
    }
}
