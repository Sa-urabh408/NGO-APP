package com.example.ngoapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class gallery extends AppCompatActivity {

    private RecyclerView dynamicRecycler;
    private LinearLayout noPostsLabel;
    private LinearLayout navHome, navDonation, navUser, navDev, navLogout;

    private List<GalleryPost> postList = new ArrayList<>();
    private GalleryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        dynamicRecycler = findViewById(R.id.dynamicGalleryRecycler);
        noPostsLabel    = findViewById(R.id.noPostsLabel);

        // Animate the whole page
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        dynamicRecycler.startAnimation(slideUp);

        dynamicRecycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GalleryAdapter(postList);
        dynamicRecycler.setAdapter(adapter);

        loadFirebasePosts();
        setupFooterNavigation();
    }

    private void loadFirebasePosts() {
        FirebaseDatabase.getInstance().getReference("GalleryPosts")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        postList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String b64     = ds.child("imageBase64").getValue(String.class);
                            String caption = ds.child("caption").getValue(String.class);
                            Long   time    = ds.child("timestamp").getValue(Long.class);
                            if (b64 != null) {
                                postList.add(new GalleryPost(b64, caption != null ? caption : "", time != null ? time : 0L));
                            }
                        }
                        // Sort newest first
                        postList.sort((a, b) -> Long.compare(b.timestamp, a.timestamp));
                        adapter.notifyDataSetChanged();
                        noPostsLabel.setVisibility(postList.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(gallery.this, "Could not load gallery", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ─── Data Model ─────────────────────────────────────────────────────────────
    static class GalleryPost {
        String imageBase64, caption; long timestamp;
        GalleryPost(String i, String c, long t) { imageBase64 = i; caption = c; timestamp = t; }
    }

    // ─── Adapter ─────────────────────────────────────────────────────────────────
    static class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.VH> {
        final List<GalleryPost> list;
        GalleryAdapter(List<GalleryPost> list) { this.list = list; }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_gallery, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            GalleryPost p = list.get(pos);
            try {
                byte[] bytes = Base64.decode(p.imageBase64, Base64.DEFAULT);
                Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                h.image.setImageBitmap(bm);
            } catch (Exception ignored) {}

            if (!p.caption.isEmpty()) {
                h.caption.setVisibility(View.VISIBLE);
                h.caption.setText(p.caption);
            } else {
                h.caption.setVisibility(View.GONE);
            }
        }

        @Override public int getItemCount() { return list.size(); }

        static class VH extends RecyclerView.ViewHolder {
            ImageView image; TextView caption;
            VH(View v) {
                super(v);
                image   = v.findViewById(R.id.galleryImage);
                caption = v.findViewById(R.id.galleryCaption);
            }
        }
    }

    private void setupFooterNavigation() {
        navHome     = findViewById(R.id.nav_home);
        navDonation = findViewById(R.id.nav_donation);
        navUser     = findViewById(R.id.nav_user);
        navDev      = findViewById(R.id.nav_dev);
        navLogout   = findViewById(R.id.nav_logout);

        navHome.setBackgroundResource(R.drawable.nav_selected_box);
        navDonation.setBackgroundResource(R.drawable.nav_unselected);
        navUser.setBackgroundResource(R.drawable.nav_unselected);
        navDev.setBackgroundResource(R.drawable.nav_unselected);
        navLogout.setBackgroundResource(R.drawable.nav_unselected);

        navHome.setOnClickListener(v -> { startActivity(new Intent(gallery.this, Home1.class)); finish(); });
        navDonation.setOnClickListener(v -> { startActivity(new Intent(gallery.this, donation.class)); finish(); });
        navUser.setOnClickListener(v -> { startActivity(new Intent(gallery.this, user.class)); finish(); });
        navDev.setOnClickListener(v -> { startActivity(new Intent(gallery.this, devloper.class)); finish(); });
        navLogout.setOnClickListener(v -> { startActivity(new Intent(gallery.this, logout.class)); finish(); });
    }
}
