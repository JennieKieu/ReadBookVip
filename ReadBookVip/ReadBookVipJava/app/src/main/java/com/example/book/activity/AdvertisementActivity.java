package com.example.book.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;

import com.example.book.MyApplication;
import com.example.book.R;
import com.example.book.constant.Constant;
import com.example.book.databinding.ActivityAdvertisementBinding;
import com.example.book.model.Advertisement;
import com.example.book.model.AdView;
import com.example.book.prefs.DataStoreManager;
import androidx.annotation.NonNull;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class AdvertisementActivity extends BaseActivity {

    private ActivityAdvertisementBinding binding;
    private Advertisement mAdvertisement;
    private ExoPlayer player;
    private Handler skipHandler;
    private Runnable skipRunnable;
    private long videoStartTime;
    private boolean isCompleted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        binding = ActivityAdvertisementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadDataIntent();
        initPlayer();
        initListener();
    }

    private void loadDataIntent() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mAdvertisement = (Advertisement) bundle.getSerializable(Constant.OBJECT_ADVERTISEMENT);
        }
    }

    private void initPlayer() {
        if (mAdvertisement == null || mAdvertisement.getVideoUrl() == null) {
            finish();
            return;
        }

        player = new ExoPlayer.Builder(this).build();
        binding.playerView.setPlayer(player);

        MediaItem mediaItem = MediaItem.fromUri(mAdvertisement.getVideoUrl());
        player.setMediaItem(mediaItem);
        player.prepare();
        player.setPlayWhenReady(true);

        videoStartTime = System.currentTimeMillis();

        // Show skip button after 5 seconds
        skipHandler = new Handler(Looper.getMainLooper());
        skipRunnable = () -> binding.btnSkip.setVisibility(View.VISIBLE);
        skipHandler.postDelayed(skipRunnable, 5000);

        // Track when video completes
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_ENDED) {
                    isCompleted = true;
                    binding.btnClose.setVisibility(View.VISIBLE);
                    trackAdView(true);
                }
            }
        });
    }

    private void initListener() {
        binding.btnSkip.setOnClickListener(v -> {
            isCompleted = false;
            trackAdView(false);
            finish();
        });

        binding.btnClose.setOnClickListener(v -> finish());
    }

    private void trackAdView(boolean completed) {
        if (mAdvertisement == null) return;

        long duration = (System.currentTimeMillis() - videoStartTime) / 1000; // seconds
        AdView adView = new AdView(
                System.currentTimeMillis(),
                mAdvertisement.getId(),
                mAdvertisement.getTitle(),
                DataStoreManager.getUser().getEmail(),
                System.currentTimeMillis(),
                (int) duration,
                completed
        );

        // Save AdView
        MyApplication.get(this).advertisementViewDatabaseReference()
                .child(String.valueOf(adView.getId()))
                .setValue(adView);

        // Update viewCount in Advertisement
        MyApplication.get(this).advertisementDatabaseReference()
                .child(String.valueOf(mAdvertisement.getId()))
                .child("viewCount")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Integer currentCount = snapshot.getValue(Integer.class);
                        int newCount = (currentCount != null ? currentCount : 0) + 1;
                        MyApplication.get(AdvertisementActivity.this).advertisementDatabaseReference()
                                .child(String.valueOf(mAdvertisement.getId()))
                                .child("viewCount")
                                .setValue(newCount);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (skipHandler != null && skipRunnable != null) {
            skipHandler.removeCallbacks(skipRunnable);
        }
        if (player != null) {
            player.release();
        }
        // Track if user closes without completing
        if (!isCompleted && mAdvertisement != null) {
            trackAdView(false);
        }
    }

    @Override
    public void onBackPressed() {
        // Prevent back button, user must skip or close
        if (binding.btnSkip.getVisibility() == View.VISIBLE || binding.btnClose.getVisibility() == View.VISIBLE) {
            isCompleted = false;
            trackAdView(false);
            super.onBackPressed();
        }
    }
}

