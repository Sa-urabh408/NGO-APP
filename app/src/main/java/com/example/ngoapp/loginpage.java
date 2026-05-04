package com.example.ngoapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class loginpage extends AppCompatActivity {

    EditText email,password;
    Button login,loginwithguest;
    TextView redirectToRegister;

    FirebaseAuth mauth;

    @Override
    protected void onStart() {
        super.onStart();
        // If user is already signed in, skip login screen
        FirebaseUser currentUser = mauth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            if (email != null && email.equalsIgnoreCase("Admin123@gmail.com")) {
                startActivity(new Intent(loginpage.this, adminpanel.class));
            } else {
                startActivity(new Intent(loginpage.this, Home1.class));
            }
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loginpage);

        email = findViewById(R.id.usermail);
        password = findViewById(R.id.userpass);
        login = findViewById(R.id.loginbtn);
        loginwithguest = findViewById(R.id.guestbtn);
        redirectToRegister = findViewById(R.id.signin);

        mauth = FirebaseAuth.getInstance();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String useremail = email.getText().toString().trim();
                String userpass = password.getText().toString().trim();

                if (useremail.isEmpty() || userpass.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please enter email and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Sign in via Firebase Auth for ALL users (including admin)
                mauth.signInWithEmailAndPassword(useremail, userpass)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Check if this is the admin account
                                if (useremail.equalsIgnoreCase("Admin123@gmail.com")) {
                                    Toast.makeText(getApplicationContext(), "Admin login successful", Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(loginpage.this, adminpanel.class));
                                } else {
                                    Toast.makeText(getApplicationContext(), "Login successful", Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(loginpage.this, Home1.class));
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });



        redirectToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(loginpage.this, signin.class);
                startActivity(intent);
            }
        });

        loginwithguest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent aa = new Intent(loginpage.this, Home1.class);
                startActivity(aa);
            }
        });

    }
}