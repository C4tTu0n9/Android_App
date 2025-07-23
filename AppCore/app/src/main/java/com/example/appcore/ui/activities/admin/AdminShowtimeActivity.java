package com.example.appcore.ui.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.appcore.R;
import com.example.appcore.data.models.ShowTime;
import com.example.appcore.ui.adapter.ShowtimeAdminAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdminShowtimeActivity extends AppCompatActivity {
    ListView listViewShowtime;
    ArrayList<ShowTime> showTimeList;
    ShowtimeAdminAdapter adapter;
    DatabaseReference showtimeRef, movieRef, roomRef;

    FloatingActionButton fabAddShowtime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_showtime);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        showtimeRef = database.getReference("ShowTimes");
        movieRef = database.getReference("Movies");
        roomRef = database.getReference("MovieRooms");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Danh sách suất chiếu");
        toolbar.setNavigationOnClickListener(v -> finish());


        listViewShowtime = findViewById(R.id.listViewShowtime);
        showTimeList = new ArrayList<>();
        adapter = new ShowtimeAdminAdapter(this, R.layout.item_showtime_admin, showTimeList);
        listViewShowtime.setAdapter(adapter);

        fabAddShowtime = findViewById(R.id.fabAddShowtime);
        fabAddShowtime.setOnClickListener(v -> {
            Intent intent = new Intent(AdminShowtimeActivity.this, AddShowtimeActivity.class);
            startActivity(intent);
        });

        loadShowtimeData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadShowtimeData();
    }

    private void loadShowtimeData() {
        showtimeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<ShowTime> tempList = new ArrayList<>();
                int total = (int) snapshot.getChildrenCount();
                if (total == 0) {
                    showTimeList.clear();
                    adapter.notifyDataSetChanged();
                    return;
                }
                final int[] loadedCount = {0};

                for (DataSnapshot data : snapshot.getChildren()) {
                    ShowTime showTime = data.getValue(ShowTime.class);
                    if (showTime == null) {
                        loadedCount[0]++;
                        if (loadedCount[0] == total) {
                            showTimeList.clear();
                            showTimeList.addAll(tempList);
                            adapter.notifyDataSetChanged();
                        }
                        continue;
                    }

                    String movieId = showTime.getMovieId();
                    String roomId = showTime.getRoomId();

                    movieRef.child(movieId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot movieSnap) {
                            if (movieSnap.exists()) {
                                String movieName = movieSnap.child("movieName").getValue(String.class);
                                showTime.setMovieName(movieName);
                            }
                            roomRef.child(roomId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot roomSnap) {
                                    if (roomSnap.exists()) {
                                        String roomName = roomSnap.child("roomName").getValue(String.class);
                                        showTime.setRoomName(roomName);
                                    }
                                    tempList.add(showTime);
                                    loadedCount[0]++;
                                    if (loadedCount[0] == total) {
                                        showTimeList.clear();
                                        showTimeList.addAll(tempList);
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    loadedCount[0]++;
                                    if (loadedCount[0] == total) {
                                        showTimeList.clear();
                                        showTimeList.addAll(tempList);
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            });
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            loadedCount[0]++;
                            if (loadedCount[0] == total) {
                                showTimeList.clear();
                                showTimeList.addAll(tempList);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminShowtimeActivity.this, "Lỗi khi tải dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
