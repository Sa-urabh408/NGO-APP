package com.example.ngoapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class Aboutus extends AppCompatActivity {

    private LinearLayout navHome, navDonation, navUser, navDev, navLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aboutus);

        setupFooterNavigation();
        animateCards();
    }
    private void animateCards() {

        int[] cardIds = {
                R.id.cardWho,
                R.id.cardVision,
                R.id.cardMission,
                R.id.cardValues,
                R.id.cardJoin
        };

        int delay = 0;

        for (int id : cardIds) {

            android.view.View card = findViewById(id);

            if (card != null) {
                try {
                    Animation anim = AnimationUtils.loadAnimation(this, R.anim.slide_up);
                    anim.setStartOffset(delay);
                    card.startAnimation(anim);
                    delay += 150;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setupFooterNavigation() {

        navHome = findViewById(R.id.nav_home);
        navDonation = findViewById(R.id.nav_donation);
        navUser = findViewById(R.id.nav_user);
        navDev = findViewById(R.id.nav_dev);
        navLogout = findViewById(R.id.nav_logout);

        // Agar footer IDs layout me nahi mile to crash mat hone do
        if (navHome == null || navDonation == null || navUser == null
                || navDev == null || navLogout == null) {
            return;
        }

        navHome.setBackgroundResource(R.drawable.nav_unselected);
        navDonation.setBackgroundResource(R.drawable.nav_unselected);
        navUser.setBackgroundResource(R.drawable.nav_unselected);
        navDev.setBackgroundResource(R.drawable.nav_unselected);
        navLogout.setBackgroundResource(R.drawable.nav_unselected);

        navHome.setBackgroundResource(R.drawable.nav_selected_box);

        navHome.setOnClickListener(v -> {
            startActivity(new Intent(Aboutus.this, Home1.class));
            finish();
        });

        navDonation.setOnClickListener(v -> {
            startActivity(new Intent(Aboutus.this, donation.class));
            finish();
        });

        navUser.setOnClickListener(v -> {
            startActivity(new Intent(Aboutus.this, user.class));
            finish();
        });

        navDev.setOnClickListener(v -> {
            startActivity(new Intent(Aboutus.this, devloper.class));
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
                    Intent intent = new Intent(Aboutus.this, loginpage.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
}