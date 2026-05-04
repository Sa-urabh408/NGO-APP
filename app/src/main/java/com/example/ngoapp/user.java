package com.example.ngoapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class user extends AppCompatActivity {

    TextView pemail, pmobile, pname, prole, pmemberSince;
    ImageView profileImage, btnChangePic;
    FirebaseAuth mauth;
    DatabaseReference databaseReference;

    // Nav containers
    private LinearLayout navHome, navDonation, navUser, navDev, navLogout;

    // Photo picker launcher
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        try {
                            // Get Bitmap from URI
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

                            // Resize to max 300x300 to keep Base64 small
                            Bitmap resized = resizeBitmap(bitmap, 300);

                            // Show preview immediately
                            profileImage.setImageBitmap(resized);

                            // Convert to Base64 and save to Firebase
                            String base64Image = bitmapToBase64(resized);
                            saveProfilePhotoToDatabase(base64Image);

                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "❌ Failed to read image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        // Bind Views
        pemail       = findViewById(R.id.pemail);
        pmobile      = findViewById(R.id.pmobile);
        pname        = findViewById(R.id.pname);
        prole        = findViewById(R.id.prole);
        pmemberSince = findViewById(R.id.pmemberSince);
        profileImage = findViewById(R.id.pp);
        btnChangePic = findViewById(R.id.btnChangePic);

        // Firebase Init
        mauth             = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Load user data from Firebase
        FirebaseUser currentUser = mauth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Show account creation date as Member Since
            long creationTime = currentUser.getMetadata() != null
                    ? currentUser.getMetadata().getCreationTimestamp() : 0;
            if (creationTime > 0) {
                String date = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        .format(new Date(creationTime));
                pmemberSince.setText(date);
            }

            DatabaseReference userRef = databaseReference.child("Users").child(userId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String email  = snapshot.child("email").getValue(String.class);
                    String mobile = snapshot.child("mobile").getValue(String.class);
                    String name   = snapshot.child("name").getValue(String.class);
                    String photo  = snapshot.child("profilePhoto").getValue(String.class);

                    pemail.setText(email  != null ? email  : currentUser.getEmail());
                    pmobile.setText(mobile != null ? mobile : "Not added");
                    pname.setText(name   != null ? name   : "User");
                    prole.setText("NGO Member");

                    // Load saved profile photo from Base64
                    if (photo != null && !photo.isEmpty()) {
                        byte[] bytes = Base64.decode(photo, Base64.DEFAULT);
                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        profileImage.setImageBitmap(bmp);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(user.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Camera icon click — open image picker
        btnChangePic.setOnClickListener(v -> openImagePicker());

        // Profile image click — also open picker
        profileImage.setOnClickListener(v -> openImagePicker());

        // Footer nav
        setupFooterNavigation();
    }

    // -------- IMAGE HELPERS --------

    /** Resize bitmap so longest side = maxSize (keeps aspect ratio) */
    private Bitmap resizeBitmap(Bitmap src, int maxSize) {
        int w = src.getWidth(), h = src.getHeight();
        float scale = Math.min((float) maxSize / w, (float) maxSize / h);
        return Bitmap.createScaledBitmap(src, (int)(w * scale), (int)(h * scale), true);
    }

    /** Convert Bitmap → compressed Base64 String */
    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos); // 70% quality
        byte[] bytes = baos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    /** Save Base64 string to Firebase Realtime Database */
    private void saveProfilePhotoToDatabase(String base64Image) {
        FirebaseUser currentUser = mauth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        Toast.makeText(this, "Saving photo...", Toast.LENGTH_SHORT).show();

        databaseReference.child("Users").child(userId).child("profilePhoto")
                .setValue(base64Image)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(user.this, "✅ Profile photo updated!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(user.this, "❌ Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // -------- GALLERY PERMISSION & PICKER --------

    private void openImagePicker() {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, 101);
        } else {
            launchGallery();
        }
    }

    private void launchGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            launchGallery();
        } else {
            Toast.makeText(this, "Permission denied. Cannot access gallery.", Toast.LENGTH_SHORT).show();
        }
    }

    // -------- FOOTER NAV --------

    private void setupFooterNavigation() {
        navHome     = findViewById(R.id.nav_home);
        navDonation = findViewById(R.id.nav_donation);
        navUser     = findViewById(R.id.nav_user);
        navDev      = findViewById(R.id.nav_dev);
        navLogout   = findViewById(R.id.nav_logout);

        navUser.setBackgroundResource(R.drawable.nav_selected_box);
        ImageView userIcon = navUser.findViewById(R.id.footer_user);
        TextView userText  = navUser.findViewById(R.id.footer_user_text);
        if (userIcon != null) userIcon.setColorFilter(0xFFFFFFFF, android.graphics.PorterDuff.Mode.SRC_IN);
        if (userText != null) userText.setTextColor(0xFFFFFFFF);

        navHome.setOnClickListener(v -> {
            startActivity(new Intent(user.this, Home1.class));
            finish();
        });
        navDonation.setOnClickListener(v -> {
            startActivity(new Intent(user.this, donation.class));
            finish();
        });
        navUser.setOnClickListener(v -> { /* already here */ });
        navDev.setOnClickListener(v -> {
            startActivity(new Intent(user.this, devloper.class));
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
                    Intent intent = new Intent(user.this, loginpage.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
