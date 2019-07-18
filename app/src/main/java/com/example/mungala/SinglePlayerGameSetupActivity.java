package com.example.mungala;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SinglePlayerGameSetupActivity extends AppCompatActivity implements SinglePlayerGameSetupFragment.OnSetupFinishListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_player_game_setup);

        setResult(RESULT_CANCELED);
    }

    @Override
    public void onSetupFinish(int difficultyLevel) {
        Intent intent = new Intent();
        intent.putExtra(Constants.EXTRA_DIFFICULTY_LEVEL,difficultyLevel);
        setResult(RESULT_OK,intent);
        finish();
    }
}
