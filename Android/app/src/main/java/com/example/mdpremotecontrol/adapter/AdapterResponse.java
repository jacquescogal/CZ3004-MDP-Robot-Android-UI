package com.example.mdpremotecontrol.adapter;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mdpremotecontrol.R;
import com.example.mdpremotecontrol.control.ResponseControl;

import java.util.ArrayList;

public class AdapterResponse extends RecyclerView.Adapter<AdapterResponse.ViewHolder> {
    ArrayList<String> responseList;
    ArrayList<String> timeList;
    Context context;
    ResponseControl responseControl;

    public AdapterResponse(ArrayList<String> responseList,ArrayList<String> timeList)
    {
        this.responseList = responseList;
        this.timeList = timeList;
    }

    public void setData(ArrayList<String>responseList,ArrayList<String> timeList){
        this.responseList = responseList;
        this.timeList = timeList;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_response,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        context = parent.getContext();
        return viewHolder;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        String output = responseList.get(position);
        String timeOutput = timeList.get(position);
        holder.textStatus.setText(output);
        holder.timeStatus.setText(timeOutput);
        holder.textStatus.setTextColor(Color.BLACK);
        holder.cvHead.setVisibility(View.INVISIBLE);

        holder.cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, output, Toast.LENGTH_SHORT).show();
                Toast.makeText(view.getContext(), "Clicked" + holder.getAdapterPosition(), Toast.LENGTH_LONG).show();
            }
        });

    }


    @Override
    public int getItemCount() {
        return responseList.size();
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
            timeStatus=itemView.findViewById(R.id.status_time);

            cvHead=itemView.findViewById(R.id.list_date_div_cv);
            textHead=itemView.findViewById(R.id.list_date_div);
        }

    }

}