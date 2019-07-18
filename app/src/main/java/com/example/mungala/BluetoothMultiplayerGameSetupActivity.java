package com.example.mungala;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class BluetoothMultiplayerGameSetupActivity extends AppCompatActivity implements BluetoothMultiplayerGameSetupFragment.OnSetupFinishListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_multiplayer_game_setup);

        setResult(RESULT_CANCELED);
    }

    @Override
    public void onSetupFinish(boolean host) {
        Intent intent = new Intent();
        intent.putExtra(Constants.EXTRA_HAS_FIRST_TURN,host);
        setResult(RESULT_OK,intent);
        finish();
    }
}
