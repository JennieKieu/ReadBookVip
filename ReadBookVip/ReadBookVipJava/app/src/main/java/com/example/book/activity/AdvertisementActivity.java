package com.example.book.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;

import android.content.Intent;
import android.net.Uri;

import com.example.book.R;
import com.example.book.api.AdvertisementApiService;
import com.example.book.api.ApiClient;
import com.example.book.constant.Constant;
import com.example.book.databinding.ActivityAdvertisementBinding;
import com.example.book.model.Advertisement;
import com.example.book.model.User;
import com.example.book.prefs.DataStoreManager;
import com.example.book.utils.GoogleDriveHelper;
import androidx.annotation.NonNull;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdvertisementActivity extends BaseActivity {

    private ActivityAdvertisementBinding binding;
    private Advertisement mAdvertisement;
    private ExoPlayer player;
    private Handler skipHandler;
    private Runnable skipRunnable;
    private long videoStartTime;
    private boolean isCompleted = false;
    private AdvertisementApiService apiService;
    private boolean isAdmin = false;  // Track if current user is admin
    private boolean hasStartedCounting = false;  // Track if 5-second countdown has started

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        binding = ActivityAdvertisementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = ApiClient.getInstance().getAdvertisementApiService();
        
        // Check if user is admin
        User user = DataStoreManager.getUser();
        isAdmin = (user != null && user.isAdmin());
        
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
        
        // Hide controller for regular users, show for admin
        binding.playerView.setUseController(isAdmin);

        // Convert Google Drive link to playable format
        String videoUrl = GoogleDriveHelper.convertToPlayableUrl(mAdvertisement.getVideoUrl());
        MediaItem mediaItem = MediaItem.fromUri(videoUrl);
        player.setMediaItem(mediaItem);
        player.prepare();
        player.setPlayWhenReady(true);

        videoStartTime = System.currentTimeMillis();

        if (isAdmin) {
            // Admin: show close button immediately, no restrictions
            binding.btnClose.setVisibility(View.VISIBLE);
        } else {
            // User: show close button after 5 seconds of actual video playback
            skipHandler = new Handler(Looper.getMainLooper());
            skipRunnable = new Runnable() {
                @Override
                public void run() {
                    // Check if video is playing and has played for at least 5 seconds
                    if (player != null && player.isPlaying() && player.getCurrentPosition() >= 5000) {
                        binding.btnClose.setVisibility(View.VISIBLE);
                        hasStartedCounting = true;
                    } else if (player != null && player.isPlaying()) {
                        // Video is playing but hasn't reached 5 seconds yet, check again in 100ms
                        skipHandler.postDelayed(this, 100);
                    } else if (player != null) {
                        // Video not playing yet, check again in 100ms
                        skipHandler.postDelayed(this, 100);
                    }
                }
            };
            // Start checking after a short delay to allow video to start
            skipHandler.postDelayed(skipRunnable, 500);
        }

        // Track when video completes (only for regular users)
        if (!isAdmin) {
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
    }

    private void initListener() {
        binding.btnClose.setOnClickListener(v -> {
            if (!isAdmin) {
                // Only track for regular users
                isCompleted = false;
                trackAdView(false);
            }
            finish();
        });
        
        // Click on video player to open URL (only for regular users)
        if (!isAdmin) {
            binding.playerView.setOnClickListener(v -> {
                if (mAdvertisement != null && mAdvertisement.getUrl() != null && !mAdvertisement.getUrl().trim().isEmpty()) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mAdvertisement.getUrl()));
                        startActivity(intent);
                    } catch (Exception e) {
                        // Invalid URL, ignore
                    }
                }
            });
        }
    }

    private void trackAdView(boolean completed) {
        // Only track for regular users, not admin
        if (isAdmin || mAdvertisement == null) return;

        // Increment view count via API
        apiService.incrementViewCount(mAdvertisement.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                // View count updated
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                // Silent fail
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
        // Track if user closes without completing (only for regular users)
        if (!isAdmin && !isCompleted && mAdvertisement != null) {
            trackAdView(false);
        }
    }

    @Override
    public void onBackPressed() {
        if (isAdmin) {
            // Admin can exit anytime
            super.onBackPressed();
        } else {
            // Regular user: prevent back button, must close after 5 seconds
            if (binding.btnClose.getVisibility() == View.VISIBLE) {
                isCompleted = false;
                trackAdView(false);
                super.onBackPressed();
            }
        }
    }
}

