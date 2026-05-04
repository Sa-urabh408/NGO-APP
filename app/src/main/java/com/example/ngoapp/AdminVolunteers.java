package com.example.ngoapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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

public class AdminVolunteers extends AppCompatActivity {

    RecyclerView recyclerView;
    TextView emptyText, totalCount;
    ImageButton backBtn;
    DatabaseReference volRef;
    List<VolunteerModel> volList = new ArrayList<>();
    VolAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_volunteers);

        recyclerView = findViewById(R.id.volunteerRecycler);
        emptyText    = findViewById(R.id.emptyText);
        totalCount   = findViewById(R.id.totalCount);
        backBtn      = findViewById(R.id.backBtn);

        backBtn.setOnClickListener(v -> finish());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VolAdapter(volList);
        recyclerView.setAdapter(adapter);

        volRef = FirebaseDatabase.getInstance().getReference("Volunteers");
        fetchVolunteers();
    }

    private void fetchVolunteers() {
        volRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                volList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String fName   = ds.child("firstName").getValue(String.class);
                    String lName   = ds.child("lastName").getValue(String.class);
                    String mobile  = ds.child("mobile").getValue(String.class);
                    String message = ds.child("message").getValue(String.class);
                    
                    if (fName != null || message != null) {
                        String fullName = (fName != null ? fName : "") + " " + (lName != null ? lName : "");
                        if (fullName.trim().isEmpty()) fullName = "Unknown";
                        volList.add(new VolunteerModel(fullName.trim(), mobile != null ? mobile : "", message != null ? message : ""));
                    }
                }
                adapter.notifyDataSetChanged();
                int count = volList.size();
                totalCount.setText(count + " application" + (count != 1 ? "s" : ""));
                emptyText.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
                recyclerView.setVisibility(count == 0 ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminVolunteers.this, "Failed to load volunteers", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ─── Data model ────────────────────────────────────────────────────────────
    static class VolunteerModel {
        String name, mobile, message;
        VolunteerModel(String name, String mobile, String message) {
            this.name = name; this.mobile = mobile; this.message = message;
        }
    }

    // ─── Adapter ────────────────────────────────────────────────────────────────
    static class VolAdapter extends RecyclerView.Adapter<VolAdapter.VH> {
        final List<VolunteerModel> list;
        VolAdapter(List<VolunteerModel> list) { this.list = list; }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin_volunteer, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            VolunteerModel m = list.get(pos);
            h.name.setText(m.name);
            h.mobile.setText(m.mobile.isEmpty() ? "No phone" : m.mobile);
            h.message.setText(m.message);
            h.index.setText(String.valueOf(pos + 1));
        }

        @Override public int getItemCount() { return list.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView name, mobile, message, index;
            VH(View v) {
                super(v);
                name    = v.findViewById(R.id.volName);
                mobile  = v.findViewById(R.id.volMobile);
                message = v.findViewById(R.id.volMessage);
                index   = v.findViewById(R.id.volIndex);
            }
        }
    }
}
