package com.garden.garden;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

public class CheckoutActivity extends AppCompatActivity implements PaymentResultListener, View.OnClickListener {
    private Button buy_now;
    private TextView product_plant_name;
    private TextView product_scientific_name;
    private ImageView product_image;
    private TextView product_description;
    private static final String TAG = "CheckoutActivity";
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference mDatabase;
    private String plant_name;
    private String scientific_name;
    private String description;
    private String url;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        Checkout.preload(getApplicationContext());
        mAuth = FirebaseAuth.getInstance();
        buy_now = findViewById(R.id.buy_plant_button);
        product_plant_name = findViewById(R.id.product_plant_name);
        product_scientific_name = findViewById(R.id.product_scientific_name);
        product_image = findViewById(R.id.product_plant_image);
        product_description = findViewById(R.id.product_description);

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
            DatabaseReference plant_ref = FirebaseDatabase.getInstance().getReference().child("plants").child(plant_name);
            plant_ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    scientific_name = dataSnapshot.child("scientific_name").getValue().toString();
                    description = dataSnapshot.child("description").getValue().toString();
                    url = dataSnapshot.child("image_url").getValue().toString();
                    Picasso.get().load(url).placeholder(R.drawable.spinningwheel).into(product_image);
                    product_plant_name.setText(plant_name);
                    product_scientific_name.setText(scientific_name);
                    product_description.setText(description);
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
    public void onPaymentSuccess(String s) {
        Intent in = new Intent(this,MainActivity.class);
        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(in);
    }

    @Override
    public void onPaymentError(int i, String s) {
        Toast.makeText(this,"There was error in making payment",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v) {
        if(v == buy_now){
            Checkout checkout = new Checkout();
            checkout.setImage(R.drawable.jasmin);
            final Activity activity = this;
            try {
                JSONObject options = new JSONObject();
                options.put("name", "Garden");
                options.put("description", "Order #123456");
                options.put("currency", "INR");
                options.put("amount", "100");
                checkout.open(activity, options);
            } catch(Exception e) {
                Log.e(TAG, "Error in starting Razorpay Checkout", e);
                Toast.makeText(this,"Error in starting Razorpay Checkout",Toast.LENGTH_LONG).show();
            }
        }
    }
}
