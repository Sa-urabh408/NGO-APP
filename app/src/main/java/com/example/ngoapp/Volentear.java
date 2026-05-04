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

public class Volentear extends AppCompatActivity {

    EditText vfname, vlname, vmobile, vmessage;
    Button vbutton;
    TextView welcomeText;

    FirebaseAuth mAuth;
    DatabaseReference databaseReference;

    // Footer nav
    private LinearLayout navHome, navDonation, navUser, navDev, navLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volentear);

        // Bind views
        vfname = findViewById(R.id.vfname);
        vlname = findViewById(R.id.vlname);
        vmobile = findViewById(R.id.vmobile);
        vmessage = findViewById(R.id.vmessage);
        vbutton = findViewById(R.id.vbutton);
        welcomeText = findViewById(R.id.welcome);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Volunteers");

        // Load user name into welcome text
        loadUserName();

        // Animate the form card sliding in
        View formCard = findViewById(R.id.formCard);
        if (formCard != null) {
            Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
            formCard.startAnimation(slideUp);
        }

        // Submit button
        vbutton.setOnClickListener(v -> submitVolunteerData());

        // Footer nav
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
                                welcomeText.setText("Welcome, Volunteer! 👋");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            welcomeText.setText("Welcome, Volunteer! 👋");
                        }
                    });
        } else {
            welcomeText.setText("Welcome, Guest! 👋");
        }
    }

    private void submitVolunteerData() {
        String firstName = vfname.getText().toString().trim();
        String lastName = vlname.getText().toString().trim();
        String mobile = vmobile.getText().toString().trim();
        String message = vmessage.getText().toString().trim();

        // Validate
        if (TextUtils.isEmpty(firstName)) {
            vfname.setError("First name required");
            vfname.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(mobile)) {
            vmobile.setError("Mobile number required");
            vmobile.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(message)) {
            vmessage.setError("Please share your motivation");
            vmessage.requestFocus();
            return;
        }

        // Save to Firebase
        String id = databaseReference.push().getKey();
        HashMap<String, String> volunteerMap = new HashMap<>();
        volunteerMap.put("firstName", firstName);
        volunteerMap.put("lastName", lastName);
        volunteerMap.put("mobile", mobile);
        volunteerMap.put("message", message);

        if (id != null) {
            databaseReference.child(id).setValue(volunteerMap)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "✅ Application submitted! We'll contact you soon.", Toast.LENGTH_LONG)
                                .show();
                        vfname.setText("");
                        vlname.setText("");
                        vmobile.setText("");
                        vmessage.setText("");
                    })
                    .addOnFailureListener(
                            e -> Toast.makeText(this, "❌ Failed to submit. Try again.", Toast.LENGTH_SHORT).show());
        }
    }

    private void setupFooterNavigation() {
        navHome = findViewById(R.id.nav_home);
        navDonation = findViewById(R.id.nav_donation);
        navUser = findViewById(R.id.nav_user);
        navDev = findViewById(R.id.nav_dev);
        navLogout = findViewById(R.id.nav_logout);

        // Reset all
        navHome.setBackgroundResource(R.drawable.nav_unselected);
        navDonation.setBackgroundResource(R.drawable.nav_unselected);
        navUser.setBackgroundResource(R.drawable.nav_unselected);
        navDev.setBackgroundResource(R.drawable.nav_unselected);
        navLogout.setBackgroundResource(R.drawable.nav_unselected);

        // Volunteer is under Home in main content
        navHome.setBackgroundResource(R.drawable.nav_selected_box);

        navHome.setOnClickListener(v -> {
            startActivity(new Intent(Volentear.this, Home1.class));
            finish();
        });

        navDonation.setOnClickListener(v -> {
            startActivity(new Intent(Volentear.this, donation.class));
            finish();
        });

        navUser.setOnClickListener(v -> {
            startActivity(new Intent(Volentear.this, user.class));
            finish();
        });

        navDev.setOnClickListener(v -> {
            startActivity(new Intent(Volentear.this, devloper.class));
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
                    Intent intent = new Intent(Volentear.this, loginpage.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
