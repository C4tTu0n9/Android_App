package com.example.appcore.dao;

import androidx.annotation.NonNull;
import com.example.appcore.data.models.Category;
import com.example.appcore.data.remote.FirebaseDataSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryDAO {
    private final DatabaseReference databaseReference;

    public CategoryDAO() {
        databaseReference = FirebaseDataSource.getInstance().getReference("Categories");
    }

    // Callback chung cho các hành động Create, Update, Delete
    public interface ActionCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    // Callback cho việc lấy dữ liệu
    public interface DataFetchListener {
        void onDataFetched(List<Category> categories);
        void onError(DatabaseError error);
    }

    public ValueEventListener getAllCategories(DataFetchListener listener) {
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Category> categories = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Category category = snapshot.getValue(Category.class);
                    if (category != null) {
                        // Gán key của Realtime Database vào trường categoryId
                        category.setCategoryId(snapshot.getKey());
                        categories.add(category);
                    }
                }
                listener.onDataFetched(categories);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onError(databaseError);
            }
        };
        databaseReference.orderByChild("categoryName").addValueEventListener(eventListener);
        return eventListener; // Trả về listener để Activity có thể gỡ bỏ
    }

    /**
     * Sửa đổi phương thức này
     * Thêm một thể loại mới với ID tuần tự (cat_01, cat_02, ...).
     * @param categoryName Tên thể loại mới.
     * @param callback Callback để thông báo thành công hoặc thất bại.
     */
    public void addCategory(String categoryName, ActionCallback callback) {
        // Lắng nghe một lần duy nhất để lấy dữ liệu hiện tại và tránh xung đột.
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int maxIdNumber = 0;
                // Vòng lặp để tìm ID lớn nhất hiện có
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String currentId = snapshot.getKey();
                    if (currentId != null && currentId.startsWith("cat_")) {
                        try {
                            // Tách lấy phần số từ ID (ví dụ: "cat_05" -> "05")
                            String numberPart = currentId.substring(4);
                            int idNumber = Integer.parseInt(numberPart);
                            // Cập nhật số lớn nhất
                            if (idNumber > maxIdNumber) {
                                maxIdNumber = idNumber;
                            }
                        } catch (NumberFormatException e) {
                            // Bỏ qua các ID không đúng định dạng và ghi log
                            System.err.println("ID không hợp lệ: " + currentId);
                        }
                    }
                }

                // Tạo ID mới bằng cách tăng ID lớn nhất lên 1
                int newIdNumber = maxIdNumber + 1;
                // Định dạng ID mới thành chuỗi "cat_XX" (ví dụ: 1 -> "cat_01", 10 -> "cat_10")
                String newCategoryId = String.format("cat_%02d", newIdNumber);

                // Tạo đối tượng và lưu vào Firebase
                Category category = new Category(newCategoryId, categoryName);
                databaseReference.child(newCategoryId).setValue(category)
                        .addOnSuccessListener(aVoid -> callback.onSuccess())
                        .addOnFailureListener(callback::onFailure);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Gọi callback thất bại nếu không thể đọc dữ liệu
                callback.onFailure(databaseError.toException());
            }
        });
    }
    public void updateCategory(String categoryId, String newName, ActionCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("categoryName", newName);

        databaseReference.child(categoryId).updateChildren(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    public void deleteCategory(String categoryId, ActionCallback callback) {
        databaseReference.child(categoryId).removeValue()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    // Phương thức để gỡ bỏ listener
    public void removeListener(ValueEventListener listener) {
        if (listener != null) {
            databaseReference.removeEventListener(listener);
        }
    }
}