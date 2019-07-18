package com.example.mungala;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public abstract class GameActivity extends AppCompatActivity implements GameCoordinator.GameFinishListiner {


    private AlertDialog dialog;

    @Override
    public void finishGame(int finishGameMessageId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.finish_game_layout,null);
        Button playAgainButton = view.findViewById(R.id.play_again_button);
        Button quitButton = view.findViewById(R.id.quit_button);
        TextView finishGameText = view.findViewById(R.id.finish_game_text);
        finishGameText.setText(finishGameMessageId);
        playAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAgain();
            }
        });
        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quit();
            }
        });
        dialog = builder.setView(view).setCancelable(false).create();
        dialog.show();
    }

    void playAgain() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
        dialog.cancel();
    }

    void quit() {
        finish();
        dialog.cancel();
    }
}
