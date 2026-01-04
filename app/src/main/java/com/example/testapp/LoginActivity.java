package com.example.testapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.testapp.models.User;
import com.example.testapp.repository.UserRepository;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton, registerButton;
    private TextView forgotPasswordTextView;
    private ProgressBar progressBar;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeViews();
        userRepository = new UserRepository();

        if (userRepository.isLoggedIn()) {
            navigateToMain();
            return;
        }

        setupClickListeners();
    }

    private void initializeViews() {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> loginUser());
        forgotPasswordTextView.setOnClickListener(v -> showForgotPasswordDialog());
        registerButton.setOnClickListener(v -> navigateToRegister());
    }

    private void loginUser() {
        String input = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(input)) {
            new AlertDialog.Builder(this)
                .setTitle("שגיאה")
                .setMessage("נא להזין כתובת מייל או מספר טלפון")
                .setPositiveButton("אישור", null)
                .show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            new AlertDialog.Builder(this)
                .setTitle("שגיאה")
                .setMessage("נא להזין סיסמה")
                .setPositiveButton("אישור", null)
                .show();
            return;
        }

        if (input.contains("@") && !isValidEmail(input)) {
            new AlertDialog.Builder(this)
                .setTitle("שגיאה")
                .setMessage("כתובת מייל לא תקינה")
                .setPositiveButton("אישור", null)
                .show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);

        userRepository.loginWithEmailOrPhone(input, password, new UserRepository.OnLoginListener() {
            @Override
            public void onSuccess(User user) {
                progressBar.setVisibility(View.GONE);
                navigateToMain();
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                loginButton.setEnabled(true);
                
                String errorMessage;
                if (error.contains("password") || error.contains("credential") || error.contains("user-not-found")) {
                    errorMessage = "מייל או סיסמה שגויים";
                } else {
                    errorMessage = "שגיאה בהתחברות: " + error;
                }
                
                new AlertDialog.Builder(LoginActivity.this)
                    .setTitle("שגיאה")
                    .setMessage(errorMessage)
                    .setPositiveButton("אישור", null)
                    .show();
            }
        });
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToRegister() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    private void showForgotPasswordDialog() {
        // Directly show the email reset dialog
        showEmailResetDialog();
    }
    
    private void showEmailResetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("איפוס סיסמה");
        builder.setMessage("שים לב, אפשרות זו מיועדת למאמנים ורכזים בעלי כתובת מייל במערכת.");
        
        final EditText emailInput = new EditText(this);
        emailInput.setHint("הכנס את כתובת המייל שלך");
        emailInput.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailInput.setPadding(50, 20, 50, 20);
        
        builder.setView(emailInput);
        builder.setPositiveButton("שלח", (dialog, which) -> {
            String email = emailInput.getText().toString().trim();
            if (!email.isEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                sendPasswordResetEmail(email);
            } else {
                Toast.makeText(this, "כתובת מייל לא תקינה", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("ביטול", null);
        builder.show();
    }
    
    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void sendPasswordResetEmail(String email) {
        progressBar.setVisibility(View.VISIBLE);
        userRepository.sendPasswordResetEmail(email, new UserRepository.OnPasswordResetListener() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                new AlertDialog.Builder(LoginActivity.this)
                    .setTitle("✓ מייל נשלח בהצלחה")
                    .setMessage("נשלח מייל לאיפוס סיסמה לכתובת: " + email)
                    .setPositiveButton("הבנתי", null)
                    .show();
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                new AlertDialog.Builder(LoginActivity.this)
                    .setTitle("שגיאה")
                    .setMessage("שליחת מייל נכשלה: " + error)
                    .setPositiveButton("אישור", null)
                    .show();
            }
        });
    }
}
