package com.example.book.widget;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.ScrollView;

import androidx.core.content.ContextCompat;

public class OverscrollScrollView extends ScrollView {
    
    private OnOverscrollListener overscrollListener;
    private OnOverscrollProgressListener progressListener;
    private float lastY = 0;
    private float overscrollDistance = 0;
    private boolean isOverscrollingTop = false;
    private boolean isOverscrollingBottom = false;
    private static final int OVERSCROLL_THRESHOLD = 60; // pixels to overscroll before triggering (reduced for better UX)
    private static final int HAPTIC_FEEDBACK_THRESHOLD = 40; // pixels for haptic feedback
    private boolean hasTriggeredHaptic = false;
    private int touchSlop;
    private Vibrator vibrator;
    
    public interface OnOverscrollListener {
        void onOverscrollTop();
        void onOverscrollBottom();
    }
    
    public interface OnOverscrollProgressListener {
        void onOverscrollProgress(float progress, boolean isTop);
        void onOverscrollReset();
    }
    
    public OverscrollScrollView(Context context) {
        super(context);
        init();
    }
    
    public OverscrollScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public OverscrollScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        // Initialize vibrator safely
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                VibratorManager vibratorManager = (VibratorManager) getContext().getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
                if (vibratorManager != null) {
                    vibrator = vibratorManager.getDefaultVibrator();
                }
            } else {
                vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
            }
        } catch (Exception e) {
            vibrator = null;
        }
    }
    
    private boolean hasVibratePermission() {
        return ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.VIBRATE) 
                == PackageManager.PERMISSION_GRANTED;
    }
    
    public void setOnOverscrollListener(OnOverscrollListener listener) {
        this.overscrollListener = listener;
    }
    
    public void setOnOverscrollProgressListener(OnOverscrollProgressListener listener) {
        this.progressListener = listener;
    }
    
    private void triggerHapticFeedback() {
        if (vibrator != null && !hasTriggeredHaptic && hasVibratePermission()) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(10);
                }
                hasTriggeredHaptic = true;
            } catch (SecurityException e) {
                // Permission not granted, silently fail
                vibrator = null;
            } catch (Exception e) {
                // Other errors, silently fail
            }
        }
    }
    
    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
        
        // Detect overscroll at top (scrolling up when already at top)
        if (scrollY == 0 && clampedY && overscrollListener != null && isOverscrollingTop) {
            if (overscrollDistance >= OVERSCROLL_THRESHOLD) {
                overscrollListener.onOverscrollTop();
                overscrollDistance = 0;
                isOverscrollingTop = false;
            }
        }
        
        // Detect overscroll at bottom (scrolling down when already at bottom)
        if (clampedY && overscrollListener != null && isOverscrollingBottom) {
            int maxScroll = computeVerticalScrollRange() - getHeight();
            if (scrollY >= maxScroll && overscrollDistance >= OVERSCROLL_THRESHOLD) {
                overscrollListener.onOverscrollBottom();
                overscrollDistance = 0;
                isOverscrollingBottom = false;
            }
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastY = ev.getY();
                overscrollDistance = 0;
                isOverscrollingTop = false;
                isOverscrollingBottom = false;
                hasTriggeredHaptic = false;
                if (progressListener != null) {
                    progressListener.onOverscrollReset();
                }
                break;
                
            case MotionEvent.ACTION_MOVE:
                float currentY = ev.getY();
                float deltaY = currentY - lastY;
                
                // Check if at top and trying to scroll up (overscroll top)
                if (getScrollY() == 0 && deltaY > 0) {
                    isOverscrollingTop = true;
                    overscrollDistance += Math.abs(deltaY);
                    
                    // Calculate progress (0.0 to 1.0)
                    float progress = Math.min(overscrollDistance / OVERSCROLL_THRESHOLD, 1.0f);
                    if (progressListener != null) {
                        progressListener.onOverscrollProgress(progress, true);
                    }
                    
                    // Haptic feedback when threshold reached
                    if (overscrollDistance >= HAPTIC_FEEDBACK_THRESHOLD && !hasTriggeredHaptic) {
                        triggerHapticFeedback();
                    }
                }
                // Check if at bottom and trying to scroll down (overscroll bottom)
                else if (getChildCount() > 0) {
                    int maxScroll = getChildAt(0).getHeight() - getHeight();
                    if (getScrollY() >= maxScroll && deltaY < 0) {
                        isOverscrollingBottom = true;
                        overscrollDistance += Math.abs(deltaY);
                        
                        // Calculate progress (0.0 to 1.0)
                        float progress = Math.min(overscrollDistance / OVERSCROLL_THRESHOLD, 1.0f);
                        if (progressListener != null) {
                            progressListener.onOverscrollProgress(progress, false);
                        }
                        
                        // Haptic feedback when threshold reached
                        if (overscrollDistance >= HAPTIC_FEEDBACK_THRESHOLD && !hasTriggeredHaptic) {
                            triggerHapticFeedback();
                        }
                    } else {
                        // Reset if not at boundaries
                        if (isOverscrollingTop || isOverscrollingBottom) {
                            overscrollDistance = 0;
                            isOverscrollingTop = false;
                            isOverscrollingBottom = false;
                            hasTriggeredHaptic = false;
                            if (progressListener != null) {
                                progressListener.onOverscrollReset();
                            }
                        }
                    }
                } else {
                    // Reset if not at boundaries
                    if (isOverscrollingTop || isOverscrollingBottom) {
                        overscrollDistance = 0;
                        isOverscrollingTop = false;
                        isOverscrollingBottom = false;
                        hasTriggeredHaptic = false;
                        if (progressListener != null) {
                            progressListener.onOverscrollReset();
                        }
                    }
                }
                
                lastY = currentY;
                break;
                
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // Check if overscrolled enough to trigger chapter change
                if (overscrollDistance >= OVERSCROLL_THRESHOLD && overscrollListener != null) {
                    if (isOverscrollingTop) {
                        overscrollListener.onOverscrollTop();
                    } else if (isOverscrollingBottom) {
                        overscrollListener.onOverscrollBottom();
                    }
                }
                
                // Reset progress
                if (progressListener != null) {
                    progressListener.onOverscrollReset();
                }
                
                overscrollDistance = 0;
                isOverscrollingTop = false;
                isOverscrollingBottom = false;
                hasTriggeredHaptic = false;
                break;
        }
        
        return super.onTouchEvent(ev);
    }
}

