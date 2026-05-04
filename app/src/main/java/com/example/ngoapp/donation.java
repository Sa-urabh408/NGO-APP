package com.example.ngoapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
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

public class donation extends AppCompatActivity implements PaymentResultListener {

    private SeekBar amountSeekBar;
    private TextView selectedAmount, welcomeText;
    private EditText messageInput;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference databaseRef;

    // Saved temporarily for use in onPaymentSuccess
    private int pendingAmount = 0;
    private String pendingMessage = "";
    private String cachedUserName = "Donor";

    // Nav containers
    private LinearLayout navHome, navDonation, navUser, navDev, navLogout;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation);

        amountSeekBar = findViewById(R.id.amountseekbar1);
        selectedAmount = findViewById(R.id.selecteamount1);
        messageInput = findViewById(R.id.mesageinput1);
        welcomeText = findViewById(R.id.welcome);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        databaseRef = FirebaseDatabase.getInstance().getReference("Users");

        setupFooterNavigation();
        loadUserName();
        setupQuickAmountChips();

        amountSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                selectedAmount.setText("₹ " + progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        findViewById(R.id.doantebutton1).setOnClickListener(view -> {
            int amount = amountSeekBar.getProgress();
            String message = messageInput.getText().toString().trim();
            if (amount == 0) {
                Toast.makeText(donation.this, "Please select amount", Toast.LENGTH_SHORT).show();
                return;
            }
            // BUG FIX: Save pending values — actual Firebase save happens in onPaymentSuccess only
            pendingAmount  = amount;
            pendingMessage = message;
            startPayment(amount);
        });

        findViewById(R.id.donationhis1)
                .setOnClickListener(v -> startActivity(new Intent(donation.this, donationhistory.class)));
    }

    private void setupQuickAmountChips() {
        findViewById(R.id.chip100).setOnClickListener(v -> amountSeekBar.setProgress(100));
        findViewById(R.id.chip500).setOnClickListener(v -> amountSeekBar.setProgress(500));
        findViewById(R.id.chip1000).setOnClickListener(v -> amountSeekBar.setProgress(1000));
        findViewById(R.id.chip5000).setOnClickListener(v -> amountSeekBar.setProgress(5000));
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

    /** Called ONLY on actual payment success — saves donation to Firebase with user name */
    @Override
    public void onPaymentSuccess(String razorpayPaymentId) {
        Toast.makeText(this, "✅ Donation successful! Thank you!", Toast.LENGTH_SHORT).show();
        saveDonationToFirebase(pendingAmount, pendingMessage);
    }

    @Override
    public void onPaymentError(int code, String description) {
        Toast.makeText(this, "❌ Donation failed. Please try again.", Toast.LENGTH_SHORT).show();
        // pendingAmount/pendingMessage are discarded — nothing is saved
    }

    private void saveDonationToFirebase(int amount, String message) {
        if (currentUser == null) return;
        String uid = currentUser.getUid();
        DatabaseReference donationRef = FirebaseDatabase.getInstance()
                .getReference("Donations").child(uid).push();

        java.util.HashMap<String, Object> map = new java.util.HashMap<>();
        map.put("amount",    amount);
        map.put("message",   message);
        map.put("name",      cachedUserName);   // BUG FIX: save name so history can display it
        map.put("timestamp", System.currentTimeMillis());

        donationRef.setValue(map);
    }

    private void loadUserName() {
        if (currentUser == null) return;
        databaseRef.child(currentUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name = snapshot.child("name").getValue(String.class);
                        if (name != null) {
                            cachedUserName = name;
                            welcomeText.setText("Welcome, " + name + "! 👋");
                        } else {
                            welcomeText.setText("Welcome, Donor! 👋");
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void setupFooterNavigation() {
        navHome     = findViewById(R.id.nav_home);
        navDonation = findViewById(R.id.nav_donation);
        navUser     = findViewById(R.id.nav_user);
        navDev      = findViewById(R.id.nav_dev);
        navLogout   = findViewById(R.id.nav_logout);

        navDonation.setBackgroundResource(R.drawable.nav_selected_box);
        ImageView donIcon = navDonation.findViewById(R.id.footer_donation);
        TextView donText  = navDonation.findViewById(R.id.footer_donation_text);
        if (donIcon != null) donIcon.setColorFilter(0xFFFFFFFF, android.graphics.PorterDuff.Mode.SRC_IN);
        if (donText != null) donText.setTextColor(0xFFFFFFFF);

        navHome.setOnClickListener(v -> { startActivity(new Intent(this, Home1.class)); finish(); });
        navUser.setOnClickListener(v -> { startActivity(new Intent(this, user.class)); finish(); });
        navDev.setOnClickListener(v -> { startActivity(new Intent(this, devloper.class)); finish(); });
        navLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(donation.this, loginpage.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
