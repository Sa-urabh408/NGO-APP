package com.example.ngoapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminDonationList extends AppCompatActivity {

    RecyclerView recyclerView;
    TextView emptyText, totalAmountHeader;
    ImageButton backBtn;
    List<DonationItem> donationList = new ArrayList<>();
    DonationAdapter adapter;
    int grandTotal = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_donation_list);

        recyclerView      = findViewById(R.id.donationRecycler);
        emptyText         = findViewById(R.id.emptyText);
        totalAmountHeader = findViewById(R.id.totalAmountHeader);
        backBtn           = findViewById(R.id.backBtn);

        backBtn.setOnClickListener(v -> finish());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DonationAdapter(donationList);
        recyclerView.setAdapter(adapter);

        fetchAllDonations();
    }

    private void fetchAllDonations() {
        // Donations/<uid>/<push-id>/{amount, name, message, timestamp}
        FirebaseDatabase.getInstance().getReference("Donations")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        donationList.clear();
                        grandTotal = 0;
                        // Iterate over all users
                        for (DataSnapshot userSnap : snapshot.getChildren()) {
                            for (DataSnapshot donSnap : userSnap.getChildren()) {
                                Integer amt  = donSnap.child("amount").getValue(Integer.class);
                                String  name = donSnap.child("name").getValue(String.class);
                                String  msg  = donSnap.child("message").getValue(String.class);
                                Long    time = donSnap.child("timestamp").getValue(Long.class);
                                if (amt != null) {
                                    grandTotal += amt;
                                    donationList.add(new DonationItem(
                                            name  != null ? name  : "Anonymous",
                                            msg   != null ? msg   : "",
                                            amt,
                                            time  != null ? time  : 0L
                                    ));
                                }
                            }
                        }
                        // Sort newest first
                        donationList.sort((a, b) -> Long.compare(b.timestamp, a.timestamp));
                        adapter.notifyDataSetChanged();
                        totalAmountHeader.setText("Total: ₹" + grandTotal + "  (" + donationList.size() + " donations)");
                        emptyText.setVisibility(donationList.isEmpty() ? View.VISIBLE : View.GONE);
                        recyclerView.setVisibility(donationList.isEmpty() ? View.GONE : View.VISIBLE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    // ─── Model ──────────────────────────────────────────────────────────────────
    static class DonationItem {
        String name, message; int amount; long timestamp;
        DonationItem(String name, String message, int amount, long timestamp) {
            this.name = name; this.message = message;
            this.amount = amount; this.timestamp = timestamp;
        }
    }

    // ─── Adapter ─────────────────────────────────────────────────────────────────
    static class DonationAdapter extends RecyclerView.Adapter<DonationAdapter.VH> {
        final List<DonationItem> list;
        DonationAdapter(List<DonationItem> list) { this.list = list; }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_donation_admin, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            DonationItem d = list.get(pos);
            h.name.setText(d.name);
            h.amount.setText("₹" + d.amount);
            h.message.setText(d.message.isEmpty() ? "No message" : "\"" + d.message + "\"");
            String date = d.timestamp > 0
                    ? new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date(d.timestamp))
                    : "";
            h.date.setText(date);
        }

        @Override public int getItemCount() { return list.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView name, amount, message, date;
            VH(View v) {
                super(v);
                name    = v.findViewById(R.id.donorName);
                amount  = v.findViewById(R.id.donorAmount);
                message = v.findViewById(R.id.donorMessage);
                date    = v.findViewById(R.id.donorDate);
            }
        }
    }
}
