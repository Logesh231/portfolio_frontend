package com.example.portfolioapp;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.SystemClock;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.portfolioapp.model.Project;

import java.util.List;

public class AdminProjectAdapter extends RecyclerView.Adapter<AdminProjectAdapter.ViewHolder> {

    public interface OnDeleteClick { void onDelete(Project project); }
    public interface OnEditClick   { void onEdit(Project project);   }

    private final List<Project> list;
    private final OnDeleteClick onDelete;
    private final OnEditClick   onEdit;
    private final boolean isAdmin;
    private View previousSelectedView = null;
    private int selectedPosition = -1;
    private MainActivity activity;

    private static final float SCALE_NORMAL   = 1.0f;
    private static final float SCALE_SELECTED = 1.05f;
    private static final long  ANIM_DURATION  = 200;
    private static final long CLICK_DEBOUNCE_MS = 250;
    private long lastClickTime = 0;
    public interface OnClearSelectionListener {
        void onClearSelection();
    }
    private final OnClearSelectionListener clearListener;
    public AdminProjectAdapter(OnClearSelectionListener clearListener,List<Project> list,
                               OnDeleteClick onDelete,
                               OnEditClick onEdit,
                               boolean isAdmin) {
        this.clearListener = clearListener;
        this.list     = list;
        this.onDelete = onDelete;
        this.onEdit   = onEdit;
        this.isAdmin  = isAdmin;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_project, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Project p = list.get(position);
        h.tvTitle.setText(p.title);
        h.tvDesc.setText(p.description);
        h.tvStack.setText(p.techStack != null ? "🛠 " + p.techStack : "");

        // Image
        if (p.imageUrl != null && !p.imageUrl.isEmpty()) {
            h.ivImage.setVisibility(View.VISIBLE);

            Glide.with(h.itemView.getContext())
                    .load(p.imageUrl)
                    .fitCenter()
                    .dontAnimate()
                    .into(h.ivImage);

        } else {
            h.ivImage.setVisibility(View.GONE);
        }


        h.tvDesc.setText(p.description);

        h.tvDesc.setMaxLines(p.isExpanded ? Integer.MAX_VALUE : 2);



        h.tvDesc.post(() -> {
            if (h.tvDesc.getLineCount() > 3) {

                String fullText = p.description;

                int end = h.tvDesc.getLayout().getLineEnd(1);
                String shortText = fullText.substring(0, Math.max(0, end - 5));

                if (!p.isExpanded) {
                    // 👉 COLLAPSED
                    String collapsed = shortText + "... More";
                    SpannableString spannable = new SpannableString(collapsed);

                    int moreStart = collapsed.indexOf("More");

                    spannable.setSpan(
                            new ForegroundColorSpan(
                                    ContextCompat.getColor(h.itemView.getContext(), R.color.blue_more)
                            ),
                            moreStart,
                            moreStart + 4,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    );

                    h.tvDesc.setText(spannable);
                    h.tvDesc.setMaxLines(3);

                } else {
                    String expanded = fullText + "  Less";
                    SpannableString spannable = new SpannableString(expanded);
                    int lessStart = expanded.indexOf("Less");
                    spannable.setSpan(
                            new ForegroundColorSpan(
                                    ContextCompat.getColor(h.itemView.getContext(), R.color.blue_less)
                            ),
                            lessStart,
                            lessStart + 4,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    );

                    h.tvDesc.setText(spannable);
                    h.tvDesc.setMaxLines(Integer.MAX_VALUE);
                }
                h.tvDesc.setOnClickListener(v -> {
                    p.isExpanded = !p.isExpanded;
                    notifyItemChanged(position);
                });
            }
        });
        if (h.ivMore != null) {
            h.ivMore.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
            h.ivMore.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(v.getContext(), h.ivMore);
                popup.inflate(R.menu.menu_project);
                popup.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.menu_edit) {
                        onEdit.onEdit(p);
                        return true;
                    } else if (item.getItemId() == R.id.menu_delete) {
                        onDelete.onDelete(p);
                        return true;
                    }

                    return false;
                });

                popup.show();
            });

        }
        boolean isSelected = (position == selectedPosition);
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
//        h.itemView.setOnClickListener(v -> {
//            if (clearListener != null) {
//                clearListener.onClearSelection();
//            }
//            long now = SystemClock.elapsedRealtime();
//            if (now - lastClickTime < CLICK_DEBOUNCE_MS) return;
//            lastClickTime = now;
//            int pos = h.getBindingAdapterPosition();
//            if (pos == RecyclerView.NO_POSITION) return;
//
//            int prevSelected = selectedPosition;
//
//            if (selectedPosition == pos) {
//                selectedPosition = -1;
//                animateItem(h.itemView, SCALE_SELECTED, SCALE_NORMAL);
//                h.itemView.setBackgroundResource(R.drawable.contact_card_bg);
//                previousSelectedView = null;
//            } else {
//                selectedPosition = pos;
//                animateItem(h.itemView, SCALE_NORMAL, SCALE_SELECTED);
//                h.itemView.setBackgroundResource(R.drawable.contact_card_selected);
//                if (previousSelectedView != null) {
//                    animateItem(previousSelectedView, SCALE_SELECTED, SCALE_NORMAL);
////                    previousSelectedView.setBackgroundResource(R.drawable.contact_card_bg);
//                }
//                previousSelectedView = h.itemView;
//            }
//
//
//            if (now - lastClickTime < CLICK_DEBOUNCE_MS) return;
//            lastClickTime = now;
//
//            Context context = v.getContext();
////
//            new AlertDialog.Builder(context)
//                    .setTitle("Download")
//                    .setMessage("Do you want to open this project link?")
//                    .setPositiveButton("OK", (dialog, which) -> {
//
//                        if (p.githubUrl != null && !p.githubUrl.isEmpty()) {
//                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(p.githubUrl));
//                            context.startActivity(intent);
//                            selectedPosition = -1;
//
//                            animateItem(h.itemView, SCALE_SELECTED, SCALE_NORMAL);
//                            h.itemView.setBackgroundResource(R.drawable.contact_card_bg);
//
//                            previousSelectedView = null;
//                        }
//
//                    })
//                    .setNegativeButton("Cancel", (dialog, which) -> {
//
//                        // ✅ Reset selection + animation
//                        selectedPosition = -1;
//
//                        animateItem(h.itemView, SCALE_SELECTED, SCALE_NORMAL);
//                        h.itemView.setBackgroundResource(R.drawable.contact_card_bg);
//
//                        previousSelectedView = null;
//                    })
//                    .show();
//        });
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

            // ✅ Selection logic
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
            if(!isAdmin){
                Context context = v.getContext();
                String message;

                if (p.techStack != null && p.techStack.equalsIgnoreCase("web")) {
                    message = "Do you want to open this website?";
                } else {
                    message = "Do you want to open this project link?";
                }

                AlertDialog dialog = new AlertDialog.Builder(context)

                        .setTitle("Project")
                        .setMessage(message)
//                        .setPositiveButton("OK", (dialogInterface, which) -> {
//                            if (p.githubUrl != null && !p.githubUrl.isEmpty()) {
//                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(p.githubUrl));
//                                context.startActivity(intent);
//                                selectedPosition = -1;
//                                animateItem(h.itemView, SCALE_SELECTED, SCALE_NORMAL);
//                                h.itemView.setBackgroundResource(R.drawable.contact_card_bg);
//                                previousSelectedView = null;
//                            }
//                        })
                        .setPositiveButton("OK", (dialogInterface, which) -> {
                            if (p.githubUrl != null && !p.githubUrl.isEmpty()) {
                                String url = p.githubUrl.trim();

                                // Ensure scheme exists
                                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                                    url = "https://" + url;
                                }

                                try {
                                    Uri uri = Uri.parse(url);

                                    // Validate URL format
                                    if (Patterns.WEB_URL.matcher(url).matches()) {
                                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);

                                        // Check if there's an app to handle it
                                        if (intent.resolveActivity(context.getPackageManager()) != null) {
                                            context.startActivity(intent);
                                        } else {
                                            Toast.makeText(context, "No app found to open link", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(context, "Invalid URL", Toast.LENGTH_SHORT).show();
                                    }

                                } catch (Exception e) {
                                    Toast.makeText(context, "Invalid URL format", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }

                                selectedPosition = -1;
                                animateItem(h.itemView, SCALE_SELECTED, SCALE_NORMAL);
                                h.itemView.setBackgroundResource(R.drawable.contact_card_bg);
                                previousSelectedView = null;
                            }else {
                                Toast.makeText(context, "Please contact admin for update url", Toast.LENGTH_SHORT).show();
                            }
                        })

                        .setNegativeButton("Cancel", (dialogInterface, which) -> {
                            selectedPosition = -1;
                            animateItem(h.itemView,SCALE_SELECTED,SCALE_NORMAL);
                            h.itemView.setBackgroundResource(R.drawable.contact_card_bg);
                            previousSelectedView=null;
                        })
                        .create();

                dialog.setOnCancelListener(dialogInterface -> {
                    selectedPosition = -1;
                    animateItem(h.itemView, SCALE_SELECTED, SCALE_NORMAL);
                    h.itemView.setBackgroundResource(R.drawable.contact_card_bg);
                    previousSelectedView = null;
                });

                dialog.show();
            }


        });
    }


    public void clearSelection() {
        int prev = selectedPosition;
        selectedPosition = -1;
//        previousSelectedView = null;
        if (prev != -1) {
            animateItem(previousSelectedView, SCALE_SELECTED, SCALE_NORMAL);
//            notifyItemChanged(prev);
            previousSelectedView.setBackgroundResource(R.drawable.contact_card_bg);
        }
    }
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


        TextView tvTitle, tvDesc, tvStack;
        ImageView ivImage, ivMore;
        LinearLayout card_root;

        ViewHolder(View v) {
            super(v);
            ivMore       = v.findViewById(R.id.iv_more);
            ivImage      = v.findViewById(R.id.iv_project_image);
            tvTitle      = v.findViewById(R.id.tv_project_title);
            tvDesc       = v.findViewById(R.id.tv_project_desc);
            tvStack      = v.findViewById(R.id.tv_project_stack);
            card_root    = v.findViewById(R.id.card_root);
        }
    }
}
