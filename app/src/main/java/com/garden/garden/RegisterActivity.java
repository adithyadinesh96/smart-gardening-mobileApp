package com.garden.garden;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private Button registerButton;
    private EditText registerFullName;
    private EditText registerEmail;
    private EditText registerPassword;
    private EditText registerPasswordAgain;
    private TextView alreadyRegistered;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        registerButton = findViewById(R.id.register_button);
        registerFullName = findViewById(R.id.register_full_name);
        registerEmail = findViewById(R.id.register_email_address);
        registerPassword = findViewById(R.id.register_password);
        registerPasswordAgain = findViewById(R.id.register_password_again);
        alreadyRegistered = findViewById(R.id.already_registered);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        registerButton.setOnClickListener(this);
        alreadyRegistered.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            //TOdo Finish Current Activity
            Intent in = new Intent(this, MainActivity.class);
            startActivity(in);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == alreadyRegistered) {
            startActivity(new Intent(this, LoginActivity.class));
        } else if (v == registerButton) {
            InputMethodManager inputManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
            registerUser();
        }
    }

    private void registerUser() {
        String email_address = registerEmail.getText().toString().trim();
        String password = registerPassword.getText().toString().trim();
        String password_again = registerPasswordAgain.getText().toString().trim();
        final String full_name = registerFullName.getText().toString().trim();
        if (TextUtils.isEmpty(email_address) || TextUtils.isEmpty(password) || TextUtils.isEmpty(password_again)) {
            Toast.makeText(this, "Please enter all the fields", Toast.LENGTH_SHORT).show();
            return;
        } else if (!password.equals(password_again)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }
        else{
            final MaterialDialog signup_dialog = new MaterialDialog.Builder(this)
                    .content(R.string.signup_please_wait)
                    .progress(true, 0)
                    .cancelable(false)
                    .show();
            mAuth.createUserWithEmailAndPassword(email_address,password).addOnCompleteListener(
                    this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                String uid = mAuth.getCurrentUser().getUid();
                                String email = mAuth.getCurrentUser().getEmail();
                                User user = new User(full_name, email,0);
                                mDatabase.child("users").child(uid).setValue(user);
                                currentUser = mAuth.getCurrentUser();
                                currentUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            Toast.makeText(RegisterActivity.this,"Successfully Regsitered to Garden.",Toast.LENGTH_SHORT).show();
                                            Toast.makeText(RegisterActivity.this,"Please verify your email address.",Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                        }
                                        else {
                                            signup_dialog.dismiss();
                                            Toast.makeText(RegisterActivity.this,"Problem in sending mail to your email-address.",Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                            else {
                                signup_dialog.dismiss();
                                try {
                                    throw task.getException();
                                    } catch(FirebaseAuthWeakPasswordException e) {
                                    registerPassword.setError(getString(R.string.error_weak_password));
                                    registerPassword.requestFocus();
                                    } catch(FirebaseAuthUserCollisionException e) {
                                    registerEmail.setError(getString(R.string.error_user_exists));
                                    registerEmail.requestFocus();
                                    } catch(Exception e) {

                                    }
                            }
                        }
                    }
            );
        }
    }
}

