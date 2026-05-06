//package com.example.portfolioapp;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.ProgressBar;
//import android.widget.TextView;
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//import com.example.portfolioapp.model.Skill;
//import java.util.List;
//
//public class AdminSkillAdapter extends RecyclerView.Adapter<AdminSkillAdapter.ViewHolder> {
//
//    public interface OnDeleteClick { void onDelete(Skill skill); }
//
//    private List<Skill> list;
//    private OnDeleteClick onDelete;
//    private Boolean showImage;
//    public AdminSkillAdapter(List<Skill> list, OnDeleteClick onDelete,Boolean showImage) {
//        this.list     = list;
//        this.onDelete = onDelete;
//        this.showImage=showImage;
//    }
//
//    @NonNull @Override
//    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View v = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.item_admin_skill, parent, false);
//        return new ViewHolder(v);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
//        Skill s = list.get(position);
//        h.tvName.setText(s.name);
//        h.tvCategory.setText(s.category);
//        h.tvPercent.setText(s.percentage + "%");
//        h.progressBar.setProgress(s.percentage);
//        h.ivDelete.setOnClickListener(v -> onDelete.onDelete(s));
//        if (showImage) {
//            h.ivDelete.setVisibility(View.VISIBLE);
//        } else {
//            h.ivDelete.setVisibility(View.GONE);
//        }
//    }
//
//    @Override public int getItemCount() { return list.size(); }
//
//    static class ViewHolder extends RecyclerView.ViewHolder {
//        TextView  tvName, tvCategory, tvPercent;
//        ProgressBar progressBar;
//        ImageView ivDelete;
//        ViewHolder(View v) {
//            super(v);
//            tvName      = v.findViewById(R.id.tv_skill_name);
//            tvCategory  = v.findViewById(R.id.tv_skill_category);
//            tvPercent   = v.findViewById(R.id.tv_skill_percent);
//            progressBar = v.findViewById(R.id.progress_skill);
//            ivDelete    = v.findViewById(R.id.iv_delete);
//        }
//    }
//}

package com.example.portfolioapp;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.portfolioapp.model.Skill;

import java.util.List;

public class AdminSkillAdapter extends RecyclerView.Adapter<AdminSkillAdapter.ViewHolder> {

    public interface OnDeleteClick { void onDelete(Skill skill); }

    private final List<Skill> list;
    private final OnDeleteClick onDelete;
    private final boolean isAdmin;

    private int selectedPosition = -1;
    private View previousSelectedView = null;

    private static final float SCALE_NORMAL   = 1.0f;
    private static final float SCALE_SELECTED = 1.06f;
    private static final long  ANIM_DURATION  = 250;

    private static final long CLICK_DEBOUNCE_MS = 250;
    private long lastClickTime = 0;
    public interface OnClearSelectionListener {
        void onClearSelection();
    }
    private final OnClearSelectionListener clearListener;
    public AdminSkillAdapter(OnClearSelectionListener clearListener, List<Skill> list, OnDeleteClick onDelete, boolean isAdmin) {
        this.list = list;
        this.onDelete = onDelete;
        this.isAdmin = isAdmin;
        this.clearListener=clearListener;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_skill, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Skill s = list.get(position);

        h.tvName.setText(s.name);
        h.tvCategory.setText(s.category);
        h.tvPercent.setText(s.percentage + "%");
        h.progressBar.setProgress(s.percentage);

        if (h.ivDelete != null) {
            h.ivDelete.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
            h.ivDelete.setOnClickListener(v -> onDelete.onDelete(s));
        }

        boolean isSelected = (position == selectedPosition);

        // cancel animations
        h.itemView.animate().cancel();
        Object tag = h.itemView.getTag();
        if (tag instanceof AnimatorSet) {
            ((AnimatorSet) tag).cancel();
        }
        h.itemView.setBackgroundResource(
                isSelected ? R.drawable.contact_card_selected
                        : R.drawable.contact_card_bg
        );
        h.itemView.setScaleX(isSelected ? SCALE_SELECTED : SCALE_NORMAL);
        h.itemView.setScaleY(isSelected ? SCALE_SELECTED : SCALE_NORMAL);
        ViewCompat.setElevation(h.itemView, isSelected ? 8f : 0f);
        h.itemView.setOnClickListener(v -> {

            if (clearListener != null) {
                clearListener.onClearSelection();
            }

            long now = SystemClock.elapsedRealtime();
            if (now - lastClickTime < CLICK_DEBOUNCE_MS) return;
            lastClickTime = now;
            int pos = h.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            int prevSelected = selectedPosition;
            if (selectedPosition == pos) {
                selectedPosition = -1;
                animateItem(h.itemView, SCALE_SELECTED, SCALE_NORMAL);
                h.itemView.setBackgroundResource(R.drawable.contact_card_bg);
                previousSelectedView = null;
            } else {
                selectedPosition = pos;
                animateItem(h.itemView, SCALE_NORMAL, SCALE_SELECTED);
                h.itemView.setBackgroundResource(R.drawable.contact_card_selected);
                if (previousSelectedView != null) {
                    animateItem(previousSelectedView, SCALE_SELECTED, SCALE_NORMAL);
                    previousSelectedView.setBackgroundResource(R.drawable.contact_card_bg);
                }
                previousSelectedView = h.itemView;
            }
            if (prevSelected != -1) {
                notifyItemChanged(prevSelected);
            }
        });
    }

    public void clearSelection() {
        if (selectedPosition != -1) {
            int prev = selectedPosition;
            selectedPosition = -1;

            notifyItemChanged(prev); // ✅ let RecyclerView handle UI reset
        }

        previousSelectedView = null; // ✅ reset reference
    }
//    public void clearSelection() {
//        animateItem(previousSelectedView, SCALE_SELECTED, SCALE_NORMAL);
//        int prev = selectedPosition;
//        selectedPosition = -1;
////        previousSelectedView = null;
//        if (prev != -1) {
//            previousSelectedView.setBackgroundResource(R.drawable.contact_card_bg);
////            notifyItemChanged(prev);
//        }
//    }



    private void animateItem(View view, float from, float to) {
        view.animate().cancel();
        Object tag = view.getTag();
        if (tag instanceof AnimatorSet) {
            ((AnimatorSet) tag).cancel();
        }
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", from, to);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", from, to);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        set.setDuration(ANIM_DURATION);
        set.setInterpolator(new DecelerateInterpolator());
        set.start();
        view.setTag(set);
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        Object tag = holder.itemView.getTag();
        if (tag instanceof AnimatorSet) {
            ((AnimatorSet) tag).cancel();
        }
        holder.itemView.animate().cancel();
        holder.itemView.setScaleX(SCALE_NORMAL);
        holder.itemView.setScaleY(SCALE_NORMAL);
        ViewCompat.setElevation(holder.itemView, 0f);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCategory, tvPercent;
        ProgressBar progressBar;
        ImageView ivDelete;
        ViewHolder(View v) {
            super(v);
            tvName      = v.findViewById(R.id.tv_skill_name);
            tvCategory  = v.findViewById(R.id.tv_skill_category);
            tvPercent   = v.findViewById(R.id.tv_skill_percent);
            progressBar = v.findViewById(R.id.progress_skill);
            ivDelete    = v.findViewById(R.id.iv_delete);
        }
    }
}