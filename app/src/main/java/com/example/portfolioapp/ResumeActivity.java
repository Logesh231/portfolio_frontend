package com.example.portfolioapp;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.portfolioapp.model.Resume;
import com.example.portfolioapp.network.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResumeActivity extends AppCompatActivity {

    LinearLayout btnOpen,btnDownload;
    ImageView ivBack;
    ProgressBar progressBar;
    TextView tvFileName, tvStatus;
    String pdfUrl=null;
    String originalName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_resume);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        View toolbar = findViewById(R.id.toolbar);
        ViewCompat.requestApplyInsets(toolbar);

        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (view, insets) -> {
            int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            view.setPadding(
                    view.getPaddingLeft(),
                    top,
                    view.getPaddingRight(),
                    view.getPaddingBottom()
            );
            return insets;
        });
        btnOpen     = findViewById(R.id.btn_open_resume);
        btnDownload = findViewById(R.id.btn_download_resume);
        ivBack      = findViewById(R.id.iv_back);
        progressBar = findViewById(R.id.progress_bar);
        tvFileName  = findViewById(R.id.tv_file_name);
        tvStatus    = findViewById(R.id.tv_status);

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.putExtra("section", "resume");
                setResult(RESULT_OK, data);
                finish();

            }
        });
        btnOpen.setEnabled(false);
        btnDownload.setEnabled(false);
        loadResume();
    }

    private void loadResume() {
        progressBar.setVisibility(View.VISIBLE);
        tvStatus.setText("Loading resume...");

        ApiClient.getService().getResume().enqueue(new Callback<Resume>() {
            @Override
            public void onResponse(Call<Resume> call, Response<Resume> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    pdfUrl=response.body().pdfUrl;
                    originalName = response.body().originalName;
                    tvFileName.setText("📄 " + (originalName != null
                            ? originalName : "resume.pdf"));
                    tvStatus.setText("Resume ready");
                    btnOpen.setEnabled(true);
                    btnDownload.setEnabled(true);
                    btnOpen.setOnClickListener(v -> openResume());
                    btnDownload.setOnClickListener(v -> downloadResume());

                } else {
                    tvStatus.setText("No resume uploaded yet.");
                    tvFileName.setText("Contact admin to upload resume.");
                }
            }

            @Override
            public void onFailure(Call<Resume> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                tvStatus.setText("Failed to load resume.");
                tvFileName.setText("Check internet connection.");
                Toast.makeText(ResumeActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openResume() {
        if (pdfUrl == null) return;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(pdfUrl));
        startActivity(Intent.createChooser(intent, "Open Resume with"));
    }

    private void downloadResume() {
        if (pdfUrl == null) return;
        try {
            String fileName = (originalName != null)
                    ? originalName : "Logesh_Resume.pdf";

            DownloadManager.Request request =
                    new DownloadManager.Request(Uri.parse(pdfUrl));
            request.setTitle(fileName);
            request.setDescription("Downloading resume...");
            request.setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS, fileName);
            request.allowScanningByMediaScanner();

            DownloadManager dm =
                    (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            dm.enqueue(request);

            Toast.makeText(this,
                    "Downloading... Check notifications", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this,
                    "Download failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}