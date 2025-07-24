package com.example.appcore.ui.activities.updateProfile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.appcore.R;
import com.example.appcore.dao.UserDAO;
import com.example.appcore.data.models.User;
import com.google.firebase.auth.FirebaseAuth;

public class UpdateProfileFragment extends Fragment {

    private static final String TAG = "UpdateProfileFragment";
    private EditText editTextName, editTextEmail, editTextPhone, editTextAddress;
    private Button btnUpdate, btnCancel;
    private UserDAO userDAO;
    private String userId;
    private User current_user;
    private boolean isFinishing = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_update_profile, container, false); // Sử dụng layout cũ
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userDAO = new UserDAO();
        userId = getUserID();

        editTextName = view.findViewById(R.id.editTextName);
        editTextEmail = view.findViewById(R.id.editTextEmail);
        editTextPhone = view.findViewById(R.id.editTextPhone);
        editTextAddress = view.findViewById(R.id.editTextAddress);
        btnUpdate = view.findViewById(R.id.btnUpdate);
        btnCancel = view.findViewById(R.id.btnCancel);

        btnCancel.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        btnUpdate.setOnClickListener(v -> {
            String new_Name = editTextName.getText().toString();
            String new_email = editTextEmail.getText().toString();
            String new_Phone = editTextPhone.getText().toString();
            String new_Address = editTextAddress.getText().toString();

            current_user.setEmail(new_email);
            current_user.setFullName(new_Name);
            current_user.setPhone(new_Phone);
            current_user.setAddress(new_Address);

            userDAO.updateUser(current_user, new UserDAO.OnUserOperationListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(requireContext(), "Thành công!", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(requireContext(), "Thất bại!", Toast.LENGTH_SHORT).show();
                    loadUserData();
                }
            });

            Log.d(TAG, "user = " + current_user);
        });

        loadUserData();
    }

    private String getUserID() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private void loadUserData() {
        userDAO.fetchUserById(userId, user -> {
            current_user = user;
            if (isFinishing || requireActivity().isFinishing()) {
                return;
            }
            if (user != null) {
                editTextName.setText(user.getFullName() != null ? user.getFullName() : "");
                editTextEmail.setText(user.getEmail() != null ? user.getEmail() : "");
                editTextPhone.setText(user.getPhone() != null ? user.getPhone() : "");
                editTextAddress.setText(user.getAddress() != null ? user.getAddress() : "");
            } else {
                Toast.makeText(requireContext(), "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isFinishing = true;
    }
}
