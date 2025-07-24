package com.example.appcore.ui.activities.profile;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.appcore.R;
import com.example.appcore.dao.UserDAO;
import com.example.appcore.ui.activities.changepassword.ChangePasswordFragment;
import com.example.appcore.ui.activities.login.LoginActivity;

import com.example.appcore.ui.activities.updateProfile.UpdateProfileFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private TextView txtTen, txtEmail, txtSDT, txtDiaChi;
    private UserDAO userDAO;
    private String userId;
    private boolean isFinishing = false;
    private LinearLayout btnUpdatePro, btnDeleteuser, btnChangePassword,btnTransaction;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtTen = view.findViewById(R.id.txt_ten_TCN);
        txtEmail = view.findViewById(R.id.txt_email_TCN);
        txtSDT = view.findViewById(R.id.txt_SDT_TCN);
        txtDiaChi = view.findViewById(R.id.txt_Dia_Chi);
        btnDeleteuser = view.findViewById(R.id.btn_delete_user);
        btnUpdatePro = view.findViewById(R.id.btn_updateprofile);
        btnChangePassword = view.findViewById(R.id.btn_change_password);
        btnTransaction = view.findViewById(R.id.btn_transaction);


        userDAO = new UserDAO();

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("login_prefs", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", "");

        if (userId.isEmpty()) {
            Log.e(TAG, "userId không hợp lệ");
            Toast.makeText(requireContext(), "userId không hợp lệ", Toast.LENGTH_SHORT).show();
            requireActivity().finish();
            return;
        }


        loadUserData();


//        btnTransaction.setOnClickListener(v -> {
//            requireActivity().getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.fragment_container, new ChangePasswordFragment())
//                    .addToBackStack(null)
//                    .commit();
//        });
//

        btnChangePassword.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ChangePasswordFragment())
                    .addToBackStack(null)
                    .commit();
        });

        btnUpdatePro.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new UpdateProfileFragment())
                    .addToBackStack(null)
                    .commit();
        });


        btnDeleteuser.setOnClickListener(v -> new AlertDialog.Builder(requireContext())
                .setTitle("Thông báo")
                .setMessage("Bạn có chắc chắn muốn xoá tài khoản không?")
                .setPositiveButton("Có", (dialog, which) -> {
                    deleteUserInAuth();
                    userDAO.deleteUser(userId);
                    startActivity(new Intent(requireContext(), LoginActivity.class));
                    requireActivity().finish();
                })
                .setNegativeButton("Không", (dialog, which) -> dialog.dismiss())
                .show());
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Làm mới dữ liệu người dùng");
        loadUserData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isFinishing = true;
    }

    private void deleteUserInAuth() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            user.delete()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Tài khoản đã được xoá thành công.");
                        } else {
                            Log.e(TAG, "Xoá tài khoản thất bại: " + task.getException());
                        }
                    });
        }
    }

    private void loadUserData() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("login_prefs", Context.MODE_PRIVATE);
        String fullName = sharedPreferences.getString("fullName", "");
        String email = sharedPreferences.getString("email", "");
        String phone = sharedPreferences.getString("phone", "");
        String address = sharedPreferences.getString("address", "");

        txtTen.setText(fullName);
        txtEmail.setText(email);
        txtSDT.setText(phone);
        txtDiaChi.setText(address);

        userDAO.fetchUserById(userId, user -> {
            if (isFinishing || requireActivity().isFinishing()) return;

            if (user != null) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("fullName", user.getFullName());
                editor.putString("email", user.getEmail());
                editor.putString("phone", user.getPhone());
                editor.putString("address", user.getAddress());
                editor.apply();

                txtTen.setText(user.getFullName());
                txtEmail.setText(user.getEmail());
                txtSDT.setText(user.getPhone());
                txtDiaChi.setText(user.getAddress());
            }
        });
    }
}
