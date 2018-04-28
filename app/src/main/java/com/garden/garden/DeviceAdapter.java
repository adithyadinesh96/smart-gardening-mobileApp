package com.garden.garden;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>{
    private Context ctx;
    private List<DeviceData>deviceDataList;

    public DeviceAdapter(Context ctx, List<DeviceData> deviceDataList) {
        this.ctx = ctx;
        this.deviceDataList = deviceDataList;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(ctx);
        View view = inflater.inflate(R.layout.device_list,null);
        DeviceViewHolder holder = new DeviceViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        DeviceData deviceData = deviceDataList.get(position);
        Picasso.get().load(deviceData.getUrl()).into(holder.imageView);
        holder.textView1.setText(deviceData.getDeviceName());
        if(deviceData.getSwitchState() == 1){
            holder.textView2.setText("On");
        }
        else{
            holder.textView2.setText("Off");
        }
        holder.deviceId.setText(deviceData.getDeviceId());
    }


    @Override
    public int getItemCount() {
        return deviceDataList.size();
    }

    public class DeviceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageView;
        TextView textView1;
        TextView textView2;
        TextView deviceId;
        public DeviceViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.device_image_view);
            textView1 = itemView.findViewById(R.id.device_name_view);
            textView2 = itemView.findViewById(R.id.device_switch_view);
            deviceId = itemView.findViewById(R.id.device_id);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            /*Toast.makeText(v.getContext(),this.deviceId.getText(),Toast.LENGTH_LONG).show();*/
            Intent in = new Intent(ctx,DeviceDetails.class);
            in.putExtra("device_id",this.deviceId.getText());
            ctx.startActivity(in);
        }
    }
}