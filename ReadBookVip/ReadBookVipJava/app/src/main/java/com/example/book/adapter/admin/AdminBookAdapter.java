package com.example.book.adapter.admin;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.book.databinding.ItemAdminBookBinding;
import com.example.book.listener.IOnAdminManagerBookListener;
import com.example.book.model.Book;
import com.example.book.utils.GlideUtils;

import java.util.List;

public class AdminBookAdapter extends RecyclerView.Adapter<AdminBookAdapter.AdminBookViewHolder> {

    private final List<Book> mListBooks;
    public final IOnAdminManagerBookListener mListener;

    public AdminBookAdapter(List<Book> list, IOnAdminManagerBookListener listener) {
        this.mListBooks = list;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public AdminBookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminBookBinding binding = ItemAdminBookBinding
                .inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new AdminBookViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminBookViewHolder holder, int position) {
        Book book = mListBooks.get(position);
        if (book == null) return;
        GlideUtils.loadUrl(book.getImage(), holder.mBinding.imgBook);
        holder.mBinding.tvName.setText(book.getTitle());
        holder.mBinding.tvCategory.setText(book.getCategoryName());
        if (book.isFeatured()) {
            holder.mBinding.tvFeatured.setText("Yes");
        } else {
            holder.mBinding.tvFeatured.setText("No");
        }

        holder.mBinding.imgEdit.setOnClickListener(v -> mListener.onClickUpdateBook(book));
        holder.mBinding.imgDelete.setOnClickListener(v -> mListener.onClickDeleteBook(book));
        holder.mBinding.layoutItem.setOnClickListener(v -> mListener.onClickDetailBook(book));
    }

    @Override
    public int getItemCount() {
        return null == mListBooks ? 0 : mListBooks.size();
    }

    public static class AdminBookViewHolder extends RecyclerView.ViewHolder {

        private final ItemAdminBookBinding mBinding;

        public AdminBookViewHolder(ItemAdminBookBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }
    }
}
