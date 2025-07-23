package com.example.appcore.ui.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appcore.R;
import com.example.appcore.dao.CategoryDAO;
import com.example.appcore.data.models.Category;
import com.example.appcore.ui.adapter.CategoryAdminAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

// Implement interface của Adapter để xử lý click
public class CategoryActivity extends BaseActivity implements CategoryAdminAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private CategoryAdminAdapter adapter;
    private List<Category> categoryList = new ArrayList<>();

    private CategoryDAO categoryDAO;
    private ValueEventListener categoryListener; // Biến để giữ listener, rất quan trọng

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_category, findViewById(R.id.content_frame));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Thêm Thể Loại Phim Mới");
        }

        categoryDAO = new CategoryDAO();

        initViews();
        setupRecyclerView();
        setupListeners();
    }

    private void setupListeners() {
        // Mở màn hình AddCategoryActivity khi nhấn nút FAB
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(CategoryActivity.this, AddCategoryActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadCategories(); // Bắt đầu lắng nghe dữ liệu khi màn hình được mở
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Dừng lắng nghe khi màn hình không còn hiển thị để tránh rò rỉ bộ nhớ
        if (categoryDAO != null && categoryListener != null) {
            categoryDAO.removeListener(categoryListener);
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewCategories);
        fabAdd = findViewById(R.id.fabAddCategory);
        // Giả sử bạn đã thêm ProgressBar vào file layout với id là 'progressBar'
        // progressBar = findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        adapter = new CategoryAdminAdapter(categoryList);
        adapter.setOnItemClickListener(this); // <<-- GÁN LISTENER CHO ADAPTER
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadCategories() {
        // if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        categoryListener = categoryDAO.getAllCategories(new CategoryDAO.DataFetchListener() {
            @Override
            public void onDataFetched(List<Category> categories) {
                // if (progressBar != null) progressBar.setVisibility(View.GONE);
                categoryList.clear();
                categoryList.addAll(categories);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(DatabaseError error) {
                // if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(CategoryActivity.this, "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onUpdateClick(Category category) {
        showUpdateDialog(category);
    }

    private void showUpdateDialog(Category category) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cập nhật thể loại");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setText(category.getCategoryName());
        input.setPadding(50, 50, 50, 50); // Thêm padding cho dễ nhìn
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty() && !newName.equals(category.getCategoryName())) {
                categoryDAO.updateCategory(category.getCategoryId(), newName, new CategoryDAO.ActionCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(CategoryActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(CategoryActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @Override
    public void onDeleteClick(Category category) {
        showDeleteConfirmationDialog(category);
    }
    private void showDeleteConfirmationDialog(Category category) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa thể loại '" + category.getCategoryName() + "' không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    categoryDAO.deleteCategory(category.getCategoryId(), new CategoryDAO.ActionCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(CategoryActivity.this, "Đã xóa thể loại", Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(CategoryActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
