package com.example.ngoapp;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class donationhistory extends AppCompatActivity {

    private LinearLayout historycontainer;
    private TextView totalAmounttext, welcomeText, emptyText;
    private DatabaseReference donationRef, userRef;
    private int totalDonated = 0;

    // Footer nav
    private LinearLayout navHome, navDonation, navUser, navDev, navLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donationhistory);

        historycontainer = findViewById(R.id.historycontainer);
        totalAmounttext = findViewById(R.id.totalAmounttext);
        welcomeText = findViewById(R.id.welcome);
        emptyText = findViewById(R.id.emptyText);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userid = user.getUid();
            donationRef = FirebaseDatabase.getInstance().getReference("Donations").child(userid);
            userRef = FirebaseDatabase.getInstance().getReference("Users").child(userid);

            loadUserName();
            fetchDonationHistory();
        }

        setupFooterNavigation();
    }

    private void loadUserName() {
        userRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.getValue(String.class);
                welcomeText.setText("Welcome, " + (name != null ? name : "Donor") + "! 👋");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void fetchDonationHistory() {
        donationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                historycontainer.removeAllViews();
                totalDonated = 0;

                if (!snapshot.exists()) {
                    emptyText.setVisibility(View.VISIBLE);
                    totalAmounttext.setText("₹ 0");
                    return;
                }

                emptyText.setVisibility(View.GONE);

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Integer amt = ds.child("amount").getValue(Integer.class);
                    String msg = ds.child("message").getValue(String.class);
                    Long time = ds.child("timestamp").getValue(Long.class);
                    String donorName = ds.child("name").getValue(String.class);

                    if (amt != null) {
                        totalDonated += amt;
                        addHistoryCard(amt, msg, time, donorName);
                    }
                }
                totalAmounttext.setText("₹ " + totalDonated);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void addHistoryCard(int amount, String message, Long timestamp, String name) {
        CardView card = new CardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 24);
        card.setLayoutParams(params);
        card.setRadius(24);
        card.setCardElevation(4);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);
        layout.setBackgroundColor(Color.WHITE);

        // Header: Amount + Date
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);

        TextView amtTv = new TextView(this);
        amtTv.setText("₹" + amount);
        amtTv.setTextSize(18);
        amtTv.setTextColor(Color.parseColor("#1A237E"));
        amtTv.setTypeface(null, Typeface.BOLD);
        header.addView(amtTv);

        TextView dateTv = new TextView(this);
        String dateStr = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(new Date(timestamp));
        dateTv.setText(dateStr);
        dateTv.setTextSize(12);
        dateTv.setTextColor(Color.GRAY);
        LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT,
                1);
        dateTv.setLayoutParams(dateParams);
        dateTv.setGravity(android.view.Gravity.END);
        header.addView(dateTv);

        layout.addView(header);

        // Donor Name
        TextView nameTv = new TextView(this);
        nameTv.setText("By: " + (name != null ? name : "Donor"));
        nameTv.setTextSize(14);
        nameTv.setTextColor(Color.parseColor("#333333"));
        nameTv.setPadding(0, 6, 0, 0);
        nameTv.setTypeface(null, Typeface.ITALIC);
        layout.addView(nameTv);

        // Message
        if (message != null && !message.isEmpty()) {
            TextView msgTv = new TextView(this);
            msgTv.setText("\"" + message + "\"");
            msgTv.setTextSize(14);
            msgTv.setTextColor(Color.DKGRAY);
            msgTv.setPadding(0, 8, 0, 0);
            layout.addView(msgTv);
        }

        card.addView(layout);
        historycontainer.addView(card, 0); // Add at top (newest first)
    }

    private void setupFooterNavigation() {
        navHome = findViewById(R.id.nav_home);
        navDonation = findViewById(R.id.nav_donation);
        navUser = findViewById(R.id.nav_user);
        navDev = findViewById(R.id.nav_dev);
        navLogout = findViewById(R.id.nav_logout);

        navHome.setBackgroundResource(R.drawable.nav_unselected);
        navDonation.setBackgroundResource(R.drawable.nav_selected_box);
        navUser.setBackgroundResource(R.drawable.nav_unselected);
        navDev.setBackgroundResource(R.drawable.nav_unselected);
        navLogout.setBackgroundResource(R.drawable.nav_unselected);

        navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, Home1.class));
            finish();
        });
        navUser.setOnClickListener(v -> {
            startActivity(new Intent(this, user.class));
            finish();
        });
        navDev.setOnClickListener(v -> {
            startActivity(new Intent(this, devloper.class));
            finish();
        });
        navLogout.setOnClickListener(v -> {
            startActivity(new Intent(this, logout.class));
            finish();
        });
    }
}