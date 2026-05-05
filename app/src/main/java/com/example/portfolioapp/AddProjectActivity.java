package com.example.portfolioapp;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.portfolioapp.model.Project;
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

public class AddProjectActivity extends AppCompatActivity {

    EditText   etTitle, etDescription, etTechStack, etGithubUrl;
    LinearLayout     btnSave;
    ImageView  ivPreview, ivBack;
    LinearLayout btnPickImage;
    TextView   tvImageName, tvToolbarTitle;
    ProgressBar progressBar;

    SessionManager sessionManager;
    Uri selectedImageUri = null;   // stores picked image URI
    String editProjectId = null;   // null = add mode

    // ── Gallery picker launcher ──────────────────────────────
    ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK
                        && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    ivPreview.setImageURI(selectedImageUri);
                    ivPreview.setVisibility(View.VISIBLE);
                    tvImageName.setText(getFileName(selectedImageUri));
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);


        setContentView(R.layout.activity_add_project);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        View toolbar = findViewById(R.id.toolbar);
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

        sessionManager= new SessionManager(this);
        etTitle= findViewById(R.id.et_title);
        etDescription   = findViewById(R.id.et_description);
        etTechStack     = findViewById(R.id.et_tech_stack);
        etGithubUrl     = findViewById(R.id.et_github_url);
        btnSave         = findViewById(R.id.btn_save);
        progressBar     = findViewById(R.id.progress_bar);
        ivPreview       = findViewById(R.id.iv_preview);
        btnPickImage    = findViewById(R.id.btn_pick_image);
        tvImageName     = findViewById(R.id.tv_image_name);
        tvToolbarTitle  = findViewById(R.id.tv_toolbar_title);
        ivBack          = findViewById(R.id.iv_back);

        ivBack.setOnClickListener(v -> finish());
        editProjectId = getIntent().getStringExtra("projectId");
        if (editProjectId != null) {
            tvToolbarTitle.setText("EDIT PROJECT");
            etTitle.setText(getIntent().getStringExtra("projectTitle"));
            etDescription.setText(getIntent().getStringExtra("projectDesc"));
            etTechStack.setText(getIntent().getStringExtra("projectStack"));
            etGithubUrl.setText(getIntent().getStringExtra("projectGithub"));
            String existingImage = getIntent().getStringExtra("projectImage");
            if (existingImage != null && !existingImage.isEmpty()) {
                tvImageName.setText("Current image saved. Pick new to replace.");
            }
        } else {
            tvToolbarTitle.setText("ADD PROJECT");
        }
        btnPickImage.setOnClickListener(v -> openGallery());
        btnSave.setOnClickListener(v -> saveProject());
    }

    // ── Open Gallery ─────────────────────────────────────────
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    // ── Save / Upload Project ─────────────────────────────────
    private void saveProject() {
        String title  = etTitle.getText().toString().trim();
        String desc   = etDescription.getText().toString().trim();
        String stack  = etTechStack.getText().toString().trim();
        String github = etGithubUrl.getText().toString().trim();

        if (title.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "Title and description required", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);
        RequestBody rbTitle  = toRequestBody(title);
        RequestBody rbDesc   = toRequestBody(desc);
        RequestBody rbStack  = toRequestBody(stack);
        RequestBody rbGithub = toRequestBody(github);
        MultipartBody.Part imagePart = null;
        if (selectedImageUri != null) {
            imagePart = buildImagePart(selectedImageUri);
        }

        String token = sessionManager.getBearerToken();

        Call<Project> call;
        if (editProjectId != null) {
            call = ApiClient.getService().updateProject(
                    token, editProjectId, rbTitle, rbDesc, rbStack, rbGithub, imagePart);
        } else {
            call = ApiClient.getService().addProject(
                    token, rbTitle, rbDesc, rbStack, rbGithub, imagePart);
        }

        call.enqueue(new Callback<Project>() {
            @Override
            public void onResponse(Call<Project> c, Response<Project> response) {
                progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(AddProjectActivity.this,
                            editProjectId != null ? "Project updated! ✅" : "Project added! ✅",
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddProjectActivity.this,
                            "Failed: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Project> c, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                Toast.makeText(AddProjectActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    // ── Helpers ───────────────────────────────────────────────

    private RequestBody toRequestBody(String value) {
        return RequestBody.create(
                MediaType.parse("text/plain"), value);
    }

    // Converts URI to MultipartBody.Part for image upload
    private MultipartBody.Part buildImagePart(Uri uri) {
        try {
            InputStream input = getContentResolver().openInputStream(uri);
            File tempFile = File.createTempFile("project_img", ".jpg", getCacheDir());
            FileOutputStream output = new FileOutputStream(tempFile);
            byte[] buffer = new byte[4096];
            int n;
            while ((n = input.read(buffer)) != -1) output.write(buffer, 0, n);
            input.close();
            output.close();

            RequestBody reqBody = RequestBody.create(
                    MediaType.parse("image/*"), tempFile);
            return MultipartBody.Part.createFormData("image", tempFile.getName(), reqBody);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getFileName(Uri uri) {
        String result = "image_selected.jpg";
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            if (idx != -1) result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }
}