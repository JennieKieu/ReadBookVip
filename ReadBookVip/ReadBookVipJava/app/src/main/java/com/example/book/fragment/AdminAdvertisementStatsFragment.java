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

import com.example.book.MyApplication;
import com.example.book.R;
import com.example.book.adapter.admin.AdminAdvertisementStatsAdapter;
import com.example.book.databinding.FragmentAdminAdvertisementStatsBinding;
import com.example.book.model.AdView;
import com.example.book.model.Advertisement;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminAdvertisementStatsFragment extends Fragment {

    private FragmentAdminAdvertisementStatsBinding binding;
    private List<Advertisement> mListAdvertisement;
    private Map<Long, Integer> mViewCountMap;  // advertisementId -> viewCount
    private Map<Long, Integer> mCompletedCountMap;  // advertisementId -> completedCount
    private AdminAdvertisementStatsAdapter mAdapter;

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
    }

    private void loadStatistics() {
        if (getActivity() == null) return;

        // Load advertisements
        MyApplication.get(getActivity()).advertisementDatabaseReference()
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        mListAdvertisement.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Advertisement advertisement = dataSnapshot.getValue(Advertisement.class);
                            if (advertisement != null) {
                                mListAdvertisement.add(advertisement);
                            }
                        }
                        // Load views after loading advertisements
                        loadAdViews();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void loadAdViews() {
        if (getActivity() == null) return;

        MyApplication.get(getActivity()).advertisementViewDatabaseReference()
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        mViewCountMap.clear();
                        mCompletedCountMap.clear();

                        Calendar today = Calendar.getInstance();
                        today.set(Calendar.HOUR_OF_DAY, 0);
                        today.set(Calendar.MINUTE, 0);
                        today.set(Calendar.SECOND, 0);
                        today.set(Calendar.MILLISECOND, 0);
                        long todayStart = today.getTimeInMillis();

                        Calendar weekStart = Calendar.getInstance();
                        weekStart.add(Calendar.DAY_OF_WEEK, -weekStart.get(Calendar.DAY_OF_WEEK) + 1);
                        weekStart.set(Calendar.HOUR_OF_DAY, 0);
                        weekStart.set(Calendar.MINUTE, 0);
                        weekStart.set(Calendar.SECOND, 0);
                        weekStart.set(Calendar.MILLISECOND, 0);
                        long weekStartTime = weekStart.getTimeInMillis();

                        Calendar monthStart = Calendar.getInstance();
                        monthStart.set(Calendar.DAY_OF_MONTH, 1);
                        monthStart.set(Calendar.HOUR_OF_DAY, 0);
                        monthStart.set(Calendar.MINUTE, 0);
                        monthStart.set(Calendar.SECOND, 0);
                        monthStart.set(Calendar.MILLISECOND, 0);
                        long monthStartTime = monthStart.getTimeInMillis();

                        int viewsToday = 0;
                        int viewsWeek = 0;
                        int viewsMonth = 0;
                        long topAdId = -1;
                        int topAdViews = 0;

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            AdView adView = dataSnapshot.getValue(AdView.class);
                            if (adView == null) continue;

                            long adId = adView.getAdvertisementId();
                            long viewedAt = adView.getViewedAt();

                            // Count total views per advertisement
                            mViewCountMap.put(adId, mViewCountMap.getOrDefault(adId, 0) + 1);

                            // Count completed views
                            if (adView.isCompleted()) {
                                mCompletedCountMap.put(adId, mCompletedCountMap.getOrDefault(adId, 0) + 1);
                            }

                            // Count views by time period
                            if (viewedAt >= todayStart) {
                                viewsToday++;
                            }
                            if (viewedAt >= weekStartTime) {
                                viewsWeek++;
                            }
                            if (viewedAt >= monthStartTime) {
                                viewsMonth++;
                            }

                            // Find top advertisement
                            int adViews = mViewCountMap.get(adId);
                            if (adViews > topAdViews) {
                                topAdViews = adViews;
                                topAdId = adId;
                            }
                        }

                        // Update UI
                        updateStatistics(viewsToday, viewsWeek, viewsMonth, topAdId, topAdViews);
                        if (mAdapter != null) {
                            mAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
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

        // Today, Week, Month
        binding.tvViewsToday.setText(String.valueOf(viewsToday));
        binding.tvViewsWeek.setText(String.valueOf(viewsWeek));
        binding.tvViewsMonth.setText(String.valueOf(viewsMonth));

        // Top advertisement
        if (topAdId != -1) {
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
}



