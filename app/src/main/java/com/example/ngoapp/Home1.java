package com.example.ngoapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Home1 extends AppCompatActivity {

    private TextView welcome;
    private LinearLayout about_us, gallery, donation_box, contact_us, volunteer, event;
    private ViewFlipper home_slider;

    // Footer nav
    private LinearLayout navHome, navDonation, navUser, navDev, navLogout;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home1);

        // Core UI Bindings
        welcome = findViewById(R.id.welcome);
        about_us = findViewById(R.id.about_us);
        gallery = findViewById(R.id.gallery);
        donation_box = findViewById(R.id.donation_box);
        contact_us = findViewById(R.id.contact_us);
        volunteer = findViewById(R.id.volunteer);
        event = findViewById(R.id.event);

        home_slider = findViewById(R.id.home_slider);
        // Note: ViewFlipper auto-start is handled in XML

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        loadUsername();

        // Safe Click Listeners
        if (about_us != null) {
            about_us.setOnClickListener(v -> {
                startActivity(new Intent(Home1.this, Aboutus.class));
            });
        }

        if (donation_box != null) {
            donation_box.setOnClickListener(v -> {
                startActivity(new Intent(Home1.this, donationbox.class));
            });
        }

        if (gallery != null) {
            gallery.setOnClickListener(v -> {
                startActivity(new Intent(Home1.this, gallery.class));
            });
        }

        if (volunteer != null) {
            volunteer.setOnClickListener(v -> {
                startActivity(new Intent(Home1.this, Volentear.class));
            });
        }

        if (event != null) {
            event.setOnClickListener(v -> {
                startActivity(new Intent(Home1.this, Event.class));
            });
        }

        if (contact_us != null) {
            contact_us.setOnClickListener(v -> {
                startActivity(new Intent(Home1.this, contactus.class));
            });
        }

        // Setup Footer
        setupFooterNavigation();
    }

    private void setupFooterNavigation() {
        navHome = findViewById(R.id.nav_home);
        navDonation = findViewById(R.id.nav_donation);
        navUser = findViewById(R.id.nav_user);
        navDev = findViewById(R.id.nav_dev);
        navLogout = findViewById(R.id.nav_logout);

        if (navHome != null) {
            navHome.setBackgroundResource(R.drawable.nav_selected_box);
            ImageView homeIcon = navHome.findViewById(R.id.footer_home_icon);
            TextView homeText = navHome.findViewById(R.id.footer_home_text);
            if (homeIcon != null) homeIcon.setColorFilter(0xFFFFFFFF, android.graphics.PorterDuff.Mode.SRC_IN);
            if (homeText != null) homeText.setTextColor(0xFFFFFFFF);
        }

        if (navDonation != null) {
            navDonation.setOnClickListener(v -> {
                startActivity(new Intent(Home1.this, donation.class));
                finish();
            });
        }

        if (navUser != null) {
            navUser.setOnClickListener(v -> {
                startActivity(new Intent(Home1.this, user.class));
                finish();
            });
        }

        if (navDev != null) {
            navDev.setOnClickListener(v -> {
                startActivity(new Intent(Home1.this, devloper.class));
                finish();
            });
        }

        if (navLogout != null) {
            navLogout.setOnClickListener(v -> showLogoutDialog());
        }
    }

    private void loadUsername() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            if (welcome != null)
                welcome.setText("Welcome Guest");
            return;
        }

        usersRef.child(user.getUid()).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.getValue(String.class);
                if (welcome != null) {
                    if (name != null)
                        welcome.setText("Welcome, " + name + "!");
                    else
                        welcome.setText("Welcome!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void showLogoutDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(Home1.this, loginpage.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
