package com.example.mungala;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    BluetoothAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            findViewById(R.id.button14).setClickable(false);
            findViewById(R.id.button15).setClickable(false);
        } else {
            //if (adapter.isEnabled())
        }
    }

    public void single(View view) {
        startActivity(new Intent(this,SinglePlayerActivity.class));
    }

    public void multiple(View view) {
        startActivity(new Intent(this,LocalMultiplayerActivity.class));
    }

    public void bluetoothConnect(View view) {

    }

    public void bluetoothHost(View view) {

    }

}
