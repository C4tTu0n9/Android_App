package com.example.appcore.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.appcore.R;
import com.example.appcore.data.models.Room;
import com.example.appcore.data.models.ShowTime;
import com.example.appcore.ui.activities.admin.EditRoomActivity;
import com.example.appcore.ui.activities.admin.EditShowtimeActivity;

import java.util.ArrayList;
import android.widget.Toast;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

public class ShowtimeAdminAdapter extends ArrayAdapter<ShowTime> {
    Activity context;
    int idLayout;
    ArrayList<ShowTime> myList;

    public ShowtimeAdminAdapter(Activity context, int idLayout, ArrayList<ShowTime> myList) {
        super(context, idLayout, myList);
        this.idLayout = idLayout;
        this.myList = myList;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(idLayout, parent, false);
        }
        ShowTime myShowtime = myList.get(position);

        ImageView imageViewShowtime = convertView.findViewById(R.id.imageViewShowtime);
        Glide.with(context)
                .load(myShowtime.getPoster())
                .into(imageViewShowtime);


        TextView tv_movieName = convertView.findViewById(R.id.tv_movieName);
        tv_movieName.setText("Tên phim: " + myShowtime.getMovieName());


        TextView textViewDate = convertView.findViewById(R.id.textViewDate);
        textViewDate.setText("Ngày chiếu: " +myShowtime.getShowDate());

        TextView textViewTime = convertView.findViewById(R.id.textViewTime);
        textViewTime.setText("Giờ chiếu: " +myShowtime.getShowTime());

        TextView textViewRoom = convertView.findViewById(R.id.textViewRoom);
        textViewRoom.setText("Phòng: " + myShowtime.getRoomName());

        ImageButton btnEdit = convertView.findViewById(R.id.btn_edit_showtime);
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditShowtimeActivity.class);
            intent.putExtra("showTime", myShowtime);
            context.startActivity(intent);
        });

        ImageButton btnDelete = convertView.findViewById(R.id.btn_delete_showtime);
        btnDelete.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(context)
                .setTitle("Xác nhận xoá")
                .setMessage("Bạn có chắc muốn xoá suất chiếu này không?")
                .setPositiveButton("Có", (dialog, which) -> {
                    DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("ShowTimes");
                    databaseRef.child(myShowtime.getShowtimeId()).removeValue()
                        .addOnSuccessListener(aVoid -> {
                            myList.remove(position);
                            notifyDataSetChanged();
                            Toast.makeText(context, "Xoá thành công!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Xoá thất bại!", Toast.LENGTH_SHORT).show();
                        });
                })
                .setNegativeButton("Không", null)
                .show();
        });

        return convertView;


    }
}
