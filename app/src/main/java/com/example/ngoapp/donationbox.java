package com.example.ngoapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
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
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONObject;

import java.util.HashMap;

public class donationbox extends AppCompatActivity implements PaymentResultListener {

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private SeekBar amountSeekBar;
    private TextView selectAmount, welcomeText;
    private EditText messageInput;
    private Button donateButton, historyButton;

    private int selectedValue = 0;
    private String userName = "";

    // Footer Nav
    private LinearLayout navHome, navDonation, navUser, navDev, navLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donationbox);

        // Bindings
        amountSeekBar = findViewById(R.id.amountseekbar);
        selectAmount = findViewById(R.id.selecteamount);
        messageInput = findViewById(R.id.mesageinput);
        donateButton = findViewById(R.id.doantebutton);
        historyButton = findViewById(R.id.donationhis);
        welcomeText = findViewById(R.id.welcome);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            usersRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
            loadUserName();
        }

        // SeekBar Logic
        amountSeekBar.setMax(10000);
        amountSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                selectedValue = progress;
                selectAmount.setText("₹ " + selectedValue);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Quick Chips
        setupChips();

        // Buttons
        donateButton.setOnClickListener(v -> {
            if (selectedValue == 0) {
                Toast.makeText(this, "Please select an amount", Toast.LENGTH_SHORT).show();
                return;
            }
            startPayment(selectedValue);
        });

        historyButton.setOnClickListener(v -> startActivity(new Intent(this, donationhistory.class)));

        // Footer
        setupFooterNavigation();
    }

    private void setupChips() {
        findViewById(R.id.chip100).setOnClickListener(v -> amountSeekBar.setProgress(100));
        findViewById(R.id.chip500).setOnClickListener(v -> amountSeekBar.setProgress(500));
        findViewById(R.id.chip1000).setOnClickListener(v -> amountSeekBar.setProgress(1000));
        findViewById(R.id.chip5000).setOnClickListener(v -> amountSeekBar.setProgress(5000));
    }

    private void loadUserName() {
        usersRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userName = snapshot.getValue(String.class);
                if (userName != null) {
                    welcomeText.setText("Welcome,"+ userName + "! 👋");
                } else {
                    welcomeText.setText("Welcome Guest!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void startPayment(int amount) {
        // Bypass Razorpay completely for dummy payment
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Dummy Payment Mode")
                .setMessage("Simulating a successful payment of ₹" + amount + " for demonstration...")
                .setPositiveButton("Simulate Success", (dialog, which) -> {
                    String dummyTxnId = "pay_dummy_" + System.currentTimeMillis();
                    onPaymentSuccess(dummyTxnId);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {
        saveDonationToFirebase(selectedValue, messageInput.getText().toString().trim());
        Toast.makeText(this, "✅ Donation Successful! ID: " + razorpayPaymentID, Toast.LENGTH_SHORT).show();

        // Reset UI to initial state after successful donation
        messageInput.setText("");
        amountSeekBar.setProgress(0);
        selectAmount.setText("₹ 0");
        selectedValue = 0;
    }

    @Override
    public void onPaymentError(int code, String response) {
        Toast.makeText(this, "❌ Payment Failed: " + response, Toast.LENGTH_SHORT).show();
    }

    private void saveDonationToFirebase(int amount, String message) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            DatabaseReference donationRef = FirebaseDatabase.getInstance()
                    .getReference("Donations").child(uid).push();

            HashMap<String, Object> map = new HashMap<>();
            map.put("amount", amount);
            map.put("message", message);
            map.put("timestamp", System.currentTimeMillis());
            map.put("name", (userName != null && !userName.isEmpty()) ? userName : "Donor");

            donationRef.setValue(map);
        }
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