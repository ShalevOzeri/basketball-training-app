package com.example.testapp.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.testapp.R;
import com.example.testapp.models.User;
import com.example.testapp.repository.UserRepository;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterFragment extends Fragment {

    private EditText nameEditText, emailEditText, passwordEditText, phoneEditText;
    private TextInputLayout emailInputLayout;
    private Spinner roleSpinner;
    private Button registerButton;
    private ProgressBar progressBar;
    private UserRepository userRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        userRepository = new UserRepository();
        
        setupRoleSpinner();

        registerButton.setOnClickListener(v -> registerUser());
    }
    
    private void setupRoleSpinner() {
        String[] roles = {"מאמן (COACH)", "שחקן (PLAYER)"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);

        roleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateEmailVisibility(getSelectedRole());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                updateEmailVisibility(getSelectedRole());
            }
        });

        // Ensure the initial state reflects the default selection
        updateEmailVisibility(getSelectedRole());
    }

    private void initializeViews(View view) {
        nameEditText = view.findViewById(R.id.nameEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        emailInputLayout = view.findViewById(R.id.emailInputLayout);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        phoneEditText = view.findViewById(R.id.phoneEditText);
        roleSpinner = view.findViewById(R.id.roleSpinner);
        registerButton = view.findViewById(R.id.registerButton);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private String getSelectedRole() {
        String selectedRole = roleSpinner.getSelectedItem().toString();
        if (selectedRole.contains("COACH")) {
            return "COACH";
        } else if (selectedRole.contains("PLAYER")) {
            return "PLAYER";
        }
        return "COACH";
    }

    private void updateEmailVisibility(String role) {
        if (emailInputLayout == null) return;
        boolean hideEmail = "PLAYER".equals(role);
        emailInputLayout.setVisibility(hideEmail ? View.GONE : View.VISIBLE);
        if (hideEmail) {
            emailEditText.setText("");
            emailEditText.setError(null);
        }
    }

    private void registerUser() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        
        // Get role from spinner
        String role = getSelectedRole();

        if (TextUtils.isEmpty(name)) {
            nameEditText.setError("Name is required");
            return;
        }

        // Email is optional for players, required for coaches
        if (role.equals("COACH") && TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required for coaches");
            return;
        }

        // For players, either email or phone must be provided
        if (role.equals("PLAYER") && TextUtils.isEmpty(email) && TextUtils.isEmpty(phone)) {
            Toast.makeText(requireContext(), "Please provide either email or phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        registerButton.setEnabled(false);

        userRepository.register(email, password, name, role, phone, new UserRepository.OnRegisterListener() {
            @Override
            public void onSuccess(User user) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Registration successful!", Toast.LENGTH_SHORT).show();
                if (getView() != null) {
                    Navigation.findNavController(getView()).navigateUp();
                }
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                registerButton.setEnabled(true);
                Toast.makeText(requireContext(), "Registration failed: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
