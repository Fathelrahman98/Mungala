package com.example.mungala;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class BluetoothMultiplayerActivity extends GameActivity implements BluetoothPrepareFragment.FragmentActionListiner {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothService bluetoothService;
    private boolean hasFirstTurn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_multiplayer);

        bluetoothService = new BluetoothService(handler);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Intent intent = getIntent();
        hasFirstTurn = intent.getBooleanExtra(Constants.EXTRA_HAS_FIRST_TURN,false);
        if (hasFirstTurn) {
            bluetoothService.start();
        } else {
            String address = intent.getStringExtra(Constants.EXTRA_DEVICE_ADDRESS);
            BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
            bluetoothService.connect(bluetoothDevice);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothService.stop();
    }

    public BluetoothService getBluetoothService() {
        return this.bluetoothService;
    }

    public boolean hasFirstTurn() {
        return hasFirstTurn;
    }

    @Override
    public void onFragmentAction() {
        if (hasFirstTurn) {
            bluetoothService.start();
        } else {
            Intent intent = getIntent();
            String address = intent.getStringExtra(Constants.EXTRA_DEVICE_ADDRESS);
            BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
            bluetoothService.connect(bluetoothDevice);
        }
    }

    @Override
    public boolean isWorking() {
        if (hasFirstTurn) {
            return (bluetoothService.getState() == BluetoothService.STATE_CONNECTED || bluetoothService.getState() == BluetoothService.STATE_LISTINING);
        } else {
            return (bluetoothService.getState() == BluetoothService.STATE_CONNECTED || bluetoothService.getState() == BluetoothService.STATE_CONNECTING);
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.CONNECTION_ESTABLISHED_CONNECT:
                    Toast.makeText(BluetoothMultiplayerActivity.this,R.string.connection_message,Toast.LENGTH_LONG).show();
                    Log.d(Constants.MY_TAG,"connected");
                    GameFragment gameFragment = new GameFragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id.bluetooth_frame,gameFragment).
                            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
                    break;
                case Constants.CONNECTION_ESTABLISHED_HOST:
                    GameFragment fragment = new GameFragment();
                    Toast.makeText(BluetoothMultiplayerActivity.this,R.string.connection_message,Toast.LENGTH_LONG).show();
                    Log.d(Constants.MY_TAG,"connected");
                    getSupportFragmentManager().beginTransaction().replace(R.id.bluetooth_frame,fragment).
                            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
                    break;
                case Constants.CONNECTION_LOST:
                    Toast.makeText(BluetoothMultiplayerActivity.this,R.string.connection_lost_message,Toast.LENGTH_LONG).show();
                    Log.d(Constants.MY_TAG,"connection lost");
                    finish();
                    break;
                case Constants.CONNECTION_FAILED:
                    Toast.makeText(BluetoothMultiplayerActivity.this,R.string.connection_failed_message,Toast.LENGTH_LONG).show();
                    Log.d(Constants.MY_TAG,"connection failed");
                    break;
                case Constants.DATA_RECEIVED:
                    byte[] buffer = (byte[]) msg.obj;
                    Toast.makeText(BluetoothMultiplayerActivity.this,"received :"+buffer[0],Toast.LENGTH_LONG).show();
                    Log.d(Constants.MY_TAG,"received :"+buffer[0]);
                    break;
                case Constants.DATA_SENT:
                    Toast.makeText(BluetoothMultiplayerActivity.this,"sent :"+((byte[])msg.obj)[0],Toast.LENGTH_LONG).show();
                    Log.d(Constants.MY_TAG,"sent :"+((byte[])msg.obj)[0]);
                    break;
            }
        }
    };

}
