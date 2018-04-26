package com.garden.garden;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class DeviceDetails extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    private FirebaseAuth mAuth;
    private String device_id;
    private FirebaseUser currentUser;
    private String uid;
    private DatabaseReference device_ref;
    private ImageView deviceImage;
    private TextView deviceName;
    private TextView plantName;
    private TextView waterState;
    private TextView temperature;
    private ToggleButton waterSwitch;
    private int switchState;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_details);
        mAuth = FirebaseAuth.getInstance();
        deviceImage = findViewById(R.id.devcie_details_image);
        deviceName = findViewById(R.id.device_details_name);
        plantName = findViewById(R.id.device_details_plant_name);
        waterState = findViewById(R.id.device_details_water_state);
        temperature = findViewById(R.id.device_details_temperature_reading);
        waterSwitch = findViewById(R.id.device_details_water_switch);
        waterSwitch.setOnCheckedChangeListener(this);
        Bundle extras = getIntent().getExtras();
        device_id = extras.getString("device_id");
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            gotoLogin();
        } else {
            uid = currentUser.getUid();
            device_ref = FirebaseDatabase.getInstance().getReference().child("devices").child(device_id);
            device_ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Picasso.get().load(dataSnapshot.child("image_url").getValue().toString()).placeholder(R.drawable.loading).into(deviceImage);
                    deviceName.setText(dataSnapshot.child("device_name").getValue().toString());
                    plantName.setText(dataSnapshot.child("plant_name").getValue().toString());
                    switchState = Integer.parseInt(dataSnapshot.child("switch_state").getValue().toString());
                    if (switchState == 1) {
                        waterState.setText("On");
                    } else {
                        waterState.setText("Off");
                    }
                    temperature.setText(dataSnapshot.child("temperature").getValue().toString());
                    if(Integer.parseInt(dataSnapshot.child("switch_state").getValue().toString()) == 1){
                        waterSwitch.setChecked(true);
                    }
                    else {
                        waterSwitch.setChecked(false);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
    private void gotoLogin(){
        Intent in = new Intent(this, LoginActivity.class);
        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(in);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked){
            device_ref.child("switch_state").setValue(1);
        }
        else {
            device_ref.child("switch_state").setValue(0);
        }
    }
}