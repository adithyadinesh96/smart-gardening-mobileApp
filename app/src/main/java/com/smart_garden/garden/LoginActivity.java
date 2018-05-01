package com.smart_garden.garden;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.auth.SignInMethodQueryResult;

import java.util.List;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private EditText loginEmailAddress;
    private EditText loginPassword;
    private Button loginButton;
    private TextView forgotPassword;
    private TextView notRegistered;
    private MaterialDialog forgotPasswordDialog;
    private String resetEmail;
    private MaterialDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.withActionAppTheme);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        loginEmailAddress = findViewById(R.id.login_email_address);
        loginPassword = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        forgotPassword = findViewById(R.id.forgot_password);
        forgotPassword.setOnClickListener(this);
        notRegistered = findViewById(R.id.not_registered);
        notRegistered.setOnClickListener(this);
        loginButton.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            //Todo Finish Current Activity
            goToHome();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == notRegistered) {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        } else if (v == loginButton) {
            InputMethodManager inputManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
            userLogin();
        } else if (v == forgotPassword) {
            forgotPasswordDialog = new MaterialDialog.Builder(this)
                    .title(R.string.reset_password)
                    .content(R.string.email_address_hint)
                    .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_TEXT)
                    .alwaysCallInputCallback()
                    .input(" ", " ", new MaterialDialog.InputCallback() {

                                @Override
                                public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                    resetEmail = input.toString();
                                }
                            }
                    )
                    .positiveText(R.string.reset)
                    .negativeText(R.string.cancel)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            mAuth.fetchSignInMethodsForEmail(resetEmail.trim()).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                                @Override
                                public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                                    if (task.getResult().getSignInMethods().size() == 1) {
                                        mAuth.sendPasswordResetEmail(resetEmail.trim()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                forgotPasswordDialog.dismiss();
                                                alertDialog = new MaterialDialog.Builder(LoginActivity.this)
                                                        .content(R.string.password_reset_sent)
                                                        .positiveText("Ok")
                                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                            @Override
                                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                                alertDialog.dismiss();
                                                            }
                                                        })
                                                        .cancelable(false)
                                                        .show();
                                            }
                                        });
                                    } else {
                                        forgotPasswordDialog.dismiss();
                                        alertDialog = new MaterialDialog.Builder(LoginActivity.this)
                                                .content(R.string.user_not_registered)
                                                .positiveText("Ok")
                                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                    @Override
                                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                        alertDialog.dismiss();
                                                    }
                                                })
                                                .cancelable(false)
                                                .show();
                                    }
                                }
                            });
                        }
                    })
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            forgotPasswordDialog.dismiss();
                        }
                    })
                    .show();
        }
    }

    private void userLogin() {
        final MaterialDialog signin_dialog = new MaterialDialog.Builder(this)
                .content(R.string.please_wait)
                .progress(true, 0)
                .cancelable(false)
                .show();

        String email = loginEmailAddress.getText().toString().trim();
        String password = loginPassword.getText().toString().trim();
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            signin_dialog.dismiss();
            Toast.makeText(this, "Please enter all the fields", Toast.LENGTH_SHORT).show();
            return;
        } else {
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        goToHome();
                    } else {
                        signin_dialog.dismiss();
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthInvalidUserException e) {
                            loginEmailAddress.setError(getString(R.string.account_does_not_exist));
                            loginEmailAddress.requestFocus();
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            loginPassword.setError(getString(R.string.password_incorrect));
                            loginPassword.requestFocus();
                        } catch (Exception e) {

                        }

                    }
                }
            });
        }
    }

    private void goToHome() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
