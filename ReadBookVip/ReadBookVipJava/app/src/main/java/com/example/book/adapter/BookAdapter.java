package com.example.book.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.book.R;
import com.example.book.api.ApiClient;
import com.example.book.api.BookApiService;
import com.example.book.constant.GlobalFunction;
import com.example.book.databinding.ItemBookBinding;
import com.example.book.listener.IOnClickBookListener;
import com.example.book.model.Book;
import com.example.book.model.Category;
import com.example.book.prefs.DataStoreManager;
import com.example.book.utils.GlideUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private final List<Book> listBook;
    private final IOnClickBookListener mListener;
    private Set<Long> favoriteBookIds = new HashSet<>(); // Cache favorite book IDs from API
    private BookApiService apiService;

    public BookAdapter(List<Book> listBook, IOnClickBookListener mListener) {
        this.listBook = listBook;
        this.mListener = mListener;
        this.apiService = ApiClient.getInstance().getBookApiService();
        loadFavoriteList();
    }
    
    private void loadFavoriteList() {
        if (DataStoreManager.getUser() == null) return;
        String userEmail = DataStoreManager.getUser().getEmail();
        if (userEmail == null || userEmail.isEmpty()) return;
        
        apiService.getFavorites(userEmail).enqueue(new Callback<List<Long>>() {
            @Override
            public void onResponse(Call<List<Long>> call, Response<List<Long>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    favoriteBookIds.clear();
                    favoriteBookIds.addAll(response.body());
                    notifyDataSetChanged(); // Refresh UI with favorite status
                }
            }

            @Override
            public void onFailure(Call<List<Long>> call, Throwable t) {
                // Silent fail
            }
        });
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

        // Check favorite from API cache
        boolean isFavorite = favoriteBookIds.contains(book.getId());
        if (isFavorite) {
            holder.mBinding.imgFavorite.setImageResource(R.drawable.ic_favorite);
        } else {
            holder.mBinding.imgFavorite.setImageResource(R.drawable.ic_unfavorite);
        }

        holder.mBinding.imgFavorite.setOnClickListener(v -> {
            boolean newFavoriteState = !isFavorite;
            // Use API method
            GlobalFunction.onClickFavoriteBook(v.getContext(), book, newFavoriteState);
            // Update local cache immediately for better UX
            if (newFavoriteState) {
                favoriteBookIds.add(book.getId());
            } else {
                favoriteBookIds.remove(book.getId());
            }
            // Update UI
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(adapterPosition);
            }
            // Also call listener for any additional handling
            mListener.onClickFavoriteBook(book, newFavoriteState);
        });
        holder.mBinding.layoutItem.setOnClickListener(v -> mListener.onClickItemBook(book));
        holder.mBinding.tvCategory.setOnClickListener(v -> mListener.onClickCategoryOfBook(
                new Category(book.getCategoryId(), book.getCategoryName())));
    }

    @Override
    public int getItemCount() {
        return null == listBook ? 0 : listBook.size();
    }
    
    public void refreshFavoriteList() {
        loadFavoriteList();
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        private final ItemBookBinding mBinding;

        public BookViewHolder(ItemBookBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }
    }
}
