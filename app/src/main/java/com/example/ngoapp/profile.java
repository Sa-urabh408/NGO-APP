package com.example.ngoapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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

public class profile extends AppCompatActivity {

    private TextView welcome;

    // Renamed variables to avoid clashing with class names
    private LinearLayout btnAboutUs, btnGallery, btnDonationBox, btnContactUs, btnVolunteer, btnEvent;
    private LinearLayout navHome, navDonation, navUser, navDev, navLogout;
    private ImageView pp;
    private ViewFlipper viewFlipper;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home1);

        bindViews();
        initializeFirebase();
        fetchUsername();
        animateLayout();
        setupClickListeners();
        setupFooterNavigation("profile");

        if (viewFlipper != null) {
            viewFlipper.startFlipping();
        }
    }

    private void bindViews() {
        welcome      = findViewById(R.id.welcome);
        navHome      = findViewById(R.id.nav_home);
        navDonation  = findViewById(R.id.nav_donation);
        navUser      = findViewById(R.id.nav_user);
        navDev       = findViewById(R.id.nav_dev);
        navLogout    = findViewById(R.id.nav_logout);
        pp           = findViewById(R.id.pp);

        // Correct IDs from activity_home1.xml
        btnAboutUs     = findViewById(R.id.about_us);
        btnGallery     = findViewById(R.id.gallery);
        btnDonationBox = findViewById(R.id.donation_box);
        btnContactUs   = findViewById(R.id.contact_us);
        btnVolunteer   = findViewById(R.id.volunteer);
        btnEvent       = findViewById(R.id.event);
        viewFlipper    = findViewById(R.id.home_slider);
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
    }

    private void fetchUsername() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            databaseReference.child(userId).child("name")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String name = snapshot.getValue(String.class);
                            welcome.setText(name != null && !name.isEmpty()
                                    ? "Welcome " + name + "!"
                                    : "Welcome, Guest!");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(profile.this, "Failed to load user name", Toast.LENGTH_SHORT).show();
                            Log.e("FirebaseError", error.getMessage());
                        }
                    });
        } else {
            welcome.setText("Welcome, Guest!");
        }
    }

    private void animateLayout() {
        try {
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.fadein_anim);
            if (btnAboutUs   != null) btnAboutUs.startAnimation(animation);
            if (btnEvent     != null) btnEvent.startAnimation(animation);
            if (btnGallery   != null) btnGallery.startAnimation(animation);
            if (btnVolunteer != null) btnVolunteer.startAnimation(animation);
            if (btnContactUs != null) btnContactUs.startAnimation(animation);
        } catch (Exception e) {
            Log.e("AnimationError", e.getMessage());
        }
    }

    private void setupClickListeners() {
        // Footer nav
        if (navDonation != null)
            navDonation.setOnClickListener(v -> startActivity(new Intent(profile.this, donation.class)));
        if (navDev != null)
            navDev.setOnClickListener(v -> startActivity(new Intent(profile.this, devloper.class)));
        if (navUser != null)
            navUser.setOnClickListener(v -> startActivity(new Intent(profile.this, user.class)));
        if (navLogout != null)
            navLogout.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(profile.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(profile.this, loginpage.class));
                finish();
            });

        // Profile pic
        if (pp != null)
            pp.setOnClickListener(v -> startActivity(new Intent(profile.this, profile1.class)));

        // Grid cards — use class names directly (no variable name clash now)
        if (btnAboutUs     != null)
            btnAboutUs.setOnClickListener(v -> startActivity(new Intent(profile.this, Aboutus.class)));
        if (btnContactUs   != null)
            btnContactUs.setOnClickListener(v -> startActivity(new Intent(profile.this, contactus.class)));
        if (btnDonationBox != null)
            btnDonationBox.setOnClickListener(v -> startActivity(new Intent(profile.this, donationbox.class)));
        if (btnVolunteer   != null)
            btnVolunteer.setOnClickListener(v -> startActivity(new Intent(profile.this, Volentear.class)));
        if (btnGallery     != null)
            btnGallery.setOnClickListener(v -> startActivity(new Intent(profile.this, gallery.class)));
        if (btnEvent       != null)
            btnEvent.setOnClickListener(v -> startActivity(new Intent(profile.this, Event.class)));
    }

    private void setupFooterNavigation(String selected) {
        if (navHome     != null) navHome.setBackgroundResource(R.drawable.nav_unselected);
        if (navDonation != null) navDonation.setBackgroundResource(R.drawable.nav_unselected);
        if (navUser     != null) navUser.setBackgroundResource(R.drawable.nav_unselected);
        if (navDev      != null) navDev.setBackgroundResource(R.drawable.nav_unselected);
        if (navLogout   != null) navLogout.setBackgroundResource(R.drawable.nav_unselected);

        // Home is the active screen here (profile.java inflates activity_home1)
        if (navHome != null) {
            navHome.setBackgroundResource(R.drawable.nav_selected_box);
            ImageView homeIcon = navHome.findViewById(R.id.footer_home_icon);
            TextView homeText  = navHome.findViewById(R.id.footer_home_text);
            if (homeIcon != null) homeIcon.setColorFilter(0xFFFFFFFF, android.graphics.PorterDuff.Mode.SRC_IN);
            if (homeText != null) homeText.setTextColor(0xFFFFFFFF);
        }
    }
}
