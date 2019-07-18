package com.example.mungala;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BluetoothService {

    private static final int STATE_NONE = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_LISTINING = 2;
    public static final int STATE_CONNECTED = 3;

    private int state;
    private final BluetoothAdapter bluetoothAdapter;
    private final Handler handler;
    private AcceptThread acceptThread;
    private ReceivingConnectedThread receivingConnectedThread;
    private SendingConnectedThread sendingConnectedThread;
    private ConnectThread connectThread;

    public BluetoothService(Handler handler) {
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.handler = handler;
        state = STATE_NONE;
    }

    public synchronized int getState() {
        return state;
    }

    public synchronized void start() {
        if (connectThread != null) {
           connectThread.cancel();
           connectThread = null;
        }

        if (sendingConnectedThread != null) {
            sendingConnectedThread.cancel();
            sendingConnectedThread = null;
        }

        if (receivingConnectedThread != null) {
            receivingConnectedThread.cancel();
            receivingConnectedThread = null;
        }


        if (acceptThread == null) {
            acceptThread = new AcceptThread();
            acceptThread.start();
        }
    }

    public synchronized void connect(BluetoothDevice bluetoothDevice) {
        if (state == STATE_CONNECTING) {
            if (connectThread != null) {
                connectThread.cancel();
                connectThread = null;
            }
        }

        if (sendingConnectedThread != null) {
            sendingConnectedThread.cancel();
            sendingConnectedThread = null;
        }

        if (receivingConnectedThread != null) {
            receivingConnectedThread.cancel();
            receivingConnectedThread = null;
        }

        connectThread = new ConnectThread(bluetoothDevice);
        connectThread.start();
    }

    private synchronized void connected(BluetoothSocket bluetoothSocket) {
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        if (sendingConnectedThread != null) {
            sendingConnectedThread.cancel();
            sendingConnectedThread = null;
        }

        if (receivingConnectedThread != null) {
            receivingConnectedThread.cancel();
            receivingConnectedThread = null;
        }

        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }

        sendingConnectedThread = new SendingConnectedThread(bluetoothSocket);
        receivingConnectedThread = new ReceivingConnectedThread(bluetoothSocket);

    }

    public synchronized void stop() {
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        if (sendingConnectedThread != null) {
            sendingConnectedThread.cancel();
            sendingConnectedThread = null;
        }

        if (receivingConnectedThread != null) {
            receivingConnectedThread.cancel();
            receivingConnectedThread = null;
        }

        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }
        state = STATE_NONE;
    }

    private synchronized void connectionFailed() {
        handler.obtainMessage(Constants.CONNECTION_FAILED).sendToTarget();
        state = STATE_NONE;
        BluetoothService.this.stop();
    }

    private synchronized void connectionLost() {
        handler.obtainMessage(Constants.CONNECTION_LOST).sendToTarget();
        state = STATE_NONE;
        BluetoothService.this.stop();
    }

    public boolean sendMove(byte move) {
        if (state == STATE_CONNECTED) {
            sendingConnectedThread.setMove(move);
            return (sendingConnectedThread.run());
        } else {
            return false;
        }
    }

    public byte receiveMove() { // blocking method
        if (state == STATE_CONNECTED) {
            if (receivingConnectedThread.run()) {
                return receivingConnectedThread.getMove();
            } else {
                return Constants.INVALID_MOVE;
            }
        } else {
            return Constants.NO_CONNECTION;
        }
    }

    class ConnectThread extends Thread{

        private final BluetoothSocket bluetoothSocket;
        private final BluetoothDevice bluetoothDevice;

        public ConnectThread(BluetoothDevice bluetoothDevice) {
            this.bluetoothDevice = bluetoothDevice;
            BluetoothSocket tmp = null;
            try {
                tmp = bluetoothDevice.createRfcommSocketToServiceRecord(Constants.MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(Constants.MY_TAG,"could not get a socket for remote device "+ e);
            }
            bluetoothSocket = tmp;
            state = STATE_CONNECTING;
        }

        @Override
        public void run() {
            bluetoothAdapter.cancelDiscovery();
            try {
                bluetoothSocket.connect();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(Constants.MY_TAG,"could not initiate a connection : "+ e);
                try {
                    bluetoothSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                    Log.d(Constants.MY_TAG,"could not closa the socket : "+ e);
                }
                connectionFailed();
                return;
            }
            synchronized (BluetoothService.this) {
                connectThread = null;
            }
            handler.obtainMessage(Constants.CONNECTION_ESTABLISHED_CONNECT).sendToTarget();
            connected(bluetoothSocket);
        }

        public void cancel() {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(Constants.MY_TAG,"could not closa the socket : "+ e);
            }
        }
    }

    class AcceptThread extends Thread {

        private final BluetoothServerSocket bluetoothServerSocket;
        AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(null,Constants.MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(Constants.MY_TAG,"could not get a server socket "+ e);
            }
            bluetoothServerSocket = tmp;
            state = STATE_LISTINING;
        }

        @Override
        public void run() {
            BluetoothSocket bluetoothSocket = null;
            while (state != STATE_CONNECTED) {
                try {
                    bluetoothSocket = bluetoothServerSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(Constants.MY_TAG,"could not accept a connection "+ e);
                }
                if (bluetoothSocket != null) {
                    switch (state) {
                        case STATE_CONNECTING:
                            connected(bluetoothSocket);
                            handler.obtainMessage(Constants.CONNECTION_ESTABLISHED_CONNECT).sendToTarget();
                            break;
                        case STATE_LISTINING:
                            connected(bluetoothSocket);
                            handler.obtainMessage(Constants.CONNECTION_ESTABLISHED_HOST).sendToTarget();
                            break;
                        case STATE_CONNECTED:
                        case STATE_NONE:
                            try {
                                bluetoothSocket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.d(Constants.MY_TAG,"could not closa the socket : "+ e);
                            }
                            break;
                    }
                }
            }

        }

        public void cancel() {
            try {
                bluetoothServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(Constants.MY_TAG,"could not closa the server socket : "+ e);
            }
        }
    }

    abstract class ConnectedThread {
        final static int MAX_COUNT = 5;

        final BluetoothSocket bluetoothSocket;
        final InputStream inputStream;
        final OutputStream outputStream;
        byte move;

        ConnectedThread(BluetoothSocket bluetoothSocket) {
            this.bluetoothSocket = bluetoothSocket;
            InputStream temp = null;
            try {
                temp = bluetoothSocket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(Constants.MY_TAG,"could not get input stream : "+ e);
            }
            inputStream = temp;

            OutputStream temp2 = null;
            try {
                temp2 = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(Constants.MY_TAG,"could not get output stream : "+ e);
            }
            outputStream = temp2;
            state = STATE_CONNECTED;
        }

        public abstract boolean run();

        public void write(byte[] buffer) {
            if (state == STATE_CONNECTED) {
                try {
                    outputStream.write(buffer);
             //       handler.obtainMessage(Constants.DATA_SENT,buffer).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(Constants.MY_TAG, "could not write to output stream : " + e);
                }
            } else {
                    handler.obtainMessage(Constants.CONNECTION_FAILED).sendToTarget();
            }
        }

        public void cancel() {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(Constants.MY_TAG,"could not closa the socket : "+ e);
            }
        }

        public byte getMove() {
            return move;
        }
    }

    class SendingConnectedThread extends ConnectedThread {

        SendingConnectedThread(BluetoothSocket bluetoothSocket) {
            super(bluetoothSocket);
        }

        public void setMove(byte move) {
            this.move = move;
        }

        @Override
        public boolean run() {
            write(new byte[] {Constants.MOVE_READY_SIGNAL});

            Log.d(Constants.MY_TAG,"sending ");
            byte[] buffer = new byte[1024];
            int bytes;
            boolean done = false;
            int counter = 0;

            while (state == STATE_CONNECTED) {
                if (counter == MAX_COUNT) {
                    return false;
                }
                try {
                    bytes = inputStream.available();
                    if (bytes > 0) {
                        bytes = inputStream.read(buffer);
                        if (bytes > 0) {
                            if (buffer[0] == Constants.MOVE_ACCEPT_SIGNAL) {
                                done = true;
                                break;
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(Constants.MY_TAG,"could not read from input stream : "+ e);
                    connectionLost();
                    break;
                }
                counter++;
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (done) {
                buffer = new byte[] {move};
                write(buffer);
                boolean done1 = false;
                counter = 0;
                while (state == STATE_CONNECTED) {
                    try {
                        if (counter == MAX_COUNT) {
                            return false;
                        }
                        bytes = inputStream.available();
                        if (bytes > 0) {
                            bytes = inputStream.read(buffer);
                            if (bytes > 0) {
                                if (buffer[0] == Constants.MOVE_ACCEPT_SIGNAL_2) {
                                    done1 = true;
                                    break;
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.d(Constants.MY_TAG,"could not read from input stream : "+ e);
                        connectionLost();
                        break;
                    }
                    counter++;
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return done1;

            } else {
                return false;
            }
        }

    }

    class ReceivingConnectedThread extends ConnectedThread {

        ReceivingConnectedThread(BluetoothSocket bluetoothSocket) {
            super(bluetoothSocket);
        }

        @Override
        public boolean run() {
            byte[] buffer = new byte[1024];
            int bytes;
            boolean done = false;
            while (state == STATE_CONNECTED) {
                try {
                    Log.d(Constants.MY_TAG,"receiving ");
                    bytes = inputStream.available();
                    if (bytes > 0) {
                        bytes = inputStream.read(buffer);
                        if (bytes > 0) {
                            if (buffer[0] == Constants.MOVE_READY_SIGNAL) {
                                done = true;
                                break;
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(Constants.MY_TAG, "could not read from input stream : " + e);
                    connectionLost();
                    break;
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (done) {
                buffer = new byte[] {Constants.MOVE_ACCEPT_SIGNAL};
                write(buffer);

                boolean done1 = false;
                int counter = 0;
                while (state == STATE_CONNECTED) {
                    if (counter == MAX_COUNT) {
                        return false;
                    }
                    try {
                        bytes = inputStream.available();
                        if (bytes > 0) {
                            bytes = inputStream.read(buffer);
                            if (bytes > 0) {
                                done1 = true;
                                break;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.d(Constants.MY_TAG, "could not read from input stream : " + e);
                        connectionLost();
                        break;
                    }
                    counter++;
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (done1) {
                    move = buffer[0];
                    write(new byte[] {Constants.MOVE_ACCEPT_SIGNAL_2});
                    return true;
                } else {
                    Log.d(Constants.MY_TAG,"not received");
                    return false;
                }
            } else {
                Log.d(Constants.MY_TAG, "not received");
                return false;
            }
        }
    }


    class ConnectionLostException extends Exception {
        public ConnectionLostException() {
            super("connection lost");
        }
    }

}
