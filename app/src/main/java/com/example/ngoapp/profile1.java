package com.example.ngoapp;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class profile1 extends AppCompatActivity {

    TextView pemail, pmobile, pname;
    ImageView ppImage;
    LinearLayout cameraOverlay;
    ProgressBar uploadProgress;

    FirebaseAuth mauth;
    DatabaseReference databaseReference;

    // Image picker launcher
    ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) uploadProfilePicture(uri);
            }
    );

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile1);

        // Bind Views
        ppImage        = findViewById(R.id.pp);
        pemail         = findViewById(R.id.pemail);
        pmobile        = findViewById(R.id.pmobile);
        pname          = findViewById(R.id.pname);
        cameraOverlay  = findViewById(R.id.cameraOverlay);
        uploadProgress = findViewById(R.id.uploadProgress);

        // Firebase Init
        mauth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Load user data
        loadUserData();

        // Tap profile image OR camera icon to pick new photo
        ppImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        cameraOverlay.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
    }

    private void loadUserData() {
        FirebaseUser currentUser = mauth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        DatabaseReference userRef = databaseReference.child("Users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String email  = snapshot.child("email").getValue(String.class);
                String mobile = snapshot.child("mobile").getValue(String.class);
                String name   = snapshot.child("name").getValue(String.class);
                String photo  = snapshot.child("profilePhoto").getValue(String.class);

                pname.setText(name   != null ? name   : "—");
                pemail.setText(email  != null ? email  : "—");
                pmobile.setText(mobile != null ? mobile : "—");

                // Load saved profile picture if exists
                if (photo != null && !photo.isEmpty()) {
                    try {
                        byte[] decodedBytes = Base64.decode(photo, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                        ppImage.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        ppImage.setImageResource(R.drawable.profile1);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(profile1.this, "❌ Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadProfilePicture(Uri uri) {
        try {
            uploadProgress.setVisibility(View.VISIBLE);

            InputStream stream = getContentResolver().openInputStream(uri);
            Bitmap original = BitmapFactory.decodeStream(stream);

            // Scale down to 300px to keep DB size small
            Bitmap scaled = scaleBitmap(original, 300);

            // Show immediately
            ppImage.setImageBitmap(scaled);

            // Convert to Base64
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            scaled.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            String base64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

            // Save to Firebase under Users/{uid}/profilePhoto
            FirebaseUser currentUser = mauth.getCurrentUser();
            if (currentUser != null) {
                databaseReference
                        .child("Users")
                        .child(currentUser.getUid())
                        .child("profilePhoto")
                        .setValue(base64)
                        .addOnSuccessListener(unused -> {
                            uploadProgress.setVisibility(View.GONE);
                            Toast.makeText(this, "✅ Profile photo updated!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            uploadProgress.setVisibility(View.GONE);
                            Toast.makeText(this, "❌ Failed to save photo: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            }

        } catch (Exception e) {
            uploadProgress.setVisibility(View.GONE);
            Toast.makeText(this, "Failed to load image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap scaleBitmap(Bitmap src, int maxWidth) {
        if (src.getWidth() <= maxWidth) return src;
        float ratio = (float) maxWidth / src.getWidth();
        return Bitmap.createScaledBitmap(src, maxWidth, (int)(src.getHeight() * ratio), true);
    }
}
