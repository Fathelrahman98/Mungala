package com.example.mungala;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class ScanningActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private TextView scanningLabel;
    private BluetoothAdapter bluetoothAdapter;
    private BroadcastReceiver receiver;

    private ArrayAdapter<String> pairedDevicesListAdapter;
    private ListView pairedDevicesListView;

    private ArrayAdapter<String> foundDevicesListAdapter;
    private ListView foundDevicesListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanning);

        setResult(RESULT_CANCELED);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        scanningLabel = findViewById(R.id.scanning_label);

        if (bluetoothAdapter.isEnabled() && ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)
        == PackageManager.PERMISSION_GRANTED) {
            bluetoothAdapter.cancelDiscovery();
            bluetoothAdapter.startDiscovery();
        }

        setScanningLabel();

        if (bluetoothAdapter.isDiscovering()) {
            setupDevicesListViews();
        }

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) {
                    case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                        setupDevicesListViews();
                        if (foundDevicesListAdapter.getCount() == 1) {
                            if (foundDevicesListAdapter.getItem(0).equals(getResources().getString(R.string.no_device_found))) {
                                foundDevicesListAdapter.remove(getResources().getString(R.string.no_device_found));
                                foundDevicesListAdapter.notifyDataSetChanged();
                            }
                        }
                    case BluetoothAdapter.ACTION_STATE_CHANGED:
                        setScanningLabel();
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                        setScanningLabel();
                        if (foundDevicesListAdapter.getCount() == 0) {
                            foundDevicesListAdapter.add(getResources().getString(R.string.no_device_found));
                            foundDevicesListAdapter.notifyDataSetChanged();
                        }
                        break;
                    case BluetoothDevice.ACTION_FOUND:
                        BluetoothDevice foundDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        boolean skipDevice = false;
                        if (foundDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
                            String tempName;
                            String tempAddress;
                            for (int i = 0; i < foundDevicesListAdapter.getCount(); i++) {
                                tempName = foundDevicesListAdapter.getItem(i);
                                tempAddress = tempName.substring(tempName.length()-17);
                                tempName = tempName.substring(0,tempName.length()-17);
                                if (foundDevice.getAddress().equals(tempAddress)) {
                                    if (foundDevice.getName().equals(tempName)) {
                                        skipDevice = true;
                                        break;
                                    } else {
                                        foundDevicesListAdapter.remove(foundDevicesListAdapter.getItem(i));
                                        break;
                                    }
                                }
                            }
                            if (!skipDevice) {
                                foundDevicesListAdapter.add(foundDevice.getName() + "\n" + foundDevice.getAddress());
                                foundDevicesListAdapter.notifyDataSetChanged();
                            }
                        }
                        break;
                }
            }
        };

    }

    @Override
    protected void onStart() {
        super.onStart();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver,intentFilter);

        intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(receiver,intentFilter);

        intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver,intentFilter);

        intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(receiver,intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (bluetoothAdapter != null) {
            bluetoothAdapter.cancelDiscovery();
        }

        if (receiver != null) {
            unregisterReceiver(receiver);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case Constants.BLUETOOTH_ENABLE_REQUEST:
                if (resultCode == RESULT_OK) {
                    ensureScanning(null);
                } else {
                    Toast.makeText(this,R.string.bluetooth_not_allowed_message,Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.ACCESS_COARSE_LOCATION_PERMISSION_REQUEST:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ensureScanning(null);
                } else {
                    Toast.makeText(this,R.string.permission_not_allowed_message,Toast.LENGTH_LONG).show();
                }
                break;
        }
    }



    public void ensureScanning(View view) {
        if (!bluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent,Constants.BLUETOOTH_ENABLE_REQUEST);
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}
                    ,Constants.ACCESS_COARSE_LOCATION_PERMISSION_REQUEST);
        } else{
            bluetoothAdapter.cancelDiscovery();
            bluetoothAdapter.startDiscovery();
        }
    }

    private void setScanningLabel() {
        if (bluetoothAdapter.isDiscovering()) {
            scanningLabel.setText(R.string.device_scanning_text);
        } else {
            scanningLabel.setText(R.string.device_not_scanning_text);
        }
    }

    private void setupDevicesListViews() {
        if (pairedDevicesListView != null && foundDevicesListView != null) {
            return;
        }
        pairedDevicesListAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1);
        pairedDevicesListView = findViewById(R.id.paired_devices_list);
        pairedDevicesListView.setAdapter(pairedDevicesListAdapter);
        pairedDevicesListView.setOnItemClickListener(this);

        foundDevicesListAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1);
        foundDevicesListView = findViewById(R.id.found_devices_list);
        foundDevicesListView.setAdapter(foundDevicesListAdapter);
        foundDevicesListView.setOnItemClickListener(this);

        Set<BluetoothDevice> pairedDevicesSet = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice pairedDevice: pairedDevicesSet) {
            pairedDevicesListAdapter.add(pairedDevice.getName()+"\n"+pairedDevice.getAddress());
        }

        if (pairedDevicesListAdapter.getCount() == 0) {
            pairedDevicesListAdapter.add(getResources().getString(R.string.no_paired_devices));
        }

        pairedDevicesListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TextView textView = (TextView) view;
        String text = textView.getText().toString();
        String address = text.substring(text.length()-17);
        Intent intent = new Intent();
        intent.putExtra(Constants.EXTRA_DEVICE_ADDRESS,address);
        setResult(RESULT_OK,intent);
        finish();
    }

}
