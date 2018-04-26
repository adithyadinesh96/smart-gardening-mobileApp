package com.garden.garden;

import android.content.Intent;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.razorpay.Checkout;

import java.util.ArrayList;
import java.util.List;

//Todo on press of back button pause activity go to home screen..
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private Button logout;
    private DatabaseReference mDatabase;
    private String uid;
    private int isActive;
    private ConstraintLayout mcontraint;
    private static final String TAG = "MainActivity";
    private Button goToMarket;
    private TextView noDevice;
    private RecyclerView rv;
    DeviceAdapter adapter;
    public List<DeviceData> deviceDataList;
    public List<String> deviceList;
    private String deviceName;
    private int switchState;
    private String url;
    private int deviceFound = 0;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Checkout.preload(getApplicationContext());
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        logout = findViewById(R.id.logout);
        logout.setOnClickListener(this);
        mcontraint = findViewById(R.id.main_constraint);
        goToMarket = findViewById(R.id.go_to_market);
        goToMarket.setOnClickListener(this);
        noDevice = findViewById(R.id.no_device_text);
        rv = findViewById(R.id.main_recycler);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        deviceList = new ArrayList<>();
        deviceDataList = new ArrayList<>();
        rv.setHasFixedSize(true);


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
            DatabaseReference user_ref = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
            if (!currentUser.isEmailVerified()) {
                MaterialDialog dialog = new MaterialDialog.Builder(this)
                        .title(R.string.email_not_verfied)
                        .content(R.string.not_verfied_text)
                        .negativeText(R.string.refresh)
                        .positiveText(R.string.logout)
                        .autoDismiss(false)
                        .cancelable(false)
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog dialog, DialogAction which) {
                                mAuth.getCurrentUser().reload();
                                Handler mHandler = new Handler();
                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = getIntent();
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                        finish();
                                        startActivity(intent);
                                    }

                                }, 1000L);
                            }
                        })
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog dialog, DialogAction which) {
                                mAuth.signOut();
                                gotoLogin();
                            }
                        })
                        .show();
            } else {
                final MaterialDialog loading_dialog = new MaterialDialog.Builder(this)
                        .content(R.string.please_wait)
                        .progress(true, 0)
                        .cancelable(false)
                        .show();
                DatabaseReference device_ref = FirebaseDatabase.getInstance().getReference().child("devices");
                device_ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        deviceDataList = new ArrayList<>();
                        Log.d("UID", uid);
                        for (DataSnapshot device : dataSnapshot.getChildren()) {
                            Log.d("Device", device.getKey());
                            try {
                                if (device.child("uid").getValue().equals(uid)) {
                                    deviceFound++;
                                    deviceId = device.getKey();
                                    deviceName = device.child("device_name").getValue().toString();
                                    url = device.child("image_url").getValue().toString();
                                    switchState = Integer.parseInt(device.child("switch_state").getValue().toString());
                                    deviceDataList.add(new DeviceData(deviceId,deviceName, switchState, 0, url));
                                }
                            }
                            catch(NullPointerException nullPointer){
                                restartActivity();
                            }
                            catch (Exception e) {
                                restartActivity();
                            }
                        }
                        if (deviceFound == 0) {
                            noDevice.setVisibility(View.VISIBLE);
                            goToMarket.setVisibility(View.VISIBLE);
                        } else {
                            goToMarket.setVisibility(View.VISIBLE);
                            adapter = new DeviceAdapter(MainActivity.this, deviceDataList);
                            rv.setAdapter(adapter);
                        }
                        loading_dialog.dismiss();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

        }
    }

    private void restartActivity() {
        Intent mIntent = getIntent();
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        startActivity(mIntent);
    }

    @Override
    public void onClick(View v) {
        if (v == logout) {
            mAuth.signOut();
            Checkout.clearUserData(this);
            gotoLogin();
        } else if (v == goToMarket) {
            Intent in = new Intent(this, MarketActivity.class);
            startActivity(in);
        }
    }

    private void gotoLogin() {
        Intent in = new Intent(this, LoginActivity.class);
        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(in);
    }
}
