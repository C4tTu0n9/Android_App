package com.example.appcore.ui.activities.admin;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import com.example.appcore.data.models.Movie;
import com.example.appcore.data.models.Room;
import com.example.appcore.data.models.ShowTime;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddShowtimeActivity extends AppCompatActivity {

    Spinner spinnerMovie, spinnerRoom;
    EditText edtDate, edtTime;
    Button btnAddShowtime;

    List<Movie> movieList = new ArrayList<>();
    List<Room> roomList = new ArrayList<>();

    DatabaseReference showtimeRef, movieRef, roomRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_showtime);

        spinnerMovie = findViewById(R.id.spinnerMovie);
        spinnerRoom = findViewById(R.id.spinnerRoom);
        edtDate = findViewById(R.id.edtDate);
        edtTime = findViewById(R.id.edtTime);
        btnAddShowtime = findViewById(R.id.btnAddShowtime);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Tạo suất chiếu");
        toolbar.setNavigationOnClickListener(v -> finish());


        showtimeRef = FirebaseDatabase.getInstance().getReference("ShowTimes");
        movieRef = FirebaseDatabase.getInstance().getReference("Movies");
        roomRef = FirebaseDatabase.getInstance().getReference("MovieRooms");

        loadMovies();
        loadRooms();

        edtDate.setOnClickListener(v -> showDatePicker());
        edtTime.setOnClickListener(v -> showTimePicker());

        btnAddShowtime.setOnClickListener(v -> addShowtime());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) ->
                edtDate.setText(String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(this, (view, hourOfDay, minute) ->
                edtTime.setText(String.format("%02d:%02d", hourOfDay, minute)),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE), true).show();
    }

    private void loadMovies() {
        movieRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                movieList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Movie movie = data.getValue(Movie.class);
                    movieList.add(movie);
                }

                List<String> movieNames = new ArrayList<>();
                for (Movie movie : movieList) {
                    movieNames.add(movie.getMovieName());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(AddShowtimeActivity.this,
                        android.R.layout.simple_spinner_item, movieNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerMovie.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadRooms() {
        roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                roomList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Room room = data.getValue(Room.class);
                    roomList.add(room);
                }

                List<String> roomNames = new ArrayList<>();
                for (Room room : roomList) {
                    roomNames.add(room.getRoomName());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(AddShowtimeActivity.this,
                        android.R.layout.simple_spinner_item, roomNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerRoom.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void addShowtime() {
        btnAddShowtime.setEnabled(false); // Disable ngay khi bắt đầu

        int movieIndex = spinnerMovie.getSelectedItemPosition();
        int roomIndex = spinnerRoom.getSelectedItemPosition();

        if (movieIndex < 0 || roomIndex < 0 || edtDate.getText().toString().isEmpty() || edtTime.getText().toString().isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            btnAddShowtime.setEnabled(true); // Enable lại nếu lỗi
            return;
        }

        Movie selectedMovie = movieList.get(movieIndex);
        Room selectedRoom = roomList.get(roomIndex);

        String dateStr = edtDate.getText().toString();
        String timeStr = edtTime.getText().toString();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        try {
            Date dateTime = sdf.parse(dateStr + " " + timeStr);
            long timestamp = dateTime.getTime();

            String newId = showtimeRef.push().getKey();
            ShowTime showTime = new ShowTime(
                    newId,
                    selectedMovie.getImage(),
                    selectedMovie.getMovieId(),
                    selectedRoom.getRoomId(),
                    timestamp,
                    selectedMovie.getMovieName(),
                    selectedRoom.getRoomName() // thêm dòng này để khớp constructor
            );

            showtimeRef.child(newId).setValue(showTime).addOnCompleteListener(task -> {
                btnAddShowtime.setEnabled(true); // Enable lại sau khi xong
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Thêm suất chiếu thành công", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Lỗi khi thêm suất chiếu", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (ParseException e) {
            Toast.makeText(this, "Định dạng ngày/giờ không hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }
}