package com.example.whatsapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.whatsapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.Objects;

public class Login extends AppCompatActivity {
    Toolbar toolbar;
    MaterialEditText email, password;
    Button login;
    FirebaseAuth auth;
    TextView forget_password;
    //ProgressDialog to display while registering user
    ProgressDialog progressDialog;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        //Tool bar
        toolbar = findViewById(R.id.toolbar);
        //Note should be imported from androidx.appcompat.widget NOT androidx.widget
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Init views
        email = findViewById(R.id.email_edittxt);
        password = findViewById(R.id.password_edittxt);
        login = findViewById(R.id.login_btn);
        forget_password = findViewById(R.id.forget_password);
        progressDialog = new ProgressDialog(this);

        //Handle login button clicks
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mEmail = Objects.requireNonNull(email.getText()).toString().trim();
                String mPassword = Objects.requireNonNull(password.getText()).toString();
                if (TextUtils.isEmpty(mEmail) || TextUtils.isEmpty(mPassword)) {
                    Toast.makeText(getApplicationContext(), "All Fields Are Required!", Toast.LENGTH_SHORT).show();

                } else {
                    login(mEmail, mPassword);

                }
            }
        });

        //Handle forget password textview clicks
        forget_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), ResetPassword.class));
            }
        });

        //init Authentication
        auth = FirebaseAuth.getInstance();

    }

    private void login(final String email, String password) {
        progressDialog.setTitle("Signing in...");
        progressDialog.show();
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            Intent intent = new Intent(Login.this, Home.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();

                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Authentication Error!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


}
