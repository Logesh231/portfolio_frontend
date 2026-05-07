package com.example.portfolioapp;

import static android.view.View.GONE;

import android.animation.ValueAnimator;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.portfolioapp.model.LoginRequest;
import com.example.portfolioapp.model.LoginResponse;
import com.example.portfolioapp.model.Project;
import com.example.portfolioapp.model.Resume;
import com.example.portfolioapp.model.Skill;
import com.example.portfolioapp.network.ApiClient;
import com.example.portfolioapp.network.SessionManager;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    ScrollView scrollView;

    FirebaseAuth auth;
    LinearLayout sectionAboutMe,get_in_touch,linear_lets_talk,linear_edu,section_skill,lin_photo,lin_pro,whatsappSection,lin_summary,
    lin_edu1,lin_edu2,lin_edu3,lin_cog,port_sec,linear_educ,lin_language;
    LinearLayout btnOpenResume,btnDownloadResume,section_resume,phoneCard,emailCard;
    TextView tvResumeFileName;
    ImageView iv_github_icon,iv_linkedin,iv_web_icon,iv_admin;
    AdminProjectAdapter projectAdapter;
    ProgressBar progress_bar,progress_bar2,progress_bar3;

    private View selectedView = null;
    String resumePdfUrl = null;
    AdminSkillAdapter   skillAdapter;
    private String currentSection = "home";
    SessionManager sessionManager;
    RecyclerView rvProjects, rvSkills;
    List<Project> projectList = new ArrayList<>();
    List<Skill>   skillList   = new ArrayList<>();

    private ActivityResultLauncher<Intent> resumeLauncher;
    String[] summaryPoints = {
            "Passionate Android Developer with expertise in Java and XML.",
            "Experienced in crafting clean, intuitive, and user-friendly UI/UX layouts.",
            "Skilled in integrating RESTful APIs and managing JSON data for smooth communication.",
            "Adept at debugging, improving performance, and optimizing app responsiveness.",
            "Committed to building scalable, maintainable, and visually appealing Android apps."
    };

    int[] summaryIds = {
            R.id.point1, R.id.point2, R.id.point3, R.id.point4, R.id.point5
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);

        auth = FirebaseAuth.getInstance();
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        View toolbar=findViewById(R.id.toolbar);
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

        sessionManager = new SessionManager(this);

        scrollView=findViewById(R.id.scroll_view);
        sectionAboutMe=findViewById(R.id.section_about_me);
        get_in_touch=findViewById(R.id.get_in_touch);
        section_skill=findViewById(R.id.section_skill);
        lin_photo=findViewById(R.id.lin_photo);
        lin_pro=findViewById(R.id.lin_pro);
        whatsappSection=findViewById(R.id.whatsappSection);
        lin_summary=findViewById(R.id.lin_summary);
        lin_edu1=findViewById(R.id.lin_edu1);
        lin_edu2=findViewById(R.id.lin_edu2);
        lin_edu3=findViewById(R.id.lin_edu3);
        lin_cog=findViewById(R.id.lin_cog);
        progress_bar=findViewById(R.id.progress_bar);
        section_resume=findViewById(R.id.section_resume);
        port_sec=findViewById(R.id.port_sec);
        linear_educ=findViewById(R.id.linear_educ);
        lin_language=findViewById(R.id.lin_language);
        progress_bar2=findViewById(R.id.progress_bar2);
        progress_bar3=findViewById(R.id.progress_bar3);

        rvProjects     = findViewById(R.id.rv_projects);
        rvSkills       = findViewById(R.id.rv_skills);

        projectAdapter = new AdminProjectAdapter( this::clearSelection,projectList, this::onDeleteProject, this::onEditProject,false);
        rvProjects.setLayoutManager(new LinearLayoutManager(this));
        rvProjects.setAdapter(projectAdapter);
        rvProjects.setNestedScrollingEnabled(false);

        skillAdapter = new AdminSkillAdapter(this::clearSelection,skillList, this::onDeleteSkill,false);
        rvSkills.setLayoutManager(new LinearLayoutManager(this));
        rvSkills.setAdapter(skillAdapter);
        rvSkills.setNestedScrollingEnabled(false);

        for (int i = 0; i < summaryIds.length; i++) {
            TextView tv = findViewById(summaryIds[i]).findViewById(R.id.tv_summary_text);
            tv.setText(summaryPoints[i]);
        }

        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            int scrollY = scrollView.getScrollY();

            if (scrollY >= get_in_touch.getTop()) {
                currentSection = "contact";
            } else if (scrollY >= section_resume.getTop()) {
                currentSection = "resume";
            } else if (scrollY >= section_skill.getTop()) {
                currentSection = "skills";
            } else if (scrollY >= sectionAboutMe.getTop()) {
                currentSection = "about";
            } else {
                currentSection = "home";
            }
        });

        emailCard = findViewById(R.id.emailCard);
        phoneCard = findViewById(R.id.phoneCard);
        iv_admin=findViewById(R.id.iv_admin);

        iv_admin.setOnClickListener(v -> {
            projectAdapter.clearSelection();
            skillAdapter.clearSelection();
            emailCard.setBackgroundResource(R.drawable.contact_card_bg);
            phoneCard.setBackgroundResource(R.drawable.contact_card_bg);
            lin_photo.setBackgroundResource(R.drawable.contact_card_bg);
            lin_pro.setBackgroundResource(R.drawable.contact_card_bg);
            whatsappSection.setBackgroundResource(R.drawable.contact_card_bg);
            lin_language.setBackgroundResource(R.drawable.contact_card_bg);
            lin_summary.setBackgroundResource(R.drawable.contact_card_bg);
            lin_edu1.setBackgroundResource(R.drawable.contact_card_bg);
            lin_edu2.setBackgroundResource(R.drawable.contact_card_bg);
            lin_edu3.setBackgroundResource(R.drawable.contact_card_bg);
            lin_cog.setBackgroundResource(R.drawable.contact_card_bg);

            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        });

        ImageView ivMenu = findViewById(R.id.iv_menu);
        ivMenu.setOnClickListener(v -> {
            projectAdapter.clearSelection();
            skillAdapter.clearSelection();
            emailCard.setBackgroundResource(R.drawable.contact_card_bg);
            phoneCard.setBackgroundResource(R.drawable.contact_card_bg);
            lin_photo.setBackgroundResource(R.drawable.contact_card_bg);
            lin_pro.setBackgroundResource(R.drawable.contact_card_bg);
            whatsappSection.setBackgroundResource(R.drawable.contact_card_bg);
            lin_language.setBackgroundResource(R.drawable.contact_card_bg);
            lin_summary.setBackgroundResource(R.drawable.contact_card_bg);
            lin_edu1.setBackgroundResource(R.drawable.contact_card_bg);
            lin_edu2.setBackgroundResource(R.drawable.contact_card_bg);
            lin_edu3.setBackgroundResource(R.drawable.contact_card_bg);
            lin_cog.setBackgroundResource(R.drawable.contact_card_bg);
            int scrollY = scrollView.getScrollY();


            if (scrollY >= get_in_touch.getTop()) {
                currentSection = "contact";
            } else if (scrollY >= linear_educ.getTop()) {
                currentSection = "education";

            } else if (scrollY >= section_skill.getTop()) {
                currentSection = "skills";

            } else if (scrollY >= port_sec.getTop()) {
                currentSection = "portfpolio";
            } else if (scrollY >= section_resume.getTop()) {
                currentSection = "resume";
            } else if (scrollY >= sectionAboutMe.getTop()) {
                currentSection = "about";
            } else {
                currentSection = "home";
            }
            MenuDialog menuDialog = new MenuDialog(this,currentSection);
            menuDialog.show();
        });
        linear_lets_talk = findViewById(R.id.linear_lets_talk);
        linear_lets_talk.setOnClickListener(v ->
                new Handler().postDelayed(() -> {
                    projectAdapter.clearSelection();
                    skillAdapter.clearSelection();
                    scrollToabout();
                }, 100)
        );

        LinearLayout btnBookCall = findViewById(R.id.btn_book_call);
        linear_edu = findViewById(R.id.linear_educ);

        btnBookCall.setOnClickListener(v ->
                new Handler().postDelayed(() -> {
                    projectAdapter.clearSelection();
                    skillAdapter.clearSelection();
                    scrollToedu();
                }, 100)
        );

        LinearLayout btnAboutMe = findViewById(R.id.btn_about_me);

        btnAboutMe.setOnClickListener(v ->
                new Handler().postDelayed(() -> {
                    projectAdapter.clearSelection();
                    skillAdapter.clearSelection();
                    scrollToAbout();
                }, 100)
        );
        warmUpServer();


        iv_github_icon=findViewById(R.id.iv_github_icon);
        iv_linkedin=findViewById(R.id.iv_linkedin);
        iv_web_icon=findViewById(R.id.iv_web_icon);

        iv_web_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://logesh231.github.io/Portfolio/";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });

        iv_linkedin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://www.linkedin.com/in/logesh19/";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });
        iv_github_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://github.com/Logesh231";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });

        EditText name = findViewById(R.id.etName);
        EditText email = findViewById(R.id.etEmail);
        EditText message = findViewById(R.id.etMessage);
        LinearLayout send = findViewById(R.id.btnSend);
        send.setOnClickListener(v -> {
            String userName = name.getText().toString().trim();
            String userEmail = email.getText().toString().trim();
            String userMessage = message.getText().toString().trim();
            if (userName.isEmpty() || userEmail.isEmpty() || userMessage.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // WhatsApp Number (with cou ntry code, no +)
            String phoneNumber = "916369636340";

            // Message Format
            String finalMessage =
                    "Hello Sir/Madam,\n\n" +
                            "Name: " + userName + "\n" +
                            "Email: " + userEmail + "\n" +
                            "Message: " + userMessage + "\n\n" +
                            "Thank you.";

            try {
                String url = "https://wa.me/" + phoneNumber + "?text=" + Uri.encode(finalMessage);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
            }
        });

        TextView tvWebsite = findViewById(R.id.tv_website);
        if (tvWebsite != null) {
            tvWebsite.setOnClickListener(v -> openUrl("https://github.com/Logesh231"));
        }

        List<View> allCards = Arrays.asList(
                emailCard,
                phoneCard,
                lin_photo,
                lin_pro,
                whatsappSection,
                lin_summary,
                lin_edu1,
                lin_edu2,
                lin_edu3,
                lin_cog,
                lin_language
        );
        Handler handler = new Handler(Looper.getMainLooper());

        emailCard.setOnClickListener(v -> {
            selectCard(emailCard, allCards);
            handler.postDelayed(() -> {
                projectAdapter.clearSelection();
                skillAdapter.clearSelection();
                Toast.makeText(this, "Opening Email...", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:logeshrio16@gmail.com"));
                startActivity(intent);
            }, 1000);
        });

        lin_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectCard(lin_photo, allCards);
                projectAdapter.clearSelection();
                skillAdapter.clearSelection();

            }
        });
        lin_pro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectCard(lin_pro, allCards);
                projectAdapter.clearSelection();
                skillAdapter.clearSelection();

            }
        });
        whatsappSection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectCard(whatsappSection, allCards);
                projectAdapter.clearSelection();
                skillAdapter.clearSelection();

            }
        });
        phoneCard.setOnClickListener(v -> {
            selectCard(phoneCard, allCards);
            projectAdapter.clearSelection();
            skillAdapter.clearSelection();
            handler.postDelayed(() -> {
                Toast.makeText(this, "Opening Dialer...", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:+919876543210"));
                startActivity(intent);
            }, 1000);
        });

        lin_summary.setOnClickListener(v -> {
            selectCard(lin_summary, allCards);
            projectAdapter.clearSelection();
            skillAdapter.clearSelection();
        });
        lin_edu1.setOnClickListener(v -> {
            selectCard(lin_edu1, allCards);
            projectAdapter.clearSelection();
            skillAdapter.clearSelection();
        });
        lin_edu2.setOnClickListener(v -> {
            selectCard(lin_edu2, allCards);
            projectAdapter.clearSelection();
            skillAdapter.clearSelection();
        });
        lin_edu3.setOnClickListener(v -> {
            selectCard(lin_edu3, allCards);
            projectAdapter.clearSelection();
            skillAdapter.clearSelection();
        });
        lin_cog.setOnClickListener(v -> {
            selectCard(lin_cog, allCards);
            projectAdapter.clearSelection();
            skillAdapter.clearSelection();
        });
        lin_language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                projectAdapter.clearSelection();
                skillAdapter.clearSelection();
                selectCard(lin_language, allCards);
            }
        });
//        lin_language.setOnClickListener(v -> {
//            new Handler(Looper.getMainLooper()).postDelayed(() -> {
//                projectAdapter.clearSelection();
//                skillAdapter.clearSelection();
//                v.post(() -> selectCard(lin_language, allCards));
//            }, 100);
//        });
        tvResumeFileName  = findViewById(R.id.tv_resume_filename);
        btnOpenResume     = findViewById(R.id.btn_open_resume);
        btnDownloadResume = findViewById(R.id.btn_download_resume);
        btnOpenResume.setOnClickListener(v -> {

            if (resumePdfUrl == null) {
                Toast.makeText(this, "Resume not available", Toast.LENGTH_SHORT).show();
                return;
            }

            Uri uri = Uri.parse(resumePdfUrl);

            try {
                // 👉 Try opening with PDF apps
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "application/pdf");
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

                startActivity(Intent.createChooser(intent, "Open Resume with"));

            } catch (Exception e) {
                // 👉 Fallback: open in browser
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(browserIntent);

                    Toast.makeText(this, "Opening in browser...", Toast.LENGTH_SHORT).show();

                } catch (Exception ex) {
                    Toast.makeText(this, "No app found to open resume", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnDownloadResume.setOnClickListener(v -> {
            if (resumePdfUrl == null) {
                Toast.makeText(this, "Resume not loaded yet", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                DownloadManager.Request request =
                        new DownloadManager.Request(Uri.parse(resumePdfUrl));
                request.setTitle("Logesh_Resume.pdf");
                request.setDescription("Downloading...");
                request.setNotificationVisibility(
                        DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS, "Logesh_Resume.pdf");
                DownloadManager dm =
                        (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                dm.enqueue(request);
                Toast.makeText(this, "Downloading... check notifications",
                        Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Download failed: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
        resumeLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == RESULT_OK) {
                                Intent data = result.getData();
                                if (data != null) {
                                    String section = data.getStringExtra("section");
                                    if (section != null) {
                                        currentSection = section;
                                        new MenuDialog(this, currentSection).show();
                                    }
                                }

                            }
                        });
        loadProjects();
        loadSkills();
        loadResume();
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadProjects();
        loadSkills();
        loadResume();
    }

    private void selectCard(View clicked, List<View> allCards) {

        // 👉 If same view clicked again → UNSELECT
        if (selectedView == clicked) {
            clicked.setBackgroundResource(R.drawable.contact_card_bg);
            selectedView = null;
            return;
        }

        // 👉 Reset all
        for (View v : allCards) {
            v.setBackgroundResource(R.drawable.contact_card_bg);
        }

        // 👉 Select new
        clicked.setBackgroundResource(R.drawable.contact_card_selected);
        selectedView = clicked;
    }

    private void warmUpServer() {
        ApiClient.getService().getProjects().enqueue(new Callback<List<Project>>() {
            @Override
            public void onResponse(Call<List<Project>> call, Response<List<Project>> response) {
                loadProjects();
            }

            @Override
            public void onFailure(Call<List<Project>> call, Throwable t) {
                new Handler().postDelayed(() -> loadProjects(), 2000);
            }
        });
    }
    public void scrollToTop() {
        new Handler().postDelayed(() ->
                smoothScrollSlowly(0), 100);
    }

    public void scrollToPort() {
        new Handler().postDelayed(() ->
                smoothScrollSlowly(port_sec.getTop()), 100);
    }

    public void scrollToAbout() {
        new Handler().postDelayed(() ->
                smoothScrollSlowly(sectionAboutMe.getTop()), 100);
    }

    public void scrollToskills() {
        new Handler().postDelayed(() ->
                smoothScrollSlowly(section_skill.getTop()), 100);
    }

    public void scrollToabout() {
        new Handler().postDelayed(() ->
                smoothScrollSlowly(get_in_touch.getTop()), 100);
    }

    public void scrollToResume() {
        new Handler().postDelayed(() ->
                smoothScrollSlowly(section_resume.getTop()), 100);
    }

    public void scrollToedu() {
        new Handler().postDelayed(() ->
                smoothScrollSlowly(linear_educ.getTop()), 100);
    }
//    public void scrollToTop() {
//        new Handler().postDelayed(() ->
//                scrollView.smoothScrollTo(0, 0), 100);
//    }
//    public void scrollToPort() {
//        new Handler().postDelayed(() ->
//                scrollView.smoothScrollTo(0, port_sec.getTop()), 100);
//    }
//    public void scrollToAbout() {
//        new Handler().postDelayed(() ->
//                scrollView.smoothScrollTo(0, sectionAboutMe.getTop()), 100);
//    }
//    public void scrollToskills() {
//        new Handler().postDelayed(() ->
//                scrollView.smoothScrollTo(0, section_skill.getTop()), 100);
//    }
//    public void scrollToabout() {
//        new Handler().postDelayed(() ->
//                scrollView.smoothScrollTo(0, get_in_touch.getTop()), 100);
//    }
//    public void scrollToResume() {
//        new Handler().postDelayed(() ->
//                scrollView.smoothScrollTo(0, section_resume.getTop()), 100);
//    }
//    public void scrollToedu() {
//        new Handler().postDelayed(() ->
//                scrollView.smoothScrollTo(0, linear_educ.getTop()), 100);
//    }
private void smoothScrollSlowly(int targetY) {

    int startY = scrollView.getScrollY();

    ValueAnimator animator = ValueAnimator.ofInt(startY, targetY);

    animator.setDuration(900); // scroll speed

    animator.setInterpolator(new DecelerateInterpolator());

    animator.addUpdateListener(animation -> {
        int value = (int) animation.getAnimatedValue();
        scrollView.scrollTo(0, value);
    });

    animator.start();
}
    public void clearSelection(){
        selectedView = null; // ✅ VERY IMPORTANT

        emailCard.setBackgroundResource(R.drawable.contact_card_bg);
        phoneCard.setBackgroundResource(R.drawable.contact_card_bg);
        lin_photo.setBackgroundResource(R.drawable.contact_card_bg);
        lin_pro.setBackgroundResource(R.drawable.contact_card_bg);
        whatsappSection.setBackgroundResource(R.drawable.contact_card_bg);
        lin_language.setBackgroundResource(R.drawable.contact_card_bg);
        lin_summary.setBackgroundResource(R.drawable.contact_card_bg);
        lin_edu1.setBackgroundResource(R.drawable.contact_card_bg);
        lin_edu2.setBackgroundResource(R.drawable.contact_card_bg);
        lin_edu3.setBackgroundResource(R.drawable.contact_card_bg);
        lin_cog.setBackgroundResource(R.drawable.contact_card_bg);
    }



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
        new Handler().postDelayed(this::loadResume, 2000);
    }





    private void onEditProject(Project project) {
        Intent intent = new Intent(this, AddProjectActivity.class);
        intent.putExtra("projectId",    project._id);
        intent.putExtra("projectTitle", project.title);
        intent.putExtra("projectDesc",  project.description);
        intent.putExtra("projectStack", project.techStack);
        startActivity(intent);
    }
    private void onDeleteProject(Project project) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Project")
                .setMessage("Delete \"" + project.title + "\"?")
                .setPositiveButton("Delete", (d, w) -> {
                    ApiClient.getService()
                            .deleteProject(sessionManager.getBearerToken(), project._id)
                            .enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> r) {
                                    Toast.makeText(MainActivity.this,
                                            "Deleted", Toast.LENGTH_SHORT).show();
                                    loadProjects();
                                }
                                @Override public void onFailure(Call<Void> call, Throwable t) {}
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

private void loadSkills() {
    ApiClient.getService().getSkills().enqueue(new Callback<List<Skill>>() {
        @Override
        public void onResponse(Call<List<Skill>> call, Response<List<Skill>> response) {
            if (response.isSuccessful() && response.body() != null) {
                progress_bar2.setVisibility(GONE);
                skillList.clear();
                skillList.addAll(response.body());
                skillAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onFailure(Call<List<Skill>> call, Throwable t) {
            new Handler().postDelayed(() -> loadSkills(), 2000);
        }
    });
}
    private void onDeleteSkill(Skill skill) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Skill")
                .setMessage("Delete \"" + skill.name + "\"?")
                .setPositiveButton("Delete", (d, w) -> {
                    ApiClient.getService()
                            .deleteSkill(sessionManager.getBearerToken(), skill._id)
                            .enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> r) {
                                    loadSkills();
                                }
                                @Override public void onFailure(Call<Void> call, Throwable t) {}
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

//    public void openResumeActivity() {
//        resumeLauncher.launch(new Intent(this, ResumeActivity.class));
//    }
    private void loadResume() {
        ApiClient.getService().getResume().enqueue(new Callback<Resume>() {
            @Override
            public void onResponse(Call<Resume> call, Response<Resume> response) {
                if (response.isSuccessful() && response.body() != null) {
                    progress_bar3.setVisibility(GONE);
                    resumePdfUrl = response.body().pdfUrl;
                    String name  = response.body().originalName;
                    tvResumeFileName.setText("📄 " + (name != null ? name : "resume.pdf"));
                } else {
                    tvResumeFileName.setText("No resume uploaded yet");
                }
            }
            @Override
            public void onFailure(Call<Resume> call, Throwable t) {
                tvResumeFileName.setText("Could not load resume");
            }
        });
    }

    private void openUrl(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }
}