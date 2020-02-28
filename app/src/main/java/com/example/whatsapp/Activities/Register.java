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
import android.widget.Toast;

import com.example.whatsapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Objects;

public class Register extends AppCompatActivity {
    Toolbar toolbar;
    MaterialEditText username, email, password;
    Button register;
    FirebaseAuth auth;
    DatabaseReference reference;
    //ProgressDialog to display while registering user
    ProgressDialog progressDialog;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_layout);

        //Tool bar
        toolbar = findViewById(R.id.toolbar);
        //Note should be imported from androidx.appcompat.widget NOT androidx.widget
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Init views
        username = findViewById(R.id.username_edittxt);
        email = findViewById(R.id.email_edittxt);
        password = findViewById(R.id.password_edittxt);
        register = findViewById(R.id.register_btn);
        progressDialog = new ProgressDialog(this);


        //Handle register button clicks
        register.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
                String mUsername = Objects.requireNonNull(username.getText()).toString().trim();
                String mEmail = Objects.requireNonNull(email.getText()).toString().trim();
                String mPassword = Objects.requireNonNull(password.getText()).toString();
                if (TextUtils.isEmpty(mUsername) || TextUtils.isEmpty(mEmail) || TextUtils.isEmpty(mPassword)) {
                    Toast.makeText(getApplicationContext(), "All Fields Are Required!", Toast.LENGTH_SHORT).show();
                } else if (mPassword.length() < 6) {
                    Toast.makeText(getApplicationContext(), "Password Must Be At Least Six Characters!", Toast.LENGTH_SHORT).show();

                } else {
                    register(mUsername, mEmail, mPassword);

                }
            }
        });

        //init Authentication
        auth = FirebaseAuth.getInstance();

    }

    private void register(final String username, String email, String password) {
        progressDialog.setTitle("Registering User...");
        progressDialog.show();
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            //To get user id
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            assert firebaseUser != null;
                            String userID = firebaseUser.getUid();

                            //Write to database
                            reference = FirebaseDatabase.getInstance().getReference("Users").child(userID);
                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("id", userID);
                            hashMap.put("username", username);
                            hashMap.put("imageURL", "default");
                            hashMap.put("status", "Offline");
                            hashMap.put("search", username.toLowerCase());


                            reference.setValue(hashMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Intent intent = new Intent(Register.this, Home.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }
                                    });

                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "You Cant't Register With This Email or Password!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
