package com.example.portfolioapp;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.portfolioapp.model.Resume;
import com.example.portfolioapp.network.ApiClient;
import com.example.portfolioapp.network.SessionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateResumeActivity extends AppCompatActivity {
    private static final String TAG = "UpdateResume";
    Button       btnUpload;
    ImageView    ivBack;
    LinearLayout btnPickPdf;
    TextView     tvPdfName, tvCurrentResume;
    ProgressBar  progressBar;
    SessionManager sessionManager;
    Uri    selectedPdfUri = null;
    String selectedFileName = null;
    ActivityResultLauncher<Intent> pdfLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK
                        && result.getData() != null) {
                    selectedPdfUri  = result.getData().getData();
                    selectedFileName = getFileName(selectedPdfUri);
                    tvPdfName.setText("📄 " + selectedFileName);
                    btnUpload.setEnabled(true);
                    Log.d(TAG, "PDF selected: " + selectedFileName + " | URI: " + selectedPdfUri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_resume);

        sessionManager  = new SessionManager(this);
        btnPickPdf      = findViewById(R.id.btn_pick_pdf);
        tvPdfName       = findViewById(R.id.tv_pdf_name);
        tvCurrentResume = findViewById(R.id.tv_current_resume);
        btnUpload       = findViewById(R.id.btn_upload);
        progressBar     = findViewById(R.id.progress_bar);
        ivBack          = findViewById(R.id.iv_back);

        ivBack.setOnClickListener(v -> finish());
        btnUpload.setEnabled(false);

        loadCurrentResume();

        btnPickPdf.setOnClickListener(v -> openPdfPicker());
        btnUpload.setOnClickListener(v  -> uploadResume());
    }

    private void loadCurrentResume() {
        ApiClient.getService().getResume().enqueue(new Callback<Resume>() {
            @Override
            public void onResponse(Call<Resume> call, Response<Resume> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Resume r = response.body();
                    // Try originalName first, then pdfUrl filename
                    String name = r.originalName;
                    if (name == null || name.isEmpty()) name = "resume.pdf";
                    tvCurrentResume.setText("Current: " + name);
                } else {
                    tvCurrentResume.setText("No resume uploaded yet");
                }
            }
            @Override
            public void onFailure(Call<Resume> call, Throwable t) {
                tvCurrentResume.setText("Could not load current resume");
                Log.e(TAG, "Load resume failed: " + t.getMessage());
            }
        });
    }


    private void openPdfPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        pdfLauncher.launch(intent);
    }

    private void uploadResume() {
        if (selectedPdfUri == null) {
            Toast.makeText(this, "Please pick a PDF first", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnUpload.setEnabled(false);
        tvPdfName.setText("Uploading... please wait ⏳");

        MultipartBody.Part pdfPart = buildPdfPart(selectedPdfUri);
        if (pdfPart == null) {
            Toast.makeText(this, "Cannot read the PDF file. Try again.", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
            btnUpload.setEnabled(true);
            tvPdfName.setText("📄 " + selectedFileName);
            return;
        }

        Log.d(TAG, "Uploading PDF to server...");

        ApiClient.getService()
                .uploadResume(sessionManager.getBearerToken(), pdfPart)
                .enqueue(new Callback<Resume>() {
                    @Override
                    public void onResponse(Call<Resume> call, Response<Resume> response) {
                        progressBar.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null) {
                            Log.d(TAG, "Upload success: " + response.body().pdfUrl);
                            Toast.makeText(UpdateResumeActivity.this,
                                    "Resume uploaded! ✅", Toast.LENGTH_SHORT).show();
                            finish();

                        } else {
                            // Show the exact error from server
                            btnUpload.setEnabled(true);
                            tvPdfName.setText("📄 " + selectedFileName);

                            String errorMsg = "Upload failed (code: " + response.code() + ")";
                            try {
                                if (response.errorBody() != null) {
                                    String body = response.errorBody().string();
                                    Log.e(TAG, "Server error body: " + body);
                                    errorMsg += "\n" + body;
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error reading error body: " + e.getMessage());
                            }
                            Toast.makeText(UpdateResumeActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Resume> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        btnUpload.setEnabled(true);
                        tvPdfName.setText("📄 " + selectedFileName);
                        Log.e(TAG, "Network failure: " + t.getMessage());
                        Toast.makeText(UpdateResumeActivity.this,
                                "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private MultipartBody.Part buildPdfPart(Uri uri) {
        try {
            String fileName = (selectedFileName != null)
                    ? selectedFileName : "resume.pdf";

            InputStream input = getContentResolver().openInputStream(uri);
            if (input == null) {
                Log.e(TAG, "InputStream is null for URI: " + uri);
                return null;
            }

            File tempFile = new File(getCacheDir(), "upload_" + System.currentTimeMillis() + ".pdf");
            FileOutputStream output = new FileOutputStream(tempFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
            input.close();
            output.flush();
            output.close();

            Log.d(TAG, "Temp PDF file size: " + tempFile.length() + " bytes");

            if (tempFile.length() == 0) {
                Log.e(TAG, "Temp file is empty!");
                return null;
            }

            RequestBody reqBody = RequestBody.create(
                    MediaType.parse("application/pdf"), tempFile);
            return MultipartBody.Part.createFormData("resume", fileName, reqBody);

        } catch (Exception e) {
            Log.e(TAG, "buildPdfPart error: " + e.getMessage(), e);
            return null;
        }
    }
    private String getFileName(Uri uri) {
        String result = "resume.pdf";
        try {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) result = cursor.getString(idx);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "getFileName error: " + e.getMessage());
        }
        return result;
    }
}