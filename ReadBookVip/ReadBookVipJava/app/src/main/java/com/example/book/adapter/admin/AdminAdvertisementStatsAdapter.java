package com.example.book.adapter.admin;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.book.databinding.ItemAdminAdvertisementStatsBinding;
import com.example.book.model.Advertisement;
import com.example.book.utils.GlideUtils;

import java.util.List;
import java.util.Map;

public class AdminAdvertisementStatsAdapter extends RecyclerView.Adapter<AdminAdvertisementStatsAdapter.AdminAdvertisementStatsViewHolder> {

    private final List<Advertisement> mListAdvertisements;
    private final Map<Long, Integer> mViewCountMap;
    private final Map<Long, Integer> mCompletedCountMap;

    public AdminAdvertisementStatsAdapter(List<Advertisement> list, 
                                         Map<Long, Integer> viewCountMap,
                                         Map<Long, Integer> completedCountMap) {
        this.mListAdvertisements = list;
        this.mViewCountMap = viewCountMap;
        this.mCompletedCountMap = completedCountMap;
    }

    @NonNull
    @Override
    public AdminAdvertisementStatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminAdvertisementStatsBinding binding = ItemAdminAdvertisementStatsBinding
                .inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new AdminAdvertisementStatsViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminAdvertisementStatsViewHolder holder, int position) {
        Advertisement advertisement = mListAdvertisements.get(position);
        if (advertisement == null) return;

        // Load thumbnail if available
        if (advertisement.getThumbnailUrl() != null && !advertisement.getThumbnailUrl().isEmpty()) {
            GlideUtils.loadUrl(advertisement.getThumbnailUrl(), holder.mBinding.imgThumbnail);
        } else {
            holder.mBinding.imgThumbnail.setImageResource(com.example.book.R.drawable.img_no_image);
        }

        holder.mBinding.tvTitle.setText(advertisement.getTitle());

        // View count
        int viewCount = mViewCountMap.getOrDefault(advertisement.getId(), 0);
        holder.mBinding.tvViewCount.setText(String.valueOf(viewCount));

        // Completion rate
        int completedCount = mCompletedCountMap.getOrDefault(advertisement.getId(), 0);
        String completionRate = "0%";
        if (viewCount > 0) {
            int rate = (completedCount * 100) / viewCount;
            completionRate = rate + "%";
        }
        holder.mBinding.tvCompletionRate.setText(completionRate);
    }

    @Override
    public int getItemCount() {
        return null == mListAdvertisements ? 0 : mListAdvertisements.size();
    }

    public static class AdminAdvertisementStatsViewHolder extends RecyclerView.ViewHolder {

        private final ItemAdminAdvertisementStatsBinding mBinding;

        public AdminAdvertisementStatsViewHolder(ItemAdminAdvertisementStatsBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }
    }
}



