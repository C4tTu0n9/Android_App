package com.example.appcore.ui.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appcore.R;
import com.example.appcore.data.models.Room;
import com.example.appcore.ui.adapter.RoomAdminAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdminRoomActivity extends AppCompatActivity {
    private ListView listViewRoom;
    private ArrayList<Room> roomList;
    private RoomAdminAdapter adapter;

    FloatingActionButton fabAddRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        FrameLayout contentFrame = findViewById(R.id.content_frame);
//        LayoutInflater.from(this).inflate(R.layout.activity_admin_room, contentFrame, true);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_room);

        listViewRoom = findViewById(R.id.listViewRoom);
        roomList = new ArrayList<>();
        adapter = new RoomAdminAdapter(this, R.layout.item_room_admin, roomList);
        listViewRoom.setAdapter(adapter);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Quản lý phòng");
        toolbar.setNavigationOnClickListener(v -> finish());

        fabAddRoom = findViewById(R.id.fabAddRoom);

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("MovieRooms");

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                roomList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Room room = data.getValue(Room.class);
                    if (room != null) {
                        Log.d("FIREBASE_ROOM", "Loaded: " + room.getRoomName());
                        roomList.add(room); // Thêm vào cuối
                    }else {
                        Log.w("FIREBASE_ROOM", "Room is null");

                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Xử lý lỗi nếu cần
            }
        });

        fabAddRoom.setOnClickListener(v -> {
            Intent intent = new Intent(AdminRoomActivity.this, AddRoomActivity.class);
            startActivity(intent);
        });
    }
}
