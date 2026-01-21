package com.example.book.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.book.R;
import com.example.book.listener.IOnChapterClickListener;
import com.example.book.model.Chapter;

import java.util.List;

public class ChapterAdapter extends RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder> {

    private final List<Chapter> mListChapters;
    private final IOnChapterClickListener iOnChapterClickListener;
    private int currentChapterIndex = -1;

    public ChapterAdapter(List<Chapter> mListChapters, IOnChapterClickListener iOnChapterClickListener) {
        this.mListChapters = mListChapters;
        this.iOnChapterClickListener = iOnChapterClickListener;
    }
    
    public void setCurrentChapterIndex(int index) {
        int oldIndex = currentChapterIndex;
        currentChapterIndex = index;
        if (oldIndex >= 0 && oldIndex < getItemCount()) {
            notifyItemChanged(oldIndex);
        }
        if (currentChapterIndex >= 0 && currentChapterIndex < getItemCount()) {
            notifyItemChanged(currentChapterIndex);
        }
    }

    @NonNull
    @Override
    public ChapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chapter, parent, false);
        return new ChapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChapterViewHolder holder, int position) {
        Chapter chapter = mListChapters.get(position);
        if (chapter == null) return;

        holder.tvChapterNumber.setText(String.format("Chapter %d", chapter.getChapterNumber()));
        holder.tvChapterTitle.setText(chapter.getTitle());

        // Highlight current chapter
        boolean isCurrentChapter = (position == currentChapterIndex);
        View layoutItem = holder.itemView.findViewById(R.id.layout_chapter_item);
        if (isCurrentChapter) {
            // Highlight current chapter
            if (layoutItem != null) {
                layoutItem.setBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.colorPrimary)
                );
            }
            holder.tvChapterNumber.setTextColor(
                ContextCompat.getColor(holder.itemView.getContext(), R.color.white)
            );
            holder.tvChapterTitle.setTextColor(
                ContextCompat.getColor(holder.itemView.getContext(), R.color.white)
            );
            holder.imgReadingIndicator.setVisibility(View.VISIBLE);
        } else {
            // Normal state
            if (layoutItem != null) {
                layoutItem.setBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.white)
                );
            }
            holder.tvChapterNumber.setTextColor(
                ContextCompat.getColor(holder.itemView.getContext(), R.color.colorPrimary)
            );
            holder.tvChapterTitle.setTextColor(
                ContextCompat.getColor(holder.itemView.getContext(), R.color.textColorPrimary)
            );
            holder.imgReadingIndicator.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (iOnChapterClickListener != null) {
                iOnChapterClickListener.onChapterClick(chapter, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mListChapters == null ? 0 : mListChapters.size();
    }

    public static class ChapterViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvChapterNumber;
        private final TextView tvChapterTitle;
        private final ImageView imgReadingIndicator;

        public ChapterViewHolder(@NonNull View itemView) {
            super(itemView);
            tvChapterNumber = itemView.findViewById(R.id.tv_chapter_number);
            tvChapterTitle = itemView.findViewById(R.id.tv_chapter_title);
            imgReadingIndicator = itemView.findViewById(R.id.img_reading_indicator);
        }
    }
}


