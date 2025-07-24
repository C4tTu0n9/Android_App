package com.example.appcore.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appcore.R;
import com.example.appcore.data.models.BookedSeat;
import com.example.appcore.data.models.Movie;
import com.example.appcore.data.models.Seat;
import com.example.appcore.data.models.ShowTime;
import com.example.appcore.data.models.TransactionHistory;
import com.example.appcore.ui.adapter.DateSelectionAdapter;
import com.example.appcore.ui.adapter.SeatAdapter;
import com.example.appcore.ui.adapter.TimeSelectionAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SeatListActivity extends AppCompatActivity implements 
        DateSelectionAdapter.OnDateSelectedListener,
        TimeSelectionAdapter.OnTimeSelectedListener,
        SeatAdapter.OnSeatSelectedListener {

    private static final String TAG = "SeatListActivity";
    
    // UI Components
    private ImageView backBtn;
    private TextView titleText;
    private RecyclerView dateRecyclerview, timeRecyclerview, seatRecyclerview;
    private TextView priceTxt, numberSelectedTxt;
    private Button bookButton;
    
    // Adapters
    private DateSelectionAdapter dateAdapter;
    private TimeSelectionAdapter timeAdapter;
    private SeatAdapter seatAdapter;
    
    // Data
    private String movieId;
    private Movie currentMovie;
    private List<Date> availableDates;
    private List<ShowTime> currentShowTimes;
    private List<Seat> currentSeats;
    private Date selectedDate;
    private ShowTime selectedShowTime;
    private int selectedSeatCount = 0;
    private int totalPrice = 0;
    
    // Firebase
    private DatabaseReference movieRef, showTimeRef, bookedSeatRef, transactionRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat_list);
        
        initViews();
        initFirebase();
        getUserId();
        getMovieFromIntent();
        setupRecyclerViews();
        setupClickListeners();
        loadAvailableDates();
    }
    
    private void initViews() {
        backBtn = findViewById(R.id.backBtn);
        titleText = findViewById(R.id.textView);
        dateRecyclerview = findViewById(R.id.dateRecyclerview);
        timeRecyclerview = findViewById(R.id.timeRecyclerview);
        seatRecyclerview = findViewById(R.id.seatRecyclerview);
        priceTxt = findViewById(R.id.priceTxt);
        numberSelectedTxt = findViewById(R.id.numberSelectedTxt);
        bookButton = findViewById(R.id.button);
        
        // Initial states
        updatePriceDisplay();
        updateSelectedSeatsDisplay();
    }
    
    private void initFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        movieRef = database.getReference("Movies");
        showTimeRef = database.getReference("ShowTimes");
        bookedSeatRef = database.getReference("BookedSeats");
        transactionRef = database.getReference("transaction_history");
    }
    
    private void getUserId() {
        SharedPreferences prefs = getSharedPreferences("login_prefs", Context.MODE_PRIVATE);
        userId = prefs.getString("userId", "");
        if (userId.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập để đặt vé", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private void getMovieFromIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("MOVIE_ID")) {
            movieId = intent.getStringExtra("MOVIE_ID");
            loadMovieDetails();
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin phim", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private void loadMovieDetails() {
        movieRef.child(movieId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentMovie = snapshot.getValue(Movie.class);
                    if (currentMovie != null) {
                        titleText.setText("Đặt vé - " + currentMovie.getMovieName());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading movie: " + error.getMessage());
            }
        });
    }
    
    private void setupRecyclerViews() {
        // Date RecyclerView
        availableDates = new ArrayList<>();
        dateAdapter = new DateSelectionAdapter(this, availableDates, this);
        dateRecyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        dateRecyclerview.setAdapter(dateAdapter);
        
        // Time RecyclerView
        currentShowTimes = new ArrayList<>();
        timeAdapter = new TimeSelectionAdapter(this, currentShowTimes, this);
        timeRecyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        timeRecyclerview.setAdapter(timeAdapter);
        
        // Seat RecyclerView (Grid layout for cinema seating)
        currentSeats = new ArrayList<>();
        seatAdapter = new SeatAdapter(this, currentSeats, this);
        seatRecyclerview.setLayoutManager(new GridLayoutManager(this, 10)); // 10 seats per row
        seatRecyclerview.setAdapter(seatAdapter);
    }
    
    private void setupClickListeners() {
        backBtn.setOnClickListener(v -> finish());
        
        bookButton.setOnClickListener(v -> {
            if (validateBooking()) {
                processBooking();
            }
        });
    }
    
    private void loadAvailableDates() {
        // Load next 7 days
        Calendar calendar = Calendar.getInstance();
        availableDates.clear();
        
        for (int i = 0; i < 7; i++) {
            availableDates.add(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        dateAdapter.notifyDataSetChanged();
        
        // Auto select first date
        if (!availableDates.isEmpty()) {
            selectedDate = availableDates.get(0);
            dateAdapter.setSelectedPosition(0);
            loadShowTimesForDate(selectedDate);
        }
    }

    @Override
    public void onDateSelected(Date date, int position) {
        selectedDate = date;
        selectedShowTime = null; // Reset selected showtime
        seatAdapter.clearSelectedSeats(); // Clear selected seats
        updatePriceDisplay();
        updateSelectedSeatsDisplay();
        
        loadShowTimesForDate(date);
    }

    private void loadShowTimesForDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String selectedDateStr = dateFormat.format(date);
        
        showTimeRef.orderByChild("movieId").equalTo(movieId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<ShowTime> showTimes = new ArrayList<>();
                        
                        for (DataSnapshot data : snapshot.getChildren()) {
                            ShowTime showTime = data.getValue(ShowTime.class);
                            if (showTime != null && showTime.getShowDate().equals(selectedDateStr)) {
                                // Load room name
                                loadRoomNameForShowTime(showTime, showTimes);
                            }
                        }
                        
                        // Update adapter when all room names are loaded
                        timeAdapter.updateShowTimes(showTimes);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error loading showtimes: " + error.getMessage());
                    }
                });
    }
    
    private void loadRoomNameForShowTime(ShowTime showTime, List<ShowTime> showTimes) {
        DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference("MovieRooms");
        roomRef.child(showTime.getRoomId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String roomName = snapshot.child("roomName").getValue(String.class);
                    showTime.setRoomName(roomName);
                }
                showTimes.add(showTime);
                timeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showTimes.add(showTime);
                timeAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onTimeSelected(ShowTime showTime, int position) {
        selectedShowTime = showTime;
        seatAdapter.clearSelectedSeats(); // Clear previous seat selection
        updatePriceDisplay();
        updateSelectedSeatsDisplay();
        
        loadSeatsForShowTime(showTime);
    }
    
    private void loadSeatsForShowTime(ShowTime showTime) {
        // Generate seats based on room capacity
        DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference("MovieRooms");
        roomRef.child(showTime.getRoomId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Integer seatCount = snapshot.child("seatCount").getValue(Integer.class);
                    if (seatCount != null) {
                        generateSeats(seatCount);
                        loadBookedSeats(showTime.getShowtimeId());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading room details: " + error.getMessage());
            }
        });
    }
    
    private void generateSeats(int totalSeats) {
        currentSeats.clear();
        
        // Create seats in rows (A, B, C, etc.) with 10 seats per row
        int seatsPerRow = 10;
        int numRows = (int) Math.ceil((double) totalSeats / seatsPerRow);
        
        for (int row = 0; row < numRows; row++) {
            char rowLetter = (char) ('A' + row);
            int seatsInThisRow = Math.min(seatsPerRow, totalSeats - (row * seatsPerRow));
            
            for (int seatNum = 1; seatNum <= seatsInThisRow; seatNum++) {
                Seat seat = new Seat();
                seat.setRow(rowLetter);
                seat.setNumber(seatNum);
                seat.setStatus(SeatAdapter.STATUS_AVAILABLE);
                seat.setPrice((int) currentMovie.getPrice());
                currentSeats.add(seat);
            }
        }
        
        seatAdapter.notifyDataSetChanged();
    }
    
    private void loadBookedSeats(String showtimeId) {
        bookedSeatRef.orderByChild("showtimeId").equalTo(showtimeId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> bookedSeatKeys = new ArrayList<>();
                        
                        for (DataSnapshot data : snapshot.getChildren()) {
                            BookedSeat bookedSeat = data.getValue(BookedSeat.class);
                            if (bookedSeat != null) {
                                bookedSeatKeys.add(bookedSeat.getSeatKey());
                            }
                        }
                        
                        seatAdapter.updateBookedSeats(bookedSeatKeys);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error loading booked seats: " + error.getMessage());
                    }
                });
    }

    @Override
    public void onSeatSelected(Seat seat, boolean isSelected) {
        if (isSelected) {
            selectedSeatCount++;
            totalPrice += seat.getPrice();
        } else {
            selectedSeatCount--;
            totalPrice -= seat.getPrice();
        }
        
        updatePriceDisplay();
        updateSelectedSeatsDisplay();
    }
    
    private void updatePriceDisplay() {
        priceTxt.setText(String.format(Locale.getDefault(), "%,d đ", totalPrice));
    }
    
    private void updateSelectedSeatsDisplay() {
        numberSelectedTxt.setText(selectedSeatCount + " ghế đã chọn");
        bookButton.setEnabled(selectedSeatCount > 0 && selectedShowTime != null);
    }
    
    private boolean validateBooking() {
        if (selectedShowTime == null) {
            Toast.makeText(this, "Vui lòng chọn suất chiếu", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (selectedSeatCount == 0) {
            Toast.makeText(this, "Vui lòng chọn ít nhất 1 ghế", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        return true;
    }
    
    private void processBooking() {
        // Save booked seats to Firebase
        List<String> selectedSeatKeys = seatAdapter.getSelectedSeats();
        
        for (String seatKey : selectedSeatKeys) {
            // Find the seat object
            Seat seat = findSeatByKey(seatKey);
            if (seat != null) {
                BookedSeat bookedSeat = new BookedSeat(
                    selectedShowTime.getShowtimeId(),
                    selectedShowTime.getRoomId(),
                    seat,
                    userId
                );
                
                // Save to Firebase
                String bookedSeatId = bookedSeatRef.push().getKey();
                if (bookedSeatId != null) {
                    bookedSeatRef.child(bookedSeatId).setValue(bookedSeat);
                }
            }
        }
        
        // Create transaction history
        createTransactionHistory(selectedSeatKeys);
        
        Toast.makeText(this, "Đặt vé thành công!", Toast.LENGTH_SHORT).show();
        finish();
    }
    
    private Seat findSeatByKey(String seatKey) {
        for (Seat seat : currentSeats) {
            if ((seat.getRow() + String.valueOf(seat.getNumber())).equals(seatKey)) {
                return seat;
            }
        }
        return null;
    }
    
    private void createTransactionHistory(List<String> selectedSeatKeys) {
        TransactionHistory transaction = new TransactionHistory();
        transaction.setMovieName(currentMovie.getMovieName());
        transaction.setShowDate(selectedShowTime.getShowDate());
        transaction.setShowTime(selectedShowTime.getShowTime());
        transaction.setRoomName(selectedShowTime.getRoomName());
        transaction.setSelectedSeats(selectedSeatKeys);
        transaction.setTotalPrice(totalPrice);
        transaction.setTimestamp(System.currentTimeMillis());
        transaction.setPoster(currentMovie.getImage());
        
        // Save to Firebase with userId as additional field
        String transactionId = transactionRef.push().getKey();
        if (transactionId != null) {
            // Create a map to include userId
            DatabaseReference userTransactionRef = transactionRef.child(transactionId);
            userTransactionRef.setValue(transaction);
            userTransactionRef.child("userId").setValue(userId);
        }
    }
} 