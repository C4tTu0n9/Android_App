package com.example.appcore.ui.activities.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.appcore.R;
import com.example.appcore.data.models.Room;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

public class AddRoomActivity extends AppCompatActivity {

    private EditText edtRoomName, edtSeatCount;
    private Spinner spinnerRoomType;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_add_room);

        edtRoomName = findViewById(R.id.edt_room_name);
        edtSeatCount = findViewById(R.id.edt_seat_count);
        spinnerRoomType = findViewById(R.id.spinner_room_type);
        btnSave = findViewById(R.id.btn_save_room);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Thêm phòng");
        toolbar.setNavigationOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> saveRoomToFirebase());
    }

    private void saveRoomToFirebase() {
        String name = edtRoomName.getText().toString().trim();
        String seatStr = edtSeatCount.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            edtRoomName.setError("Vui lòng nhập tên phòng");
            return;
        }

        if (TextUtils.isEmpty(seatStr)) {
            edtSeatCount.setError("Vui lòng nhập số ghế");
            return;
        }

        int seatCount;
        try {
            seatCount = Integer.parseInt(seatStr);
        } catch (NumberFormatException e) {
            edtSeatCount.setError("Số ghế không hợp lệ");
            return;
        }

        int roomType = spinnerRoomType.getSelectedItemPosition(); // 0, 1, 2

        // Tạo ID ngẫu nhiên
        String roomId = UUID.randomUUID().toString();

        // Tạo object Room
        Room room = new Room(roomId, name, roomType, seatCount);

        // Ghi lên Firebase
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("MovieRooms");
        dbRef.child(roomId).setValue(room)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Đã thêm phòng!", Toast.LENGTH_SHORT).show();
                    finish(); // quay lại danh sách
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Thêm thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
