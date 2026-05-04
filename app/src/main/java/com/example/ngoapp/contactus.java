package com.example.ngoapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class contactus extends AppCompatActivity {

    EditText etname, etmobile, etmessage;
    Button btnsubmit;
    TextView welcomeText;

    DatabaseReference databaseReference;
    FirebaseAuth mAuth;

    // Footer nav
    private LinearLayout navHome, navDonation, navUser, navDev, navLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contactus);

        etname = findViewById(R.id.etname);
        etmobile = findViewById(R.id.etmobile);
        etmessage = findViewById(R.id.etmessage);
        btnsubmit = findViewById(R.id.btnsubmit);
        welcomeText = findViewById(R.id.welcome);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Messages");

        // Load Firebase user name
        loadUserName();

        // Animate the query form card
        View queryCard = findViewById(R.id.queryCard);
        if (queryCard != null) {
            Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
            slideUp.setStartOffset(200);
            queryCard.startAnimation(slideUp);
        }

        btnsubmit.setOnClickListener(v -> contactData());

        setupFooterNavigation();
    }

    private void loadUserName() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(currentUser.getUid())
                    .child("name")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String name = snapshot.getValue(String.class);
                            if (name != null && !name.isEmpty()) {
                                welcomeText.setText("Welcome, " + name + "! 👋");
                            } else {
                                welcomeText.setText("Welcome! 👋");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            welcomeText.setText("Welcome! 👋");
                        }
                    });
        } else {
            welcomeText.setText("Welcome, Guest! 👋");
        }
    }

    private void contactData() {
        String name = etname.getText().toString().trim();
        String mobile = etmobile.getText().toString().trim();
        String message = etmessage.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etname.setError("Name is required");
            etname.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(mobile)) {
            etmobile.setError("Mobile number is required");
            etmobile.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(message)) {
            etmessage.setError("Please write your message");
            etmessage.requestFocus();
            return;
        }

        String id = databaseReference.push().getKey();
        HashMap<String, String> contactMap = new HashMap<>();
        contactMap.put("Name", name);
        contactMap.put("Mobile", mobile);
        contactMap.put("message", message);

        if (id != null) {
            databaseReference.child(id).setValue(contactMap)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getApplicationContext(), "✅ Message sent! We'll reach out soon.",
                                Toast.LENGTH_LONG).show();
                        etname.setText("");
                        etmobile.setText("");
                        etmessage.setText("");
                    })
                    .addOnFailureListener(
                            e -> Toast.makeText(this, "❌ Failed to send. Try again.", Toast.LENGTH_SHORT).show());
        }
    }

    private void setupFooterNavigation() {
        navHome = findViewById(R.id.nav_home);
        navDonation = findViewById(R.id.nav_donation);
        navUser = findViewById(R.id.nav_user);
        navDev = findViewById(R.id.nav_dev);
        navLogout = findViewById(R.id.nav_logout);

        navHome.setBackgroundResource(R.drawable.nav_unselected);
        navDonation.setBackgroundResource(R.drawable.nav_unselected);
        navUser.setBackgroundResource(R.drawable.nav_unselected);
        navDev.setBackgroundResource(R.drawable.nav_unselected);
        navLogout.setBackgroundResource(R.drawable.nav_unselected);

        // Contact Us is under Home
        navHome.setBackgroundResource(R.drawable.nav_selected_box);

        navHome.setOnClickListener(v -> {
            startActivity(new Intent(contactus.this, Home1.class));
            finish();
        });

        navDonation.setOnClickListener(v -> {
            startActivity(new Intent(contactus.this, donation.class));
            finish();
        });

        navUser.setOnClickListener(v -> {
            startActivity(new Intent(contactus.this, user.class));
            finish();
        });

        navDev.setOnClickListener(v -> {
            startActivity(new Intent(contactus.this, devloper.class));
            finish();
        });

        navLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(contactus.this, loginpage.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
}