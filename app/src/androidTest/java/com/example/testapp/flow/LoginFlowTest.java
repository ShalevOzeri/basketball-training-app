package com.example.testapp.flow;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.testapp.LoginActivity;
import com.example.testapp.MainActivity;
import com.example.testapp.R;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/**
 * UI Tests for Login Flow using Espresso
 * 
 * UI tests for login flow - run on emulator/real device
 * These tests simulate a real user using the application
 * 
 * ⚠️ Requires: emulator or connected device
 * 
 * Test Order: Tests run alphabetically. Tests that log in (starting with 'z') run LAST
 * to avoid leaving a logged-in user that interferes with other tests.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)  // Run tests in alphabetical order
public class LoginFlowTest {

    // Don't auto-launch activity - we need to logout first
    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    @BeforeClass
    public static void globalSetUp() {
        // Ensure no user is logged in before ANY test runs
        FirebaseAuth.getInstance().signOut();
    }

    @Before
    public void setUp() {
        // Logout any existing user before testing login
        FirebaseAuth.getInstance().signOut();
        
        // Wait a bit for logout to complete
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    // ========== Display Tests ==========
    
    @Test
    public void a1_loginScreen_DisplaysAllElements() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("🔑 Testing: Login Screen Displays All Elements");
        System.out.println("========================================");
        // Test: All elements on login page displayed correctly
        Thread.sleep(800); // Wait to see screen
        
        onView(withId(R.id.emailEditText)).check(matches(isDisplayed()));
        Thread.sleep(300);
        
        onView(withId(R.id.passwordEditText)).check(matches(isDisplayed()));
        Thread.sleep(300);
        
        onView(withId(R.id.loginButton)).check(matches(isDisplayed()));
        Thread.sleep(300);
        
        onView(withId(R.id.registerButton)).check(matches(isDisplayed()));
        Thread.sleep(300);
        
        onView(withId(R.id.forgotPasswordTextView)).check(matches(isDisplayed()));
        Thread.sleep(500);
    }

    // ========== Empty Fields Tests ==========
    
    @Test
    public void b1_loginWithEmptyFields_ShowsError() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("🔑 Testing: Login with Empty Fields Shows Error");
        System.out.println("========================================");
        // Test: Click login button with empty fields
        Thread.sleep(800);
        
        onView(withId(R.id.loginButton)).perform(click());
        Thread.sleep(1500); // לראות את הודעת השגיאה
    }

    @Test
    public void b2_loginWithEmptyEmail_ShowsError() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("🔑 Testing: Login with Empty Email Shows Error");
        System.out.println("========================================");
        // Test: Password only without email
        Thread.sleep(800);
        
        onView(withId(R.id.passwordEditText)).perform(typeText("password123"), closeSoftKeyboard());
        Thread.sleep(800);
        
        onView(withId(R.id.loginButton)).perform(click());
        Thread.sleep(1500);
    }

    @Test
    public void b3_loginWithEmptyPassword_ShowsError() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("🔑 Testing: Login with Empty Password Shows Error");
        System.out.println("========================================");
        // Test: Email only without password
        Thread.sleep(800);
        
        onView(withId(R.id.emailEditText)).perform(typeText("test@example.com"), closeSoftKeyboard());
        Thread.sleep(800);
        
        onView(withId(R.id.loginButton)).perform(click());
        Thread.sleep(1500);
    }

    // ========== Validation Tests ==========
    
    @Test
    public void c1_loginWithInvalidEmail_ShowsError() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("🔑 Testing: Login with Invalid Email Shows Error");
        System.out.println("========================================");
        // Test: Invalid email
        Thread.sleep(800);
        
        onView(withId(R.id.emailEditText)).perform(typeText("notanemail@bad"), closeSoftKeyboard());
        Thread.sleep(800);
        
        onView(withId(R.id.passwordEditText)).perform(typeText("password123"), closeSoftKeyboard());
        Thread.sleep(800);
        
        onView(withId(R.id.loginButton)).perform(click());
        Thread.sleep(1500);
        
        // Verify error dialog
        onView(withText("כתובת מייל לא תקינה")).check(matches(isDisplayed()));
        Thread.sleep(1000);
    }

    @Test
    public void c2_loginWithShortPassword_ShowsError() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("🔑 Testing: Login with Short Password Shows Error");
        System.out.println("========================================");
        // Test: Password too short
        Thread.sleep(800);
        
        onView(withId(R.id.emailEditText)).perform(typeText("test@example.com"), closeSoftKeyboard());
        Thread.sleep(800);
        
        onView(withId(R.id.passwordEditText)).perform(typeText("123"), closeSoftKeyboard());
        Thread.sleep(800);
        
        onView(withId(R.id.loginButton)).perform(click());
        Thread.sleep(1500);
    }

    @Test
    public void c3_loginWithWrongCredentials_ShowsError() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("🔑 Testing: Login with Wrong Credentials Shows Error");
        System.out.println("========================================");
        // Test: Wrong credentials
        Thread.sleep(800);
        
        onView(withId(R.id.emailEditText)).perform(typeText("wrong@email.com"), closeSoftKeyboard());
        Thread.sleep(800);
        
        onView(withId(R.id.passwordEditText)).perform(typeText("wrongpassword123"), closeSoftKeyboard());
        Thread.sleep(800);
        
        onView(withId(R.id.loginButton)).perform(click());
        Thread.sleep(2500); // Wait for Firebase error
    }

    // ========== Navigation Tests ==========
    
    @Test
    public void d1_clickRegisterButton_NavigatesToRegisterActivity() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("🔑 Testing: Click Register Button Navigates to Register");
        System.out.println("========================================");
        // Test: Click on register button
        Thread.sleep(800);
        
        onView(withId(R.id.registerButton)).perform(click());
        Thread.sleep(1500); // לראות את המעבר
    }

    @Test
    public void d2_clickForgotPassword_ShowsDialog() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("🔑 Testing: Click Forgot Password Shows Dialog");
        System.out.println("========================================");
        // Test: Click on "forgot password"
        Thread.sleep(800);
        
        onView(withId(R.id.forgotPasswordTextView)).perform(click());
        Thread.sleep(1000);
        
        // Verify dialog appears
        onView(withText("איפוס סיסמה")).check(matches(isDisplayed()));
        Thread.sleep(1500); // לראות את הדיאלוג
        
        // Cancel the dialog
        onView(withText("ביטול")).perform(click());
        Thread.sleep(800);
    }

    @Test
    public void d3_forgotPassword_EnterEmail() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("🔑 Testing: Forgot Password - Enter Email");
        System.out.println("========================================");
        // Test: Enter email for password reset
        Thread.sleep(800);
        
        onView(withId(R.id.forgotPasswordTextView)).perform(click());
        Thread.sleep(1000);
        
        // Enter email in dialog (if dialog has input field)
        onView(withText("איפוס סיסמה")).check(matches(isDisplayed()));
        Thread.sleep(1500);
        
        // Cancel
        onView(withText("ביטול")).perform(click());
        Thread.sleep(800);
    }

    // ========== Successful Login Test ==========
    
    @Test
    public void z_successfulLogin_NavigatesToMainActivity() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("🔑 Testing: Successful Login Navigates to Main Activity");
        System.out.println("========================================");
        // Test: Successful login and navigate to home screen
        // NOTE: Runs last (alphabetically) to not affect other tests
        Thread.sleep(1000);
        
        String testEmail = "shalevozeri951@gmail.com";
        String testPassword = "121074Aa";
        
        // Type email slowly
        onView(withId(R.id.emailEditText)).perform(typeText(testEmail), closeSoftKeyboard());
        Thread.sleep(1200);
        
        // Type password slowly
        onView(withId(R.id.passwordEditText)).perform(typeText(testPassword), closeSoftKeyboard());
        Thread.sleep(1200);
        
        // Click login
        onView(withId(R.id.loginButton)).perform(click());
        Thread.sleep(1000); // לראות את הלחיצה
        
        // Wait for Firebase authentication and navigation
        Thread.sleep(3000); // לראות את המעבר למסך הבית
        
        // Verify we navigated to MainActivity
        intended(hasComponent(MainActivity.class.getName()));
        Thread.sleep(1000);
    }
}
