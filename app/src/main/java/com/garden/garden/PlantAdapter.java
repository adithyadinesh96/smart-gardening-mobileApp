package com.garden.garden;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintSet;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.List;

public class PlantAdapter extends RecyclerView.Adapter<PlantAdapter.PlantViewHolder>{

    private Context ctx;
    private List<PlantData> plantDataList;
    public PlantAdapter(Context ctx, List<PlantData> plantDataList) {
        this.ctx = ctx;
        this.plantDataList = plantDataList;
    }

    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(ctx);
        View view = inflater.inflate(R.layout.plantlist,null);
        PlantViewHolder holder = new PlantViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
    PlantData plantData = plantDataList.get(position);
    Picasso.get().load(plantData.getImage_url()).into(holder.imageView);
    holder.textView.setText(plantData.getName());

    }

    @Override
    public int getItemCount() {
        return plantDataList.size();
    }

    class PlantViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView imageView;
        TextView textView;
        public PlantViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.market_plant_image);
            textView = itemView.findViewById(R.id.market_plant_name);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            String name = this.textView.getText().toString().trim();
            Intent in = new Intent(ctx,CheckoutActivity.class);
            in.putExtra("plant_name",name);
            ctx.startActivity(in);
        }
    }
}
