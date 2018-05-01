package com.smart_garden.garden;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.razorpay.Checkout;
import com.squareup.picasso.Picasso;

public class DeviceDetails extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
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
    private TextView nav_name;
    private TextView nav_email;
    private ImageView changeDeviceName;
    private String deviceNameText;
    private MaterialDialog deviceNameDialog;
    private ToggleButton deviceMode;
    private int deviceModeValue;
    private MaterialDialog alert_dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_details_container);
        mAuth = FirebaseAuth.getInstance();
        deviceImage = findViewById(R.id.devcie_details_image);
        deviceName = findViewById(R.id.device_details_name);
        plantName = findViewById(R.id.device_details_plant_name);
        waterState = findViewById(R.id.device_details_water_state);
        temperature = findViewById(R.id.device_details_temperature_reading);
        waterSwitch = findViewById(R.id.device_details_water_switch);
        changeDeviceName = findViewById(R.id.changeDeviceName);
        changeDeviceName.setOnClickListener(this);
        waterSwitch.setOnCheckedChangeListener(this);
        deviceMode = findViewById(R.id.device_mode_toggle);
        deviceMode.setOnCheckedChangeListener(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        nav_name = header.findViewById(R.id.nav_name);
        nav_email = header.findViewById(R.id.nav_email);
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
                    deviceNameText = dataSnapshot.child("device_name").getValue().toString();
                    deviceName.setText(deviceNameText);
                    plantName.setText(dataSnapshot.child("plant_name").getValue().toString());
                    switchState = Integer.parseInt(dataSnapshot.child("switch_state").getValue().toString());
                    if (switchState == 1) {
                        waterState.setText("On");
                    } else {
                        waterState.setText("Off");
                    }
                    temperature.setText(dataSnapshot.child("temperature").getValue().toString());
                    if (Integer.parseInt(dataSnapshot.child("switch_state").getValue().toString()) == 1) {
                        waterSwitch.setChecked(true);
                    } else {
                        waterSwitch.setChecked(false);
                    }
                    deviceModeValue = Integer.parseInt(dataSnapshot.child("mode").getValue().toString());
                    if (deviceModeValue == 1) {
                        deviceMode.setChecked(true);
                    } else {
                        deviceMode.setChecked(false);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void gotoLogin() {
        Intent in = new Intent(this, LoginActivity.class);
        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(in);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == waterSwitch) {
            if (deviceModeValue == 1) {
                if (switchState == 1) {
                    waterSwitch.setChecked(true);
                } else {
                    waterSwitch.setChecked(false);
                }
                alert_dialog = new MaterialDialog.Builder(this)
                        .content(R.string.automatic_mode)
                        .positiveText("Ok")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                alert_dialog.dismiss();
                            }
                        })
                        .cancelable(false)
                        .show();

            } else {
                if (isChecked) {
                    device_ref.child("switch_state").setValue(1);
                } else {
                    device_ref.child("switch_state").setValue(0);
                }
            }

        }
        if (buttonView == deviceMode) {
            if (isChecked) {
                device_ref.child("mode").setValue(1);
            } else {
                device_ref.child("mode").setValue(2);
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement


        return super.onOptionsItemSelected(item);
    }

    private void goMarket() {
        Intent in = new Intent(this, MarketActivity.class);
        startActivity(in);
    }

    private void gotoHome() {
        Intent in = new Intent(this, MainActivity.class);
        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(in);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_my_devices) {
            gotoHome();
        }
        if (id == R.id.nav_logout) {
            mAuth.signOut();
            Checkout.clearUserData(this);
            gotoLogin();
        }
        if (id == R.id.nav_market) {
            goMarket();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View v) {
        if (v == changeDeviceName) {
            deviceNameDialog = new MaterialDialog.Builder(this)
                    .title(R.string.enter_device_name)
                    .content(R.string.device_name_content)
                    .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_TEXT)
                    .alwaysCallInputCallback()
                    .input(" ", deviceNameText, new MaterialDialog.InputCallback() {

                                @Override
                                public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                    deviceNameText = input.toString();
                                }
                            }
                    )
                    .positiveText(R.string.save)
                    .negativeText(R.string.cancel)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            device_ref.child("device_name").setValue(deviceNameText);
                            deviceNameDialog.dismiss();
                        }
                    })
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            deviceNameDialog.dismiss();
                        }
                    })
                    .show();
        }
    }
}