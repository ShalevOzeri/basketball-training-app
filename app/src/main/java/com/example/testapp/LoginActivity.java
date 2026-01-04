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
import com.google.firebase.auth.FirebaseAuth;

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

        // Check if user already logged in
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

        // If contains @, validate as email. Otherwise treat as phone
        if (input.contains("@")) {
            if (!isValidEmail(input)) {
                new AlertDialog.Builder(this)
                    .setTitle("שגיאה")
                    .setMessage("כתובת מייל לא תקינה")
                    .setPositiveButton("אישור", null)
                    .show();
                return;
            }
        }

        progressBar.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);

        // Use new login method that supports both email and phone
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
                
                // Parse error and show user-friendly message
                String errorMessage;
                if (error.contains("password") || error.contains("INVALID_PASSWORD") || 
                    error.contains("wrong-password") || error.contains("credential") || 
                    error.contains("INVALID_CREDENTIAL") || error.contains("invalid-credential") ||
                    error.contains("user") || error.contains("USER_NOT_FOUND") || 
                    error.contains("user-not-found")) {
                    errorMessage = "מייל או סיסמה שגויים";
                } else if (error.contains("invalid-email")) {
                    errorMessage = "כתובת מייל לא תקינה";
                } else if (error.contains("network") || error.contains("network-request-failed")) {
                    errorMessage = "בעיית תקשורת - בדוק את החיבור לאינטרנט";
                } else if (error.contains("too-many-requests")) {
                    errorMessage = "יותר מדי ניסיונות - נסה שוב מאוחר יותר";
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("איפוס סיסמה");
        
        // Create EditText for email input
        final EditText emailInput = new EditText(this);
        emailInput.setHint("הכנס את כתובת המייל שלך");
        emailInput.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailInput.setPadding(50, 20, 50, 20);
        
        // Pre-fill with existing email if available
        String currentEmail = emailEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(currentEmail)) {
            emailInput.setText(currentEmail);
        }
        
        builder.setView(emailInput);
        
        builder.setPositiveButton("שלח", (dialog, which) -> {
            String email = emailInput.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(LoginActivity.this, "יש להזין כתובת מייל", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Validate email format
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                new AlertDialog.Builder(LoginActivity.this)
                    .setTitle("שגיאה")
                    .setMessage("כתובת מייל לא תקינה")
                    .setPositiveButton("אישור", null)
                    .show();
                return;
            }
            
            sendPasswordResetEmail(email);
        });
        
        builder.setNegativeButton("ביטול", (dialog, which) -> dialog.dismiss());
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    
    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        // Check basic email pattern: must have @ and . after @
        if (!email.contains("@") || !email.contains(".")) {
            return false;
        }
        // Check @ position (not at start or end)
        int atIndex = email.indexOf("@");
        if (atIndex <= 0 || atIndex == email.length() - 1) {
            return false;
        }
        // Check there's a dot after @
        int dotAfterAt = email.indexOf(".", atIndex);
        if (dotAfterAt == -1 || dotAfterAt == email.length() - 1) {
            return false;
        }
        // Use Android's pattern matcher as well
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void sendPasswordResetEmail(String email) {
        progressBar.setVisibility(View.VISIBLE);
        
        userRepository.sendPasswordResetEmail(email, new UserRepository.OnPasswordResetListener() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                
                // Show detailed success message
                new AlertDialog.Builder(LoginActivity.this)
                    .setTitle("✓ מייל נשלח בהצלחה")
                    .setMessage("נשלח מייל לאיפוס סיסמה לכתובת:\n" + email + 
                               "\n\nאנא בדוק את תיבת הדואר שלך (וגם בספאם).\n" +
                               "הקישור תקף למשך שעה אחת.")
                    .setPositiveButton("הבנתי", null)
                    .show();
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                
                // Check if email doesn't exist
                String errorMessage;
                if (error.contains("no user record") || error.contains("user-not-found")) {
                    errorMessage = "לא קיים משתמש במערכת";
                } else if (error.contains("invalid-email")) {
                    errorMessage = "כתובת מייל לא תקינה";
                } else if (error.contains("network")) {
                    errorMessage = "❌ בעיית תקשורת\n\nאנא בדוק את החיבור לאינטרנט ונסה שוב.";
                } else {
                    errorMessage = "❌ שגיאה בשליחת מייל\n\n" + error;
                }
                
                new AlertDialog.Builder(LoginActivity.this)
                    .setTitle("שגיאה")
                    .setMessage(errorMessage)
                    .setPositiveButton("אישור", null)
                    .show();
            }
        });
    }
}
