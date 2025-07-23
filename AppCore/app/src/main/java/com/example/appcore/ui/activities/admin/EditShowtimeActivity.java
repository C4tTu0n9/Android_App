    package com.example.appcore.ui.activities.admin;

    import android.content.Intent;
    import android.os.Bundle;
    import android.widget.ArrayAdapter;
    import android.widget.Button;
    import android.widget.EditText;
    import android.widget.Spinner;
    import android.widget.Toast;

    import androidx.annotation.NonNull;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.appcompat.widget.Toolbar;

    import com.example.appcore.R;
    import com.example.appcore.data.models.ShowTime;
    import com.google.firebase.database.DataSnapshot;
    import com.google.firebase.database.DatabaseError;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;
    import com.google.firebase.database.ValueEventListener;

    import java.text.SimpleDateFormat;
    import java.util.ArrayList;
    import java.util.Calendar;
    import java.util.Date;
    import java.util.List;

    import android.app.DatePickerDialog;
    import android.app.TimePickerDialog;

    public class EditShowtimeActivity extends AppCompatActivity {

        private Spinner spinnerMovie, spinnerRoom;
        private EditText edtDate, edtTime;

        private Button btnUpdate;

        private ShowTime showTime;

        private List<String> movieNames = new ArrayList<>();
        private List<String> movieIds = new ArrayList<>();

        private List<String> roomNames = new ArrayList<>();
        private List<String> roomIds = new ArrayList<>();

        private DatabaseReference showTimeRef;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_edit_showtime);

            // Khởi tạo các View
            spinnerMovie = findViewById(R.id.spinnerMovie);
            spinnerRoom = findViewById(R.id.spinnerRoom);
            btnUpdate = findViewById(R.id.btnUpdateShowtime);
            edtDate = findViewById(R.id.edtDate);
            edtTime = findViewById(R.id.edtTime);

            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Cập nhật suất chiếu");
            toolbar.setNavigationOnClickListener(v -> finish());



            // Nhận dữ liệu showTime được truyền sang từ AdminShowtimesFragment
            showTime = (ShowTime) getIntent().getSerializableExtra("showTime");
            if (showTime == null) {
                Toast.makeText(this, "Dữ liệu lịch chiếu không tồn tại", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Tham chiếu Firebase
            showTimeRef = FirebaseDatabase.getInstance().getReference("ShowTimes").child(showTime.getShowtimeId());

            // Load movie và room
            loadMovies();
            loadRooms();

            // Gán dữ liệu cho edtDate, edtTime, edtPrice ngay sau khi nhận showTime.
            if (showTime != null) {
                // Chuyển timestamp về chuỗi ngày và giờ
                long timestamp = showTime.getShowDateTime();
                Date date = new Date(timestamp);
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                edtDate.setText(dateFormat.format(date));
                edtTime.setText(timeFormat.format(date));
            }

            // Xử lý nhập ngày và giờ
            edtDate.setOnClickListener(v -> {
                Calendar calendar = Calendar.getInstance();
                new DatePickerDialog(EditShowtimeActivity.this, (view, year, month, dayOfMonth) -> {
                    String dateStr = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);
                    edtDate.setText(dateStr);
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            });

            edtTime.setOnClickListener(v -> {
                Calendar calendar = Calendar.getInstance();
                new TimePickerDialog(EditShowtimeActivity.this, (view, hourOfDay, minute) -> {
                    String timeStr = String.format("%02d:%02d", hourOfDay, minute);
                    edtTime.setText(timeStr);
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
            });


            // Xử lý cập nhật
            btnUpdate.setOnClickListener(view -> {
                int selectedMovieIndex = spinnerMovie.getSelectedItemPosition();
                int selectedRoomIndex = spinnerRoom.getSelectedItemPosition();

                String selectedMovieId = movieIds.get(selectedMovieIndex);
                String selectedMovieName = movieNames.get(selectedMovieIndex);

                String selectedRoomId = roomIds.get(selectedRoomIndex);
                String selectedRoomName = roomNames.get(selectedRoomIndex);

                // Cập nhật dữ liệu
                showTime.setMovieId(selectedMovieId);
                showTime.setMovieName(selectedMovieName);
                showTime.setRoomId(selectedRoomId);
                showTime.setRoomName(selectedRoomName);


                try {
                    String dateStr = edtDate.getText().toString();  // ví dụ: "22/07/2025"
                    String timeStr = edtTime.getText().toString();  // ví dụ: "14:00"
                    String dateTimeStr = dateStr + " " + timeStr;   // "22/07/2025 14:00"

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                    Date date = sdf.parse(dateTimeStr);
                    long timestamp = date.getTime();

                    showTime.setShowDateTime(timestamp);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Định dạng ngày giờ không hợp lệ!", Toast.LENGTH_SHORT).show();
                }




                // Ghi lên Firebase
                showTimeRef.setValue(showTime)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(EditShowtimeActivity.this, "Cập nhật lịch chiếu thành công", Toast.LENGTH_SHORT).show();
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("isUpdated", true);
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(EditShowtimeActivity.this, "Lỗi khi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            });
        }

        private void loadMovies() {
            DatabaseReference moviesRef = FirebaseDatabase.getInstance().getReference("Movies");
            moviesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    movieNames.clear();
                    movieIds.clear();

                    for (DataSnapshot movieSnapshot : snapshot.getChildren()) {
                        String movieId = movieSnapshot.getKey();
                        String movieName = movieSnapshot.child("movieName").getValue(String.class);

                        movieIds.add(movieId);
                        movieNames.add(movieName);
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(EditShowtimeActivity.this, android.R.layout.simple_spinner_item, movieNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerMovie.setAdapter(adapter);

                    // Set spinner to current movie
                    int index = movieNames.indexOf(showTime.getMovieName());
                    if (index >= 0) spinnerMovie.setSelection(index);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(EditShowtimeActivity.this, "Không thể tải danh sách phim", Toast.LENGTH_SHORT).show();

                }
            });
        }

        private void loadRooms() {
            DatabaseReference roomsRef = FirebaseDatabase.getInstance().getReference("MovieRooms");
            roomsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    roomNames.clear();
                    roomIds.clear();

                    for (DataSnapshot roomSnapshot : snapshot.getChildren()) {
                        String roomId = roomSnapshot.getKey();
                        String roomName = roomSnapshot.child("roomName").getValue(String.class);

                        roomIds.add(roomId);
                        roomNames.add(roomName);
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(EditShowtimeActivity.this, android.R.layout.simple_spinner_item, roomNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerRoom.setAdapter(adapter);

                    // Set spinner to current room
                    int index = roomNames.indexOf(showTime.getRoomName());
                    if (index >= 0) spinnerRoom.setSelection(index);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(EditShowtimeActivity.this, "Không thể tải danh sách phòng", Toast.LENGTH_SHORT).show();
                }
            });
        }



        @Override
        public void onResume() {
            super.onResume();
        }
    }
