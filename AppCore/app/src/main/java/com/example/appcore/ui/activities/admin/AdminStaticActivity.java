package com.example.appcore.ui.activities.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.appcore.R;
import com.example.appcore.dao.TransactionHistoryDAO;
import com.example.appcore.data.models.TransactionHistory;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.example.appcore.ui.activities.admin.BaseActivity;
public class AdminStaticActivity extends BaseActivity {
    private static final String TAG = "AdminStaticActivity";

    // UI Components
    private ImageView btnBack;
    private Spinner spinnerTimeRange;
    private TextView txtTotalRevenue, txtTotalTickets, txtAveragePrice, txtTopMovie;
    private CardView cardTotalRevenue, cardTotalTickets, cardAveragePrice, cardTopMovie;
    private BarChart barChartDaily;
    private LineChart lineChartTrend;
    private PieChart pieChartMovies;
    private ProgressBar progressLoading;

    // Data
    private TransactionHistoryDAO transactionDAO;
    private List<TransactionHistory> allTransactions;
    private NumberFormat currencyFormat;
    private SimpleDateFormat dateFormat;

    // Configuration
    private String[] timeRanges = {"7 ngày qua", "30 ngày qua", "3 tháng qua", "6 tháng qua", "1 năm qua"};
    private int selectedTimeRange = 1; // Default: 30 ngày qua

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_admin_static, findViewById(R.id.content_frame));

        // Đặt tiêu đề cho Toolbar (có thể truy cập vì nó từ BaseActivity)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Thống Kê Doanh Thu");
        }

        initViews();
        initData();
        setupClickListeners();
        setupSpinner();
        loadStatistics();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        spinnerTimeRange = findViewById(R.id.spinner_time_range);
        progressLoading = findViewById(R.id.progress_loading);

        // Summary cards
        txtTotalRevenue = findViewById(R.id.txt_total_revenue);
        txtTotalTickets = findViewById(R.id.txt_total_tickets);
        txtAveragePrice = findViewById(R.id.txt_average_price);
        txtTopMovie = findViewById(R.id.txt_top_movie);

        cardTotalRevenue = findViewById(R.id.card_total_revenue);
        cardTotalTickets = findViewById(R.id.card_total_tickets);
        cardAveragePrice = findViewById(R.id.card_average_price);
        cardTopMovie = findViewById(R.id.card_top_movie);

        // Charts
        barChartDaily = findViewById(R.id.bar_chart_daily);
        lineChartTrend = findViewById(R.id.line_chart_trend);
        pieChartMovies = findViewById(R.id.pie_chart_movies);
    }

    private void initData() {
        transactionDAO = new TransactionHistoryDAO();
        allTransactions = new ArrayList<>();
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        dateFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        // Add click listeners for cards to show detailed views
        cardTotalRevenue.setOnClickListener(v -> showRevenueDetails());
        cardTotalTickets.setOnClickListener(v -> showTicketDetails());
        cardAveragePrice.setOnClickListener(v -> showAverageDetails());
        cardTopMovie.setOnClickListener(v -> showTopMovieDetails());
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, timeRanges);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTimeRange.setAdapter(adapter);
        spinnerTimeRange.setSelection(selectedTimeRange);

        spinnerTimeRange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (selectedTimeRange != position) {
                    selectedTimeRange = position;
                    loadStatistics();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadStatistics() {
        showLoading(true);

        // Calculate time range
        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();

        switch (selectedTimeRange) {
            case 0: // 7 ngày qua
                calendar.add(Calendar.DAY_OF_YEAR, -7);
                break;
            case 1: // 30 ngày qua
                calendar.add(Calendar.DAY_OF_YEAR, -30);
                break;
            case 2: // 3 tháng qua
                calendar.add(Calendar.MONTH, -3);
                break;
            case 3: // 6 tháng qua
                calendar.add(Calendar.MONTH, -6);
                break;
            case 4: // 1 năm qua
                calendar.add(Calendar.YEAR, -1);
                break;
        }
        long startTime = calendar.getTimeInMillis();

        // Load data from database
        transactionDAO.getRevenueByTimeRange(startTime, endTime, new TransactionHistoryDAO.TransactionHistoryCallback() {
            @Override
            public void onSuccess(List<TransactionHistory> transactions) {
                runOnUiThread(() -> {
                    allTransactions = transactions;
                    updateMetrics();
                    updateCharts();
                    showLoading(false);
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Error loading statistics: " + error);
                    Toast.makeText(AdminStaticActivity.this,
                                 "Lỗi tải thống kê: " + error,
                                 Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
            }
        });
    }

    private void showLoading(boolean show) {
        progressLoading.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void updateMetrics() {
        if (allTransactions.isEmpty()) {
            txtTotalRevenue.setText("0 đ");
            txtTotalTickets.setText("0");
            txtAveragePrice.setText("0 đ");
            txtTopMovie.setText("Chưa có dữ liệu");
            return;
        }

        // Calculate metrics
        long totalRevenue = 0;
        int totalTickets = allTransactions.size();
        Map<String, Integer> movieCounts = new HashMap<>();
        Map<String, Long> movieRevenues = new HashMap<>();

        for (TransactionHistory transaction : allTransactions) {
            totalRevenue += transaction.getTotalPrice();

            String movieName = transaction.getMovieName();
            movieCounts.put(movieName, movieCounts.getOrDefault(movieName, 0) + 1);
            movieRevenues.put(movieName, movieRevenues.getOrDefault(movieName, 0L) + transaction.getTotalPrice());
        }

        // Update UI
        txtTotalRevenue.setText(formatCurrency(totalRevenue));
        txtTotalTickets.setText(String.valueOf(totalTickets));

        long averagePrice = totalTickets > 0 ? totalRevenue / totalTickets : 0;
        txtAveragePrice.setText(formatCurrency(averagePrice));

        // Find top movie by revenue
        String topMovie = "Chưa có dữ liệu";
        long maxRevenue = 0;
        for (Map.Entry<String, Long> entry : movieRevenues.entrySet()) {
            if (entry.getValue() > maxRevenue) {
                maxRevenue = entry.getValue();
                topMovie = entry.getKey();
            }
        }
        txtTopMovie.setText(topMovie);
    }

    private void updateCharts() {
        updateDailyRevenueChart();
        updateTrendChart();
        updateMovieDistributionChart();
    }

    private void updateDailyRevenueChart() {
        Map<String, Long> dailyRevenue = new HashMap<>();

        for (TransactionHistory transaction : allTransactions) {
            String date = dateFormat.format(new Date(transaction.getTimestamp()));
            dailyRevenue.put(date, dailyRevenue.getOrDefault(date, 0L) + transaction.getTotalPrice());
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int index = 0;

        for (Map.Entry<String, Long> entry : dailyRevenue.entrySet()) {
            entries.add(new BarEntry(index, entry.getValue()));
            labels.add(entry.getKey());
            index++;
        }

        if (entries.isEmpty()) {
            barChartDaily.clear();
            return;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Doanh thu theo ngày");
        dataSet.setColor(Color.parseColor("#3182CE"));
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barChartDaily.setData(barData);

        // Customize chart
        barChartDaily.getDescription().setEnabled(false);
        barChartDaily.setDrawGridBackground(false);
        barChartDaily.getAxisRight().setEnabled(false);
        barChartDaily.getLegend().setEnabled(false);

        XAxis xAxis = barChartDaily.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setGranularity(1f);

        barChartDaily.invalidate();
    }

    private void updateTrendChart() {
        // Group by week for trend analysis
        Map<String, Long> weeklyRevenue = new HashMap<>();
        SimpleDateFormat weekFormat = new SimpleDateFormat("ww/yyyy", Locale.getDefault());

        for (TransactionHistory transaction : allTransactions) {
            String week = weekFormat.format(new Date(transaction.getTimestamp()));
            weeklyRevenue.put(week, weeklyRevenue.getOrDefault(week, 0L) + transaction.getTotalPrice());
        }

        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int index = 0;

        for (Map.Entry<String, Long> entry : weeklyRevenue.entrySet()) {
            entries.add(new Entry(index, entry.getValue()));
            labels.add(entry.getKey());
            index++;
        }

        if (entries.isEmpty()) {
            lineChartTrend.clear();
            return;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Xu hướng doanh thu");
        dataSet.setColor(Color.parseColor("#38A169"));
        dataSet.setCircleColor(Color.parseColor("#38A169"));
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(5f);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(10f);

        LineData lineData = new LineData(dataSet);
        lineChartTrend.setData(lineData);

        // Customize chart
        lineChartTrend.getDescription().setEnabled(false);
        lineChartTrend.setDrawGridBackground(false);
        lineChartTrend.getAxisRight().setEnabled(false);
        lineChartTrend.getLegend().setEnabled(false);

        XAxis xAxis = lineChartTrend.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setGranularity(1f);

        lineChartTrend.invalidate();
    }

    private void updateMovieDistributionChart() {
        Map<String, Long> movieRevenues = new HashMap<>();

        for (TransactionHistory transaction : allTransactions) {
            String movieName = transaction.getMovieName();
            movieRevenues.put(movieName, movieRevenues.getOrDefault(movieName, 0L) + transaction.getTotalPrice());
        }

        List<PieEntry> entries = new ArrayList<>();
        int[] colors = {
            Color.parseColor("#3182CE"),
            Color.parseColor("#38A169"),
            Color.parseColor("#E53E3E"),
            Color.parseColor("#D69E2E"),
            Color.parseColor("#805AD5"),
            Color.parseColor("#DD6B20")
        };

        for (Map.Entry<String, Long> entry : movieRevenues.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        if (entries.isEmpty()) {
            pieChartMovies.clear();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        PieData pieData = new PieData(dataSet);
        pieChartMovies.setData(pieData);

        // Customize chart
        pieChartMovies.getDescription().setEnabled(false);
        pieChartMovies.setDrawHoleEnabled(true);
        pieChartMovies.setHoleRadius(40f);
        pieChartMovies.setTransparentCircleRadius(45f);
        pieChartMovies.setDrawCenterText(true);
        pieChartMovies.setCenterText("Phân bố\nDoanh thu");
        pieChartMovies.setCenterTextSize(14f);
        pieChartMovies.getLegend().setEnabled(true);

        pieChartMovies.invalidate();
    }

    private String formatCurrency(long amount) {
        return String.format(Locale.getDefault(), "%,d đ", amount);
    }

    // Detail view methods
    private void showRevenueDetails() {
        Toast.makeText(this, "Chi tiết doanh thu", Toast.LENGTH_SHORT).show();
        // TODO: Implement detailed revenue view
    }

    private void showTicketDetails() {
        Toast.makeText(this, "Chi tiết giao dịch", Toast.LENGTH_SHORT).show();
        // TODO: Implement detailed ticket view
    }

    private void showAverageDetails() {
        Toast.makeText(this, "Chi tiết doanh thu trung bình", Toast.LENGTH_SHORT).show();
        // TODO: Implement detailed average view
    }

    private void showTopMovieDetails() {
        Toast.makeText(this, "Chi tiết phim bán chạy", Toast.LENGTH_SHORT).show();
        // TODO: Implement detailed top movie view
    }
}

