package com.example.appcore.ui.activities.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appcore.R;
import com.example.appcore.dao.CategoryDAO;
import com.google.android.material.textfield.TextInputEditText;

public class AddCategoryActivity extends AppCompatActivity {

    private TextInputEditText editTextCategoryName;
    private Button buttonSaveCategory;
    private CategoryDAO categoryDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);

        Toolbar toolbar = findViewById(R.id.toolbar_add_category);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Thêm thể loại mới");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Hiển thị nút back
        }

        categoryDAO = new CategoryDAO();
        editTextCategoryName = findViewById(R.id.editTextCategoryName);
        buttonSaveCategory = findViewById(R.id.buttonSaveCategory);

        buttonSaveCategory.setOnClickListener(v -> saveCategory());
    }

    private void saveCategory() {
        String categoryName = editTextCategoryName.getText().toString().trim();

        if (TextUtils.isEmpty(categoryName)) {
            Toast.makeText(this, "Vui lòng nhập tên thể loại", Toast.LENGTH_SHORT).show();
            return;
        }

        // Vô hiệu hóa nút để tránh double-click
        buttonSaveCategory.setEnabled(false);

        categoryDAO.addCategory(categoryName, new CategoryDAO.ActionCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(AddCategoryActivity.this, "Thêm thể loại thành công!", Toast.LENGTH_SHORT).show();
                finish(); // Đóng Activity sau khi thêm thành công
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AddCategoryActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                buttonSaveCategory.setEnabled(true); // Kích hoạt lại nút nếu có lỗi
            }
        });
    }

    // Xử lý khi người dùng nhấn nút back trên toolbar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}