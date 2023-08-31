package com.example.mdpremotecontrol.adapter;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mdpremotecontrol.R;
import com.example.mdpremotecontrol.boundary.MainActivity;
import com.example.mdpremotecontrol.control.BluetoothControl;

import java.util.ArrayList;

public class AdapterBluetooth extends RecyclerView.Adapter<AdapterBluetooth.ViewHolder> {
    ArrayList<BluetoothDevice> btList;
    Context context;
    BluetoothControl bluetoothControl;

    public AdapterBluetooth(ArrayList<BluetoothDevice>btList)
    {
        this.btList = btList;
    }

    public void setData(ArrayList<BluetoothDevice>btList){
        this.btList = btList;
    }

    public void setBluetoothControl(BluetoothControl btControl)
    {

    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bt,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        context = parent.getContext();
        return viewHolder;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        BluetoothDevice device = btList.get(position);
        holder.textStatus.setText(device.getName());
        holder.textStatus.setTextColor(Color.BLACK);
        holder.cvHead.setVisibility(View.INVISIBLE);

        if (BluetoothControl.getInstance().isPaired(device)){
            holder.statusImage.setVisibility(View.VISIBLE);
        }

        holder.cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BluetoothDevice device = btList.get(holder.getAdapterPosition());
                device.createBond();
                checkBond(device,view);
                view.setBackgroundColor(context.getResources().getColor(R.color.green_prime));
                holder.textStatus.setTextColor(Color.WHITE);
                MainActivity.getInstance().btSwitch();
            }
        });

    }

    @SuppressLint("MissingPermission")
    public void checkBond(BluetoothDevice mDevice,View view){
            //BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
//                Toast.makeText(context, "BroadcastReceiver: BOND_BONDED", Toast.LENGTH_LONG).show();
                Log.d("BLUETOOTH", "BroadcastReceiver: BOND_BONDED.");
                BluetoothControl.getInstance().btThreadSetup(mDevice, view.getContext());
            }
            if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
//                Toast.makeText(context, "BroadcastReceiver: BOND_BONDING", Toast.LENGTH_LONG).show();
                Log.d("BLUETOOTH", "BroadcastReceiver: BOND_BONDING.");
            }
            if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
//                Toast.makeText(context, "BroadcastReceiver: BOND_NONE", Toast.LENGTH_LONG).show();
                Log.d("BLUETOOTH", "BroadcastReceiver: BOND_NONE.");
            }
        }

    @Override
    public int getItemCount() {
        return btList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        //ImageView imgTvShow;
        TextView textStatus,timeStatus;
        CardView cv;
        ImageView statusImage;

        CardView cvHead;
        TextView textHead;

        public ViewHolder(View itemView)
        {
            super(itemView);
            //imgTvShow = (ImageView)itemView.findViewById(R.id.imgTvshow);
            textStatus = (TextView)itemView.findViewById(R.id.status_text);
            cv = (CardView)itemView.findViewById(R.id.cv);
            statusImage=itemView.findViewById(R.id.status_image);
//            timeStatus=itemView.findViewById(R.id.status_time);

            cvHead=itemView.findViewById(R.id.list_date_div_cv);
            textHead=itemView.findViewById(R.id.list_date_div);
        }

    }

}