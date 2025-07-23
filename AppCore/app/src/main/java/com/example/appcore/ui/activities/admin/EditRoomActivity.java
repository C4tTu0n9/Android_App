package com.example.appcore.ui.activities.admin;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.appcore.R;
import com.example.appcore.data.models.Room;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditRoomActivity extends AppCompatActivity {

    private EditText edtRoomName, edtSeatCount;
    private Button btnUpdate;

    private String roomId;
    private DatabaseReference roomRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_room);

        edtRoomName = findViewById(R.id.edtRoomName);
        edtSeatCount = findViewById(R.id.edtSeatCount);
        btnUpdate = findViewById(R.id.btnUpdateRoom);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Cập nhật phòng");
        toolbar.setNavigationOnClickListener(v -> finish());

        roomId = getIntent().getStringExtra("roomId");
        if (roomId == null) {
            Toast.makeText(this, "Không tìm thấy phòng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        roomRef = FirebaseDatabase.getInstance().getReference("MovieRooms").child(roomId);

        // Load room data
        roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Room room = snapshot.getValue(Room.class);
                if (room != null) {
                    edtRoomName.setText(room.getRoomName());
                    edtSeatCount.setText(String.valueOf(room.getSeatCount()));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(EditRoomActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });

        btnUpdate.setOnClickListener(v -> {
            String name = edtRoomName.getText().toString().trim();
            String seat = edtSeatCount.getText().toString().trim();

            if (name.isEmpty() || seat.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int seatCount = Integer.parseInt(seat);
                Room updatedRoom = new Room(roomId, name, 0, seatCount); // Bạn có thể sửa thêm roomType nếu cần
                roomRef.setValue(updatedRoom)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Số ghế phải là số", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
