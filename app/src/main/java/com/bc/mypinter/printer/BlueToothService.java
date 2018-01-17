package com.bc.mypinter.printer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class BlueToothService {

    private BluetoothAdapter adapter;
    private Context context;
    private int mState;
    private Boolean D = true;
    private String TAG = "BlueToothService";
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    private static final UUID MY_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String NAME = "BTPrinter";

    private Handler mHandler;

    public BlueToothService(Context context, Handler handler) {
        this.context = context;
        this.mHandler = handler;
        mState = PrinterClass.STATE_NONE;
        adapter = BluetoothAdapter.getDefaultAdapter();

    }

    public boolean HasDevice() {
        if (adapter != null) {
            return true;
        }
        return false;
    }

    public boolean IsOpen() {
        synchronized (this) {
            if (adapter.isEnabled()) {
                return true;
            }
            return false;
        }
    }

    public void OpenDevice() {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        // 请求开启蓝牙设备
        context.startActivity(intent);

    }

    public void CloseDevice() {
        adapter.disable();
    }

    // 获取已经配对的蓝牙设备的物理地址
    public Set<BluetoothDevice> GetBondedDevice() {

        Set<BluetoothDevice> devices = adapter.getBondedDevices();
        return devices;
    }

    // 扫描蓝牙设备

    public void ScanDevice() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(mReceiver, filter);

        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        context.registerReceiver(mReceiver, filter);
        if (adapter.isDiscovering()) {
            adapter.cancelDiscovery();
        }
        setState(PrinterClass.STATE_SCANING);
        // Request discover from BluetoothAdapter
        adapter.startDiscovery();
    }

    public void StopScan() {
        context.unregisterReceiver(mReceiver);
        adapter.cancelDiscovery();
        setState(PrinterClass.STATE_SCAN_STOP);
    }

    public OnReceiveDataHandleEvent OnReceive = null;

    public interface OnReceiveDataHandleEvent {
        public void OnReceive(BluetoothDevice device);
    }

    public OnReceiveDataHandleEvent getOnReceive() {
        return OnReceive;
    }

    public void setOnReceive(OnReceiveDataHandleEvent onReceive) {
        OnReceive = onReceive;
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed
                // already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    setState(PrinterClass.STATE_SCANING);
                    OnReceive.OnReceive(device);
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
                    .equals(action)) {
                setState(PrinterClass.STATE_SCAN_STOP);

                OnReceive.OnReceive(null);
            }
        }

        private void OnFinished() {
            // TODO Auto-generated method stub

        }
    };
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    public void ConnectToDevice(String address) {
        if (BluetoothAdapter.checkBluetoothAddress(address)) {
            BluetoothDevice device = adapter.getRemoteDevice(address);
            connect(device);
            setState(PrinterClass.STATE_CONNECTING);
        }
    }

    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != PrinterClass.STATE_CONNECTED)
                return;
            r = mConnectedThread;
        }
        if (r != null) {
            r.write(out);
        } else {
            DisConnected();
            Nopointstart();

        }
    }

    public synchronized void start() {
        if (D)
            Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to listen on a BluetoothServerSocket
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
        setState(PrinterClass.STATE_LISTEN);
    }

    public synchronized void setState(int state) {
        mState = state;
    }

    public synchronized int getState() {
        return mState;

    }

    public synchronized void connect(BluetoothDevice device) {

        if (mState == PrinterClass.STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
    }

    public synchronized void DisConnected() {
        if (mState == PrinterClass.STATE_CONNECTED) {

            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }

            if (mConnectedThread != null) {
                mConnectedThread.cancel();
                mConnectedThread = null;
            }

            if (mAcceptThread != null) {
                mAcceptThread.cancel();
                mAcceptThread = null;
            }

            setState(PrinterClass.STATE_NONE);
        }
    }

    public synchronized void connected(BluetoothSocket socket,
                                       BluetoothDevice device) {

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }

        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        setState(PrinterClass.STATE_CONNECTED);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if (D)
            Log.d(TAG, "stop");
        setState(PrinterClass.STATE_NONE);
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
    }

    private void connectionSuccess() {
        setState(PrinterClass.STATE_CONNECTED);
        mHandler.obtainMessage(PrinterClass.MESSAGE_STATE_CHANGE,
                PrinterClass.SUCCESS_CONNECT, -1).sendToTarget();
    }

    private void connectionFailed() {
        setState(PrinterClass.STATE_LISTEN);
        mHandler.obtainMessage(PrinterClass.MESSAGE_STATE_CHANGE,
                PrinterClass.FAILED_CONNECT, -1).sendToTarget();
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        setState(PrinterClass.STATE_LISTEN);
        mHandler.obtainMessage(PrinterClass.MESSAGE_STATE_CHANGE,
                PrinterClass.LOSE_CONNECT, -1).sendToTarget();

    }

    private void Nopointstart() {
        setState(PrinterClass.STATE_LISTEN);
        mHandler.obtainMessage(PrinterClass.MESSAGE_STATE_CHANGE,
                PrinterClass.LOSE_CONNECT, 0).sendToTarget();

    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted (or
     * until cancelled).
     */
    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            // Create a new listening server socket
            try {
                tmp = adapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "listen() failed", e);
            }
            mmServerSocket = tmp;
        }

        @Override
        public void run() {
            if (D)
                Log.d(TAG, "BEGIN mAcceptThread" + this);
            setName("AcceptThread");
            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connected
            while (true) {// mState != PrinterClass.STATE_CONNECTED
                try {
                    if (mmServerSocket != null) {
                        socket = mmServerSocket.accept();
                    }
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (BlueToothService.this) {
                        switch (mState) {
                            case PrinterClass.STATE_LISTEN:
                            case PrinterClass.STATE_CONNECTING:
                                // Situation normal. Start the connected thread.
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case PrinterClass.STATE_NONE:
                            case PrinterClass.STATE_CONNECTED:
                                try {
                                    socket.close();
                                    ;
                                } catch (IOException e) {

                                }
                                break;
                        }
                    }
                }
            }

        }

        public void cancel() {
            if (D)
                Log.d(TAG, "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of server failed", e);
            }
        }
    }

    /**
     * This thread runs while attempting to make an outgoing connection with a
     * device. It runs straight through; the connection either succeeds or
     * fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
        }

        @Override
        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            adapter.cancelDiscovery();
            setState(PrinterClass.STATE_SCAN_STOP);

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
                connectionSuccess();
            } catch (IOException e) {
                connectionFailed();
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG,
                            "unable to close() socket during connection failure",
                            e2);
                }
                // Start the service over to restart listening mode
                BlueToothService.this.start();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BlueToothService.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private boolean isCancle = false;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            isCancle = false;
            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    if (bytes <= 0) {
                        return;
                    }
                    if (buffer[0] == 0x13) {
                        PrintService.isFUll = true;
                        Log.i(TAG, "0x13:");
                    } else if (buffer[0] == 0x11) {
                        PrintService.isFUll = false;
                        Log.i(TAG, "0x11:");
                    } else {

                        // Send the obtained bytes to the UI Activity
                        mHandler.obtainMessage(PrinterClass.MESSAGE_READ,
                                bytes, -1, buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }

        public void write(byte[] buffer) {
            try {

                mmOutStream.write(buffer);
                Log.i("BTPWRITE", new String(buffer));
                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(PrinterClass.MESSAGE_WRITE, -1, -1,
                        buffer).sendToTarget();
            } catch (IOException e) {

            }
        }

        public void cancel() {
            try {
                isCancle = true;
                mmSocket.close();
                Log.d(TAG, "562cancel suc");
                setState(PrinterClass.STATE_LISTEN);
            } catch (IOException e) {
                Log.d(TAG, "565cancel failed");
            }
        }
    }
}
