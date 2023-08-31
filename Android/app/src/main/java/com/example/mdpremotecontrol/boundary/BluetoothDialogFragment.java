package com.example.mdpremotecontrol.boundary;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.mdpremotecontrol.R;
import com.example.mdpremotecontrol.adapter.AdapterBluetooth;
import com.example.mdpremotecontrol.control.BluetoothControl;
import com.example.mdpremotecontrol.utils.SpacingItemDecoration;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Set;


public class BluetoothDialogFragment extends DialogFragment implements View.OnClickListener {

    public static String TAG = "BluetoothDialogFragment";
    private final int PERMISSION_REQUEST_BLUETOOTH = 100;

    AdapterBluetooth adapterBluetooth;
    SwipeRefreshLayout swipeRefreshLayout;

    Button buttonCloseBluetooth;
    BluetoothAdapter btAdapter;
    View view;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view=inflater.inflate(R.layout.bluetooth_dialog, container, false);

        BluetoothControl bluetoothControl=BluetoothControl.getInstance();
        ArrayList<BluetoothDevice> btList=new ArrayList<BluetoothDevice>();
        for (BluetoothDevice device:bluetoothControl.getDeviceList()){
            btList.add(device);
        }

        adapterBluetooth = new AdapterBluetooth(btList);


        RecyclerView recyclerView = (RecyclerView)view.findViewById(R.id.bt_feed);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        SpacingItemDecoration itemDecoration = new SpacingItemDecoration(5);
        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapterBluetooth);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                btList=new ArrayList<BluetoothDevice>();
//                for (Friend friendSub:friendManager.getFriendList()){
//                    friendIDList.add(friendSub.getUserID());
//                }
//                SocialManager socialManager = SocialManager.getInstance();
//                socialManager.loadFriendStatusList();
//                statusAdapter.setData(socialManager.getFriendStatusList(),friendIDList, friendManager.getFriendList());
//                statusAdapter.notifyDataSetChanged();
                Toast.makeText(getContext(), "refresh", Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.bringToFront();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        buttonCloseBluetooth = getView().findViewById(R.id.bt_close_button);
        buttonCloseBluetooth.setOnClickListener(this);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        BluetoothManager btManager = context.getSystemService(BluetoothManager.class);
        btAdapter = btManager.getAdapter();

        // Checks if device has bluetooth capability
        if (btAdapter == null) {
            return;
        }

        // Enable bluetooth
        if (!btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
        }


        // The request of permission does not show. Online forums suggest that bluetooth_connect need not have explicit perm
        // However the check still returns False though online forum states that bluetooth functionality should still work
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.BLUETOOTH_CONNECT},1);
        }
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();

        // Check for already paired devices
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                BluetoothControl.getInstance().addDevice(device);
                BluetoothControl.getInstance().addPairedDevice(device);
            }
        }
        // If another discovery is in progress, cancels it before starting the new one.
        if (btAdapter.isDiscovering()) {
            btAdapter.cancelDiscovery();
        }
        IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        requireActivity().registerReceiver(mBroadcastReceiver, filter2);

        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        requireActivity().registerReceiver(mReceiver, filter);

        btAdapter.startDiscovery();
        BluetoothControl.getInstance().start();
    }

    @Override
    public void onDetach() {
        requireActivity().unregisterReceiver(mBroadcastReceiver);
        requireActivity().unregisterReceiver(mReceiver);
        BluetoothControl.getInstance().destroyArrayList();
        super.onDetach();
    }
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothControl bluetoothControl=BluetoothControl.getInstance();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                //discovery starts, we can show progress dialog or perform other tasks
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //discovery finishes, dismiss progress dialog
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //bluetooth device found
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (!bluetoothControl.contained(device) && device.getName()!=null){
                bluetoothControl.addDevice(device);
                adapterBluetooth.setData(bluetoothControl.getDeviceList());
                adapterBluetooth.notifyDataSetChanged();
                }
            }
        }
    };

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Toast.makeText(context, "BroadcastReceiver: Check bonding", Toast.LENGTH_LONG).show();
            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Toast.makeText(context, "BroadcastReceiver: BOND_BONDED", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED.");
                }
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Toast.makeText(context, "BroadcastReceiver: BOND_BONDING", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING.");
                }
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Toast.makeText(context, "BroadcastReceiver: BOND_NONE", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "BroadcastReceiver: BOND_NONE.");
                }
            }
        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bt_close_button:
                dismiss();
        }
    }

    //Array Adapter
    //ArrayAdapter adapter = new ArrayAdapter<>()
}
