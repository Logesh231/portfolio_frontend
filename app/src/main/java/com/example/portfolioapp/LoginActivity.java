package com.example.portfolioapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.portfolioapp.model.LoginResponse;
import com.example.portfolioapp.network.ApiClient;
import com.example.portfolioapp.network.SessionManager;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    LinearLayout layoutPhone;
    EditText     etPhone;
    LinearLayout btnSendOtp;

    LinearLayout layoutOtp;
    EditText     etOtp;
    LinearLayout btnVerifyOtp;
    TextView     tvTimer, tvResend;

    // Common views
    ImageView   ivBack;
    TextView    tvTitle, tvSubtitle;
    ProgressBar progressBar;
    FirebaseAuth firebaseAuth;
    String       verificationId = null;
    PhoneAuthProvider.ForceResendingToken resendToken = null;
    SessionManager sessionManager;
    CountDownTimer countDownTimer;
    boolean        canResend = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        View toolbar=findViewById(R.id.toolbar);
        ViewCompat.requestApplyInsets(toolbar);
        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (view, insets) -> {
            int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
//            int extraPadding = (int) (14 * getResources().getDisplayMetrics().density);
            view.setPadding(
                    view.getPaddingLeft(),
                    top,
                    view.getPaddingRight(),
                    view.getPaddingBottom()
            );

            return insets;
        });

        firebaseAuth   = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(this);

        ivBack        = findViewById(R.id.iv_back);
        tvTitle       = findViewById(R.id.tv_title);
        tvSubtitle    = findViewById(R.id.tv_subtitle);
        progressBar   = findViewById(R.id.progress_bar);
        layoutPhone   = findViewById(R.id.layout_phone);
        etPhone       = findViewById(R.id.et_phone);
        btnSendOtp    = findViewById(R.id.btn_send_otp);

        layoutOtp     = findViewById(R.id.layout_otp);
        etOtp         = findViewById(R.id.et_otp);
        btnVerifyOtp  = findViewById(R.id.btn_verify_otp);
        tvTimer       = findViewById(R.id.tv_timer);
        tvResend      = findViewById(R.id.tv_resend);

        ivBack.setOnClickListener(v -> finish());
        showPhaseOne();
        btnSendOtp.setOnClickListener(v -> {
            if (canResend && verificationId != null) {
                resendOtp();
            } else {
                sendOtp();
            }
        });
        btnVerifyOtp.setOnClickListener(v -> verifyOtp());
        tvResend.setOnClickListener(v -> {
            if (canResend) resendOtp();
        });
    }

    private void showPhaseOne() {
        layoutPhone.setVisibility(View.VISIBLE);
        btnSendOtp.setVisibility(View.VISIBLE);
        layoutOtp.setVisibility(View.GONE);
        btnVerifyOtp.setVisibility(View.GONE);
        tvTitle.setText("Admin Login");
        tvSubtitle.setText("Enter your phone number to receive OTP");
    }

    private void showPhaseTwo(String phone) {
        layoutPhone.setVisibility(View.GONE);
        btnSendOtp.setVisibility(View.GONE);
        layoutOtp.setVisibility(View.VISIBLE);
        btnVerifyOtp.setVisibility(View.VISIBLE);
        tvTitle.setText("Enter OTP");
        tvSubtitle.setText("OTP sent to " + phone);
        startResendTimer();
    }

    private void sendOtp() {
        String phone = etPhone.getText().toString().trim();
        if (phone.isEmpty()) {
            Toast.makeText(this, "Enter phone number", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!phone.startsWith("+")) phone = "+91" + phone;

        progressBar.setVisibility(View.VISIBLE);
        btnSendOtp.setEnabled(false);

        Log.d(TAG, "Sending OTP to: " + phone);

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(buildCallbacks())
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void resendOtp() {
        String phone = etPhone.getText().toString().trim();
        if (!phone.startsWith("+")) phone = "+91" + phone;

        progressBar.setVisibility(View.VISIBLE);
        canResend = false;
        tvResend.setAlpha(0.4f);

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(buildCallbacks())
                .setForceResendingToken(resendToken)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks buildCallbacks() {
        return new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                // Auto-verified (happens on some emulators)
                Log.d(TAG, "Auto verified");
                progressBar.setVisibility(View.GONE);
                signInWithCredential(credential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                progressBar.setVisibility(View.GONE);
                btnSendOtp.setEnabled(true);
                Log.e(TAG, "OTP failed: " + e.getMessage());

                String msg = e.getMessage() != null ? e.getMessage() : "";
                if (msg.contains("INVALID_PHONE_NUMBER")) {
                    Toast.makeText(LoginActivity.this,
                            "Invalid phone number", Toast.LENGTH_LONG).show();
                } else if (msg.contains("quota")) {
                    Toast.makeText(LoginActivity.this,
                            "SMS quota exceeded. Try later.", Toast.LENGTH_LONG).show();
                } else if (msg.contains("TOO_SHORT")) {
                    Toast.makeText(LoginActivity.this,
                            "Phone number too short", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this,
                            "Error: " + msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCodeSent(@NonNull String vId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                progressBar.setVisibility(View.GONE);
                btnSendOtp.setEnabled(true);
                verificationId = vId;
                resendToken    = token;
                Log.d(TAG, "OTP sent successfully");
                Toast.makeText(LoginActivity.this,
                        "OTP sent! Check SMS.", Toast.LENGTH_SHORT).show();
                String phone = etPhone.getText().toString().trim();
                if (!phone.startsWith("+")) phone = "+91" + phone;
                showPhaseTwo(phone);
            }
        };
    }

    private void verifyOtp() {
        String otp = etOtp.getText().toString().trim();
        if (otp.length() < 6) {
            Toast.makeText(this, "Enter 6-digit OTP", Toast.LENGTH_SHORT).show();
            return;
        }
        if (verificationId == null) {
            Toast.makeText(this, "Please request OTP first", Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        btnVerifyOtp.setEnabled(false);

        PhoneAuthCredential credential =
                PhoneAuthProvider.getCredential(verificationId, otp);
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful() && task.getResult() != null
                            && task.getResult().getUser() != null) {
                        // Get Firebase ID token
                        task.getResult().getUser().getIdToken(true)
                                .addOnCompleteListener(tokenTask -> {
                                    progressBar.setVisibility(View.GONE);
                                    if (tokenTask.isSuccessful()
                                            && tokenTask.getResult() != null) {
                                        String idToken = tokenTask.getResult().getToken();
                                        sendTokenToBackend(idToken);
                                    } else {
                                        btnVerifyOtp.setEnabled(true);
                                        Toast.makeText(this,
                                                "Token error. Try again.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        progressBar.setVisibility(View.GONE);
                        btnVerifyOtp.setEnabled(true);
                        String err = task.getException() != null
                                ? task.getException().getMessage() : "";
                        if (err != null && err.contains("invalid")) {
                            Toast.makeText(this,
                                    "Wrong OTP. Try again.", Toast.LENGTH_SHORT).show();
                        } else if (err != null && err.contains("expired")) {
                            Toast.makeText(this,
                                    "OTP expired. Resend.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this,
                                    "Verification failed. Try again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        Log.e(TAG, "signIn failed: " + err);
                    }
                });
    }

    private void sendTokenToBackend(String idToken) {
        progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "Sending ID token to backend...");
        String json = "{\"idToken\":\"" + idToken + "\"}";
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), json);
        ApiClient.getService().verifyOtp(body)
                .enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call,
                                           Response<LoginResponse> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            LoginResponse data = response.body();
                            sessionManager.saveSession(
                                    data.token, data.role,
                                    data.username != null ? data.username : "Admin");
                            Toast.makeText(LoginActivity.this,
                                    "Welcome Admin! 🎉", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Admin login success");
                            startActivity(new Intent(LoginActivity.this,
                                    AdminDashboardActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                            finish();
                        } else {
                            btnVerifyOtp.setEnabled(true);
                            String errMsg = "Not an admin number.";
                            try {
                                if (response.errorBody() != null) {
                                    errMsg = response.errorBody().string();
                                }
                            } catch (Exception ignored) {}
                            Log.e(TAG, "Backend rejected: " + errMsg);
                            Toast.makeText(LoginActivity.this,
                                    "Access denied: not admin number",
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        btnVerifyOtp.setEnabled(true);
                        Log.e(TAG, "Backend error: " + t.getMessage());
                        Toast.makeText(LoginActivity.this,
                                "Server error: " + t.getMessage(),

                                Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void startResendTimer() {
        canResend = false;
        tvResend.setAlpha(0.4f);
        if (countDownTimer != null) countDownTimer.cancel();
        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long ms) {
                tvTimer.setText("Resend in " + (ms / 1000) + "s");
            }
            @Override
            public void onFinish() {
                tvTimer.setText("");
                tvResend.setAlpha(1.0f);
                canResend = true;
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}