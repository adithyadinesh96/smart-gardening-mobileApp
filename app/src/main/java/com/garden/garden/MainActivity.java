package com.garden.garden;

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
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.os.SystemClock.sleep;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private Button logout;
    private DatabaseReference mDatabase;
    private String uid;
    private int isActive;
    private ConstraintLayout mcontraint;
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        logout = findViewById(R.id.logout);
        logout.setOnClickListener(this);
        mcontraint = findViewById(R.id.main_constraint);

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
            if(!currentUser.isEmailVerified()){
                MaterialDialog dialog = new MaterialDialog.Builder(this)
                        .title(R.string.email_not_verfied)
                        .content(R.string.not_verfied_text)
                        .positiveText(R.string.refresh)
                        .autoDismiss(false)
                        .cancelable(false)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
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
                        .show();
            }
            else {
                user_ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(!dataSnapshot.hasChild("devices")){
                            Snackbar.make(mcontraint,"No devices found",Snackbar.LENGTH_INDEFINITE).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG,"Database error" + databaseError.getMessage());
                    }
                });
            }
        }
    }
    @Override
    public void onClick(View v) {
        if(v == logout){
            mAuth.signOut();
            gotoLogin();
        }
    }
    private void gotoLogin(){
        Intent in = new Intent(this, LoginActivity.class);
        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(in);
    }
}
