package com.example.testapp.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.testapp.R;
import com.example.testapp.models.User;
import com.example.testapp.repository.UserRepository;

public class LoginFragment extends Fragment {

    private EditText emailEditText, passwordEditText;
    private Button loginButton, registerButton;
    private TextView forgotPasswordTextView;
    private ProgressBar progressBar;
    private UserRepository userRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        userRepository = new UserRepository();

        // Note: Removed auto-navigation to Home when already logged in
        // This was causing issues with instrumented tests
        // The user should explicitly navigate from login screen
        
        setupClickListeners();
    }

    private void initializeViews(View view) {
        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        loginButton = view.findViewById(R.id.loginButton);
        registerButton = view.findViewById(R.id.registerButton);
        forgotPasswordTextView = view.findViewById(R.id.forgotPasswordTextView);
        progressBar = view.findViewById(R.id.progressBar);
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
            new AlertDialog.Builder(requireContext())
                .setTitle("שגיאה")
                .setMessage("נא להזין כתובת מייל או מספר טלפון")
                .setPositiveButton("אישור", null)
                .show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            new AlertDialog.Builder(requireContext())
                .setTitle("שגיאה")
                .setMessage("נא להזין סיסמה")
                .setPositiveButton("אישור", null)
                .show();
            return;
        }

        if (input.contains("@") && !isValidEmail(input)) {
            new AlertDialog.Builder(requireContext())
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
                navigateToHome();
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
                
                new AlertDialog.Builder(requireContext())
                    .setTitle("שגיאה")
                    .setMessage(errorMessage)
                    .setPositiveButton("אישור", null)
                    .show();
            }
        });
    }

    private void navigateToHome() {
        if (getView() != null) {
            Navigation.findNavController(getView()).navigate(R.id.action_login_to_home);
        }
    }

    private void navigateToRegister() {
        if (getView() != null) {
            Navigation.findNavController(getView()).navigate(R.id.action_login_to_register);
        }
    }

    private void showForgotPasswordDialog() {
        // Directly show the email reset dialog
        showEmailResetDialog();
    }
    
    private void showEmailResetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("איפוס סיסמה");
        builder.setMessage("שים לב, אפשרות זו מיועדת למאמנים ורכזים בעלי כתובת מייל במערכת.");
        
        final EditText emailInput = new EditText(requireContext());
        emailInput.setHint("הכנס את כתובת המייל שלך");
        emailInput.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailInput.setPadding(50, 20, 50, 20);
        
        builder.setView(emailInput);
        builder.setPositiveButton("שלח", (dialog, which) -> {
            String email = emailInput.getText().toString().trim();
            if (!email.isEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                sendPasswordResetEmail(email);
            } else {
                Toast.makeText(requireContext(), "כתובת מייל לא תקינה", Toast.LENGTH_SHORT).show();
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
                new AlertDialog.Builder(requireContext())
                    .setTitle("✓ מייל נשלח בהצלחה")
                    .setMessage("נשלח מייל לאיפוס סיסמה לכתובת: " + email)
                    .setPositiveButton("הבנתי", null)
                    .show();
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                new AlertDialog.Builder(requireContext())
                    .setTitle("שגיאה")
                    .setMessage("שליחת מייל נכשלה: " + error)
                    .setPositiveButton("אישור", null)
                    .show();
            }
        });
    }
}
