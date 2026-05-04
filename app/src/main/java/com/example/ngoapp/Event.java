package com.example.ngoapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Event extends AppCompatActivity {

    private RecyclerView eventsRecycler;
    private TextView emptyLabel;
    private LinearLayout navHome, navDonation, navUser, navDev, navLogout;

    private final List<EventItem> eventList = new ArrayList<>();
    private EventAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        eventsRecycler = findViewById(R.id.eventsRecycler);
        emptyLabel     = findViewById(R.id.emptyEventsLabel);

        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        eventsRecycler.startAnimation(slideUp);

        eventsRecycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventAdapter(eventList);
        eventsRecycler.setAdapter(adapter);

        loadEvents();
        setupFooterNavigation();
    }

    private void loadEvents() {
        FirebaseDatabase.getInstance().getReference("Events")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        eventList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String b64   = ds.child("imageBase64").getValue(String.class);
                            String title = ds.child("title").getValue(String.class);
                            String desc  = ds.child("description").getValue(String.class);
                            Long   time  = ds.child("timestamp").getValue(Long.class);
                            if (title != null) {
                                eventList.add(new EventItem(
                                        b64   != null ? b64   : "",
                                        title,
                                        desc  != null ? desc  : "",
                                        time  != null ? time  : 0L
                                ));
                            }
                        }
                        eventList.sort((a, b) -> Long.compare(b.timestamp, a.timestamp));
                        adapter.notifyDataSetChanged();
                        emptyLabel.setVisibility(eventList.isEmpty() ? View.VISIBLE : View.GONE);
                        eventsRecycler.setVisibility(eventList.isEmpty() ? View.GONE : View.VISIBLE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(Event.this, "Could not load events", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ─── Data model ─────────────────────────────────────────────────────────────
    static class EventItem {
        String imageBase64, title, description; long timestamp;
        EventItem(String i, String t, String d, long ts) {
            imageBase64 = i; title = t; description = d; timestamp = ts;
        }
    }

    // ─── Adapter ─────────────────────────────────────────────────────────────────
    static class EventAdapter extends RecyclerView.Adapter<EventAdapter.VH> {
        final List<EventItem> list;
        EventAdapter(List<EventItem> list) { this.list = list; }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_event, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            EventItem e = list.get(pos);
            h.title.setText(e.title);
            h.desc.setText(e.description);
            String dateStr = e.timestamp > 0
                    ? new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date(e.timestamp))
                    : "";
            h.date.setText(dateStr);

            if (!e.imageBase64.isEmpty()) {
                try {
                    byte[] bytes = Base64.decode(e.imageBase64, Base64.DEFAULT);
                    Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    h.image.setImageBitmap(bm);
                } catch (Exception ignored) {}
            }
        }

        @Override public int getItemCount() { return list.size(); }

        static class VH extends RecyclerView.ViewHolder {
            ImageView image; TextView title, desc, date;
            VH(View v) {
                super(v);
                image = v.findViewById(R.id.eventImage);
                title = v.findViewById(R.id.eventTitle);
                desc  = v.findViewById(R.id.eventDesc);
                date  = v.findViewById(R.id.eventDate);
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

        navHome.setOnClickListener(v -> { startActivity(new Intent(Event.this, Home1.class)); finish(); });
        navDonation.setOnClickListener(v -> { startActivity(new Intent(Event.this, donation.class)); finish(); });
        navUser.setOnClickListener(v -> { startActivity(new Intent(Event.this, user.class)); finish(); });
        navDev.setOnClickListener(v -> { startActivity(new Intent(Event.this, devloper.class)); finish(); });
        navLogout.setOnClickListener(v -> { startActivity(new Intent(Event.this, logout.class)); finish(); });
    }
}
