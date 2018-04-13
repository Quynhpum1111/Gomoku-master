package com.example.hoshiko.gomoku.game;


import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import com.example.hoshiko.gomoku.events.GameListener;
import com.example.hoshiko.gomoku.player.HumanPlayer;
import com.example.hoshiko.gomoku.player.NegamaxPlayer;
import com.example.hoshiko.gomoku.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

/**
 * Main game loop responsible for running the game from start to finish.
 */
public class Game {

    private static Logger LOGGER = Logger.getGlobal();

    private final List<GameListener> listeners;
    private final GameSettings settings;
    private final ExecutorService executor;
    public final Player[] players;

    private final long[] times;
    private final Timer timer;
    private Future<Move> futureMove;
    public Thread gameThread;
    private TimerTask timeUpdateSender;
    public GameState state;




    /**
     * Create a new game instance.
     */
    public Game() {

        this.settings = new GameSettings();
        this.times = new long[2];
        this.players = new Player[2];
        this.executor = Executors.newSingleThreadExecutor();
        this.listeners = new ArrayList<>();
        this.gameThread = new Thread(getRunnable());
        this.timer = new Timer();
        this.state = new GameState(settings.getSize());

        /*this.settings.addListener(new SettingsListener() {
            @Override
            public void settingsChanged() {
                // State is no longer valid if settings change
                // TODO: Only invalidate state if size changes
                state = new GameState(settings.getSize());
            }
        });*/
    }

    /**
     * Start the game. Reads the game settings and launches a new game thread.
     * Has no effect if the game thread is already running.
     */
    public void start() {
        if(!this.gameThread.isAlive()) {
            this.state = new GameState(settings.getSize());
            players[0] = settings.getPlayer1();
            players[1] = settings.getPlayer2();
            times[0] = settings.getGameTimeMillis();
            times[1] = settings.getGameTimeMillis();
            this.gameThread = new Thread( getRunnable());
            this.gameThread.start();
        }
    }



    /**
     * Called by the GUI to set a user's move for the game.
     * @param move Move from the user
     * @return True if the move was accepted
     */
    public boolean setUserMove(Move move) {

        Player currentPlayer = players[state.getCurrentIndex() - 1];
        if (currentPlayer instanceof HumanPlayer) {
            if (!state.getMoves().contains(move)) {
                synchronized (currentPlayer) {
                    ((HumanPlayer) currentPlayer).setMove(move);
                    players[state.getCurrentIndex() - 1].notify();
                }
                return true;
            }
        }
        return false;
    }


    /**
     * Get the game settings.
     * @retun GameSettings instance
     */
    public GameSettings getSettings() {
        return settings;
    }



    private Runnable getRunnable(){
        return () -> {
            while(state.terminal() == 0) {
                try {
                    state.getCurrentIndex();
                    Move move = requestMove(state.getCurrentIndex());
                    state.makeMove(move);

                } catch (InterruptedException ex) {
                    break;
                } catch (ExecutionException ex) {
                    ex.printStackTrace();
                    break;
                } catch (TimeoutException e) {
                    e.printStackTrace();
                }
            }
            if(state.terminal() != 0) {
                Log.i("win", "win");
            }
        };
    }


        private Move requestMove(int playerIndex) throws
        InterruptedException, ExecutionException, TimeoutException {
            Player player = players[playerIndex - 1];
            long timeout = calculateTimeoutMillis(playerIndex);
            this.futureMove = executor.submit(() -> player.getMove(state));

            if(player instanceof HumanPlayer) {
                listeners.forEach(listener -> listener.userMoveRequested
                        (playerIndex));
            }

            if (timeout > 0) {
                try {
                    return futureMove.get(timeout, TimeUnit.MILLISECONDS);
                } catch(TimeoutException ex) {
                    futureMove.cancel(true);
                    throw(ex);
                }
            } else {
                return futureMove.get();
            }
        }

    /**
     * Calculate the timeout value for a player or return 0 if timing is not
     * enabled for this game.
     * @param player Player index
     * @return Timeout value in milliseconds
     */
    private long calculateTimeoutMillis(int player) {
        if(settings.moveTimingEnabled() && settings.gameTimingEnabled()) {
            // Both move timing and game timing are enabled
            return Math.min(settings.getMoveTimeMillis(), times[player - 1]);
        } else if(settings.gameTimingEnabled()) {
            // Only game timing is enabled
            return times[player - 1];
        } else if(settings.moveTimingEnabled()) {
            // Only move timing is enabled
            return settings.getMoveTimeMillis();
        } else {
            // No timing is enabled
            return 0;
        }
    }



}






