package com.garden.garden;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MarketActivity extends AppCompatActivity {
    private RecyclerView rv;
    PlantAdapter adapter;
    public List<PlantData> plantDataList;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market);
        rv = findViewById(R.id.market_recycler);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        plantDataList = new ArrayList<>();
        rv.setHasFixedSize(true);
        DatabaseReference plant_ref = FirebaseDatabase.getInstance().getReference().child("plants");
        plant_ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot plants: dataSnapshot.getChildren()){
                    String plant_name = plants.getKey();
                    String url = plants.child("image_url").getValue().toString();
                    plantDataList.add(
                            new PlantData(plant_name,url));
                }
                adapter = new PlantAdapter(MarketActivity.this,plantDataList);
                rv.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

    }
}
