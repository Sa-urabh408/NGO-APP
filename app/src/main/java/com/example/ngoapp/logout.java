package com.example.ngoapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class logout extends AppCompatActivity {

    Button logoutButton, cancelButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logout);

        logoutButton = findViewById(R.id.logout);
        cancelButton = findViewById(R.id.cancel_button);

        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(getApplicationContext(), "Logged out successfully!", Toast.LENGTH_SHORT).show();

            // Send user back to login screen
            Intent intent = new Intent(logout.this, loginpage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
            startActivity(intent);
            finish();
        });

        cancelButton.setOnClickListener(v -> {
            finish(); // Go back to previous screen
        });
    }
}
