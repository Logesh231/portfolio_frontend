package com.example.portfolioapp;

import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.portfolioapp.model.Skill;
import com.example.portfolioapp.network.ApiClient;
import com.example.portfolioapp.network.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddSkillActivity extends AppCompatActivity {

    EditText etSkillName, etPercentage;
    Spinner spinnerCategory;
    LinearLayout btnSave;
    ProgressBar progressBar;
    SessionManager sessionManager;

    String[] categories = {"Web", "Android", "Backend"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_skill);


        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        View toolbar = findViewById(R.id.toolbar);
        ViewCompat.requestApplyInsets(toolbar);
        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (view, insets) -> {
            int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;

            int extraPadding = (int) (14 * getResources().getDisplayMetrics().density);

            view.setPadding(
                    view.getPaddingLeft(),
                    top,
                    view.getPaddingRight(),
                    view.getPaddingBottom()
            );

            return insets;
        });

        sessionManager  = new SessionManager(this);
        etSkillName     = findViewById(R.id.et_skill_name);
        etPercentage    = findViewById(R.id.et_percentage);
        spinnerCategory = findViewById(R.id.spinner_category);
        btnSave         = findViewById(R.id.btn_save);
        progressBar     = findViewById(R.id.progress_bar);

        findViewById(R.id.iv_back).setOnClickListener(v -> finish());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        btnSave.setOnClickListener(v -> saveSkill());
    }

    private void saveSkill() {
        String name       = etSkillName.getText().toString().trim();
        String percentStr = etPercentage.getText().toString().trim();
        String category   = categories[spinnerCategory.getSelectedItemPosition()];

        if (name.isEmpty()) {
            Toast.makeText(this, "Skill name required", Toast.LENGTH_SHORT).show();
            return;
        }

        Skill skill      = new Skill();
        skill.name       = name;
        skill.category   = category;
        skill.percentage = percentStr.isEmpty() ? 80 : Integer.parseInt(percentStr);

        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        ApiClient.getService().addSkill(sessionManager.getBearerToken(), skill)
                .enqueue(new Callback<Skill>() {
                    @Override
                    public void onResponse(Call<Skill> call, Response<Skill> response) {
                        progressBar.setVisibility(View.GONE);
                        btnSave.setEnabled(true);
                        if (response.isSuccessful()) {
                            Toast.makeText(AddSkillActivity.this,
                                    "Skill added!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(AddSkillActivity.this,
                                    "Failed: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<Skill> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        btnSave.setEnabled(true);
                        Toast.makeText(AddSkillActivity.this,
                                "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}