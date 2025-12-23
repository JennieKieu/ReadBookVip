package com.example.book.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.book.R;
import com.example.book.constant.GlobalFunction;
import com.example.book.databinding.ItemBookBinding;
import com.example.book.listener.IOnClickBookListener;
import com.example.book.model.Book;
import com.example.book.model.Category;
import com.example.book.utils.GlideUtils;

import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private final List<Book> listBook;
    private final IOnClickBookListener mListener;

    public BookAdapter(List<Book> listBook, IOnClickBookListener mListener) {
        this.listBook = listBook;
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBookBinding binding = ItemBookBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new BookViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = listBook.get(position);
        if (book == null) return;

        GlideUtils.loadUrl(book.getImage(), holder.mBinding.imgBook);
        holder.mBinding.tvName.setText(book.getTitle());
        holder.mBinding.tvCategory.setText(book.getCategoryName());

        boolean isFavorite = GlobalFunction.isFavoriteBook(book);
        if (isFavorite) {
            holder.mBinding.imgFavorite.setImageResource(R.drawable.ic_favorite);
        } else {
            holder.mBinding.imgFavorite.setImageResource(R.drawable.ic_unfavorite);
        }

        holder.mBinding.imgFavorite.setOnClickListener(v -> mListener.onClickFavoriteBook(book, !isFavorite));
        holder.mBinding.layoutItem.setOnClickListener(v -> mListener.onClickItemBook(book));
        holder.mBinding.tvCategory.setOnClickListener(v -> mListener.onClickCategoryOfBook(
                new Category(book.getCategoryId(), book.getCategoryName())));
    }

    @Override
    public int getItemCount() {
        return null == listBook ? 0 : listBook.size();
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        private final ItemBookBinding mBinding;

        public BookViewHolder(ItemBookBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }
    }
}
