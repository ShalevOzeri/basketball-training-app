package com.example.testapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.testapp.models.User;
import com.example.testapp.repository.UserRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {

    private EditText nameEditText, emailEditText, passwordEditText, phoneEditText;
    private TextInputLayout emailInputLayout;
    private Spinner roleSpinner;
    private Button registerButton;
    private ProgressBar progressBar;
    private UserRepository userRepository;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        initializeViews();
        userRepository = new UserRepository();
        
        setupRoleSpinner();

        registerButton.setOnClickListener(v -> registerUser());
    }
    
    private void setupRoleSpinner() {
        String[] roles = {"מאמן (COACH)", "שחקן (PLAYER)"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
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

    private void initializeViews() {
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordEditText = findViewById(R.id.passwordEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        roleSpinner = findViewById(R.id.roleSpinner);
        registerButton = findViewById(R.id.registerButton);
        progressBar = findViewById(R.id.progressBar);
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
            Toast.makeText(this, "Please provide either email or phone number", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                registerButton.setEnabled(true);
                Toast.makeText(RegisterActivity.this, "Registration failed: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
