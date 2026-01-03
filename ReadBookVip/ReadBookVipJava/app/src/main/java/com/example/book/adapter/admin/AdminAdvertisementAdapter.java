package com.example.book.adapter.admin;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.book.R;
import com.example.book.databinding.ItemAdminAdvertisementBinding;
import com.example.book.model.Advertisement;
import com.example.book.utils.GlideUtils;

import java.util.List;

public class AdminAdvertisementAdapter extends RecyclerView.Adapter<AdminAdvertisementAdapter.AdminAdvertisementViewHolder> {

    private final List<Advertisement> mListAdvertisements;
    public final IOnAdminAdvertisementListener mListener;

    public interface IOnAdminAdvertisementListener {
        void onClickUpdate(Advertisement advertisement);
        void onClickDelete(Advertisement advertisement);
        void onClickToggleActive(Advertisement advertisement);
    }

    public AdminAdvertisementAdapter(List<Advertisement> list, IOnAdminAdvertisementListener listener) {
        this.mListAdvertisements = list;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public AdminAdvertisementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminAdvertisementBinding binding = ItemAdminAdvertisementBinding
                .inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new AdminAdvertisementViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminAdvertisementViewHolder holder, int position) {
        Advertisement advertisement = mListAdvertisements.get(position);
        if (advertisement == null) return;

        // Load thumbnail if available
        if (advertisement.getThumbnailUrl() != null && !advertisement.getThumbnailUrl().isEmpty()) {
            GlideUtils.loadUrl(advertisement.getThumbnailUrl(), holder.mBinding.imgThumbnail);
        } else {
            // Use default image or video icon
            holder.mBinding.imgThumbnail.setImageResource(com.example.book.R.drawable.img_no_image);
        }

        holder.mBinding.tvTitle.setText(advertisement.getTitle());
        
        // Status
        if (advertisement.isActive()) {
            holder.mBinding.tvStatus.setText(holder.mBinding.getRoot().getContext().getString(R.string.label_ad_active));
            holder.mBinding.tvStatus.setTextColor(holder.mBinding.getRoot().getContext().getColor(android.R.color.holo_green_dark));
        } else {
            holder.mBinding.tvStatus.setText(holder.mBinding.getRoot().getContext().getString(R.string.label_ad_inactive));
            holder.mBinding.tvStatus.setTextColor(holder.mBinding.getRoot().getContext().getColor(android.R.color.holo_red_dark));
        }

        // View count
        holder.mBinding.tvViewCount.setText(String.valueOf(advertisement.getViewCount()));

        // Toggle active icon
        if (advertisement.isActive()) {
            holder.mBinding.imgToggleActive.setImageResource(R.drawable.ic_favorite);
        } else {
            holder.mBinding.imgToggleActive.setImageResource(R.drawable.ic_unfavorite);
        }

        holder.mBinding.imgEdit.setOnClickListener(v -> mListener.onClickUpdate(advertisement));
        holder.mBinding.imgDelete.setOnClickListener(v -> mListener.onClickDelete(advertisement));
        holder.mBinding.imgToggleActive.setOnClickListener(v -> mListener.onClickToggleActive(advertisement));
    }

    @Override
    public int getItemCount() {
        return null == mListAdvertisements ? 0 : mListAdvertisements.size();
    }

    public static class AdminAdvertisementViewHolder extends RecyclerView.ViewHolder {

        private final ItemAdminAdvertisementBinding mBinding;

        public AdminAdvertisementViewHolder(ItemAdminAdvertisementBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }
    }
}




