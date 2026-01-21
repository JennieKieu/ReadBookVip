package com.example.book.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.book.R;
import com.example.book.listener.IOnChapterClickListener;
import com.example.book.model.Chapter;

import java.util.List;

public class ChapterAdapter extends RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder> {

    private final List<Chapter> mListChapters;
    private final IOnChapterClickListener iOnChapterClickListener;

    public ChapterAdapter(List<Chapter> mListChapters, IOnChapterClickListener iOnChapterClickListener) {
        this.mListChapters = mListChapters;
        this.iOnChapterClickListener = iOnChapterClickListener;
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

        holder.tvChapterNumber.setText(String.format("Chương %d", chapter.getChapterNumber()));
        holder.tvChapterTitle.setText(chapter.getTitle());

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

        public ChapterViewHolder(@NonNull View itemView) {
            super(itemView);
            tvChapterNumber = itemView.findViewById(R.id.tv_chapter_number);
            tvChapterTitle = itemView.findViewById(R.id.tv_chapter_title);
        }
    }
}


