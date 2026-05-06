package com.example.portfolioapp;

import static android.view.View.GONE;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.portfolioapp.model.Project;
import com.example.portfolioapp.model.Skill;
import com.example.portfolioapp.network.ApiClient;
import com.example.portfolioapp.network.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDashboardActivity extends AppCompatActivity {

    private static final String TAG = "AdminDashboard";
    SessionManager      sessionManager;
    ImageView iv_back;
    RecyclerView        rvProjects, rvSkills;
    LinearLayout        btnAddProject, btnAddSkill, btnUpdateResume;
    TextView            tvAdminName;

    List<Project>       projectList    = new ArrayList<>();
    List<Skill>         skillList      = new ArrayList<>();
    AdminProjectAdapter projectAdapter;
    AdminSkillAdapter   skillAdapter;
    ProgressBar progress_bar1,progress_bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);
        EdgeToEdge.enable(this);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        View root = findViewById(R.id.main_root);
        iv_back=findViewById(R.id.iv_back);

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(AdminDashboardActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;

            view.setPadding(
                    view.getPaddingLeft(),
                    top,
                    view.getPaddingRight(),
                    view.getPaddingBottom()
            );

            return insets;
        });

        sessionManager = new SessionManager(this);

        if (!sessionManager.isAdmin()) {
            Toast.makeText(this, "Admin access required", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvAdminName     = findViewById(R.id.tv_admin_name);
        rvProjects      = findViewById(R.id.rv_projects);
        rvSkills        = findViewById(R.id.rv_skills);
        btnAddProject   = findViewById(R.id.btn_add_project);
        btnAddSkill     = findViewById(R.id.btn_add_skill);
        btnUpdateResume = findViewById(R.id.btn_update_resume);
        progress_bar1 = findViewById(R.id.progress_bar1);
        progress_bar = findViewById(R.id.progress_bar);

        tvAdminName.setText("Welcome, " + sessionManager.getUser());

        // Setup adapters
        projectAdapter = new AdminProjectAdapter(this::clearSelection,
                projectList,
                this::confirmDeleteProject,
                this::openEditProject,true
        );
        rvProjects.setLayoutManager(new LinearLayoutManager(this));
        rvProjects.setAdapter(projectAdapter);
        rvProjects.setNestedScrollingEnabled(false);

        skillAdapter = new AdminSkillAdapter(this::clearSelection,skillList, this::confirmDeleteSkill,true);
        rvSkills.setLayoutManager(new LinearLayoutManager(this));
        rvSkills.setAdapter(skillAdapter);
        rvSkills.setNestedScrollingEnabled(false);


        rvSkills.post(() -> rvSkills.requestLayout());
        rvProjects.post(() -> rvProjects.requestLayout());


        rvProjects.setHasFixedSize(false);
        rvSkills.setHasFixedSize(false);

        loadProjects();
        loadSkills();

        btnAddProject.setOnClickListener(v ->
                startActivity(new Intent(this, AddProjectActivity.class)));

        btnAddSkill.setOnClickListener(v ->
                startActivity(new Intent(this, AddSkillActivity.class)));

        btnUpdateResume.setOnClickListener(v ->
                startActivity(new Intent(this, UpdateResumeActivity.class)));

       /* findViewById(R.id.btn_logout).setOnClickListener(v -> {
            sessionManager.logout();
            startActivity(new Intent(this, MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
        });*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProjects();
        loadSkills();
    }

    // ── Load projects ─────────────────────────────────────────
    private void loadProjects() {
        ApiClient.getService().getProjects().enqueue(new Callback<List<Project>>() {
            @Override
            public void onResponse(Call<List<Project>> call, Response<List<Project>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    progress_bar.setVisibility(GONE);
                    projectList.clear();
                    projectList.addAll(response.body());
                    projectAdapter.notifyDataSetChanged();
                } else {
                    retryLoadProjects();
                }
            }

            @Override
            public void onFailure(Call<List<Project>> call, Throwable t) {
                retryLoadProjects();
            }
        });
    }

    private void retryLoadProjects() {
        new Handler().postDelayed(this::loadProjects, 2000);
        new Handler().postDelayed(this::loadSkills, 2000);
    }

    // ── Load skills ───────────────────────────────────────────
    private void loadSkills() {
        ApiClient.getService().getSkills().enqueue(new Callback<List<Skill>>() {
            @Override
            public void onResponse(Call<List<Skill>> call, Response<List<Skill>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    skillList.clear();
                    skillList.addAll(response.body());
                    skillAdapter.notifyDataSetChanged();
                    progress_bar1.setVisibility(GONE);

                }
            }

            @Override
            public void onFailure(Call<List<Skill>> call, Throwable t) {
                new Handler().postDelayed(() -> loadSkills(), 2000);
            }
        });
    }

    // ── Confirm + Delete project ──────────────────────────────
    private void confirmDeleteProject(Project project) {

        String projectId = project._id;
        Log.d(TAG, "Delete project — _id: " + projectId + " title: " + project.title);
        if (projectId == null || projectId.isEmpty()) {
            Toast.makeText(this, "Error: project ID is null", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Delete Project")
                .setMessage("Delete \"" + project.title + "\"?\nThis cannot be undone.")
                .setPositiveButton("Delete", (d, w) -> deleteProject(projectId))
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void deleteProject(String projectId) {
        Log.d(TAG, "Calling DELETE /api/projects/" + projectId);
        ApiClient.getService()
                .deleteProject(sessionManager.getBearerToken(), projectId)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> r) {
                        if (r.isSuccessful()) {
                            Toast.makeText(AdminDashboardActivity.this,
                                    "✅ Project deleted", Toast.LENGTH_SHORT).show();
                            loadProjects();
                        } else {
                            Log.e(TAG, "Delete project failed: " + r.code());
                            Toast.makeText(AdminDashboardActivity.this,
                                    "Delete failed: " + r.code(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e(TAG, "Delete project error: " + t.getMessage());
                        Toast.makeText(AdminDashboardActivity.this,
                                "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ── Open edit project ─────────────────────────────────────
    private void openEditProject(Project project) {
        Intent intent = new Intent(this, AddProjectActivity.class);
        intent.putExtra("projectId",     project._id);    // ✅ use _id
        intent.putExtra("projectTitle",  project.title);
        intent.putExtra("projectDesc",   project.description);
        intent.putExtra("projectStack",  project.techStack);
        intent.putExtra("projectGithub", project.githubUrl);
        intent.putExtra("projectImage",  project.imageUrl);
        startActivity(intent);
    }

    // ── Confirm + Delete skill ────────────────────────────────
    private void confirmDeleteSkill(Skill skill) {
        String skillId = skill._id;
        Log.d(TAG, "Delete skill — _id: " + skillId + " name: " + skill.name);
        if (skillId == null || skillId.isEmpty()) {
            Toast.makeText(this, "Error: skill ID is null", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Delete Skill")
                .setMessage("Delete \"" + skill.name + "\"?")
                .setPositiveButton("Delete", (d, w) -> deleteSkill(skillId))
                .setNegativeButton("Cancel", null)
                .show();
    }
    public void clearSelection(){

    }


    private void deleteSkill(String skillId) {
        Log.d(TAG, "Calling DELETE /api/skills/" + skillId);
        ApiClient.getService()
                .deleteSkill(sessionManager.getBearerToken(), skillId)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> r) {
                        if (r.isSuccessful()) {
                            Toast.makeText(AdminDashboardActivity.this,
                                    "✅ Skill deleted", Toast.LENGTH_SHORT).show();
                            loadSkills();
                        } else {
                            Log.e(TAG, "Delete skill failed: " + r.code());
                            Toast.makeText(AdminDashboardActivity.this,
                                    "Delete failed: " + r.code(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e(TAG, "Delete skill error: " + t.getMessage());
                        Toast.makeText(AdminDashboardActivity.this,
                                "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}