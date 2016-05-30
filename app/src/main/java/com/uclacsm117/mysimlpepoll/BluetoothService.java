package com.uclacsm117.mysimlpepoll;

/**
 * Created by shijunhuang on 5/28/16.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


public class BluetoothService {
    private static final String TAG = "BluetoothChatService";
    private static final boolean D = true;

    // Name for the SDP record when creating server socket
    private static final String NAME = "BluetoothChatMulti";

    // Member fields
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
//    private AcceptThread mAcceptThread;
//    private AcceptThread mAcceptThread2;
    private ConnectThread mConnectThread;

//    private ConnectedThread mConnectedThread;
//    private ConnectedThread mConnectedThread2;
    private int mState;

    private ArrayList<AcceptThread> m_acc;
    private ArrayList<ConnectThread> m_conn;

    private ArrayList<String> mDeviceAddresses;
    private ArrayList<ConnectedThread> mConnThreads;
    private ArrayList<BluetoothSocket> mSockets;
    /**
     * A bluetooth piconet can support up to 7 connections. This array holds 7 unique UUIDs.
     * When attempting to make a connection, the UUID on the client must match one that the server
     * is listening for. When accepting incoming connections server listens for all 7 UUIDs.
     * When trying to form an outgoing connection, the client tries each UUID one at a time.
     */
    private ArrayList<UUID> mUuids;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    /**
     * Constructor. Prepares a new BluetoothChat session.
     * @param context  The UI Activity Context
     * @param handler  A Handler to send messages back to the UI Activity
     */
    public BluetoothService(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
        mDeviceAddresses = new ArrayList<String>();
        mConnThreads = new ArrayList<ConnectedThread>();
        mSockets = new ArrayList<BluetoothSocket>();
        mUuids = new ArrayList<UUID>();
        // 7 randomly-generated UUIDs. These must match on both server and client.
        mUuids.add(UUID.fromString("b7746a40-c758-4868-aa19-7ac6b3475dfc"));
        mUuids.add(UUID.fromString("2d64189d-5a2c-4511-a074-77f199fd0834"));
        mUuids.add(UUID.fromString("e442e09a-51f3-4a7b-91cb-f638491d1412"));
        mUuids.add(UUID.fromString("a81d6504-4536-49ee-a475-7d96d09439e4"));
        mUuids.add(UUID.fromString("aa91eab1-d8ad-448e-abdb-95ebba4a9b55"));
        mUuids.add(UUID.fromString("4d34da73-d0a4-4f40-ac38-917e0a9dee97"));
        mUuids.add(UUID.fromString("5e14d4df-9c8a-4db7-81e4-c937564c86e0"));
    }

    /**
     * Set the current state of the chat connection
     * @param state  An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Return the current connection state. */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume() */
    public synchronized void start() {
        if (D) Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        for(ConnectThread c: m_conn){
            if (c != null) {c.cancel(); c = null;}
        }

        // Cancel any thread currently running a connection
//        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
//        if (mConnectedThread2 != null) {mConnectedThread2.cancel(); mConnectedThread2 = null;}

        // Start the thread to listen on a BluetoothServerSocket
//        if (mAcceptThread == null) {
//            mAcceptThread = new AcceptThread(0);
//            mAcceptThread.start();
//            mAcceptThread2 = new AcceptThread(1);
//            mAcceptThread2.start();
//        }
//        mAcceptThread = new AcceptThread(0);
//         mAcceptThread.start();
//            mAcceptThread2 = new AcceptThread(1);
//            mAcceptThread2.start();

        for(int i =0;i<2;i++){
            AcceptThread t = new AcceptThread(i);
            m_acc.add(t);
            t.start();
        }


        setState(STATE_LISTEN);
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
        if (D) Log.e(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        }

        // Cancel any thread currently running a connection

//        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}


        // Create a new thread and attempt to connect to each UUID one-by-one.
//        for (int i = 0; i < 7; i++) {
//            try {
//                mConnectThread = new ConnectThread(device, mUuids.get(i));
//                mConnectThread.start();
//                setState(STATE_CONNECTING);
//                break;
//            } catch (Exception e) {
//            }
//        }

        try {
            mConnectThread = new ConnectThread(device, UUID.fromString("b7746a40-c758-4868-aa19-7ac6b3475dfc"));
            //mConnectThread = new ConnectThread(device, UUID.fromString("2d64189d-5a2c-4511-a074-77f199fd0834"));

            mConnectThread.start();
            setState(STATE_CONNECTING);
        } catch (Exception e) {
        }

    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (D) Log.e(TAG,"connect");

        //Commented out all the cancellations of existing threads, since we want multiple connections.
        /*
        // Cancel the thread that completed the connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
        // Cancel the accept thread because we only want to connect to one device
        if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}
         */


//        if (mConnectThread != null) {
//            mConnectThread.cancel();
//            mConnectThread = null;
//        }
//
//        // Cancel any thread currently running a connection
//        if (mConnectedThread != null) {
//            mConnectedThread.cancel();
//            mConnectedThread = null;
//        }

        // Cancel the accept thread because we only want to connect to one device

        // Start the thread to manage the connection and perform transmissions
        ConnectedThread mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
        // Add each connected thread to an array
        mConnThreads.add(mConnectedThread);

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(
                Constants.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if (D) Log.e(TAG,"stop");
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
//        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
//        if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}
//        if (mAcceptThread2 != null) {mAcceptThread2.cancel(); mAcceptThread2= null;}

        for(ConnectThread c: m_conn){
            if (c != null) {c.cancel(); c = null;}
        }
        for(AcceptThread c: m_acc){
            if (c != null) {c.cancel(); c = null;}
        }
        setState(STATE_NONE);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // When writing, try to write out to all connected threads
        for (int i = 0; i < mConnThreads.size(); i++) {
            try {
                // Create temporary object
                ConnectedThread r;
                // Synchronize a copy of the ConnectedThread
                synchronized (this) {
                    if (mState != STATE_CONNECTED) return;
                    r = mConnThreads.get(i);
                }
                // Perform the write unsynchronized
                r.write(out);
            } catch (Exception e) {
            }
        }

//        ConnectedThread r;
//        // Synchronize a copy of the ConnectedThread
//        synchronized (this) {
//            if (mState != STATE_CONNECTED) return;
//            r = mConnectedThread;
//        }
//        // Perform the write unsynchronized
//        r.write(out);


    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        setState(STATE_LISTEN);
        // Commented out, because when trying to connect to all 7 UUIDs, failures will occur
        // for each that was tried and unsuccessful, resulting in multiple failure toasts.
        /*
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(BluetoothChat.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothChat.TOAST, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        */
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        this.start();
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        setState(STATE_LISTEN);

        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        this.start();
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        BluetoothServerSocket serverSocket = null;
        int counter;
        public AcceptThread(int i) {
            this.counter = i;
        }

        public void run() {
            if (D) Log.e(TAG,"begin accept thread");
            setName("AcceptThread");
            BluetoothSocket socket = null;
            try {
                // Listen for all 7 UUIDs
                //for (int i = 0; i < 7; i++) {
                  //  serverSocket = mAdapter.listenUsingRfcommWithServiceRecord(NAME, mUuids.get(i));



//                    socket = serverSocket.accept(300);
//                    if (socket != null) {
//                        String address = socket.getRemoteDevice().getAddress();
//                        mSockets.add(socket);
//                        mDeviceAddresses.add(address);
//                        connected(socket, socket.getRemoteDevice());
//                    }



                //}
                serverSocket = mAdapter.listenUsingRfcommWithServiceRecord(NAME,mUuids.get(this.counter));
//


                while (mState != STATE_CONNECTED) {
                    try {
                        // This is a blocking call and will only return on a
                        // successful connection or an exception
                        socket = serverSocket.accept();
                    } catch (IOException e) {
                        Log.e(TAG, "Socket Type: "  + "accept() failed", e);
                        break;
                    }

                    // If a connection was accepted
                    if (socket != null) {
                        synchronized (BluetoothService.this) {
                            switch (mState) {
                                case STATE_LISTEN:
                                case STATE_CONNECTING:
                                    // Situation normal. Start the connected thread.
                                    connected(socket, socket.getRemoteDevice());
                                    break;
                                case STATE_NONE:
                                case STATE_CONNECTED:
                                    // Either not ready or already connected. Terminate new socket.
                                    try {
                                        socket.close();
                                    } catch (IOException e) {
                                        Log.e(TAG, "Could not close unwanted socket", e);
                                    }
                                    break;
                            }
                        }
                    }
                }

            } catch (IOException e) {
                Log.e(TAG,"uuid not working, try another one");
            }
            if (D) Log.e(TAG,"end accept thread");
        }

        public void cancel() {
            if (D) Log.e(TAG,"cancel");
            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.e(TAG,"close of server failed");
            }
        }
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private UUID tempUuid;

        public ConnectThread(BluetoothDevice device, UUID uuidToTry) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            tempUuid = uuidToTry;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(uuidToTry);
            } catch (IOException e) {
                Log.e(TAG,"create thread failed");
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.e(TAG,"begin connect thread");
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
//                if (tempUuid.toString().contentEquals(mUuids.get(6).toString())) {
//                    connectionFailed();
//                }
//                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG,"unable to close socket connectthread");
                }
                // Start the service over to restart listening mode
                connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG,"close connect thread failed");
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.e(TAG,"connected thread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG,"temp socket not created");
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.e(TAG,"BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);

                    // Send the obtained bytes to the UI Activity
                    mHandler.obtainMessage(Constants.MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG,"disconnected");
                    connectionLost();
                    BluetoothService.this.start();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         * @param buffer  The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG,"Exception during write");
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG,"close() of connect socket failed");
            }
        }
    }
}

