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

public class AdminMessages extends AppCompatActivity {

    RecyclerView recyclerView;
    TextView emptyText, totalCount;
    ImageButton backBtn;
    DatabaseReference msgRef;
    List<MessageModel> messageList = new ArrayList<>();
    MsgAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_messages);

        recyclerView = findViewById(R.id.messageRecycler);
        emptyText    = findViewById(R.id.emptyText);
        totalCount   = findViewById(R.id.totalCount);
        backBtn      = findViewById(R.id.backBtn);

        backBtn.setOnClickListener(v -> finish());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MsgAdapter(messageList);
        recyclerView.setAdapter(adapter);

        msgRef = FirebaseDatabase.getInstance().getReference("Messages");
        fetchMessages();
    }

    private void fetchMessages() {
        msgRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // BUG FIX: was reading "Name:" and "message:" (with colon) — now correct
                    String name    = ds.child("Name").getValue(String.class);
                    String mobile  = ds.child("Mobile").getValue(String.class);
                    String message = ds.child("message").getValue(String.class);
                    if (name != null || message != null) {
                        messageList.add(new MessageModel(
                                name    != null ? name    : "Unknown",
                                mobile  != null ? mobile  : "",
                                message != null ? message : ""
                        ));
                    }
                }
                adapter.notifyDataSetChanged();
                int count = messageList.size();
                totalCount.setText(count + " message" + (count != 1 ? "s" : ""));
                emptyText.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
                recyclerView.setVisibility(count == 0 ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminMessages.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ─── Data model ────────────────────────────────────────────────────────────
    static class MessageModel {
        String name, mobile, message;
        MessageModel(String name, String mobile, String message) {
            this.name = name; this.mobile = mobile; this.message = message;
        }
    }

    // ─── Adapter ────────────────────────────────────────────────────────────────
    static class MsgAdapter extends RecyclerView.Adapter<MsgAdapter.VH> {
        final List<MessageModel> list;
        MsgAdapter(List<MessageModel> list) { this.list = list; }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin_message, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            MessageModel m = list.get(pos);
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
                name    = v.findViewById(R.id.msgName);
                mobile  = v.findViewById(R.id.msgMobile);
                message = v.findViewById(R.id.msgText);
                index   = v.findViewById(R.id.msgIndex);
            }
        }
    }
}