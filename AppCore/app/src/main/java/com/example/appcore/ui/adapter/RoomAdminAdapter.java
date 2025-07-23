package com.example.appcore.ui.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.appcore.R;
import com.example.appcore.data.models.Room;
import com.example.appcore.ui.activities.admin.EditRoomActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class RoomAdminAdapter extends ArrayAdapter<Room> {
    Activity context;
    int idLayout;
    ArrayList<Room> myList;


    public RoomAdminAdapter(Activity context, int idLayout, ArrayList<Room> myList) {
        super(context, idLayout, myList);
        this.context = context;
        this.idLayout = idLayout;
        this.myList = myList;
    }

    //gọi hàm getView để tiến hàng sắp xếp dữ liệu
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(idLayout, parent, false);
        }
        Room myRoom = myList.get(position);

        TextView tv_roomName = convertView.findViewById(R.id.tv_roomName);
        tv_roomName.setText(myRoom.getRoomName());

        TextView tv_seatCount = convertView.findViewById(R.id.tv_seatCount);
        tv_seatCount.setText("Số ghế: " + myRoom.getSeatCount());

        ImageButton btnDelete = convertView.findViewById(R.id.buttonDeleteRoom);

        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc muốn xóa phòng \"" + myRoom.getRoomName() + "\" không?")
                    .setPositiveButton("Có", (dialog, which) -> {
                        FirebaseDatabase.getInstance()
                                .getReference("MovieRooms")
                                .child(myRoom.getRoomId())
                                .removeValue()
                                .addOnSuccessListener(aVoid ->
                                        Toast.makeText(context, "Đã xóa thành công", Toast.LENGTH_SHORT).show()
                                )
                                .addOnFailureListener(e ->
                                        Toast.makeText(context, "Xóa thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                );
                    })
                    .setNegativeButton("Không", null)
                    .show();
        });


        ImageButton btnEdit = convertView.findViewById(R.id.buttonEdit);
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditRoomActivity.class);
            intent.putExtra("roomId", myRoom.getRoomId());
            context.startActivity(intent);
        });



        return convertView;
    }
}
