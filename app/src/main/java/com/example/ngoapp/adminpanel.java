package com.example.ngoapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import androidx.cardview.widget.CardView;

public class adminpanel extends AppCompatActivity {

    CardView allusers, adminDonation, adminMessages, adminGallery, adminEvent, adminVolunteers, adminLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adminpanel);

        allusers      = findViewById(R.id.alluser);
        adminDonation = findViewById(R.id.admindonation);
        adminMessages = findViewById(R.id.adminMessages);
        adminGallery  = findViewById(R.id.adminGallery);
        adminEvent    = findViewById(R.id.adminEvent);
        adminVolunteers = findViewById(R.id.adminVolunteers);
        adminLogout   = findViewById(R.id.adminLogout);

        allusers.setOnClickListener(v ->
                startActivity(new Intent(adminpanel.this, Userlist.class)));

        adminDonation.setOnClickListener(v ->
                startActivity(new Intent(adminpanel.this, AdminDonationList.class)));

        adminMessages.setOnClickListener(v ->
                startActivity(new Intent(adminpanel.this, AdminMessages.class)));

        adminGallery.setOnClickListener(v ->
                startActivity(new Intent(adminpanel.this, AdminGalleryUpload.class)));

        adminEvent.setOnClickListener(v ->
                startActivity(new Intent(adminpanel.this, AdminPostEvent.class)));

        adminVolunteers.setOnClickListener(v ->
                startActivity(new Intent(adminpanel.this, AdminVolunteers.class)));

        adminLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Logout Admin")
                .setMessage("Bhai, kya aap sach me Admin session logout karna chahte hain?")
                .setPositiveButton("Yes, Logout", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(adminpanel.this, loginpage.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No, Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
}