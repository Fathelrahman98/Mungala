package com.example.mungala;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements SinglePlayerGameSetupFragment.OnSetupFinishListener,BluetoothMultiplayerGameSetupFragment.OnSetupFinishListener{

    private static final String EXTRA_DIFFICULTY = BuildConfig.APPLICATION_ID + ".EXTRA_DIFFICULTY";

    // a label shows whether the device is discoverable or not.
    //private TextView bluetoothVisibilityLabel;
    // a receiver registered to sense any changes in bluetooth scan mode.
    private BroadcastReceiver bluetoothVisibilityReceiver;

    // the device's bluetooth adapter
    private BluetoothAdapter bluetoothAdapter;

    // the difficulty of single player games.
    private int difficulty;

    // label shows the difficulty level for single player games.
    //private TextView difficultyText;
    // a menu contains the different difficulty options
    //private Menu difficultyMenu;

    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(Constants.MY_TAG,"application started");

        /*
        // get the views from the layout and store it as member variable for further modifications.
        bluetoothVisibilityLabel = findViewById(R.id.visibility_label);
        difficultyText = findViewById(R.id.difficulty_text);
        Button singlePlayerButton = findViewById(R.id.button12);
        TextView difficultyChangeText = findViewById(R.id.textView4);
        */

        // restore any saved values if any.
        if (savedInstanceState != null) {
            // restore
            //setDifficulty(savedInstanceState.getInt(EXTRA_DIFFICULTY));
        } else {
            // no saved values
            //setDifficulty(1);
        }

        bluetoothVisibilityReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) {
                    // when the receiver receives a broadcast, it updates the bluetooth visibility label.
                    case BluetoothAdapter.ACTION_SCAN_MODE_CHANGED:
                        //setBluetoothVisibilityLabel();
                }
            }
        };

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // bluetooth is not supported.
            /*
            findViewById(R.id.button14).setClickable(false);
            findViewById(R.id.button15).setClickable(false);
            */
            findViewById(R.id.cardview3).setEnabled(false);
        } else {
            // the bluetooth is supported
            // initially, set the bluetooth visibility label according to the initial state of the device.
            //setBluetoothVisibilityLabel();
            // register the bluetooth visibility receiver to sense any changes in the device scan mode
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
            registerReceiver(bluetoothVisibilityReceiver,intentFilter);
        }

        /*
        // allow the user to hold any of the 3 views to change the difficulty throw the difficulty menu.
        registerForContextMenu(singlePlayerButton);
        registerForContextMenu(difficultyText);
        registerForContextMenu(difficultyChangeText);
        */
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bluetoothAdapter != null) {
            // if the device is discovering other bluetooth devices, stop it.
            bluetoothAdapter.cancelDiscovery();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothVisibilityReceiver != null) {
            unregisterReceiver(bluetoothVisibilityReceiver);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case Constants.BLUETOOTH_ENABLE_REQUEST:
                if (resultCode == RESULT_OK) {
                    // when bluetooth is enabled try to look for a bluetooth discoverable device.
                    bluetoothConnect();
                } else {
                    Toast.makeText(this,R.string.bluetooth_not_allowed_message,Toast.LENGTH_LONG).show();
                }
                break;
            case Constants.BLUETOOTH_DISCOVERABLE_REQUEST:
                if (resultCode == 60) {
                    // when bluetooth discoverability is enabled try to host a bluetooth game again.
                    startBluetoothMultiplayerGameActivity(true, null);
                } else {
                    Toast.makeText(this,R.string.discoverability_not_allowed_message,Toast.LENGTH_LONG).show();
                }
                break;
            case Constants.FIND_DEVICE_REQUEST:
                if (resultCode == RESULT_OK) {
                    // // when the user selects a hosting device, connect to it.
                    String address = data.getStringExtra(Constants.EXTRA_DEVICE_ADDRESS);
                    startBluetoothMultiplayerGameActivity(false, address);
                }
                break;
            case Constants.SETUP_SINGLE_PLAYER_GAME_REQUEST:
                if (resultCode == RESULT_OK) {
                    difficulty = data.getIntExtra(Constants.EXTRA_DIFFICULTY_LEVEL,1);
                    startSingleGame();
                }
                break;
            case Constants.BLUETOOTH_MULTIPLAYER_GAME_REQUEST:
                if (requestCode == RESULT_OK) {
                    if (data.getBooleanExtra(Constants.EXTRA_HAS_FIRST_TURN,true)) {
                        bluetoothHost();
                    } else {
                        bluetoothConnect();
                    }

                }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.ACCESS_COARSE_LOCATION_PERMISSION_REQUEST:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    bluetoothConnect();
                } else {
                    Toast.makeText(this,R.string.permission_not_allowed_message,Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // save values to be restored when the activity created again.
        outState.putInt(EXTRA_DIFFICULTY,difficulty);
        super.onSaveInstanceState(outState);
    }

    /*

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        MenuInflater menuInflater = getMenuInflater();
        switch (v.getId()) {
            // when any of the 3 views is held, the user will be allowed to change the difficulty throw the difficulty menu.
            case R.id.button12:
            case R.id.difficulty_text:
            case R.id.textView4:
                menuInflater.inflate(R.menu.difficulty_menu, menu);
                difficultyMenu = menu;
                // first: uncheck all options.
                difficultyMenu.findItem(R.id.level_1).setChecked(false);
                difficultyMenu.findItem(R.id.level_2).setChecked(false);
                difficultyMenu.findItem(R.id.level_3).setChecked(false);
                difficultyMenu.findItem(R.id.level_4).setChecked(false);
                difficultyMenu.findItem(R.id.level_5).setChecked(false);
                // second: check the option that matches the difficulty. (the values of difficulty start from 1 while the items start from 0).
                difficultyMenu.getItem(difficulty-1).setChecked(true);
                break;
            default:
                super.onCreateContextMenu(menu,v,menuInfo);
        }
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // first: uncheck all options.
        difficultyMenu.findItem(R.id.level_1).setChecked(false);
        difficultyMenu.findItem(R.id.level_2).setChecked(false);
        difficultyMenu.findItem(R.id.level_3).setChecked(false);
        difficultyMenu.findItem(R.id.level_4).setChecked(false);
        difficultyMenu.findItem(R.id.level_5).setChecked(false);
        // second: check the selected option.
        item.setChecked(true);
        // third: set the difficulty variable according to the selected option.
        switch (item.getItemId()) {
            case R.id.level_1:
                setDifficulty(1);
                return true;
            case R.id.level_2:
                setDifficulty(2);
                return true;
            case R.id.level_3:
                setDifficulty(3);
                return true;
            case R.id.level_4:
                setDifficulty(4);
                return true;
            case R.id.level_5:
                setDifficulty(5);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }*/


    public void startSingleGame() {
        Intent intent = new Intent(this,SinglePlayerActivity.class);
        intent.putExtra(Constants.EXTRA_DIFFICULTY_LEVEL,difficulty);
        startActivity(intent);
    }

    /**
     * starts single player game activity.
     * passes the difficulty value to the started activity.
     * @param view
     */
    public void startSingleGame(View view) {
        if (isScreenLong()) {
            if (fragment == null) {
                fragment = new SinglePlayerGameSetupFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.setup_frame, fragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
            }
        } else {
            Intent intent = new Intent(this,SinglePlayerGameSetupActivity.class);
            startActivityForResult(intent,Constants.SETUP_SINGLE_PLAYER_GAME_REQUEST);
        }
    }

    /**
     * starts local multiplayer game activity.
     * @param view
     */
    public void startLocalMultiplayerGame(View view) {
        startActivity(new Intent(this,LocalMultiplayerActivity.class));
    }

    /**
     * onclick method for bluetooth host and connect buttons.
     * starts bluetooth game accordingly
     * @param view either (button14: hosting button.) or (button15: connecting button.)
     */
    public void startBluetoothMultiplayerGame(View view) {
        /*
        switch (view.getId()) {
            case R.id.button14:
                bluetoothHost();
                break;
            case R.id.button15:
                bluetoothConnect();
                break;
        }
        */
        if (isScreenLong()) {
            fragment = new BluetoothMultiplayerGameSetupFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.setup_frame, fragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
        } else {
            Intent intent = new Intent(this,BluetoothMultiplayerGameSetupActivity.class);
            startActivityForResult(intent,Constants.BLUETOOTH_MULTIPLAYER_GAME_REQUEST);
        }
    }

    /**
     * connect to a bluetooth multiplayer game.
     * enables bluetooth if not enabled.
     * if bluetooth is enabled, starts an activity to scan bluetooth discoverable devices. The user selectes the device to connect with. Then the connection is made.
     */
    private void bluetoothConnect() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent,Constants.BLUETOOTH_ENABLE_REQUEST);
        } else if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // ensure that location permission is granted for this app. it is used by the bluetooth.
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}
            ,Constants.ACCESS_COARSE_LOCATION_PERMISSION_REQUEST);
        } else{
                bluetoothAdapter.cancelDiscovery();
                bluetoothAdapter.startDiscovery();
                Intent intent = new Intent(this,ScanningActivity.class);
                startActivityForResult(intent,Constants.FIND_DEVICE_REQUEST);
        }
    }

    /**
     * hosts a bluetooth multiplayer game.
     * enables bluetooth discoverability if not enabled.
     * if discoverability is enabled, starts a bluetooth multiplayer game activity which starts listining to incoming connections from other devices.
     */
    private void bluetoothHost() {
        if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent intent = new Intent();
            intent.setAction(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,60);
            startActivityForResult(intent,Constants.BLUETOOTH_DISCOVERABLE_REQUEST);
        } else {
            Toast.makeText(this,R.string.already_visible_message,Toast.LENGTH_LONG).show();
            startBluetoothMultiplayerGameActivity(true, null);
        }
    }

    /**
     * starts bluetooth game creation activity.
     * One player will start the activity with true, and the other will start it with false.
     * @param hosting indicates whether this device is hosting the game or connecting to it. Accordingly, the player will have the first turn or the second one in the game.
     * @param address the hosting device mac address or null. if this device is connecting to a game address represents the hosting device mac address. if this device is hosting the game, address is set to null
     */
    public void startBluetoothMultiplayerGameActivity(boolean hosting, String address) {
        Intent intent = new Intent(this,BluetoothMultiplayerActivity.class);
        intent.putExtra(Constants.EXTRA_HAS_FIRST_TURN,hosting);
        if (!hosting) {
            intent.putExtra(Constants.EXTRA_DEVICE_ADDRESS,address);
        }
        startActivity(intent);
    }

    @Override
    public void onSetupFinish(int difficultyLevel) {
        this.difficulty = difficultyLevel;
        getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        startSingleGame();
    }

    private boolean isScreenLong() {
        return (findViewById(R.id.card) != null);
    }

    @Override
    public void onSetupFinish(boolean host) {
        getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        if (host) {
            bluetoothHost();
        } else {
            bluetoothConnect();
        }
    }

    /**
     * sets the discoverability label depending on the current state of the device.
     * visible means the device is discoverable and vice versa.
     */
    /*
    private void setBluetoothVisibilityLabel() {
        if (bluetoothAdapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            bluetoothVisibilityLabel.setText(R.string.device_visible_text);
        } else {
            bluetoothVisibilityLabel.setText(R.string.device_invisible_text);
        }
    }
    */


    /**
     * sets the member value difficulty to the parameter difficulty. accordingly the next single player games will be played againest this difficulty level
     * Also, it changes the difficulty label accordingly.
     * @param difficulty
     */
    /*
    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
        switch (difficulty) {
            case 1:
                difficultyText.setText(R.string.level_1);
                break;
            case 2:
                difficultyText.setText(R.string.level_2);
                break;
            case 3:
                difficultyText.setText(R.string.level_3);
                break;
            case 4:
                difficultyText.setText(R.string.level_4);
                break;
            case 5:
                difficultyText.setText(R.string.level_5);
                break;
        }
    }
    */
}
