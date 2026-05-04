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

import java.util.ArrayList;
import java.util.List;

public class Userlist extends AppCompatActivity {

    RecyclerView recyclerView;
    TextView emptyText, totalCount;
    ImageButton backBtn;
    DatabaseReference userRef;
    List<UserModel> userList = new ArrayList<>();
    UserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userlist);

        recyclerView = findViewById(R.id.userRecycler);
        emptyText    = findViewById(R.id.emptyText);
        totalCount   = findViewById(R.id.totalCount);
        backBtn      = findViewById(R.id.backBtn);

        backBtn.setOnClickListener(v -> finish());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter(userList);
        recyclerView.setAdapter(adapter);

        userRef = FirebaseDatabase.getInstance().getReference("Users");
        fetchUsers();
    }

    private void fetchUsers() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String name   = ds.child("name").getValue(String.class);
                    String email  = ds.child("email").getValue(String.class);
                    String mobile = ds.child("mobile").getValue(String.class);
                    if (name != null) {
                        userList.add(new UserModel(
                                name,
                                email  != null ? email  : "",
                                mobile != null ? mobile : ""
                        ));
                    }
                }
                adapter.notifyDataSetChanged();
                int c = userList.size();
                totalCount.setText(c + " user" + (c != 1 ? "s" : "") + " registered");
                emptyText.setVisibility(c == 0 ? View.VISIBLE : View.GONE);
                recyclerView.setVisibility(c == 0 ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // ─── Data model ─────────────────────────────────────────────────────────────
    static class UserModel {
        String name, email, mobile;
        UserModel(String name, String email, String mobile) {
            this.name = name; this.email = email; this.mobile = mobile;
        }
    }

    // ─── Adapter ─────────────────────────────────────────────────────────────────
    static class UserAdapter extends RecyclerView.Adapter<UserAdapter.VH> {
        final List<UserModel> list;
        UserAdapter(List<UserModel> list) { this.list = list; }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin_user, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            UserModel u = list.get(pos);
            h.name.setText(u.name);
            h.email.setText(u.email.isEmpty() ? "No email" : u.email);
            h.phone.setText("📞 " + (u.mobile.isEmpty() ? "No phone" : u.mobile));
            // Avatar: first letter of name
            h.avatar.setText(u.name.substring(0, 1).toUpperCase());
        }

        @Override public int getItemCount() { return list.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView name, email, phone, avatar;
            VH(View v) {
                super(v);
                name   = v.findViewById(R.id.userName);
                email  = v.findViewById(R.id.userEmail);
                phone  = v.findViewById(R.id.userPhone);
                avatar = v.findViewById(R.id.userAvatar);
            }
        }
    }
}