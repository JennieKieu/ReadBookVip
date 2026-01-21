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

public class AdminChapterAdapter extends RecyclerView.Adapter<AdminChapterAdapter.AdminChapterViewHolder> {

    private List<Chapter> mListChapters;
    private IOnAdminChapterListener mListener;

    public interface IOnAdminChapterListener {
        void onClickEditChapter(Chapter chapter);
        void onClickDeleteChapter(Chapter chapter);
    }

    public AdminChapterAdapter(List<Chapter> listChapters, IOnAdminChapterListener listener) {
        this.mListChapters = listChapters;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public AdminChapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_chapter, parent, false);
        return new AdminChapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminChapterViewHolder holder, int position) {
        Chapter chapter = mListChapters.get(position);
        if (chapter == null) return;

        holder.tvChapterNumber.setText("Chapter " + chapter.getChapterNumber());
        holder.tvChapterTitle.setText(chapter.getTitle() != null && !chapter.getTitle().isEmpty()
                ? chapter.getTitle() : "No title");

        holder.imgEdit.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onClickEditChapter(chapter);
            }
        });

        holder.imgDelete.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onClickDeleteChapter(chapter);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mListChapters != null ? mListChapters.size() : 0;
    }

    public static class AdminChapterViewHolder extends RecyclerView.ViewHolder {
        private TextView tvChapterNumber;
        private TextView tvChapterTitle;
        private ImageView imgEdit;
        private ImageView imgDelete;

        public AdminChapterViewHolder(@NonNull View itemView) {
            super(itemView);
            tvChapterNumber = itemView.findViewById(R.id.tv_chapter_number);
            tvChapterTitle = itemView.findViewById(R.id.tv_chapter_title);
            imgEdit = itemView.findViewById(R.id.img_edit);
            imgDelete = itemView.findViewById(R.id.img_delete);
        }
    }
}

