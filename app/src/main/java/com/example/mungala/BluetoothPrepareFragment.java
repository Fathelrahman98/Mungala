package com.example.mungala;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class BluetoothPrepareFragment extends Fragment implements View.OnClickListener {

    boolean hosting;
    Button hostButton;
    TextView visibilityLabel;
    BluetoothAdapter bluetoothAdapter;
    BroadcastReceiver receiver;
    FragmentActionListiner listiner;

    public BluetoothPrepareFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listiner = (FragmentActionListiner) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listiner = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {

            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    switch (action) {
                        case BluetoothAdapter.ACTION_SCAN_MODE_CHANGED:
                            setVisiblityLabel();
                    }
                }
            };

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
            getActivity().registerReceiver(receiver,intentFilter);

        } else {
            // todo bluetooth not enabled
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            getActivity().unregisterReceiver(receiver);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view  = inflater.inflate(R.layout.fragment_blank, container, false);
        hostButton = view.findViewById(R.id.button17);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        hosting = getActivity().getIntent().getBooleanExtra(Constants.EXTRA_HAS_FIRST_TURN,false);
        if (hosting) {
            hostButton.setText(R.string.host);
        } else {
            hostButton.setText(R.string.connect);
        }
        visibilityLabel = getView().findViewById(R.id.textView);
        hostButton.setOnClickListener(this);
        setVisiblityLabel();
    }

    private void setVisiblityLabel() {
        if (hosting) {
            if (bluetoothAdapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                if (listiner.isWorking()) {
                    visibilityLabel.setText(getResources().getString(R.string.hosting));
                } else {
                    visibilityLabel.setText(getResources().getString(R.string.not_hosting));
                }
            } else {
                visibilityLabel.setText(getResources().getString(R.string.not_hosting));
            }
        } else {
            if (bluetoothAdapter.isEnabled()) {
                if (listiner.isWorking()) {
                    visibilityLabel.setText(getResources().getString(R.string.connecting));
                } else {
                    visibilityLabel.setText(getResources().getString(R.string.not_connecting));
                }
            } else {
                visibilityLabel.setText(getResources().getString(R.string.not_connecting));
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.BLUETOOTH_DISCOVERABLE_REQUEST:
                if (resultCode == 60) {
                    //setVisibilityLabel();
                    onClick(null);
                } else {
                    Toast.makeText(getContext(),R.string.discoverability_not_allowed_message,Toast.LENGTH_LONG).show();
                }
                break;
            case Constants.BLUETOOTH_ENABLE_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    onClick(null);
                } else {
                    Toast.makeText(getContext(),R.string.bluetooth_not_allowed_message,Toast.LENGTH_LONG).show();
                }
        }
    }

    @Override
    public void onClick(View v) {
        if (hosting) {
            if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                Intent intent = new Intent();
                intent.setAction(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,60);
                startActivityForResult(intent,Constants.BLUETOOTH_DISCOVERABLE_REQUEST);
            } else {
                listiner.onFragmentAction();
            }
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent,Constants.BLUETOOTH_ENABLE_REQUEST);
            } else {
                listiner.onFragmentAction();
            }
        }
    }

    interface FragmentActionListiner {
        void onFragmentAction();
        boolean isWorking();
    }
}
