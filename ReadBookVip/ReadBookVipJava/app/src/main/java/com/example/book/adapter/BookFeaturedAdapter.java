package com.example.book.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.book.databinding.ItemBookFeaturedBinding;
import com.example.book.listener.IOnClickBookListener;
import com.example.book.model.Book;
import com.example.book.utils.GlideUtils;

import java.util.List;

public class BookFeaturedAdapter extends RecyclerView.Adapter<BookFeaturedAdapter.BookFeaturedViewHolder> {

    private final List<Book> mListBook;
    public final IOnClickBookListener mListener;

    public BookFeaturedAdapter(List<Book> list, IOnClickBookListener listener) {
        this.mListBook = list;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public BookFeaturedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBookFeaturedBinding binding = ItemBookFeaturedBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new BookFeaturedViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BookFeaturedViewHolder holder, int position) {
        Book book = mListBook.get(position);
        if (book == null) return;
        GlideUtils.loadUrlBanner(book.getBanner(), holder.mBinding.imgBook);
        holder.mBinding.layoutItem.setOnClickListener(v -> mListener.onClickItemBook(book));
    }

    @Override
    public int getItemCount() {
        if (mListBook != null) {
            return mListBook.size();
        }
        return 0;
    }

    public static class BookFeaturedViewHolder extends RecyclerView.ViewHolder {

        private final ItemBookFeaturedBinding mBinding;

        public BookFeaturedViewHolder(@NonNull ItemBookFeaturedBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }
    }
}
