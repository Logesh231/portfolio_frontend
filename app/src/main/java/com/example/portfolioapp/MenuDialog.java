package com.example.portfolioapp;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class MenuDialog extends Dialog {

    private final MainActivity activity;
    private final String currentSection;
    public MenuDialog(@NonNull MainActivity activity, String currentSection) {
        super(activity, R.style.MenuDialogTheme);
        this.activity = activity;
        this.currentSection = currentSection;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_menu);

        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.TOP | Gravity.START;
            params.width   = (int) (getContext().getResources()
                    .getDisplayMetrics().widthPixels * 0.60f);
            params.height  = WindowManager.LayoutParams.MATCH_PARENT;
            params.x = 0;
            params.y = 0;
            window.setAttributes(params);
            window.setWindowAnimations(R.style.MenuSlideAnimation);
        }

        LinearLayout menuContainer = findViewById(R.id.menu_container);
        Animation slideDown = AnimationUtils.loadAnimation(getContext(), R.anim.slide_down);
        menuContainer.startAnimation(slideDown);
        ImageView ivClose = findViewById(R.id.iv_close);
        ivClose.setOnClickListener(v -> dismiss());
        setupMenuItems();
    }
    private void setupMenuItems() {
        TextView navHome      = findViewById(R.id.nav_home);
        TextView navAbout     = findViewById(R.id.nav_about);
        TextView navResume    = findViewById(R.id.nav_resume);
        TextView navPortfolio = findViewById(R.id.nav_portfolio);
        TextView navSkills    = findViewById(R.id.nav_skills);
        TextView navContact   = findViewById(R.id.nav_contact);
        TextView nav_edu   = findViewById(R.id.nav_edu);
        View lineHome      = findViewById(R.id.line_home);
        View lineAbout     = findViewById(R.id.line_about);
        View lineResume    = findViewById(R.id.line_resume);
        View linePortfolio = findViewById(R.id.line_portfolio);
        View lineSkills    = findViewById(R.id.line_skills);
        View lineContact   = findViewById(R.id.line_contact);
        View line_edu     = findViewById(R.id.line_edu);


        TextView[] allTexts = {
                navHome, navAbout, navResume,
                navPortfolio, navSkills, navContact,nav_edu
        };

        View[] allLines = {
                lineHome, lineAbout, lineResume,
                linePortfolio, lineSkills, lineContact,line_edu
        };
        switch (currentSection) {
            case "about":
                setActiveMenu(navAbout, lineAbout, allTexts, allLines);
                break;

            case "skills":
                setActiveMenu(navSkills, lineSkills, allTexts, allLines);
                break;

            case "contact":
                setActiveMenu(navContact, lineContact, allTexts, allLines);
                break;

            case "portfpolio":
                setActiveMenu(navPortfolio, linePortfolio, allTexts, allLines);
                break;

            case "resume":
                setActiveMenu(navResume, lineResume, allTexts, allLines);
                break;

            case "education":
                setActiveMenu(nav_edu, line_edu, allTexts, allLines);
                break;

            default:
                setActiveMenu(navHome, lineHome, allTexts, allLines);
        }

        navHome.setOnClickListener(v -> {
            dismiss();
            activity.scrollToTop();
        });
        navContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                activity.scrollToabout();

            }
        });

        navSkills.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                activity.scrollToskills();
            }
        });
        navPortfolio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                activity.scrollToPort();
            }
        });

        navAbout.setOnClickListener(v -> {
            dismiss();
            activity.scrollToAbout();
        });
        navResume.setOnClickListener(v -> {
            dismiss();
            activity.scrollToResume();
        });

        nav_edu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                activity.scrollToedu();
            }
        });


    }

    private void setActiveMenu(
            TextView activeText,
            View activeLine,
            TextView[] allTexts,
            View[] allLines
    ) {
        for (TextView tv : allTexts) {
            tv.setTextColor(0xFFFFFFFF);
        }

        for (View v : allLines) {
            v.setVisibility(View.GONE);
        }

        activeText.setTextColor(0xFF7C9ED9);
        activeLine.setVisibility(View.VISIBLE);
    }

//    private void openUrl(String url) {
//        getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
//    }
}