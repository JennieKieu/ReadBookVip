package com.example.book.adapter.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.book.R;
import com.example.book.model.Chapter;

import java.util.List;

public class InlineChapterAdapter extends RecyclerView.Adapter<InlineChapterAdapter.ChapterViewHolder> {

    private List<Chapter> chapters;
    private IClickListener listener;

    public interface IClickListener {
        void onClickEdit(Chapter chapter, int position);
        void onClickDelete(Chapter chapter, int position);
    }

    public InlineChapterAdapter(List<Chapter> chapters, IClickListener listener) {
        this.chapters = chapters;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inline_chapter, parent, false);
        return new ChapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChapterViewHolder holder, int position) {
        Chapter chapter = chapters.get(position);
        if (chapter == null) return;

        holder.tvChapterNumber.setText("Chapter " + chapter.getChapterNumber());
        holder.tvChapterTitle.setText(chapter.getTitle() != null && !chapter.getTitle().isEmpty() 
                ? chapter.getTitle() 
                : "No title");

        holder.imgEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClickEdit(chapter, holder.getAdapterPosition());
            }
        });

        holder.imgDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClickDelete(chapter, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return chapters != null ? chapters.size() : 0;
    }

    public void updateData(List<Chapter> newChapters) {
        this.chapters = newChapters;
        notifyDataSetChanged();
    }

    public static class ChapterViewHolder extends RecyclerView.ViewHolder {
        private TextView tvChapterNumber;
        private TextView tvChapterTitle;
        private ImageView imgEdit;
        private ImageView imgDelete;

        public ChapterViewHolder(@NonNull View itemView) {
            super(itemView);
            tvChapterNumber = itemView.findViewById(R.id.tv_chapter_number);
            tvChapterTitle = itemView.findViewById(R.id.tv_chapter_title);
            imgEdit = itemView.findViewById(R.id.img_edit);
            imgDelete = itemView.findViewById(R.id.img_delete);
        }
    }
}

