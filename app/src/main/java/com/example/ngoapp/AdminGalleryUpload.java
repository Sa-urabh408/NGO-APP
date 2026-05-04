package com.example.ngoapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;

public class AdminGalleryUpload extends AppCompatActivity {

    ImageView previewImage;
    LinearLayout placeholderLayout;
    Button selectImageBtn, uploadBtn;
    EditText captionInput;
    ProgressBar uploadProgress;
    ImageButton backBtn;

    String base64Image = null;
    DatabaseReference galleryRef;

    ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) loadAndPreviewImage(uri);
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_gallery_upload);

        previewImage      = findViewById(R.id.previewImage);
        placeholderLayout = findViewById(R.id.placeholderLayout);
        selectImageBtn    = findViewById(R.id.selectImageBtn);
        uploadBtn         = findViewById(R.id.uploadBtn);
        captionInput      = findViewById(R.id.captionInput);
        uploadProgress    = findViewById(R.id.uploadProgress);
        backBtn           = findViewById(R.id.backBtn);

        galleryRef = FirebaseDatabase.getInstance().getReference("GalleryPosts");

        backBtn.setOnClickListener(v -> finish());

        selectImageBtn.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        // Also tap on preview card to pick image
        previewImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        uploadBtn.setOnClickListener(v -> uploadGalleryPost());
    }

    private void loadAndPreviewImage(Uri uri) {
        try {
            InputStream stream = getContentResolver().openInputStream(uri);
            Bitmap original = BitmapFactory.decodeStream(stream);

            // Scale down to max 600px wide to keep DB size reasonable
            Bitmap scaled = scaleBitmap(original, 600);

            // Show preview
            placeholderLayout.setVisibility(View.GONE);
            previewImage.setImageBitmap(scaled);

            // Convert to Base64
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            scaled.compress(Bitmap.CompressFormat.JPEG, 60, baos);
            base64Image = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

        } catch (Exception e) {
            Toast.makeText(this, "Failed to load image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap scaleBitmap(Bitmap src, int maxWidth) {
        if (src.getWidth() <= maxWidth) return src;
        float ratio = (float) maxWidth / src.getWidth();
        int newHeight = (int) (src.getHeight() * ratio);
        return Bitmap.createScaledBitmap(src, maxWidth, newHeight, true);
    }

    private void uploadGalleryPost() {
        if (base64Image == null) {
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
            return;
        }
        String caption = captionInput.getText().toString().trim();

        uploadProgress.setVisibility(View.VISIBLE);
        uploadBtn.setEnabled(false);

        String postId = galleryRef.push().getKey();
        if (postId == null) {
            Toast.makeText(this, "Failed to generate post ID", Toast.LENGTH_SHORT).show();
            uploadProgress.setVisibility(View.GONE);
            uploadBtn.setEnabled(true);
            return;
        }

        HashMap<String, Object> post = new HashMap<>();
        post.put("imageBase64", base64Image);
        post.put("caption",     caption);
        post.put("timestamp",   System.currentTimeMillis());

        galleryRef.child(postId).setValue(post)
                .addOnSuccessListener(unused -> {
                    uploadProgress.setVisibility(View.GONE);
                    uploadBtn.setEnabled(true);
                    Toast.makeText(this, "✅ Photo posted to Gallery!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    uploadProgress.setVisibility(View.GONE);
                    uploadBtn.setEnabled(true);
                    Toast.makeText(this, "❌ Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
