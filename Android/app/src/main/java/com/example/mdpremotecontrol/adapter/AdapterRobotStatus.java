package com.example.mdpremotecontrol.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
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
import com.example.mdpremotecontrol.control.RobotStatusControl;

import java.util.ArrayList;

public class AdapterRobotStatus extends RecyclerView.Adapter<AdapterRobotStatus.ViewHolder> {
    ArrayList<String> robotResponseList;
    ArrayList<String> robotTimeList;
    Context context;
    RobotStatusControl robotStatusControl;

    public AdapterRobotStatus(ArrayList<String> robotResponseList,ArrayList<String> robotTimeList)
    {
        this.robotResponseList = robotResponseList;
        this.robotTimeList = robotTimeList;
    }

    public void setData(ArrayList<String>robotResponseList,ArrayList<String> robotTimeList){
        this.robotResponseList = robotResponseList;
        this.robotTimeList = robotTimeList;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_robot,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        context = parent.getContext();
        return viewHolder;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        String output = robotResponseList.get(position);
        String timeOutput = robotTimeList.get(position);
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
        return robotResponseList.size();
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