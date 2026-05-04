package com.example.ngoapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class devloper extends AppCompatActivity {

    // Nav containers
    private LinearLayout navHome, navDonation, navUser, navDev, navLogout;
    private LinearLayout btnGithub, btnInstagram;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devloper);

        // Social link buttons
        btnGithub    = findViewById(R.id.btn_github);
        btnInstagram = findViewById(R.id.btn_instagram);

        // Open GitHub profile in browser
        btnGithub.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/saurabhkalamkar"));
            startActivity(intent);
        });

        // Open Instagram profile in browser / Instagram app
        btnInstagram.setOnClickListener(v -> {
            // Try opening Instagram app first, fallback to browser
            Uri instagramUri = Uri.parse("http://instagram.com/_u/saurabhkalamkar_");
            Intent appIntent  = new Intent(Intent.ACTION_VIEW, instagramUri);
            appIntent.setPackage("com.instagram.android");

            try {
                startActivity(appIntent);
            } catch (Exception e) {
                // Instagram app not installed — open browser
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.instagram.com/saurabhkalamkar_")));
            }
        });

        // Footer nav
        setupFooterNavigation();
    }

    private void setupFooterNavigation() {
        navHome     = findViewById(R.id.nav_home);
        navDonation = findViewById(R.id.nav_donation);
        navUser     = findViewById(R.id.nav_user);
        navDev      = findViewById(R.id.nav_dev);
        navLogout   = findViewById(R.id.nav_logout);

        navDev.setBackgroundResource(R.drawable.nav_selected_box);
        ImageView devIcon = navDev.findViewById(R.id.footer_developer);
        TextView devText  = navDev.findViewById(R.id.footer_dev_text);
        if (devIcon != null) devIcon.setColorFilter(0xFFFFFFFF, android.graphics.PorterDuff.Mode.SRC_IN);
        if (devText != null) devText.setTextColor(0xFFFFFFFF);

        navHome.setOnClickListener(v -> {
            startActivity(new Intent(devloper.this, Home1.class));
            finish();
        });
        navDonation.setOnClickListener(v -> {
            startActivity(new Intent(devloper.this, donation.class));
            finish();
        });
        navUser.setOnClickListener(v -> {
            startActivity(new Intent(devloper.this, user.class));
            finish();
        });
        navDev.setOnClickListener(v -> {
            // Already on Developer screen
        });
        navLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(devloper.this, loginpage.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
