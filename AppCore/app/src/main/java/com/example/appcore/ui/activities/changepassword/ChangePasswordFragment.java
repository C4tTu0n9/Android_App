package com.example.appcore.ui.activities.changepassword;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.appcore.R;
import com.example.appcore.dao.AuthDAO;
import com.google.android.material.textfield.TextInputEditText;

public class ChangePasswordFragment extends Fragment {

    private AuthDAO authDAO;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_change_password, container, false); // Dùng lại layout
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authDAO = new AuthDAO();

        TextInputEditText edtOldPassword = view.findViewById(R.id.edt_old_password);
        TextInputEditText edtNewPassword = view.findViewById(R.id.edt_new_password);
        TextInputEditText edtConfirmPassword = view.findViewById(R.id.edt_confirm_password);
        Button btnChangePassword = view.findViewById(R.id.btn_change_password);
        TextView txtQuayLai = view.findViewById(R.id.txt_back);

        btnChangePassword.setOnClickListener(v -> {
            String oldPassword = edtOldPassword.getText().toString().trim();
            String newPassword = edtNewPassword.getText().toString().trim();
            String confirmPassword = edtConfirmPassword.getText().toString().trim();

            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            authDAO.changePassword(requireContext(), oldPassword, newPassword, confirmPassword);
        });

        txtQuayLai.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });
    }
}
