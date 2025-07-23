package com.example.appcore.ui.common.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appcore.R;

import java.util.List;

import lombok.Setter;

/**
 * Một Adapter có thể tái sử dụng cho ViewPager2 hoặc RecyclerView để hiển thị một slider hình ảnh.
 * @param <T> Kiểu dữ liệu của đối tượng trong danh sách (ví dụ: Movie, Actor, String).
 */
public class GenericSliderAdapter<T> extends RecyclerView.Adapter<GenericSliderAdapter.GenericViewHolder> {

    private Context context;
    private List<T> items;
    private int layoutResId;
    private int imageViewId;
    private ImageUrlExtractor<T> imageUrlExtractor;
    @Setter
    private OnItemClickListener<T> onItemClickListener;

    /**
     * Interface để định nghĩa cách lấy URL hình ảnh từ một đối tượng item.
     * @param <T>
     */
    public interface ImageUrlExtractor<T> {
        String getUrl(T item);
    }

    /**
     * Interface cho sự kiện click vào một item.
     * @param <T>
     */
    public interface OnItemClickListener<T> {
        void onItemClick(T item, int position);
    }

    /**
     * Constructor của GenericSliderAdapter.
     *
     * @param context           Context của Activity/Fragment.
     * @param items             Danh sách các đối tượng để hiển thị.
     * @param layoutResId       ID của file layout cho một item (ví dụ: R.layout.item_banner_slide).
     * @param imageViewId       ID của ImageView bên trong layout item (ví dụ: R.id.imageSlide).
     * @param imageUrlExtractor Một implementation của ImageUrlExtractor để chỉ cho adapter cách lấy URL.
     */
    public GenericSliderAdapter(Context context, List<T> items, @LayoutRes int layoutResId, int imageViewId, ImageUrlExtractor<T> imageUrlExtractor) {
        this.context = context;
        this.items = items;
        this.layoutResId = layoutResId;
        this.imageViewId = imageViewId;
        this.imageUrlExtractor = imageUrlExtractor;
    }

    @NonNull
    @Override
    public GenericViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(layoutResId, parent, false);
        return new GenericViewHolder(view, imageViewId);
    }

    @Override
    public void onBindViewHolder(@NonNull GenericViewHolder holder, int position) {
        T currentItem = items.get(position);

        // Sử dụng interface để lấy URL
        String imageUrl = imageUrlExtractor.getUrl(currentItem);

        Glide.with(context)
                .load(imageUrl)
//                    .placeholder(R.drawable.placeholder_image) // Thay bằng ảnh placeholder của bạn
//                    .error(R.drawable.error_image)         // Thay bằng ảnh báo lỗi của bạn
                .into(holder.imageView);

        // Bắt sự kiện click
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(currentItem, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    // ViewHolder không cần là generic
    static class GenericViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public GenericViewHolder(@NonNull View itemView, int imageViewId) {
            super(itemView);
            imageView = itemView.findViewById(imageViewId);
        }
    }
}