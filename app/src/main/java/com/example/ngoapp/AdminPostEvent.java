package com.example.ngoapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
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
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;

public class AdminPostEvent extends AppCompatActivity {

    ImageView eventPreviewImage;
    LinearLayout eventPlaceholder;
    Button selectEventImageBtn, postEventBtn;
    EditText eventTitle, eventDescription;
    ProgressBar eventProgress;
    ImageButton backBtn;

    String base64Image = null;
    DatabaseReference eventsRef;

    ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) loadAndPreviewImage(uri);
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_post_event);

        eventPreviewImage   = findViewById(R.id.eventPreviewImage);
        eventPlaceholder    = findViewById(R.id.eventPlaceholder);
        selectEventImageBtn = findViewById(R.id.selectEventImageBtn);
        postEventBtn        = findViewById(R.id.postEventBtn);
        eventTitle          = findViewById(R.id.eventTitle);
        eventDescription    = findViewById(R.id.eventDescription);
        eventProgress       = findViewById(R.id.eventProgress);
        backBtn             = findViewById(R.id.backBtn);

        eventsRef = FirebaseDatabase.getInstance().getReference("Events");

        backBtn.setOnClickListener(v -> finish());
        selectEventImageBtn.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        eventPreviewImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        postEventBtn.setOnClickListener(v -> postEvent());
    }

    private void loadAndPreviewImage(Uri uri) {
        try {
            InputStream stream = getContentResolver().openInputStream(uri);
            Bitmap original = BitmapFactory.decodeStream(stream);
            Bitmap scaled   = scaleBitmap(original, 700);

            eventPlaceholder.setVisibility(View.GONE);
            eventPreviewImage.setImageBitmap(scaled);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            scaled.compress(Bitmap.CompressFormat.JPEG, 65, baos);
            base64Image = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

        } catch (Exception e) {
            Toast.makeText(this, "Failed to load image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap scaleBitmap(Bitmap src, int maxWidth) {
        if (src.getWidth() <= maxWidth) return src;
        float ratio = (float) maxWidth / src.getWidth();
        return Bitmap.createScaledBitmap(src, maxWidth, (int)(src.getHeight() * ratio), true);
    }

    private void postEvent() {
        String title = eventTitle.getText().toString().trim();
        String desc  = eventDescription.getText().toString().trim();

        if (base64Image == null) {
            Toast.makeText(this, "Please select an event image", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(title)) {
            eventTitle.setError("Title is required");
            eventTitle.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(desc)) {
            eventDescription.setError("Description is required");
            eventDescription.requestFocus();
            return;
        }

        eventProgress.setVisibility(View.VISIBLE);
        postEventBtn.setEnabled(false);

        String postId = eventsRef.push().getKey();
        if (postId == null) {
            Toast.makeText(this, "Failed to create event ID", Toast.LENGTH_SHORT).show();
            eventProgress.setVisibility(View.GONE);
            postEventBtn.setEnabled(true);
            return;
        }

        HashMap<String, Object> event = new HashMap<>();
        event.put("imageBase64", base64Image);
        event.put("title",       title);
        event.put("description", desc);
        event.put("timestamp",   System.currentTimeMillis());

        eventsRef.child(postId).setValue(event)
                .addOnSuccessListener(unused -> {
                    eventProgress.setVisibility(View.GONE);
                    postEventBtn.setEnabled(true);
                    Toast.makeText(this, "✅ Event posted!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    eventProgress.setVisibility(View.GONE);
                    postEventBtn.setEnabled(true);
                    Toast.makeText(this, "❌ Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
