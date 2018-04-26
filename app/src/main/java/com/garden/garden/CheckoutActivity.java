package com.garden.garden;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.razorpay.PaymentResultListener;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

public class CheckoutActivity extends AppCompatActivity implements PaymentResultListener, View.OnClickListener {
    private Button buy_now;
    private TextView product_plant_name;
    private TextView product_scientific_name;
    private ImageView product_image;
    private TextView product_description;
    private TextView checkoutPrice;
    private static final String TAG = "CheckoutActivity";
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference mDatabase;
    private String plant_name;
    private String scientific_name;
    private String description;
    private String url;
    private String uid;
    private String total_price;
    private ConstraintLayout checkout_constraint;
    private long deviceCount = 0;
    private String deviceName;
    DatabaseReference device_ref;
    DatabaseReference user_ref;
    Date today;
    private String mPaymentId;
    private MaterialDialog loading_dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        mAuth = FirebaseAuth.getInstance();
        buy_now = findViewById(R.id.buy_plant_button);
        product_plant_name = findViewById(R.id.product_plant_name);
        product_scientific_name = findViewById(R.id.product_scientific_name);
        product_image = findViewById(R.id.product_plant_image);
        product_description = findViewById(R.id.product_description);
        checkoutPrice = findViewById(R.id.checkout_price);
        checkout_constraint = findViewById(R.id.checkout_constraint);

        buy_now.setOnClickListener(this);
        Bundle extras = getIntent().getExtras();
        plant_name = extras.getString("plant_name");
    }

    @Override
    public void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            gotoLogin();
        }
        else{
            final MaterialDialog loading_dialog = new MaterialDialog.Builder(this)
                    .content(R.string.please_wait)
                    .progress(true, 0)
                    .cancelable(false)
                    .show();
            DatabaseReference plant_ref = FirebaseDatabase.getInstance().getReference().child("plants").child(plant_name);
            plant_ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    scientific_name = dataSnapshot.child("scientific_name").getValue().toString();
                    description = dataSnapshot.child("description").getValue().toString();
                    url = dataSnapshot.child("image_url").getValue().toString();
                    total_price = dataSnapshot.child("price").getValue().toString();
                    Picasso.get().load(url).placeholder(R.drawable.loading).into(product_image);
                    product_plant_name.setText(plant_name);
                    product_scientific_name.setText(scientific_name);
                    product_description.setText(description);
                    checkoutPrice.setText("Total Price \u20B9 "+ total_price);
                    loading_dialog.dismiss();
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
    public void onPaymentSuccess(String payment_id) {
        uid = currentUser.getUid();
        mPaymentId = payment_id;
        today = Calendar.getInstance().getTime();
        user_ref = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
        device_ref = FirebaseDatabase.getInstance().getReference().child("devices");
        user_ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    deviceCount = Integer.parseInt(dataSnapshot.child("device_count").getValue().toString());
                    deviceCount++;
                    String did;
                    if(deviceCount < 10) {
                         did = "d0" + deviceCount + uid;
                    }
                    else {
                         did = "d" + deviceCount + uid;
                    }
                    user_ref.child("device_count").setValue(deviceCount);
                    device_ref.child(did).child("device_name").setValue("Device "+ deviceCount);
                    device_ref.child(did).child("uid").setValue(uid);
                    device_ref.child(did).child("temperature").setValue(25);
                    device_ref.child(did).child("transaction_id").setValue(mPaymentId);
                    device_ref.child(did).child("payment_date").setValue(today);
                    device_ref.child(did).child("plant_name").setValue(plant_name);
                    device_ref.child(did).child("is_installed").setValue(0);
                    device_ref.child(did).child("switch_state").setValue(0);
                    device_ref.child(did).child("image_url").setValue(url);
                    gotoHome();
                }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void gotoHome() {
        Intent in = new Intent(this, MainActivity.class);
        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(in);
    }

    @Override
    public void onPaymentError(int i, String s) {
        loading_dialog.dismiss();
        Toast.makeText(this,"There was error in making payment",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v) {
        if(v == buy_now){
            checkout_constraint.setVisibility(View.INVISIBLE);
             loading_dialog = new MaterialDialog.Builder(this)
                    .content(R.string.please_wait)
                    .progress(true, 0)
                    .cancelable(false)
                    .show();
            Integer r_price = (Integer.parseInt(total_price)*100);
            Checkout checkout = new Checkout();
            checkout.setFullScreenDisable(true);
            checkout.setImage(R.drawable.app_icon);
            final Activity activity = this;
            try {
                JSONObject options = new JSONObject();
                options.put("name", "Garden");
                options.put("description", "Order #123456");
                options.put("currency", "INR");
                options.put("amount",r_price);
                checkout.open(activity, options);
            } catch(Exception e) {
                Log.e(TAG, "Error in starting Razorpay Checkout", e);
                Toast.makeText(this,"Error in starting Razorpay Checkout",Toast.LENGTH_LONG).show();
            }
        }
    }
}
