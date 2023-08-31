package com.example.mdpremotecontrol.control;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.appcompat.app.AppCompatActivity;

import android.os.ParcelUuid;
import android.util.Log;

import com.example.mdpremotecontrol.boundary.MainActivity;
import com.example.mdpremotecontrol.boundary.ObstacleView;
import com.example.mdpremotecontrol.boundary.RobotView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;


public class BluetoothControl extends AppCompatActivity {
    //Singleton
    private static BluetoothControl instance = null;
    private BluetoothSocket bss = null;
    private BluetoothDevice bd = null;
    private UUID deviceUUID;
    private ConnectThread mConnectThread;
    private BluetoothLostReceiver bluetoothLostReceiver;

    private static final String TAG = "BLUETOOTH";
    private static final UUID My_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothServerSocket btServerS;

    private AcceptThread mInsecureAcceptThread;

    private static ConnectedThread mConnectedThread;
    private Context con;

    private Thread sendThread;

    private boolean sendThreadRunning=false;


    public static BluetoothControl getInstance() {
        if (instance == null) {
            instance = new BluetoothControl();
        }
        return instance;
    }

    private ArrayList<BluetoothDevice> bluetoothDeviceList = new ArrayList<>();
    private ArrayList<BluetoothDevice> bluetoothPairedList = new ArrayList<>();

    public void addPairedDevice(BluetoothDevice device) {
        bluetoothPairedList.add(device);
    }

    public boolean isPaired(BluetoothDevice btDevice){
        return bluetoothPairedList.contains(btDevice);
    }

    public void addDevice(BluetoothDevice device) {bluetoothDeviceList.add(device);}

    public ArrayList<BluetoothDevice> getDeviceList() {
        ArrayList<BluetoothDevice> resultList=new ArrayList<BluetoothDevice>();
//        for (BluetoothDevice device:bluetoothDeviceList){
//            if (isPaired(device)){
//                resultList.add(device);
//            }
//        }
        for (BluetoothDevice device:bluetoothDeviceList){
            resultList.add(device);
        }
        return resultList;
    }

    public boolean contained(BluetoothDevice device) {
        return bluetoothDeviceList.contains(device);
    }

    public void destroyArrayList() {
        bluetoothDeviceList.clear();
    }

    public BluetoothDevice getBd(){
        return bd;
    }

/*    @SuppressLint("MissingPermission")
    public void connectDevice(Context context) {
        Toast.makeText(context, "In connect device function trying to connect to " + bd.getName(), Toast.LENGTH_SHORT).show();

        try {
            bss.connect();
            Toast.makeText(context, "Connected to " + bd.getName(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            try {

                Toast.makeText(context, e + "Fail to connect to " + bd.getName(), Toast.LENGTH_SHORT).show();
                System.out.println(bss);
                bss.close();
            } catch (IOException ex) {
                Toast.makeText(context, "Fail to close socket ", Toast.LENGTH_SHORT).show();
                //throw new RuntimeException(ex);
            }
        }

        return;
    }
*/
    public void setContext(Context context){
        this.con = context;
    }
    @SuppressLint("MissingPermission")
    public void btThreadSetup(BluetoothDevice device, Context context) {

        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        bd = device;
        try {
            //Toast.makeText(context, "Creating bt thread for " + bd.getName(), Toast.LENGTH_SHORT).show();
            ParcelUuid[] puuid = bd.getUuids();
            ParcelUuid tmp = puuid[0];
            System.out.println(tmp.getUuid());

            //bd.fetchUuidsWithSdp();
            //btServerS = BluetoothAdapter.getDefaultAdapter().listenUsingInsecureRfcommWithServiceRecord("MDP_G26", My_UUID);
            //bss = device.createRfcommSocketToServiceRecord(tmp.getUuid());
            //System.out.println(bss.getRemoteDevice().getName());
            //bss = device.createRfcommSocketToServiceRecord(UUID.fromString("a94f84b0-1fb5-4018-8cdb-dc60e3462a65"));
            //bss = device.createRfcommSocketToServiceRecord(tempUUID[0].getUuid());
            //connectDevice(context);
            startClient(bd,My_UUID);
        } catch (Exception e) {
            //throw new RuntimeException(e);
            Log.e(TAG, "createBTThread: IOException: " + e.getMessage());
        }
    }

    private class AcceptThread extends Thread {
        private BluetoothServerSocket ServerSocket;
        private boolean runThread = true;

        @SuppressLint("MissingPermission")
        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            try {
                ServerSocket = BluetoothAdapter.getDefaultAdapter().listenUsingInsecureRfcommWithServiceRecord("MDP_G26", My_UUID);
                Log.d(TAG, "Accept Thread: Setting up Server using: " + My_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Accept Thread: IOException: " + e.getMessage());
            }
        }

        public void run() {
            Log.d(TAG, "run: AcceptThread Running. ");
            while (BluetoothAdapter.getDefaultAdapter().getState() != BluetoothAdapter.STATE_OFF & runThread) {
                BluetoothSocket socket = null;
                try {
                    Log.d(TAG, "run: RFCOM server socket start here...");
                    socket = ServerSocket.accept();
                    Log.d(TAG, "run: RFCOM server socket accepted connection.");
                    //@SuppressLint("MissingPermission") ParcelUuid[] puuid = socket.getRemoteDevice().getUuids();;
                    //ParcelUuid tmp = puuid[0];
                } catch (IOException e) {
                    Log.e(TAG, "AcceptThread: Failed to accept client socket: " + e.getMessage());
                }
                if (socket != null) {
                    connected(socket, socket.getRemoteDevice());
                    break;
                }
            }
            Log.i(TAG, "END AcceptThread");
        }

        public void cancel() {
            Log.d(TAG, "cancel: Cancelling AcceptThread");

            // stop while loop in run
            runThread = false;

            // close server socket
            try {
                ServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: Failed to close AcceptThread ServerSocket " + e.getMessage());
            }

            mInsecureAcceptThread = null;
        }
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device, UUID uuid) {
            Log.d(TAG, "ConnectThread: started.");
            bd = device;
            deviceUUID = uuid;
            if (bluetoothLostReceiver == null)
            {
                bluetoothLostReceiver = new BluetoothLostReceiver();
                //bluetoothLostReceiver.setMainActivity(MainActivity);
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
                Log.d("BLUETOOTH", "Disconnection sensor up");
                //registerReceiver(bluetoothLostReceiver, filter);
            }
        }

        @SuppressLint("MissingPermission")
        public void run(){
            BluetoothSocket tmp = null;
            Log.i(TAG, "RUN mConnectThread ");

            try {
                Log.d(TAG, "ConnectThread: Trying to create InsecureRfcommSocket using UUID: "
                        +My_UUID );
                tmp = bd.createRfcommSocketToServiceRecord(deviceUUID);

            } catch (IOException e) {
                Log.e(TAG, "ConnectThread: Could not create InsecureRfcommSocket " + e.getMessage());
            }

            mmSocket = tmp;

            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();


            try {
                mmSocket.connect();

                Log.d(TAG, "run: ConnectThread connected.");
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                    Log.d(TAG, "run: Closed Socket.");
                } catch (IOException e1) {
                    Log.e(TAG, "mConnectThread: run: Unable to close connection in socket " + e1.getMessage());
                }
                Log.d(TAG, "run: ConnectThread: Could not connect to UUID: " + My_UUID );
            }

            connected(mmSocket,bd);
        }
        public void cancel() {
            try {
                Log.d(TAG, "cancel: Closing Client Socket.");
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: close() of mmSocket in Connectthread failed. " + e.getMessage());
            }
            catch (Exception e){
                Log.e(TAG, "cancel: close() of mmSocket in Connectthread failed. " + e.getMessage());
            }
        }
    }

    public synchronized void start() {
        Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread();
            mInsecureAcceptThread.start();
        }
    }

    public void startClient(BluetoothDevice device,UUID uuid){
        Log.d(TAG, "startClient: Started.");

        mConnectThread = new ConnectThread(device, uuid);
        mConnectThread.start();
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Starting.");

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run(

        ){
            byte[] buffer = new byte[1024];

            int bytes;

            while (true) {
                try {
                    bytes = mmInStream.read(buffer);

                    //Log.d(TAG, Byte.toString(buffer));
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "InputStream: " + incomingMessage);

                    //split incoming message by '\\{';
                    if (!incomingMessage.contains("{")){
                        continue;
                    }
                    Queue<String> messageQ = new LinkedList<>();
                    String[] wholeMessage=incomingMessage.split("\\{");
                    for (int sentinal=1;sentinal<wholeMessage.length;sentinal++){
                        if (wholeMessage[sentinal].contains("}")) {
                            messageQ.add("{" + wholeMessage[sentinal]);
                        }
                    }


                    while (!messageQ.isEmpty()) {
                        incomingMessage=messageQ.remove();
                        char[] charIm = incomingMessage.toCharArray();
                        if (charIm[0] == '{') {
                            //String message = incomingMessage.substring(11, incomingMessage.length()-2);
                            String[] messageArray = incomingMessage.split(":");
                            messageArray[0] = messageArray[0].substring(1, messageArray[0].length());
                            if (messageArray[0].equals("answer")) {
                                messageArray[2] = messageArray[2].substring(0, messageArray[2].length() - 1);
                                Log.e(TAG, messageArray[2]);
                                try {
                                    messageArray[2] = messageArray[2].substring(0, messageArray[2].indexOf('}'));
                                } catch (Exception e) {
                                    //pass
                                }
                            } else {
                                messageArray[1] = messageArray[1].substring(0, messageArray[1].length() - 1);
                                try {
                                    messageArray[1] = messageArray[1].substring(0, messageArray[1].indexOf('}'));
                                } catch (Exception e) {
                                    //pass
                                }
                            }
                            Log.e(TAG, messageArray[0] + ": " + messageArray[1]);
                            //TextView tv = (TextView) ((Activity)con).findViewById(R.id.status_Text);
                            //tv.setText(messageArray[0]);
                            //tv.setText("Test");
                            //if(messageArray[0] == "status"){
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    // UI Updates

                                }
                            });
                            switch (messageArray[0]) {
                                case "answer":
                                    //MainActivity.getInstance().getTextView(1, messageArray[1]);
                                    runOnUiThread(() -> {
                                        // UI Updates
                                        Log.e(TAG, messageArray[1]);
                                        try {
                                            ObstacleView ov = MainActivity.getInstance().getOV(Integer.parseInt(messageArray[1]));
                                            ov.updateImage(messageArray[2]);
                                            MainActivity.getInstance().responseAddAndRefresh("Block "+messageArray[1] +"("+ov.getGridX()+","+ov.getGridY()+")"+ ": " + messageArray[2]);
                                        } catch (Exception e) {
                                            Log.e(TAG, "run: Next");
                                        }
                                    });
                                    break;
                                case "status":
                                    runOnUiThread(() -> {
                                        // UI Updates
                                        try {
                                            MainActivity.getInstance().robotStatusAddAndRefresh(messageArray[1]);
                                        } catch (Exception e) {
                                            Log.e(TAG, messageArray[1]);
                                        }
                                    });
                                    break;
                                case "mode":
                                    runOnUiThread(() -> {
                                        // UI Updates
                                        MainActivity.getInstance().robotStatusAddAndRefresh(messageArray[1]);
                                    });
                                    if (messageArray[1].equals("EXPLORE")) {
                                        Log.e(TAG, "run: Explore received");
                                        interruptSendThread();
                                    }
                                    break;
                                case "move":
                                    //TextView tv = (TextView) ((Activity)con).findViewById(R.id.status_Text);
                                    //TextView tv = MainActivity.getInstance().getTextView();
                                    runOnUiThread(() -> {

                                        switch (messageArray[1]) {
                                            case "w":
                                                MainActivity.getInstance().robotMove("FF");
                                                break;
                                            case "s":
                                                MainActivity.getInstance().robotMove("BB");
                                                break;
                                            case "e":
                                                MainActivity.getInstance().robotMove("FR");
                                                break;
                                            case "q":
                                                //MainActivity.getInstance().pressButton(1);
                                                MainActivity.getInstance().robotMove("FL");
                                                break;
                                            case "d":
                                                MainActivity.getInstance().robotMove("BR");
                                                break;
                                            case "a":
                                                MainActivity.getInstance().robotMove("BL");
                                                break;
                                            default:
                                                break;
                                        }

                                    });
                                    break;
                                case "TP":
                                    runOnUiThread(() -> {
                                        Log.e(TAG, messageArray[1]);
                                        RobotView rv = MainActivity.getInstance().getRV();
                                        String[] coord = messageArray[1].split(",");
                                        rv.teleport(Integer.parseInt(coord[0]), Integer.parseInt(coord[1]), coord[2]);
                                    });
                                    break;
                                default:
                                    break;

                            }

                        }
                    }

                    //}
                } catch (IOException e) {
                    Log.e(TAG, "write: Error reading Input Stream. " + e.getMessage() );
                    cancel();
                    try {
                        mmInStream.close();
                    } catch (IOException ex) {
                        //throw new RuntimeException(ex);
                        Log.e(TAG, "closeInputStream: Error closing input stream. " + ex.getMessage() );
                    }
                    //retryConnection();
                    //start();
                    //startClient(bd, My_UUID);
                    break;
                }
                catch (Exception e){
                    break;
                }
            }
        }

        public void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to outputstream: " + text);
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "write: Error writing to output stream. " + e.getMessage() );
                cancel();
                try {
                    mmOutStream.close();
                } catch (IOException ex) {
                    //throw new RuntimeException(ex);
                    Log.e(TAG, "closeInputStream: Error closing output stream. " + ex.getMessage() );
                }
                //retryConnection();
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { Log.e(TAG, "cancelSocket: Error canceling " + e.getMessage() ); }
        }
    }

    private void connected(BluetoothSocket mmSocket, BluetoothDevice mmDevice) {
        Log.d(TAG, "connected: Starting.");

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;

        // Synchronize a copy of the ConnectedThread
        Log.d(TAG, "write: Write Called.");
        //perform the write
        mConnectedThread.write(out);
    }

    public class BluetoothLostReceiver extends BroadcastReceiver {

        MainActivity main = null;

        public void setMainActivity(MainActivity main)
        {
            this.main = main;
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d("BLUETOOTH", "Disconnect detected");
            if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(intent.getAction()))
            {
                //BluetoothControl.getInstance().btThreadSetup(BluetoothControl.getInstance().getBd(), context);
                BluetoothControl.getInstance().start();
            }else
            {
                //Global.tryBluetoothReconnect = true;
            }
        }
    }

    public void retryConnection(){
        //for(int x = 0; x < 3; x++){
            //btThreadSetup(bd,con);
            //startClient(bd,My_UUID);
        //}
        //@SuppressLint("MissingPermission") ParcelUuid[] puuid = bd.getUuids();
        //ParcelUuid tmp = puuid[0];
        //System.out.println(tmp.getUuid());

        if (bd!=null) startClient(bd,My_UUID);
        //mInsecureAcceptThread.cancel();
        //BluetoothControl.this.start();
    }
    public void cancelConnection(){
        mConnectedThread.cancel();
    }

    public void setSendThread(Thread thread){
        if (this.sendThread!=null && sendThreadRunning==true){
            this.sendThread.interrupt();
            sendThreadRunning=false;
        }
        this.sendThread=thread;
    }

    public void startSendThread(){
        if (this.sendThread!=null && this.sendThreadRunning==false){
            this.sendThread.start();
            this.sendThreadRunning=true;
        }
    }

    public void interruptSendThread(){
        if (this.sendThread!=null && sendThreadRunning==true) {
            this.sendThread.interrupt();
            this.sendThreadRunning=false;
        }
    }
}