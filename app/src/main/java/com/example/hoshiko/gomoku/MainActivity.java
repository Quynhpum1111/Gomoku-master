package com.example.hoshiko.gomoku;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.hoshiko.gomoku.game.Game;
import com.example.hoshiko.gomoku.game.GameSettings;
import com.example.hoshiko.gomoku.game.Move;
import com.example.hoshiko.gomoku.ui.BoardPane;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private BoardPane boardView;
    private Game game;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imgView);


        boardView = new BoardPane(getBaseContext(), 8, 700, 2);
        imageView.setImageBitmap(boardView.drawBoard(2));

        //Setup
        game = new Game();
        game.start();


        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int row = boardView.getClosestRow(v, event);
                int col = boardView.getClosestCol(v, event);
                if (game.state.terminal() == 2) {
                    Toast.makeText(MainActivity.this, "bot thang ", Toast.LENGTH_SHORT).show();
                }

                if (game.state.getCurrentIndex() == 1) {
                    Toast.makeText( MainActivity.this, "hello" + game.players[1].getMove(game.state).row
                            + "----" + game.players[1].getMove(game.state).col, Toast.LENGTH_SHORT).show();

                     boardView.onTouchforBot(v, game.players[1].getMove(game.state).row,
                            game.players[1].getMove(game.state).col, 1);

                }



                if (game.setUserMove(new Move(row, col)) == true) {
                     return boardView.onTouch(v, event, 0);
                }

                return true;
            }
        });
    }
}












