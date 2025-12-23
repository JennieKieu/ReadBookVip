package com.example.book.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.book.databinding.ItemCategoryHomeBinding;
import com.example.book.listener.IOnClickCategoryListener;
import com.example.book.model.Category;
import com.example.book.utils.GlideUtils;

import java.util.List;

public class CategoryHomeAdapter extends RecyclerView.Adapter<CategoryHomeAdapter.CategoryHomeViewHolder> {

    private final List<Category> listCategory;
    private final IOnClickCategoryListener mListener;

    public CategoryHomeAdapter(List<Category> listCategory, IOnClickCategoryListener listener) {
        this.listCategory = listCategory;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public CategoryHomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCategoryHomeBinding binding = ItemCategoryHomeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new CategoryHomeViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryHomeViewHolder holder, int position) {
        Category category = listCategory.get(position);
        if (category == null) return;
        GlideUtils.loadUrl(category.getImage(), holder.mBinding.imgCategory);
        holder.mBinding.tvName.setText(category.getName());
        holder.mBinding.layoutItem.setOnClickListener(v -> mListener.onClickItemCategory(category));
    }

    @Override
    public int getItemCount() {
        return null == listCategory ? 0 : listCategory.size();
    }

    public static class CategoryHomeViewHolder extends RecyclerView.ViewHolder {
        private final ItemCategoryHomeBinding mBinding;

        public CategoryHomeViewHolder(ItemCategoryHomeBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }
    }
}
