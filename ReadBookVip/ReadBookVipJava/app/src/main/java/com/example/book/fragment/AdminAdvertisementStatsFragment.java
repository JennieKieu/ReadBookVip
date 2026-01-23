package com.example.book.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.book.R;
import com.example.book.adapter.admin.AdminAdvertisementStatsAdapter;
import com.example.book.api.AdvertisementApiService;
import com.example.book.api.ApiClient;
import com.example.book.databinding.FragmentAdminAdvertisementStatsBinding;
import com.example.book.model.Advertisement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminAdvertisementStatsFragment extends Fragment {

    private FragmentAdminAdvertisementStatsBinding binding;
    private List<Advertisement> mListAdvertisement;
    private Map<Long, Integer> mViewCountMap;  // advertisementId -> viewCount
    private Map<Long, Integer> mCompletedCountMap;  // advertisementId -> completedCount (not available from API, keep for compatibility)
    private AdminAdvertisementStatsAdapter mAdapter;
    private AdvertisementApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminAdvertisementStatsBinding.inflate(inflater, container, false);

        initView();
        loadStatistics();

        return binding.getRoot();
    }

    private void initView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        binding.rcvAdvertisementStats.setLayoutManager(linearLayoutManager);
        mListAdvertisement = new ArrayList<>();
        mViewCountMap = new HashMap<>();
        mCompletedCountMap = new HashMap<>();
        mAdapter = new AdminAdvertisementStatsAdapter(mListAdvertisement, mViewCountMap, mCompletedCountMap);
        binding.rcvAdvertisementStats.setAdapter(mAdapter);
        apiService = ApiClient.getInstance().getAdvertisementApiService();
    }

    private void loadStatistics() {
        if (getActivity() == null) return;

        // Load advertisements from API
        apiService.getAllAdvertisements().enqueue(new Callback<List<Advertisement>>() {
            @Override
            public void onResponse(@NonNull Call<List<Advertisement>> call, @NonNull Response<List<Advertisement>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mListAdvertisement.clear();
                    mListAdvertisement.addAll(response.body());
                    
                    // Build view count map from ViewCount field in each advertisement
                    mViewCountMap.clear();
                    mCompletedCountMap.clear();
                    
                    int totalViews = 0;
                    long topAdId = -1;
                    int topAdViews = 0;
                    
                    for (Advertisement ad : mListAdvertisement) {
                        int viewCount = ad.getViewCount();
                        mViewCountMap.put(ad.getId(), viewCount);
                        totalViews += viewCount;
                        
                        // Find top advertisement
                        if (viewCount > topAdViews) {
                            topAdViews = viewCount;
                            topAdId = ad.getId();
                        }
                    }
                    
                    // Update UI (views today/week/month not available from API, show 0 or "-")
                    updateStatistics(0, 0, 0, topAdId, topAdViews);
                    if (mAdapter != null) {
                        mAdapter.notifyDataSetChanged();
                    }
                } else {
                    // Show error or empty state
                    updateStatistics(0, 0, 0, -1, 0);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Advertisement>> call, @NonNull Throwable t) {
                // Show error or empty state
                updateStatistics(0, 0, 0, -1, 0);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void updateStatistics(int viewsToday, int viewsWeek, int viewsMonth, long topAdId, int topAdViews) {
        // Total views
        int totalViews = 0;
        for (Integer count : mViewCountMap.values()) {
            totalViews += count;
        }
        binding.tvTotalViews.setText(String.valueOf(totalViews));

        // Today, Week, Month (not available from API, show "-" or 0)
        // Note: To show actual values, need to create AdView table in SQL Server and track views with timestamps
        binding.tvViewsToday.setText("-");
        binding.tvViewsWeek.setText("-");
        binding.tvViewsMonth.setText("-");

        // Top advertisement
        if (topAdId != -1 && topAdViews > 0) {
            for (Advertisement ad : mListAdvertisement) {
                if (ad.getId() == topAdId) {
                    binding.tvTopAdvertisement.setText(ad.getTitle() + " (" + topAdViews + " " + getString(R.string.label_ad_view_count) + ")");
                    break;
                }
            }
        } else {
            binding.tvTopAdvertisement.setText("-");
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Reload statistics when fragment becomes visible
        loadStatistics();
    }
}




