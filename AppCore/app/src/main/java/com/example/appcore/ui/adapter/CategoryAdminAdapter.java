package com.example.appcore.ui.adapter; // Bạn có thể đặt trong package adapters

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appcore.R;
import com.example.appcore.data.models.Category;

import java.util.List;

public class CategoryAdminAdapter extends RecyclerView.Adapter<CategoryAdminAdapter.CategoryViewHolder> {

    private final List<Category> categoryList;
    private Context context;
    private OnItemClickListener listener;


    public CategoryAdminAdapter(Context context, List<Category> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
    }

    public interface OnItemClickListener {
        void onUpdateClick(Category category);
        void onDeleteClick(Category category);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public CategoryAdminAdapter(List<Category> categoryList) {
        this.categoryList = categoryList;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Tạo view cho mỗi item từ file layout item_category_admin.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_admin, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        // Lấy dữ liệu từ danh sách tại vị trí 'position'
        Category currentCategory = categoryList.get(position);

        // Gán dữ liệu vào các view trong ViewHolder
        holder.textViewCategoryName.setText("Tên thể loại: " + currentCategory.getCategoryName());
        holder.textViewCategoryId.setText("Mã thể loại: " + currentCategory.getCategoryId());

        // Gắn listener cho các nút
        holder.buttonUpdate.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUpdateClick(currentCategory);
            }
        });

        holder.buttonDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(currentCategory);
            }
        });
    }

    @Override
    public int getItemCount() {
        // Trả về số lượng item trong danh sách
        return categoryList.size();
    }

    // Lớp ViewHolder chứa các view của một item
    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView textViewCategoryName;
        TextView textViewCategoryId;
        ImageButton buttonUpdate;
        ImageButton buttonDelete;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewCategoryName = itemView.findViewById(R.id.textViewCategoryName);
            textViewCategoryId = itemView.findViewById(R.id.textViewCategoryId);
            buttonUpdate = itemView.findViewById(R.id.buttonUpdate);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}