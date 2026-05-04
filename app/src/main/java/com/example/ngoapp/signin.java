package com.example.ngoapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class signin extends AppCompatActivity {

    EditText name, mobile, email, password;
    Button registerbtn;
    TextView backtologin;

    private FirebaseAuth mauth;
    private DatabaseReference databaseReference;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        // Bind Views
        name = findViewById(R.id.username);
        email = findViewById(R.id.useremail);
        mobile = findViewById(R.id.usermobile);
        password = findViewById(R.id.userpassword);
        registerbtn = findViewById(R.id.Registerbtn);
        backtologin = findViewById(R.id.backtologin);

        // Firebase setup
        mauth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Button click: Register
        registerbtn.setOnClickListener(v -> registerUser());

        // Button click: Back to login
        backtologin.setOnClickListener(v -> {
            Intent intent = new Intent(signin.this, loginpage.class);
            startActivity(intent);
            finish();
        });
    }

    private void registerUser() {
        String rName = name.getText().toString().trim();
        String rEmail = email.getText().toString().trim();
        String rMobile = mobile.getText().toString().trim();
        String rPassword = password.getText().toString().trim();

        // Input validation
        if (TextUtils.isEmpty(rName)) {
            name.setError("Name is required");
            return;
        }
        if (TextUtils.isEmpty(rEmail)) {
            email.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(rMobile)) {
            mobile.setError("Mobile number is required");
            return;
        }
        if (TextUtils.isEmpty(rPassword)) {
            password.setError("Password is required");
            return;
        }
        if (rPassword.length() < 6) {
            password.setError("Password must be at least 6 characters");
            return;
        }

        // Firebase Auth registration
        mauth.createUserWithEmailAndPassword(rEmail, rPassword).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mauth.getCurrentUser();
                if (user != null) {
                    String userId = user.getUid();

                    // Create user data map
                    HashMap<String, String> userData = new HashMap<>();
                    userData.put("name", rName);
                    userData.put("email", rEmail);
                    userData.put("mobile", rMobile);

                    // Try to store in Realtime Database, but redirect regardless
                    databaseReference.child(userId).setValue(userData)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(signin.this, "✅ Registered Successfully!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                // DB write failed but user is still registered — show warning only
                                Toast.makeText(signin.this, "⚠️ Profile save failed, but account created!", Toast.LENGTH_LONG).show();
                            });

                    // Always redirect to Home1 after successful auth registration
                    Intent intent = new Intent(signin.this, Home1.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            } else {
                String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                Toast.makeText(signin.this, "Registration Failed: " + errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }
}
